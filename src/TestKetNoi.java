import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestKetNoi {

    public static void main(String[] args) {
        System.out.println("Dang kiem tra ket noi...");

        if (!DBConnection.kiemTraKetNoi()) {
            System.out.println("KET NOI THAT BAI: " + DBConnection.layThongBaoLoi());
            return;
        }
        System.out.println("Ket noi thanh cong!");

        // Đếm số dòng của từng bảng để chắc chắn data.sql đã chạy
        String[] tenBang = {"nguoi_dung", "vi", "danh_muc", "giao_dich", "ngan_sach"};

        for (int i = 0; i < tenBang.length; i++) {
            String sql = "SELECT COUNT(*) FROM " + tenBang[i];
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("Bang " + tenBang[i] + ": " + rs.getInt(1) + " dong");
                }
                rs.close();
                ps.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println("Loi doc bang " + tenBang[i] + ": " + e.getMessage());
            }
        }
    }
}