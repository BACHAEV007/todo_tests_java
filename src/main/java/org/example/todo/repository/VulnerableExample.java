package org.example.todo.repository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VulnerableExample {
    public void vulnerableMethod(String userInput) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM users WHERE name = '" + userInput + "'";
        ResultSet rs = stmt.executeQuery(query);
    }
}