import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * FORM 2: GIAO DỊCH (form chính, mở ra sau khi đăng nhập).
 * Từ đây mở được FORM 3 (Quản lý ví), FORM 4 (Quản lý danh mục) hoặc đăng xuất.
 *
 * CÁCH GẮN SỰ KIỆN (xem hàm ganSuKien() và actionPerformed()):
 *   1. Lớp này "implements ActionListener" nên tự nó là bộ lắng nghe sự kiện.
 *   2. Mỗi nút gắn vào bằng:  nut.addActionListener(this);
 *   3. Khi bấm nút, Swing tự gọi actionPerformed(); trong đó ta xem
 *      e.getSource() là nút nào để chạy đúng chức năng.
 */
public class MainFrame extends JFrame implements ActionListener {

    // ----- Thành phần giao diện -----
    private JLabel lblXinChao = new JLabel();
    private JButton btnVi = new JButton("Quản lý ví");
    private JButton btnDanhMuc = new JButton("Quản lý danh mục");
    private JButton btnDangXuat = new JButton("Đăng xuất");

    // Form nhập liệu
    private JComboBox<String> cboLoai = new JComboBox<String>(new String[] {"Chi", "Thu"});
    private JComboBox<Database.DanhMuc> cboDanhMuc = new JComboBox<Database.DanhMuc>();
    private JComboBox<Database.Vi> cboVi = new JComboBox<Database.Vi>();
    private JTextField txtSoTien = new JTextField();
    private JTextField txtNgay = new JTextField();
    private JTextField txtGhiChu = new JTextField();
    private JButton btnThem = new JButton("Thêm");
    private JButton btnCapNhat = new JButton("Cập nhật");
    private JButton btnXoa = new JButton("Xóa");
    private JButton btnLamMoi = new JButton("Làm mới");

    // Tìm kiếm / lọc
    private JTextField txtTuKhoa = new JTextField(20);
    private JComboBox<String> cboLocLoai = new JComboBox<String>(new String[] {"Tất cả", "Thu", "Chi"});
    private JButton btnTim = new JButton("Tìm kiếm");

    // Bảng
    private DefaultTableModel bangModel;
    private JTable bang;
    private JLabel lblTongKet = new JLabel(" ");

    // ----- Dữ liệu đang dùng -----
    private int nguoiDungId; // người đang đăng nhập
    private ArrayList<Database.GiaoDich> danhSach = new ArrayList<Database.GiaoDich>(); // các dòng đang hiện trên bảng
    private DecimalFormat dinhDangTien = new DecimalFormat("#,##0");

    public MainFrame(Database.NguoiDung nguoiDung) {
        nguoiDungId = nguoiDung.id();
        lblXinChao.setText("Xin chào, " + nguoiDung);
        setTitle("QUẢN LÝ CHI TIÊU CÁ NHÂN");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        taoGiaoDien();
        napLaiDuLieu();
        ganSuKien(); // gắn sự kiện SAU CÙNG, để việc nạp dữ liệu ban đầu không kích hoạt sự kiện
    }

    // =================================================================
    // 1. DỰNG GIAO DIỆN
    // =================================================================
    private void taoGiaoDien() {
        setLayout(new BorderLayout(10, 10));

        // Hàng trên cùng: lời chào bên trái, các nút mở form khác bên phải
        JPanel pnlNutPhai = new JPanel();
        pnlNutPhai.add(btnVi);
        pnlNutPhai.add(btnDanhMuc);
        pnlNutPhai.add(btnDangXuat);
        JPanel pnlTrenCung = new JPanel(new BorderLayout());
        pnlTrenCung.setBorder(BorderFactory.createEmptyBorder(5, 12, 0, 5));
        pnlTrenCung.add(lblXinChao, BorderLayout.WEST);
        pnlTrenCung.add(pnlNutPhai, BorderLayout.EAST);
        add(pnlTrenCung, BorderLayout.NORTH);

        // Form nhập liệu: lưới 3 hàng x 4 cột (nhãn, ô nhập, nhãn, ô nhập)
        JPanel pnlLuoi = new JPanel(new GridLayout(3, 4, 10, 8));
        pnlLuoi.add(new JLabel("Loại giao dịch:"));
        pnlLuoi.add(cboLoai);
        pnlLuoi.add(new JLabel("Danh mục:"));
        pnlLuoi.add(cboDanhMuc);
        pnlLuoi.add(new JLabel("Số tiền (chỉ nhập số):"));
        pnlLuoi.add(txtSoTien);
        pnlLuoi.add(new JLabel("Ví:"));
        pnlLuoi.add(cboVi);
        pnlLuoi.add(new JLabel("Ngày (yyyy-MM-dd):"));
        pnlLuoi.add(txtNgay);
        pnlLuoi.add(new JLabel("Ghi chú:"));
        pnlLuoi.add(txtGhiChu);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlNut.add(btnThem);
        pnlNut.add(btnCapNhat);
        pnlNut.add(btnXoa);
        pnlNut.add(btnLamMoi);

        JPanel pnlForm = new JPanel(new BorderLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin giao dịch"));
        pnlForm.add(pnlLuoi, BorderLayout.CENTER);
        pnlForm.add(pnlNut, BorderLayout.SOUTH);

        // Khu vực tìm kiếm / lọc
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlTim.setBorder(BorderFactory.createTitledBorder("Tìm kiếm / Lọc"));
        pnlTim.add(new JLabel("Ghi chú hoặc danh mục:"));
        pnlTim.add(txtTuKhoa);
        pnlTim.add(new JLabel("Loại:"));
        pnlTim.add(cboLocLoai);
        pnlTim.add(btnTim);

        JPanel pnlTren = new JPanel(new BorderLayout(5, 5));
        pnlTren.add(pnlForm, BorderLayout.CENTER);
        pnlTren.add(pnlTim, BorderLayout.SOUTH);

        // Bảng giao dịch
        String[] tenCot = {"ID", "Loại", "Số tiền", "Danh mục", "Ví", "Ngày giao dịch", "Ghi chú"};
        bangModel = new DefaultTableModel(tenCot, 0);
        bang = new JTable(bangModel);
        bang.setDefaultEditor(Object.class, null); // không cho gõ trực tiếp vào ô của bảng
        bang.setRowHeight(24);

        // Ghép tất cả vào cửa sổ
        JPanel pnlGiua = new JPanel(new BorderLayout(5, 5));
        pnlGiua.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        pnlGiua.add(pnlTren, BorderLayout.NORTH);
        pnlGiua.add(new JScrollPane(bang), BorderLayout.CENTER);
        add(pnlGiua, BorderLayout.CENTER);

        lblTongKet.setBorder(BorderFactory.createEmptyBorder(5, 12, 8, 12));
        add(lblTongKet, BorderLayout.SOUTH);
    }

    // =================================================================
    // 2. GẮN SỰ KIỆN
    // =================================================================
    private void ganSuKien() {
        // Các nút và ô chọn đều gọi về actionPerformed() ở dưới
        btnVi.addActionListener(this);
        btnDanhMuc.addActionListener(this);
        btnDangXuat.addActionListener(this);
        btnThem.addActionListener(this);
        btnCapNhat.addActionListener(this);
        btnXoa.addActionListener(this);
        btnLamMoi.addActionListener(this);
        btnTim.addActionListener(this);
        cboLoai.addActionListener(this);

        // Bấm chuột vào một dòng của bảng -> đổ dữ liệu dòng đó lên form
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chonDongTrenBang();
            }
        });
    }

    /** Swing gọi hàm này mỗi khi người dùng bấm nút hoặc đổi ô chọn đã gắn sự kiện. */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object nguon = e.getSource(); // đối tượng vừa được bấm

        if (nguon == btnThem) {
            themGiaoDich();
        } else if (nguon == btnCapNhat) {
            capNhatGiaoDich();
        } else if (nguon == btnXoa) {
            xoaGiaoDich();
        } else if (nguon == btnLamMoi) {
            xoaForm();
            txtTuKhoa.setText("");
            cboLocLoai.setSelectedIndex(0);
            napBang(false);
        } else if (nguon == btnTim) {
            napBang(true);
        } else if (nguon == cboLoai) {
            napDanhMuc(); // đổi Thu/Chi thì nạp lại danh sách danh mục
        } else if (nguon == btnVi) {
            new ViDialog(this, nguoiDungId).setVisible(true);      // mở FORM 3 (đóng lại thì chạy tiếp dòng dưới)
            napLaiDuLieu();
        } else if (nguon == btnDanhMuc) {
            new DanhMucDialog(this, nguoiDungId).setVisible(true); // mở FORM 4
            napLaiDuLieu();
        } else if (nguon == btnDangXuat) {
            int traLoi = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất không?",
                    "Đăng xuất", JOptionPane.YES_NO_OPTION);
            if (traLoi == JOptionPane.YES_OPTION) {
                dispose();                         // đóng form này
                new LoginFrame().setVisible(true); // quay về FORM 1 (đăng nhập)
            }
        }
    }

    // =================================================================
    // 3. NẠP DỮ LIỆU
    // =================================================================

    /** Nạp lại ví, danh mục, bảng (lúc mở form, và sau khi đóng form ví / danh mục). */
    private void napLaiDuLieu() {
        try {
            cboVi.removeAllItems();
            ArrayList<Database.Vi> dsVi = Database.layVi(nguoiDungId, "");
            for (int i = 0; i < dsVi.size(); i++) {
                cboVi.addItem(dsVi.get(i));
            }
        } catch (SQLException e) {
            baoLoi("Không đọc được danh sách ví:\n" + e.getMessage());
        }
        napDanhMuc();
        xoaForm();
        napBang(false);
    }

    /** Nạp danh mục đúng với loại Thu/Chi đang chọn. */
    private void napDanhMuc() {
        String loai = ((String) cboLoai.getSelectedItem()).toLowerCase(); // "Thu" -> "thu"
        try {
            cboDanhMuc.removeAllItems();
            ArrayList<Database.DanhMuc> ds = Database.layDanhMuc(nguoiDungId, loai, "");
            for (int i = 0; i < ds.size(); i++) {
                cboDanhMuc.addItem(ds.get(i));
            }
        } catch (SQLException e) {
            baoLoi("Không đọc được danh sách danh mục:\n" + e.getMessage());
        }
    }

    /** Lấy giao dịch theo ô tìm kiếm rồi đổ lên bảng. Ô tìm kiếm trống = hiển thị tất cả. */
    private void napBang(boolean baoNeuRong) {
        String loai = ""; // "" = cả thu lẫn chi
        String locLoai = (String) cboLocLoai.getSelectedItem();
        if (!locLoai.equals("Tất cả")) {
            loai = locLoai.toLowerCase();
        }

        try {
            danhSach = Database.timGiaoDich(nguoiDungId, txtTuKhoa.getText().trim(), loai);
        } catch (SQLException e) {
            baoLoi("Lỗi khi đọc giao dịch:\n" + e.getMessage());
            return;
        }

        bangModel.setRowCount(0); // xóa các dòng cũ
        double tongThu = 0;
        double tongChi = 0;
        for (int i = 0; i < danhSach.size(); i++) {
            Database.GiaoDich gd = danhSach.get(i);
            String tenLoai = "Chi";
            if (gd.loai().equals("thu")) {
                tenLoai = "Thu";
                tongThu = tongThu + gd.soTien();
            } else {
                tongChi = tongChi + gd.soTien();
            }
            Object[] dong = {gd.id(), tenLoai, dinhDangTien.format(gd.soTien()),
                             gd.tenDanhMuc(), gd.tenVi(), gd.ngay().toString(), gd.ghiChu()};
            bangModel.addRow(dong);
        }

        lblTongKet.setText("Số giao dịch: " + danhSach.size()
                + "   |   Tổng thu: " + dinhDangTien.format(tongThu)
                + "   |   Tổng chi: " + dinhDangTien.format(tongChi)
                + "   |   Chênh lệch: " + dinhDangTien.format(tongThu - tongChi) + " VNĐ");

        if (baoNeuRong && danhSach.size() == 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy giao dịch nào phù hợp.");
        }
    }

    /** Bấm vào một dòng của bảng: đổ dữ liệu của dòng đó lên form. */
    private void chonDongTrenBang() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            return;
        }
        Database.GiaoDich gd = danhSach.get(dong); // thứ tự dòng trên bảng = thứ tự trong danhSach

        // Chọn Thu/Chi, nạp lại danh mục cho đúng loại, rồi chọn đúng danh mục và ví (so sánh theo id)
        cboLoai.setSelectedItem(gd.loai().equals("thu") ? "Thu" : "Chi");
        napDanhMuc();
        for (int i = 0; i < cboDanhMuc.getItemCount(); i++) {
            if (cboDanhMuc.getItemAt(i).id() == gd.danhMucId()) {
                cboDanhMuc.setSelectedIndex(i);
            }
        }
        for (int i = 0; i < cboVi.getItemCount(); i++) {
            if (cboVi.getItemAt(i).id() == gd.viId()) {
                cboVi.setSelectedIndex(i);
            }
        }

        // Số tiền: bỏ phần ".0" nếu là số nguyên
        double tien = gd.soTien();
        txtSoTien.setText(tien == Math.floor(tien) ? String.valueOf((long) tien) : String.valueOf(tien));
        txtNgay.setText(gd.ngay().toString());
        txtGhiChu.setText(gd.ghiChu());
    }

    // =================================================================
    // 4. THÊM / SỬA / XÓA
    // =================================================================
    private void themGiaoDich() {
        Database.GiaoDich gd = layDuLieuTuForm();
        if (gd == null) {
            return; // dữ liệu chưa hợp lệ (đã báo lỗi rồi)
        }
        try {
            ketQua(Database.themGiaoDich(gd), "Thêm giao dịch");
        } catch (SQLException e) {
            baoLoi("Lỗi khi thêm giao dịch:\n" + e.getMessage());
        }
    }

    private void capNhatGiaoDich() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            baoLoi("Hãy chọn một giao dịch trên bảng trước khi cập nhật.");
            return;
        }
        Database.GiaoDich moi = layDuLieuTuForm();
        if (moi == null) {
            return;
        }
        // Giao dịch mang id của dòng đang chọn cùng dữ liệu mới từ form
        Database.GiaoDich gd = new Database.GiaoDich(danhSach.get(dong).id(), nguoiDungId,
                moi.viId(), moi.danhMucId(), moi.soTien(), moi.ngay(), moi.ghiChu());
        try {
            ketQua(Database.capNhatGiaoDich(gd), "Cập nhật giao dịch");
        } catch (SQLException e) {
            baoLoi("Lỗi khi cập nhật giao dịch:\n" + e.getMessage());
        }
    }

    private void xoaGiaoDich() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            baoLoi("Hãy chọn một giao dịch trên bảng trước khi xóa.");
            return;
        }
        int id = danhSach.get(dong).id();
        int traLoi = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa giao dịch có ID = " + id + " không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (traLoi != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            ketQua(Database.xoaGiaoDich(id, nguoiDungId), "Xóa giao dịch");
        } catch (SQLException e) {
            baoLoi("Lỗi khi xóa giao dịch:\n" + e.getMessage());
        }
    }

    /** Báo kết quả sau khi thêm/sửa/xóa: thành công thì làm mới form và bảng, thất bại thì báo lỗi. */
    private void ketQua(boolean thanhCong, String viecDaLam) {
        if (thanhCong) {
            JOptionPane.showMessageDialog(this, viecDaLam + " thành công!");
            xoaForm();
            napBang(false);
        } else {
            baoLoi(viecDaLam + " thất bại.");
        }
    }

    // =================================================================
    // 5. CÁC HÀM PHỤ
    // =================================================================

    /** Đọc và kiểm tra dữ liệu trên form. Trả về null (và báo lỗi) nếu chưa hợp lệ. */
    private Database.GiaoDich layDuLieuTuForm() {
        Database.DanhMuc danhMuc = (Database.DanhMuc) cboDanhMuc.getSelectedItem();
        Database.Vi vi = (Database.Vi) cboVi.getSelectedItem();
        if (danhMuc == null || vi == null) {
            baoLoi("Hãy chọn danh mục và ví.");
            return null;
        }

        double soTien = 0;
        try {
            soTien = Double.parseDouble(txtSoTien.getText().trim());
        } catch (NumberFormatException e) {
            baoLoi("Số tiền phải là số, ví dụ: 50000 (không nhập dấu chấm/phẩy ngăn cách).");
            return null;
        }
        if (!(soTien > 0) || soTien > 9999999999999.0) {
            baoLoi("Số tiền phải lớn hơn 0.");
            return null;
        }

        Date ngay = null;
        try {
            ngay = Date.valueOf(LocalDate.parse(txtNgay.getText().trim())); // đúng dạng yyyy-MM-dd và ngày có thật
        } catch (DateTimeParseException e) {
            baoLoi("Ngày không hợp lệ. Hãy nhập dạng yyyy-MM-dd, ví dụ 2026-09-15.");
            return null;
        }

        String ghiChu = txtGhiChu.getText().trim();
        if (ghiChu.length() > 255) {
            baoLoi("Ghi chú tối đa 255 ký tự.");
            return null;
        }
        return new Database.GiaoDich(0, nguoiDungId, vi.id(), danhMuc.id(), soTien, ngay, ghiChu);
    }

    /** Đưa form về trạng thái trống, ngày mặc định là hôm nay. */
    private void xoaForm() {
        txtSoTien.setText("");
        txtGhiChu.setText("");
        txtNgay.setText(LocalDate.now().toString());
        bang.clearSelection();
    }

    private void baoLoi(String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }
}
