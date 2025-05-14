package org.example.todo;


import org.example.todo.model.TaskEntity;
import org.example.todo.model.TaskPriority;
import org.example.todo.model.TaskStatus;
import org.example.todo.repository.TaskRepository;
import org.example.todo.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class TaskServiceTest {
    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = Mockito.mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);

        List<TaskEntity> saved = new ArrayList<>();
        AtomicLong idGen = new AtomicLong(1);

        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(inv -> {
            TaskEntity e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(idGen.getAndIncrement());
            }
            saved.removeIf(x -> Objects.equals(x.getId(), e.getId()));
            saved.add(e);
            return e;
        });

        when(taskRepository.findById(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return saved.stream()
                    .filter(e -> Objects.equals(e.getId(), id))
                    .findFirst();
        });
    }
    private TaskEntity newTask(String title, TaskPriority priority) {
        TaskEntity task = new TaskEntity();
        task.setTitle(title);
        task.setPriority(priority);
        return task;
    }
    @Test
    @DisplayName("Использует приоритет из макроса, если явно не указан")
    void testPriorityFromMacro() {
        TaskEntity task = newTask("Test task !2", null);
        TaskEntity created = taskService.create(task);
        assertEquals(TaskPriority.High, created.getPriority());
        assertEquals("Test task", created.getTitle());
    }

    @Test
    @DisplayName("Игнорирует макрос приоритета, если приоритет задан явно")
    void testPriorityFromExplicitFieldOverridesMacro() {
        TaskEntity task = newTask("Test task !1", TaskPriority.High);
        TaskEntity created = taskService.create(task);
        assertEquals(TaskPriority.High, created.getPriority());
        assertEquals("Test task", created.getTitle());
    }

    @Test
    @DisplayName("Использует дедлайн из макроса, если явно не указан")
    void testDeadlineFromMacro() {
        TaskEntity task = newTask("Test task !before 01.01.2026", null);
        TaskEntity created = taskService.create(task);
        assertEquals(LocalDate.of(2026, 1, 1), created.getDeadline());
        assertEquals("Test task", created.getTitle());
    }

    @Test
    @DisplayName("Игнорирует макрос дедлайна, если дедлайн задан явно")
    void testDeadlineFromExplicitFieldOverridesMacro() {
        TaskEntity task = newTask("Test task !before 01.01.2026", null);
        task.setDeadline(LocalDate.of(2030, 12, 31));
        TaskEntity created = taskService.create(task);
        assertEquals(LocalDate.of(2030, 12, 31), created.getDeadline());
        assertEquals("Test task", created.getTitle());
    }

    @Test
    @DisplayName("Парсит дедлайн с конкретной датой без времени")
    void testDeadlineParsingSpecificDate() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        TaskEntity task = newTask("Do homework !before " + today, null);
        TaskEntity created = taskService.create(task);
        assertNotNull(created.getDeadline());
        assertEquals(LocalDate.now(), created.getDeadline());
        assertEquals("Do homework", created.getTitle());
    }

    @Test
    @DisplayName("Присваивает статус Active при создании новой задачи")
    void testStatusActive() {
        TaskEntity task = newTask("Work", null);
        TaskEntity created = taskService.create(task);
        assertEquals(TaskStatus.Active, created.getStatus());
    }

    @Test
    @DisplayName("Присваивает статус Overdue, если дедлайн в прошлом")
    void testStatusOverdue() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        String macro = past.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        TaskEntity task = newTask("Old task !before " + macro, null);
        TaskEntity created = taskService.create(task);
        assertEquals(TaskStatus.Overdue, created.getStatus());
    }

    @Test
    @DisplayName("Присваивает статус Completed, если задача завершена до дедлайна")
    void testMarkCompletedBeforeDeadline() {
        String futureDate = LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        TaskEntity task = taskService.create(newTask("Task !before " + futureDate, null));

        TaskEntity updated = taskService.markCompleted(task.getId(), true);

        assertEquals(TaskStatus.Completed, updated.getStatus());
    }

    @Test
    @DisplayName("Присваивает статус Late, если задача завершена после дедлайна")
    void testMarkCompletedAfterDeadline() {
        String pastDate = LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        TaskEntity unsavedTask = newTask("Task !before " + pastDate, null);

        TaskEntity task = taskService.create(unsavedTask);

        TaskEntity updated = taskService.markCompleted(task.getId(), true);

        assertEquals(TaskStatus.Late, updated.getStatus());
    }
    @Test
    @DisplayName("Игнорирует несуществующие макросы и не меняет поля задачи")
    void testInvalidMacrosIgnored() {
        TaskEntity task = newTask("Something !wrongmacro", TaskPriority.Low);
        TaskEntity created = taskService.create(task);
        assertEquals("Something !wrongmacro", created.getTitle());
        assertEquals(TaskPriority.Low, created.getPriority());
        assertNull(created.getDeadline());
    }

    @Test
    @DisplayName("Бросает исключение, если после удаления макросов заголовок слишком короткий")
    void testShortTitleThrows() {
        TaskEntity task = newTask("!1 abc", null);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> taskService.create(task)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Title must be at least 4 characters long\"", ex.getMessage());
    }
    @Test
    @DisplayName("Бросает исключение, если после удаления макросов заголовок становится пустым")
    void toEntity_titleEmptyAfterMacro_throws() {
        TaskEntity task = newTask("!1 !before 01.01.2025", null);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> taskService.create(task)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Title must be at least 4 characters long\"", ex.getMessage());
    }
    @ParameterizedTest
    @CsvSource({
            "'!1 !before 01.01.2025 Важная задача', 'Важная задача', Critical, 2025-01-01",
            "'!before 01.01.2025 !1 Важная задача', 'Важная задача', Critical, 2025-01-01",
            "'Важная задача !before 01.01.2025 !1', 'Важная задача', Critical, 2025-01-01",
            "'Важная задача !1 !before 01.01.2025', 'Важная задача', Critical, 2025-01-01"
    })
    @DisplayName("Корректно парсит приоритет и дедлайн при разных порядках макросов")
    void testMixedMacroOrder(String input, String expectedTitle, TaskPriority expectedPriority, String expectedDeadline) {
        TaskEntity task = newTask(input, expectedPriority);
        TaskEntity created = taskService.create(task);

        assertEquals(expectedTitle, created.getTitle());
        assertEquals(expectedPriority, created.getPriority());
        assertEquals(LocalDate.parse(expectedDeadline), created.getDeadline());
    }
    @ParameterizedTest
    @CsvSource({
            "'Сделать отчёт !0', ''",
            "'Сделать отчёт !1', Critical",
            "'Задача !2', High",
            "'Просто задача !3', Medium",
            "'Лёгкая задача !4', Low",
            "'Неправильный !5', ''"
    })
    @DisplayName("Корректно парсит приоритет из макроса в названии задачи")
    void testPriorityMacroParsing(String input, String expectedPriorityStr) {
        TaskPriority expectedPriority = TaskPriority.valueOf(
                expectedPriorityStr.isBlank() ? "Medium" : expectedPriorityStr);

        TaskEntity task = newTask(input, expectedPriority);
        TaskEntity created = taskService.create(task);

        assertEquals(expectedPriority, created.getPriority());
    }

    @ParameterizedTest
    @CsvSource({
            "'Задача !before 01.05.2025', 2025-05-01",
            "'Задача !before 01-05-2025', 2025-05-01"
    })
    @DisplayName("Корректно парсит дедлайн из макроса в названии задачи")
    void testDeadlineMacroParsingValid(String input, String expectedDateStr) {
        TaskEntity task = newTask(input, null);
        TaskEntity created = taskService.create(task);

        assertEquals(LocalDate.parse(expectedDateStr), created.getDeadline());
    }

    @Test
    @DisplayName("Бросает исключение, если заголовок короче 4 символов")
    void testTitleShorterThan4Throws() {
        TaskEntity task = newTask("ooo", null);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> taskService.create(task)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Title must be at least 4 characters long\"", ex.getMessage());
    }

    @Test
    @DisplayName("Не бросает исключение, если заголовок содержит ровно 4 символа")
    void testTitleExactly4CharactersDoesNotThrow() {
        TaskEntity task = newTask("Test", null);
        assertDoesNotThrow(() -> {
            taskService.create(task);
        });
    }
}
