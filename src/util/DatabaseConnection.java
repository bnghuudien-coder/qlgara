package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final Path CONFIG_PATH = Path.of("database.properties");
    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=a1;encrypt=true;trustServerCertificate=true;";

    private DatabaseConnection() {
    }

    public static Connection open() throws SQLException {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
                properties.load(reader);
            } catch (IOException exception) {
                throw new SQLException("Không đọc được cấu hình database.properties.", exception);
            }
        }

        String url = getSetting(properties, "gara.db.url", "GARA_DB_URL", "url", DEFAULT_URL);
        String user = getSetting(properties, "gara.db.user", "GARA_DB_USER", "user", "sa");
        String password = getSetting(properties, "gara.db.password", "GARA_DB_PASSWORD", "password", null);
        if (password == null || password.isBlank()) {
            throw new SQLException("Chưa cấu hình mật khẩu database. Kiểm tra database.properties.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String getSetting(
            Properties properties, String systemProperty, String environmentVariable,
            String configKey, String defaultValue) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        if (value == null || value.isBlank()) {
            value = properties.getProperty(configKey);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
