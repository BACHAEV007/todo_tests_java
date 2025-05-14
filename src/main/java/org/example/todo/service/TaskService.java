package org.example.todo.service;

import jakarta.transaction.Transactional;
import org.example.todo.model.TaskEntity;
import org.example.todo.model.TaskPriority;
import org.example.todo.model.TaskStatus;
import org.example.todo.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaskService {

    private final TaskRepository repo;
    private static final DateTimeFormatter DTF_DOT  = DateTimeFormatter.ofPattern("d.MM.uuuu");
    private static final DateTimeFormatter DTF_DASH = DateTimeFormatter.ofPattern("d-MM-uuuu");
    private static final Pattern PRIORITY_MACRO = Pattern.compile("!([1-4])");
    private static final Pattern DEADLINE_MACRO = Pattern.compile("!before\\s+(\\d{1,2}[.\\-]\\d{1,2}[.\\-]\\d{4})");

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }
    @Transactional
    public List<TaskEntity> getAllSorted(String sortBy) {
        List<TaskEntity> tasks;
        switch (sortBy) {
            case "deadline": tasks = repo.findAllByOrderByDeadlineAsc(); break;
            case "created":  tasks = repo.findAllByOrderByCreatedAtDesc(); break;
            default:         tasks = repo.findAll();
        }
        return tasks;
    }
    @Transactional
    public TaskEntity getById(Long id) {
        TaskEntity t = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Task not found"));
        return t;
    }

    @Transactional
    public TaskEntity create(TaskEntity in) {
        applyMacros(in);
        in.setStatus(TaskStatus.Active);
        updateStatusIfNeeded(in);
        return repo.save(in);
    }

    @Transactional
    public TaskEntity update(Long id, TaskEntity in) {
        TaskEntity t = getById(id);
        t.setTitle(in.getTitle());
        t.setDescription(in.getDescription());
        t.setDeadline(in.getDeadline());
        t.setPriority(in.getPriority());

        applyMacros(t);
        t.setStatus(TaskStatus.Active);
        updateStatusIfNeeded(t);
        return repo.save(t);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional
    public TaskEntity markCompleted(Long id, boolean completed) {
        TaskEntity t = getById(id);
        LocalDate now = LocalDate.now();
        if (completed) {
            t.setStatus(t.getDeadline() != null && now.isAfter(t.getDeadline())
                    ? TaskStatus.Late
                    : TaskStatus.Completed);
        } else {
            t.setStatus(t.getDeadline() != null && now.isAfter(t.getDeadline())
                    ? TaskStatus.Overdue
                    : TaskStatus.Active);
        }
        return repo.save(t);
    }

    private void applyMacros(TaskEntity t) {
        String title = t.getTitle();

        Matcher mP = PRIORITY_MACRO.matcher(title);
        if (mP.find()) {
            int lvl = Integer.parseInt(mP.group(1));
            if (t.getPriority() == null) {
                t.setPriority(TaskPriority.values()[lvl - 1]);
            }
            title = mP.replaceAll("").trim();
        } else if (t.getPriority() == null) {
            t.setPriority(TaskPriority.Medium);
        }

        Matcher mD = DEADLINE_MACRO.matcher(title);
        if (mD.find()) {
            String dateStr = mD.group(1);
            if (t.getDeadline() == null) {
                LocalDate ld = parseDate(dateStr);
                t.setDeadline(ld);
            }
            title = mD.replaceAll("").trim();
        }

        else if (t.getDeadline() == null && title.contains("!before")) {
            t.setDeadline(LocalDate.now());
            title = title.replace("!before", "").trim();
        }

        title = title.trim();
        if (title.length() < 4) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title must be at least 4 characters long"
            );
        }

        t.setTitle(title);
    }
    @Transactional
    public void updateStatusIfNeeded(TaskEntity t) {
        if (t.getStatus() == TaskStatus.Active && t.getDeadline() != null
                && LocalDate.now().isAfter(t.getDeadline())) {
            t.setStatus(TaskStatus.Overdue);
            repo.save(t);
        }
    }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DTF_DOT);
        } catch (DateTimeParseException ex) {
            return LocalDate.parse(s, DTF_DASH);
        }
    }
}