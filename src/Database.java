import java.sql.*;
import java.util.ArrayList;

/**
 * Toàn bộ phần dữ liệu của chương trình, gồm 4 phần:
 *   1. Kết nối database (BƯỚC 1 và BƯỚC 5 của JDBC)
 *   2. Các "record": mỗi record là một dòng của bảng trong database
 *   3. Các hàm SELECT (lấy dữ liệu)
 *   4. Các hàm INSERT / UPDATE / DELETE (thêm, sửa, xóa)
 *
 * Database là MỘT FILE trong thư mục data/ (H2 Database), nên chép project
 * sang máy khác là chạy được ngay, không cần cài SQL Server.
 * Câu SQL nào cũng có "nguoi_dung_id = ?" để mỗi người chỉ thấy dữ liệu của mình.
 */
public class Database {

    // =================================================================
    // 1. KẾT NỐI DATABASE
    // =================================================================

    public static final String DUONG_DAN_FILE = "data/quanlychitieu.mv.db";
    // IFEXISTS=TRUE: chưa có file database thì báo lỗi, không tự tạo database rỗng
    private static final String URL = "jdbc:h2:file:./data/quanlychitieu;IFEXISTS=TRUE;DB_CLOSE_DELAY=-1";

    /** BƯỚC 1: tạo kết nối tới database. */
    public static Connection moKetNoi() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }

    /** BƯỚC 5: đóng ResultSet, Statement, Connection (cái nào không dùng thì truyền null). */
    public static void dongKetNoi(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Loi khi dong ket noi: " + e.getMessage());
        }
    }

    // =================================================================
    // 2. CÁC RECORD
    // "record" là cách viết ngắn của một lớp chỉ để chứa dữ liệu: Java tự tạo
    // constructor và hàm lấy giá trị, ví dụ vi.id(), vi.tenVi().
    // toString() quyết định chữ hiện trong ô chọn (JComboBox).
    // =================================================================

    public record NguoiDung(int id, String hoTen, String email) {
        public String toString() {
            return hoTen + " (" + email + ")";
        }
    }

    public record Vi(int id, String tenVi, double soDuBanDau) {
        public String toString() {
            return tenVi;
        }
    }

    public record DanhMuc(int id, String tenDanhMuc, String loai) {
        public String toString() {
            return tenDanhMuc;
        }
    }

    /**
     * Một giao dịch. Bảng giao_dich không có cột "loai" (thu/chi), nên 3 trường cuối
     * (tenDanhMuc, loai, tenVi) là thông tin lấy thêm bằng JOIN để hiển thị lên bảng.
     */
    public record GiaoDich(int id, int nguoiDungId, int viId, int danhMucId, double soTien,
                           Date ngay, String ghiChu, String tenDanhMuc, String loai, String tenVi) {

        // Cách tạo rút gọn khi thêm / sửa (không cần 3 thông tin JOIN)
        public GiaoDich(int id, int nguoiDungId, int viId, int danhMucId,
                        double soTien, Date ngay, String ghiChu) {
            this(id, nguoiDungId, viId, danhMucId, soTien, ngay, ghiChu, "", "", "");
        }
    }

    // =================================================================
    // 3. CÁC HÀM SELECT (đều đi qua 5 bước JDBC)
    // =================================================================

    /** Lấy danh sách người dùng (cho form đăng nhập). */
    public static ArrayList<NguoiDung> layNguoiDung() throws SQLException {
        ArrayList<NguoiDung> ds = new ArrayList<NguoiDung>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = moKetNoi();                                                   // BƯỚC 1
            String sql = "SELECT id, ho_ten, email FROM nguoi_dung ORDER BY id"; // BƯỚC 2
            ps = conn.prepareStatement(sql);                                     // BƯỚC 3
            rs = ps.executeQuery();                                              // BƯỚC 4
            while (rs.next()) {
                ds.add(new NguoiDung(rs.getInt("id"), rs.getString("ho_ten"), rs.getString("email")));
            }
        } finally {
            dongKetNoi(conn, ps, rs);                                            // BƯỚC 5
        }
        return ds;
    }

    /** Lấy ví của một người. tuKhoa = "" thì lấy tất cả, ngược lại tìm theo tên ví. */
    public static ArrayList<Vi> layVi(int nguoiDungId, String tuKhoa) throws SQLException {
        ArrayList<Vi> ds = new ArrayList<Vi>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = moKetNoi();                                                   // BƯỚC 1
            // "? = ''" nghĩa là: nếu ô tìm kiếm trống thì điều kiện đó luôn đúng.
            // LOWER(...) để tìm không phân biệt hoa/thường (kể cả chữ có dấu).
            String sql = "SELECT id, ten_vi, so_du_ban_dau FROM vi "
                       + "WHERE nguoi_dung_id = ? AND (? = '' OR LOWER(ten_vi) LIKE ?) ORDER BY id"; // BƯỚC 2
            ps = conn.prepareStatement(sql);                                     // BƯỚC 3
            ps.setInt(1, nguoiDungId);                                           // điền giá trị cho dấu ? thứ 1
            ps.setString(2, tuKhoa);
            ps.setString(3, "%" + tuKhoa.toLowerCase() + "%");
            rs = ps.executeQuery();                                              // BƯỚC 4
            while (rs.next()) {
                ds.add(new Vi(rs.getInt("id"), rs.getString("ten_vi"), rs.getDouble("so_du_ban_dau")));
            }
        } finally {
            dongKetNoi(conn, ps, rs);                                            // BƯỚC 5
        }
        return ds;
    }

    /** Lấy danh mục của một người. loai = "thu" / "chi" / "" (tất cả); tuKhoa = "" thì không lọc tên. */
    public static ArrayList<DanhMuc> layDanhMuc(int nguoiDungId, String loai, String tuKhoa) throws SQLException {
        ArrayList<DanhMuc> ds = new ArrayList<DanhMuc>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = moKetNoi();                                                   // BƯỚC 1
            String sql = "SELECT id, ten_danh_muc, loai FROM danh_muc "
                       + "WHERE nguoi_dung_id = ? AND (? = '' OR loai = ?) "
                       + "AND (? = '' OR LOWER(ten_danh_muc) LIKE ?) ORDER BY loai, id"; // BƯỚC 2 (chi trước thu)
            ps = conn.prepareStatement(sql);                                     // BƯỚC 3
            ps.setInt(1, nguoiDungId);
            ps.setString(2, loai);
            ps.setString(3, loai);
            ps.setString(4, tuKhoa);
            ps.setString(5, "%" + tuKhoa.toLowerCase() + "%");
            rs = ps.executeQuery();                                              // BƯỚC 4
            while (rs.next()) {
                ds.add(new DanhMuc(rs.getInt("id"), rs.getString("ten_danh_muc"), rs.getString("loai")));
            }
        } finally {
            dongKetNoi(conn, ps, rs);                                            // BƯỚC 5
        }
        return ds;
    }

    /** Lấy giao dịch, có tìm kiếm / lọc. tuKhoa = "" hoặc loai = "" thì không lọc theo mục đó. */
    public static ArrayList<GiaoDich> timGiaoDich(int nguoiDungId, String tuKhoa, String loai) throws SQLException {
        ArrayList<GiaoDich> ds = new ArrayList<GiaoDich>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = moKetNoi();                                                   // BƯỚC 1
            // JOIN để lấy thêm tên danh mục, loại (thu/chi), tên ví
            String sql = "SELECT g.id, g.vi_id, g.danh_muc_id, g.so_tien, g.ngay_giao_dich, g.ghi_chu, "
                       + "       d.ten_danh_muc, d.loai, v.ten_vi "
                       + "FROM giao_dich g "
                       + "JOIN danh_muc d ON g.danh_muc_id = d.id "
                       + "JOIN vi v ON g.vi_id = v.id "
                       + "WHERE g.nguoi_dung_id = ? "
                       + "AND (? = '' OR LOWER(g.ghi_chu) LIKE ? OR LOWER(d.ten_danh_muc) LIKE ?) "
                       + "AND (? = '' OR d.loai = ?) "
                       + "ORDER BY g.ngay_giao_dich DESC, g.id DESC";            // BƯỚC 2
            ps = conn.prepareStatement(sql);                                     // BƯỚC 3
            String mauTim = "%" + tuKhoa.toLowerCase() + "%";
            ps.setInt(1, nguoiDungId);
            ps.setString(2, tuKhoa);
            ps.setString(3, mauTim);
            ps.setString(4, mauTim);
            ps.setString(5, loai);
            ps.setString(6, loai);
            rs = ps.executeQuery();                                              // BƯỚC 4: executeQuery() dùng cho SELECT
            while (rs.next()) {
                ds.add(new GiaoDich(rs.getInt("id"), nguoiDungId, rs.getInt("vi_id"),
                        rs.getInt("danh_muc_id"), rs.getDouble("so_tien"), rs.getDate("ngay_giao_dich"),
                        rs.getString("ghi_chu"), rs.getString("ten_danh_muc"), rs.getString("loai"),
                        rs.getString("ten_vi")));
            }
        } finally {
            dongKetNoi(conn, ps, rs);                                            // BƯỚC 5
        }
        return ds;
    }

    // =================================================================
    // 4. CÁC HÀM INSERT / UPDATE / DELETE
    // Cả ba loại câu lệnh này cùng dùng hàm chayCapNhat() bên dưới
    // (viết 1 lần, dùng nhiều lần) nên mỗi hàm chỉ còn câu SQL và các giá trị.
    // =================================================================

    /**
     * Chạy một câu INSERT / UPDATE / DELETE. "Object... thamSo" nghĩa là truyền bao nhiêu
     * giá trị cũng được, lần lượt điền vào các dấu ? của câu SQL.
     * Trả về true nếu có ít nhất một dòng bị ảnh hưởng.
     */
    private static boolean chayCapNhat(String sql, Object... thamSo) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = moKetNoi();                                                   // BƯỚC 1
            ps = conn.prepareStatement(sql);                                     // BƯỚC 2 + 3 (câu SQL do nơi gọi viết)
            for (int i = 0; i < thamSo.length; i++) {
                ps.setObject(i + 1, thamSo[i]);                                  // điền lần lượt các dấu ?
            }
            return ps.executeUpdate() > 0;                                       // BƯỚC 4: executeUpdate() dùng cho INSERT/UPDATE/DELETE
        } finally {
            dongKetNoi(conn, ps, null);                                          // BƯỚC 5
        }
    }

    /** Chạy câu SELECT COUNT(*) có một tham số id, trả về số đếm được. */
    private static int dem(String sql, int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = moKetNoi();                                                   // BƯỚC 1
            ps = conn.prepareStatement(sql);                                     // BƯỚC 2 + 3
            ps.setInt(1, id);
            rs = ps.executeQuery();                                              // BƯỚC 4
            rs.next();
            return rs.getInt(1);
        } finally {
            dongKetNoi(conn, ps, rs);                                            // BƯỚC 5
        }
    }

    // ----- Giao dịch -----
    public static boolean themGiaoDich(GiaoDich gd) throws SQLException {
        return chayCapNhat("INSERT INTO giao_dich (nguoi_dung_id, vi_id, danh_muc_id, so_tien, ngay_giao_dich, ghi_chu) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                gd.nguoiDungId(), gd.viId(), gd.danhMucId(), gd.soTien(), gd.ngay(), gd.ghiChu());
    }

    public static boolean capNhatGiaoDich(GiaoDich gd) throws SQLException {
        return chayCapNhat("UPDATE giao_dich SET vi_id = ?, danh_muc_id = ?, so_tien = ?, ngay_giao_dich = ?, ghi_chu = ? "
                + "WHERE id = ? AND nguoi_dung_id = ?",
                gd.viId(), gd.danhMucId(), gd.soTien(), gd.ngay(), gd.ghiChu(), gd.id(), gd.nguoiDungId());
    }

    public static boolean xoaGiaoDich(int id, int nguoiDungId) throws SQLException {
        return chayCapNhat("DELETE FROM giao_dich WHERE id = ? AND nguoi_dung_id = ?", id, nguoiDungId);
    }

    // ----- Ví -----
    public static boolean themVi(int nguoiDungId, String tenVi, double soDuBanDau) throws SQLException {
        return chayCapNhat("INSERT INTO vi (nguoi_dung_id, ten_vi, so_du_ban_dau) VALUES (?, ?, ?)",
                nguoiDungId, tenVi, soDuBanDau);
    }

    public static boolean capNhatVi(int id, int nguoiDungId, String tenVi, double soDuBanDau) throws SQLException {
        return chayCapNhat("UPDATE vi SET ten_vi = ?, so_du_ban_dau = ? WHERE id = ? AND nguoi_dung_id = ?",
                tenVi, soDuBanDau, id, nguoiDungId);
    }

    public static boolean xoaVi(int id, int nguoiDungId) throws SQLException {
        return chayCapNhat("DELETE FROM vi WHERE id = ? AND nguoi_dung_id = ?", id, nguoiDungId);
    }

    /** Số giao dịch đang dùng ví này (ví đang có giao dịch thì không được xóa). */
    public static int demGiaoDichCuaVi(int viId) throws SQLException {
        return dem("SELECT COUNT(*) FROM giao_dich WHERE vi_id = ?", viId);
    }

    // ----- Danh mục -----
    public static boolean themDanhMuc(int nguoiDungId, String tenDanhMuc, String loai) throws SQLException {
        return chayCapNhat("INSERT INTO danh_muc (nguoi_dung_id, ten_danh_muc, loai) VALUES (?, ?, ?)",
                nguoiDungId, tenDanhMuc, loai);
    }

    // Chỉ cho sửa tên, không cho đổi loại (đổi loại sẽ làm sai các giao dịch đã có)
    public static boolean capNhatDanhMuc(int id, int nguoiDungId, String tenDanhMuc) throws SQLException {
        return chayCapNhat("UPDATE danh_muc SET ten_danh_muc = ? WHERE id = ? AND nguoi_dung_id = ?",
                tenDanhMuc, id, nguoiDungId);
    }

    public static boolean xoaDanhMuc(int id, int nguoiDungId) throws SQLException {
        return chayCapNhat("DELETE FROM danh_muc WHERE id = ? AND nguoi_dung_id = ?", id, nguoiDungId);
    }

    public static int demGiaoDichCuaDanhMuc(int danhMucId) throws SQLException {
        return dem("SELECT COUNT(*) FROM giao_dich WHERE danh_muc_id = ?", danhMucId);
    }

    // Quan trọng: bảng ngan_sach tự động bị XÓA THEO khi xóa danh mục (ON DELETE CASCADE),
    // nên phải tự đếm để không làm mất ngân sách mà người dùng không biết.
    public static int demNganSachCuaDanhMuc(int danhMucId) throws SQLException {
        return dem("SELECT COUNT(*) FROM ngan_sach WHERE danh_muc_id = ?", danhMucId);
    }
}
