package dao;

import entity.PhieuBanGiao;
import entity.KhachHang;
import entity.NhanVien;
import entity.Xe;
import util.DatabaseConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuBanGiaoDAO {
    public List<PhieuBanGiao> findAll() throws SQLException {
        String sql = "SELECT maPhieuBanGiao, maXe, maKH, tenKhachHang, maNV, "
                + "tinhTrangXeHienTai, tenNguoiNhan, soDienThoaiNguoiNhan FROM PhieuBanGiao "
                + "ORDER BY maPhieuBanGiao";
        List<PhieuBanGiao> danhSach = new ArrayList<>();
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                danhSach.add(mapRow(resultSet));
            }
        }
        return danhSach;
    }

    public PhieuBanGiao findById(String maPhieuBanGiao) throws SQLException {
        String sql = "SELECT maPhieuBanGiao, maXe, maKH, tenKhachHang, maNV, "
                + "tinhTrangXeHienTai, tenNguoiNhan, soDienThoaiNguoiNhan "
                + "FROM PhieuBanGiao WHERE maPhieuBanGiao = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maPhieuBanGiao);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public boolean insert(PhieuBanGiao phieu) throws SQLException {
        String sql = "INSERT INTO PhieuBanGiao (maPhieuBanGiao, maXe, maKH, tenKhachHang, maNV, "
                + "tinhTrangXeHienTai, tenNguoiNhan, soDienThoaiNguoiNhan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu, true);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(PhieuBanGiao phieu) throws SQLException {
        String sql = "UPDATE PhieuBanGiao SET maXe = ?, maKH = ?, tenKhachHang = ?, maNV = ?, "
                + "tinhTrangXeHienTai = ?, tenNguoiNhan = ?, soDienThoaiNguoiNhan = ? "
                + "WHERE maPhieuBanGiao = ?";
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ganThamSo(statement, phieu, false);
            statement.setString(8, phieu.getMaPhieuBanGiao());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String maPhieuBanGiao) throws SQLException {
        try (Connection connection = moKetNoi();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM PhieuBanGiao WHERE maPhieuBanGiao = ?")) {
            statement.setString(1, maPhieuBanGiao);
            return statement.executeUpdate() > 0;
        }
    }

    private PhieuBanGiao mapRow(ResultSet resultSet) throws SQLException {
        return new PhieuBanGiao(
                resultSet.getString("maPhieuBanGiao"),
                new Xe(resultSet.getString("maXe")),
                new KhachHang(resultSet.getString("maKH")),
                resultSet.getString("tenKhachHang"),
                new NhanVien(resultSet.getString("maNV")),
                resultSet.getString("tinhTrangXeHienTai"),
                resultSet.getString("tenNguoiNhan"),
                resultSet.getString("soDienThoaiNguoiNhan"));
    }

    private void ganThamSo(PreparedStatement statement, PhieuBanGiao phieu, boolean includeKey)
            throws SQLException {
        int index = 1;
        if (includeKey) {
            statement.setString(index++, phieu.getMaPhieuBanGiao());
        }
        statement.setString(index++, phieu.getXe() == null ? null : phieu.getXe().getMaXe());
        statement.setString(index++, phieu.getKhachHang() == null
                ? null : phieu.getKhachHang().getMaKH());
        statement.setString(index++, phieu.getTenKhachHang());
        statement.setString(index++, phieu.getNhanVien() == null
                ? null : phieu.getNhanVien().getMaNV());
        statement.setString(index++, phieu.getTinhTrangXeHienTai());
        statement.setString(index++, phieu.getTenNguoiNhan());
        statement.setString(index, phieu.getSoDienThoaiNguoiNhan());
    }

    private Connection moKetNoi() throws SQLException {
        return DatabaseConnection.open();
    }
}
