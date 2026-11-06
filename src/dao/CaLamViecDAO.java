package dao;

import entity.CaLamViec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CaLamViecDAO {
    public List<CaLamViec> findAll() throws SQLException {
        String sql = "SELECT maCa, gioBatDau, gioKetThuc FROM CaLamViec";
        List<CaLamViec> danhSachCa = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSachCa.add(new CaLamViec(
                        resultSet.getString("maCa"),
                        resultSet.getTime("gioBatDau"),
                        resultSet.getTime("gioKetThuc")));
            }
        }
        danhSachCa.sort(Comparator.comparing(CaLamViec::getGioBatDau,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return danhSachCa;
    }

    private Connection moKetNoi() throws SQLException {
        java.util.Properties properties = new java.util.Properties();
        try (var reader = Files.newBufferedReader(Path.of("database.properties"))) {
            properties.load(reader);
        } catch (IOException exception) {
            throw new SQLException("Không đọc được cấu hình database.properties.", exception);
        }
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("user"),
                properties.getProperty("password"));
    }
}
