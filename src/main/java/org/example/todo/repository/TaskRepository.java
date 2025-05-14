package org.example.todo.repository;

import org.example.todo.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findAllByOrderByDeadlineAsc();
    List<TaskEntity> findAllByOrderByCreatedAtDesc();

}
