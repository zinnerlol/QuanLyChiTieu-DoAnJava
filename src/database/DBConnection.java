package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp này chỉ làm MỘT việc: mở kết nối tới SQL Server.
 * Các lớp DAO sẽ gọi DBConnection.getConnection() mỗi khi cần chạy SQL.
 */
public class DBConnection {

    // ===================== SỬA Ở ĐÂY =====================

    private static final String SERVER = "DESKTOP-BKL4PCC";
    private static final String PORT = "1433";          // để nguyên, không dùng khi có INSTANCE
    private static final String INSTANCE = "SQLEXPRESS";
    private static final boolean DUNG_WINDOWS_AUTH = true;
    private static final String USERNAME = "sa";        // không dùng khi Windows Auth
    private static final String PASSWORD = "";          // không dùng khi Windows Auth

    // ======================================================

    // Tên database, giữ nguyên, không sửa.
    private static final String DATABASE = "quan_ly_chi_tieu";

    /**
     * Ghép các thông tin ở trên thành chuỗi kết nối (URL).
     */
    private static String taoUrl() {
        String url = "jdbc:sqlserver://" + SERVER;

        if (INSTANCE.equals("")) {
            // Bản mặc định: nối thêm cổng
            url = url + ":" + PORT;
        } else {
            // Bản có instance name: nối thêm \TenInstance
            url = url + "\\" + INSTANCE;
        }

        url = url + ";databaseName=" + DATABASE;
        // encrypt + trustServerCertificate: tránh lỗi chứng chỉ SSL trên máy cá nhân
        url = url + ";encrypt=true;trustServerCertificate=true";

        if (DUNG_WINDOWS_AUTH) {
            url = url + ";integratedSecurity=true";
        }
        return url;
    }

    /**
     * Mở một kết nối mới. Nơi gọi hàm này phải tự đóng kết nối sau khi dùng xong.
     * Nếu lỗi, hàm ném SQLException để nơi gọi hiển thị thông báo.
     */
    public static Connection getConnection() throws SQLException {
        String url = taoUrl();

        if (DUNG_WINDOWS_AUTH) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, USERNAME, PASSWORD);
    }

    /**
     * Kiểm tra kết nối. Trả về true nếu kết nối được, false nếu thất bại.
     * Dùng khi khởi động chương trình để báo lỗi sớm.
     */
    public static boolean kiemTraKetNoi() {
        Connection conn = null;
        try {
            conn = getConnection();
            return true;
        } catch (SQLException e) {
            System.out.println("Loi ket noi: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                // không cần xử lý thêm
            }
        }
    }

    /**
     * Trả về nội dung lỗi để hiện cho người dùng khi kết nối thất bại.
     */
    public static String layThongBaoLoi() {
        try {
            Connection conn = getConnection();
            conn.close();
            return "";
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}