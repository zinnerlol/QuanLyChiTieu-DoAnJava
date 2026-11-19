import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * FORM 4: QUẢN LÝ DANH MỤC (thêm, sửa tên, xóa, tìm kiếm; phân loại Thu / Chi).
 * Là một JDialog "modal": đóng lại thì form chính được nạp lại dữ liệu.
 */
public class DanhMucDialog extends JDialog implements ActionListener {

    private int nguoiDungId;
    private ArrayList<Database.DanhMuc> danhSach = new ArrayList<Database.DanhMuc>(); // các dòng đang hiện trên bảng

    private JTextField txtTen = new JTextField();
    private JComboBox<String> cboLoai = new JComboBox<String>(new String[] {"Chi", "Thu"});
    private JButton btnThem = new JButton("Thêm");
    private JButton btnCapNhat = new JButton("Cập nhật");
    private JButton btnXoa = new JButton("Xóa");
    private JButton btnLamMoi = new JButton("Làm mới");
    private JTextField txtTuKhoa = new JTextField(20);
    private JButton btnTim = new JButton("Tìm kiếm");
    private DefaultTableModel bangModel;
    private JTable bang;

    public DanhMucDialog(JFrame cha, int nguoiDungId) {
        super(cha, "Quản lý danh mục", true); // true = modal
        this.nguoiDungId = nguoiDungId;
        setSize(640, 520);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(cha);
        setLayout(new BorderLayout(10, 10));

        // Form nhập liệu + 4 nút
        JPanel pnlLuoi = new JPanel(new GridLayout(2, 2, 10, 8));
        pnlLuoi.add(new JLabel("Tên danh mục:"));
        pnlLuoi.add(txtTen);
        pnlLuoi.add(new JLabel("Loại:"));
        pnlLuoi.add(cboLoai);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlNut.add(btnThem);
        pnlNut.add(btnCapNhat);
        pnlNut.add(btnXoa);
        pnlNut.add(btnLamMoi);

        JPanel pnlForm = new JPanel(new BorderLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin danh mục (khi sửa chỉ đổi được tên)"));
        pnlForm.add(pnlLuoi, BorderLayout.CENTER);
        pnlForm.add(pnlNut, BorderLayout.SOUTH);

        // Khu vực tìm kiếm
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlTim.setBorder(BorderFactory.createTitledBorder("Tìm kiếm"));
        pnlTim.add(new JLabel("Tên danh mục:"));
        pnlTim.add(txtTuKhoa);
        pnlTim.add(btnTim);

        JPanel pnlTren = new JPanel(new BorderLayout(5, 5));
        pnlTren.add(pnlForm, BorderLayout.CENTER);
        pnlTren.add(pnlTim, BorderLayout.SOUTH);

        // Bảng
        bangModel = new DefaultTableModel(new String[] {"ID", "Tên danh mục", "Loại"}, 0);
        bang = new JTable(bangModel);
        bang.setDefaultEditor(Object.class, null); // không cho gõ trực tiếp vào ô của bảng
        bang.setRowHeight(24);

        JPanel pnlGiua = new JPanel(new BorderLayout(5, 5));
        pnlGiua.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlGiua.add(pnlTren, BorderLayout.NORTH);
        pnlGiua.add(new JScrollPane(bang), BorderLayout.CENTER);
        add(pnlGiua, BorderLayout.CENTER);

        napBang(false);

        // Gắn sự kiện
        btnThem.addActionListener(this);
        btnCapNhat.addActionListener(this);
        btnXoa.addActionListener(this);
        btnLamMoi.addActionListener(this);
        btnTim.addActionListener(this);
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chonDong();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object nguon = e.getSource();
        if (nguon == btnThem) {
            themDanhMuc();
        } else if (nguon == btnCapNhat) {
            capNhatDanhMuc();
        } else if (nguon == btnXoa) {
            xoaDanhMuc();
        } else if (nguon == btnLamMoi) {
            xoaForm();
            txtTuKhoa.setText("");
            napBang(false);
        } else if (nguon == btnTim) {
            napBang(true);
        }
    }

    /** Lấy danh mục (theo ô tìm kiếm) từ database rồi đổ lên bảng. */
    private void napBang(boolean baoNeuRong) {
        try {
            danhSach = Database.layDanhMuc(nguoiDungId, "", txtTuKhoa.getText().trim());
        } catch (SQLException e) {
            baoLoi("Lỗi khi đọc danh sách danh mục:\n" + e.getMessage());
            return;
        }
        bangModel.setRowCount(0);
        for (int i = 0; i < danhSach.size(); i++) {
            Database.DanhMuc dm = danhSach.get(i);
            Object[] dong = {dm.id(), dm.tenDanhMuc(), dm.loai().equals("thu") ? "Thu" : "Chi"};
            bangModel.addRow(dong);
        }
        if (baoNeuRong && danhSach.size() == 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy danh mục nào phù hợp.");
        }
    }

    /** Bấm vào một dòng: đổ lên form, và khóa ô Loại (vì khi sửa chỉ đổi được tên). */
    private void chonDong() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            return;
        }
        Database.DanhMuc dm = danhSach.get(dong);
        txtTen.setText(dm.tenDanhMuc());
        cboLoai.setSelectedItem(dm.loai().equals("thu") ? "Thu" : "Chi");
        cboLoai.setEnabled(false);
    }

    private void themDanhMuc() {
        String loai = ((String) cboLoai.getSelectedItem()).toLowerCase(); // "Thu" -> "thu"
        String ten = docTen(loai, -1);
        if (ten == null) {
            return; // tên chưa hợp lệ (đã báo lỗi rồi)
        }
        try {
            ketQua(Database.themDanhMuc(nguoiDungId, ten, loai), "Thêm danh mục");
        } catch (SQLException e) {
            baoLoi("Lỗi khi thêm danh mục:\n" + e.getMessage());
        }
    }

    private void capNhatDanhMuc() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            baoLoi("Hãy chọn một danh mục trên bảng trước khi cập nhật.");
            return;
        }
        Database.DanhMuc dm = danhSach.get(dong);
        String ten = docTen(dm.loai(), dm.id());
        if (ten == null) {
            return;
        }
        try {
            ketQua(Database.capNhatDanhMuc(dm.id(), nguoiDungId, ten), "Cập nhật danh mục");
        } catch (SQLException e) {
            baoLoi("Lỗi khi cập nhật danh mục:\n" + e.getMessage());
        }
    }

    private void xoaDanhMuc() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            baoLoi("Hãy chọn một danh mục trên bảng trước khi xóa.");
            return;
        }
        Database.DanhMuc dm = danhSach.get(dong);
        try {
            // Không cho xóa nếu còn giao dịch hoặc ngân sách đang dùng danh mục này
            int soGiaoDich = Database.demGiaoDichCuaDanhMuc(dm.id());
            int soNganSach = Database.demNganSachCuaDanhMuc(dm.id());
            if (soGiaoDich > 0 || soNganSach > 0) {
                baoLoi("Danh mục \"" + dm.tenDanhMuc() + "\" đang có " + soGiaoDich
                        + " giao dịch và " + soNganSach + " ngân sách nên không thể xóa.");
                return;
            }
            int traLoi = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa danh mục \"" + dm.tenDanhMuc() + "\" không?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (traLoi == JOptionPane.YES_OPTION) {
                ketQua(Database.xoaDanhMuc(dm.id(), nguoiDungId), "Xóa danh mục");
            }
        } catch (SQLException e) {
            baoLoi("Lỗi khi xóa danh mục:\n" + e.getMessage());
        }
    }

    /** Báo kết quả: thành công thì làm mới form và bảng, thất bại thì báo lỗi. */
    private void ketQua(boolean thanhCong, String viecDaLam) {
        if (thanhCong) {
            JOptionPane.showMessageDialog(this, viecDaLam + " thành công!");
            xoaForm();
            napBang(false);
        } else {
            baoLoi(viecDaLam + " thất bại.");
        }
    }

    /**
     * Đọc và kiểm tra tên danh mục: không trống, tối đa 100 ký tự, không trùng danh mục CÙNG LOẠI khác.
     * idBoQua = id đang sửa, -1 nếu đang thêm mới. Trả về null (và báo lỗi) nếu chưa hợp lệ.
     */
    private String docTen(String loai, int idBoQua) {
        String ten = txtTen.getText().trim();
        if (ten.equals("")) {
            baoLoi("Vui lòng nhập tên danh mục.");
            return null;
        }
        if (ten.length() > 100) {
            baoLoi("Tên danh mục tối đa 100 ký tự.");
            return null;
        }
        try {
            ArrayList<Database.DanhMuc> tatCa = Database.layDanhMuc(nguoiDungId, loai, "");
            for (int i = 0; i < tatCa.size(); i++) {
                Database.DanhMuc dm = tatCa.get(i);
                if (dm.id() != idBoQua && dm.tenDanhMuc().equalsIgnoreCase(ten)) {
                    baoLoi("Đã có danh mục \"" + dm.tenDanhMuc() + "\" cùng loại.");
                    return null;
                }
            }
        } catch (SQLException e) {
            baoLoi("Lỗi khi kiểm tra tên danh mục:\n" + e.getMessage());
            return null;
        }
        return ten;
    }

    private void xoaForm() {
        txtTen.setText("");
        cboLoai.setEnabled(true);
        bang.clearSelection();
    }

    private void baoLoi(String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }
}
