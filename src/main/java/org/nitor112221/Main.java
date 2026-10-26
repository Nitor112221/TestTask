package org.nitor112221;


import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses("127.0.0.1:10800");


        // Работа с распределённым кэшем в виде Map
        try (IgniteClient client = Ignition.startClient(cfg)) {
            System.out.println("!!! Успешное подключение !!!");
            ClientCache<Integer, String> cache = client.getOrCreateCache("DistributeMap");
            Map<Integer, String> data = new HashMap<>();
            for (int i = 1; i <= 10; i++) {
                data.put(i, "Value_" + i);
            }

            cache.putAll(data);
            for (int i = 1; i <= 10; i++) {
                String value = cache.get(i);
                System.out.printf("Ключ %d Значение %s%n", i, value);
            }
        }

        // Работа с распределённым кэшем через SQL запросы
        Class.forName("org.apache.ignite.IgniteJdbcThinDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1:10800")) {
            System.out.println("!!! Успешное подключение через JDBC !!!");
            try (Statement stmp = conn.createStatement()) {
                stmp.executeUpdate("CREATE TABLE IF NOT EXISTS Example (" +
                        "id LONG PRIMARY KEY, " +
                        "value1 VARCHAR, " +
                        "value2 INT)");
            }

            String insertSql = "INSERT INTO Example (id, value1, value2) VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setLong(1, 1L);
                pstmt.setString(2, "test1");
                pstmt.setInt(3, 321);
                pstmt.executeUpdate();

                pstmt.setLong(1, 2L);
                pstmt.setString(2, "test2");
                pstmt.setInt(3, 123);
                pstmt.executeUpdate();

                pstmt.setLong(1, 3L);
                pstmt.setString(2, "test3");
                pstmt.setInt(3, 213);
                pstmt.executeUpdate();
            }

            try (
                    Statement stmp = conn.createStatement();
                    ResultSet rs = stmp.executeQuery("SELECT * FROM Example ORDER BY value2 DESC")
            ) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String value1 = rs.getString("value1");
                    int value2 = rs.getInt("value2");
                    System.out.printf("%d, %s, %d%n", id, value1, value2);
                }
            }
        }
    }
}