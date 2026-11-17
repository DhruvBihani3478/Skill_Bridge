
// package com.skillbridge;

// import java.sql.Connection;
// import java.sql.SQLException;

// import oracle.jdbc.pool.OracleDataSource;

// public class DBConnection {

//     private static final String URL =
//         "jdbc:oracle:thin:@(description=(address=(protocol=tcps)(port=2484)(host=db.freesql.com))(connect_data=(service_name=26ai_un3c1)))";

//     public static Connection getConnection() throws SQLException {

//         String username = System.getenv("SKILLBRIDGE_DB_USER");
//         String password = System.getenv("SKILLBRIDGE_DB_PASSWORD");

//         if (username == null || username.trim().isEmpty()
//                 || password == null || password.isEmpty()) {
//             throw new SQLException(
//                 "Database credentials are missing. Set SKILLBRIDGE_DB_USER and SKILLBRIDGE_DB_PASSWORD."
//             );
//         }

//         OracleDataSource dataSource = new OracleDataSource();

//         dataSource.setURL(URL);
//         dataSource.setUser(username);
//         dataSource.setPassword(password);

//         return dataSource.getConnection();
//     }
// }



package com.skillbridge;

import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.pool.OracleDataSource;

public class DBConnection {

    private static final String URL =
        "jdbc:oracle:thin:@(description=(address=(protocol=tcps)(port=2484)(host=db.freesql.com))(connect_data=(service_name=26ai_un3c1)))";

    private static final OracleDataSource DATA_SOURCE = createDataSource();

    private static OracleDataSource createDataSource() {

        try {
            String username = System.getenv("SKILLBRIDGE_DB_USER");
            String password = System.getenv("SKILLBRIDGE_DB_PASSWORD");

            if (username == null || username.trim().isEmpty()
                    || password == null || password.isEmpty()) {
                throw new SQLException(
                    "Database credentials are missing."
                );
            }

            OracleDataSource dataSource = new OracleDataSource();

            dataSource.setURL(URL);
            dataSource.setUser(username);
            dataSource.setPassword(password);

            return dataSource;

        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }
}
