package org.example.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.todo.model.TaskEntity;
import org.example.todo.model.TaskPriority;
import org.example.todo.model.TaskStatus;
import org.example.todo.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TaskService taskService;



    private TaskEntity sample() {
        TaskEntity t = new TaskEntity();
        t.setId(1L);
        t.setTitle("Test Task");
        t.setDescription("Desc");
        t.setDeadline(LocalDate.now().plusDays(1));
        t.setStatus(TaskStatus.Active);
        t.setPriority(TaskPriority.Medium);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        return t;
    }

    @Nested @DisplayName("GET /api/tasks")
    class ListTasks {
        @Test @DisplayName("200 и список задач")
        void okList() throws Exception {
            given(taskService.getAllSorted(anyString()))
                    .willReturn(Arrays.asList(sample(), sample()));

            mockMvc.perform(get("/api/tasks").param("sort", "title"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("Test Task"));
        }

        @Test @DisplayName("400 при неверном sort")
        void wrongSort() throws Exception {
            given(taskService.getAllSorted(eq("wrong")))
                    .willThrow(new IllegalArgumentException("Invalid sort"));

            mockMvc.perform(get("/api/tasks").param("sort", "wrong"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Invalid sort"));
        }
    }

    @Nested @DisplayName("GET /api/tasks/{id}")
    class GetById {
        @Test @DisplayName("200 и задача по ID")
        void okGet() throws Exception {
            given(taskService.getById(1L)).willReturn(sample());

            mockMvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.deadline").exists());
        }

        @Test @DisplayName("404 если нет")
        void notFound() throws Exception {
            given(taskService.getById(99L))
                    .willThrow(new IllegalArgumentException("Not found"));

            mockMvc.perform(get("/api/tasks/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Not found"));
        }
    }

    @Nested @DisplayName("POST /api/tasks")
    class CreateTask {
        @Test @DisplayName("201 и созданная задача")
        void okCreate() throws Exception {
            TaskEntity in = sample();
            in.setId(null);
            given(taskService.create(any(TaskEntity.class)))
                    .willReturn(sample());

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(in)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Task"));
        }

        @Test @DisplayName("400 при попытке создать задачу задним числом")
        void badRequestOnPastDueDate() throws Exception {
            TaskEntity in = sample();
            in.setId(null);
            in.setDeadline(LocalDate.now().minusDays(2));

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(in)))
                    .andExpect(status().isBadRequest());
        }

        @Test @DisplayName("201 при дедлайне сегодня")
        void okCreateOnTodayDeadline() throws Exception {
            TaskEntity in = sample();
            in.setDeadline(LocalDate.now());

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(in)))
                    .andExpect(status().isCreated());
        }

        @ParameterizedTest(name = "title length={0} -> status {1}")
        @MethodSource("titleLengthCases")
        @DisplayName("Проверка валидаций Title")
        void titleLengthBoundary(String title, int expectedStatus) throws Exception {
            TaskEntity in = sample();
            in.setTitle(title);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(in)))
                    .andExpect(status().is(expectedStatus));
        }

        static Stream<Arguments> titleLengthCases() {
            String empty = "";
            String macros = "!1";
            String three = "abc";
            String four = "abcd";
            String hundred = "A".repeat(1000);
            String hundredOne = "A".repeat(1001);

            return Stream.of(
                    Arguments.of(empty,        400),
                    Arguments.of(three,        400),
                    Arguments.of(four,         201),
                    Arguments.of(hundred,      201),
                    Arguments.of(hundredOne,   400),
                    Arguments.of(macros,   400)
            );
        }
    }

    @Nested @DisplayName("PUT /api/tasks/{id}")
    class UpdateTask {
        @Test @DisplayName("200 и обновлённая задача")
        void okUpdate() throws Exception {
            TaskEntity in = sample();
            in.setTitle("Updated");
            given(taskService.update(eq(1L), any(TaskEntity.class)))
                    .willReturn(in);

            mockMvc.perform(put("/api/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(in)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated"));
        }

        @Test @DisplayName("400 при ошибке обновления")
        void badUpdate() throws Exception {
            TaskEntity in = sample();
            in.setTitle("Updated");
            given(taskService.update(eq(1L), any()))
                    .willThrow(new RuntimeException("Update failed"));

            mockMvc.perform(put("/api/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(in)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Update failed"));
        }
    }

    @Nested @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTask {
        @Test @DisplayName("204 No Content при удалении")
        void okDelete() throws Exception {
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isNoContent());
            Mockito.verify(taskService).delete(1L);
        }

        @Test @DisplayName("400 при ошибке удаления")
        void badDelete() throws Exception {
            doThrow(new RuntimeException("Delete failed"))
                    .when(taskService).delete(1L);

            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Delete failed"));
        }
    }

    @Nested @DisplayName("PATCH /api/tasks/{id}/complete & uncomplete")
    class PatchComplete {
        @Test @DisplayName("200 при complete")
        void okComplete() throws Exception {
            TaskEntity done = sample();
            done.setStatus(TaskStatus.Completed);
            given(taskService.markCompleted(1L, true)).willReturn(done);

            mockMvc.perform(patch("/api/tasks/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Completed"));
        }

        @Test @DisplayName("200 при uncomplete")
        void okUncomplete() throws Exception {
            TaskEntity undone = sample();
            undone.setStatus(TaskStatus.Active);
            given(taskService.markCompleted(1L, false)).willReturn(undone);

            mockMvc.perform(patch("/api/tasks/1/uncomplete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Active"));
        }

        @Test @DisplayName("400 при ошибке patch")
        void badPatch() throws Exception {
            given(taskService.markCompleted(1L, true))
                    .willThrow(new RuntimeException("Patch error"));

            mockMvc.perform(patch("/api/tasks/1/complete"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Patch error"));
        }
    }
}
