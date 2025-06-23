package org.example.todo.repository;

import com.sun.jdi.connect.spi.Connection;
import org.example.todo.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findAllByOrderByDeadlineAsc();
    List<TaskEntity> findAllByOrderByCreatedAtDesc();
    List<TaskEntity> findTasksByTitle(String title) {
        List<TaskEntity> tasks = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tasks WHERE title = '" + title + "'")) {
            while (rs.next()) {

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}
