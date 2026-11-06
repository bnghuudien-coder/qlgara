package dao;

import entity.CaLamViec;
import util.DatabaseConnection;
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
        return DatabaseConnection.open();
    }
}
