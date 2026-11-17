
package com.skillbridge;

import java.sql.Connection;

public class TestDBConnection {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {

            if (connection != null && connection.isValid(5)) {
                System.out.println("Database connected successfully!");
                System.out.println("Oracle connection is active.");
            }

        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
