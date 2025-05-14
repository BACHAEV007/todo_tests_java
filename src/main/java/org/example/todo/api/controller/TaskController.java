package org.example.todo.api.controller;

import jakarta.validation.Valid;
import org.example.todo.model.TaskEntity;
import org.example.todo.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@Validated
public class TaskController {

    private final TaskService svc;

    public TaskController(TaskService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<TaskEntity> list(@RequestParam Optional<String> sort) {
        return svc.getAllSorted(sort.orElse(""));
    }

    @GetMapping("/{id}")
    public TaskEntity getOne(@PathVariable Long id) {
        return svc.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskEntity create(@Valid @RequestBody TaskEntity in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    public TaskEntity update(@PathVariable Long id, @Valid @RequestBody TaskEntity in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }

    @PatchMapping("/{id}/complete")
    public TaskEntity complete(@PathVariable Long id) {
        return svc.markCompleted(id, true);
    }

    @PatchMapping("/{id}/uncomplete")
    public TaskEntity uncomplete(@PathVariable Long id) {
        return svc.markCompleted(id, false);
    }
}
