import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * FORM 3: QUẢN LÝ VÍ (thêm, sửa, xóa, tìm kiếm).
 * Là một JDialog "modal": khi đang mở thì không bấm được form chính;
 * đóng lại thì form chính được nạp lại dữ liệu.
 */
public class ViDialog extends JDialog implements ActionListener {

    private int nguoiDungId;
    private ArrayList<Database.Vi> danhSach = new ArrayList<Database.Vi>(); // các dòng đang hiện trên bảng
    private DecimalFormat dinhDangTien = new DecimalFormat("#,##0");

    private JTextField txtTenVi = new JTextField();
    private JTextField txtSoDu = new JTextField();
    private JButton btnThem = new JButton("Thêm");
    private JButton btnCapNhat = new JButton("Cập nhật");
    private JButton btnXoa = new JButton("Xóa");
    private JButton btnLamMoi = new JButton("Làm mới");
    private JTextField txtTuKhoa = new JTextField(20);
    private JButton btnTim = new JButton("Tìm kiếm");
    private DefaultTableModel bangModel;
    private JTable bang;

    public ViDialog(JFrame cha, int nguoiDungId) {
        super(cha, "Quản lý ví", true); // true = modal
        this.nguoiDungId = nguoiDungId;
        setSize(640, 500);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(cha);
        setLayout(new BorderLayout(10, 10));

        // Form nhập liệu + 4 nút
        JPanel pnlLuoi = new JPanel(new GridLayout(2, 2, 10, 8));
        pnlLuoi.add(new JLabel("Tên ví:"));
        pnlLuoi.add(txtTenVi);
        pnlLuoi.add(new JLabel("Số dư ban đầu (VNĐ):"));
        pnlLuoi.add(txtSoDu);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlNut.add(btnThem);
        pnlNut.add(btnCapNhat);
        pnlNut.add(btnXoa);
        pnlNut.add(btnLamMoi);

        JPanel pnlForm = new JPanel(new BorderLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin ví"));
        pnlForm.add(pnlLuoi, BorderLayout.CENTER);
        pnlForm.add(pnlNut, BorderLayout.SOUTH);

        // Khu vực tìm kiếm
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlTim.setBorder(BorderFactory.createTitledBorder("Tìm kiếm"));
        pnlTim.add(new JLabel("Tên ví:"));
        pnlTim.add(txtTuKhoa);
        pnlTim.add(btnTim);

        JPanel pnlTren = new JPanel(new BorderLayout(5, 5));
        pnlTren.add(pnlForm, BorderLayout.CENTER);
        pnlTren.add(pnlTim, BorderLayout.SOUTH);

        // Bảng
        bangModel = new DefaultTableModel(new String[] {"ID", "Tên ví", "Số dư ban đầu"}, 0);
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
            themVi();
        } else if (nguon == btnCapNhat) {
            capNhatVi();
        } else if (nguon == btnXoa) {
            xoaVi();
        } else if (nguon == btnLamMoi) {
            xoaForm();
            txtTuKhoa.setText("");
            napBang(false);
        } else if (nguon == btnTim) {
            napBang(true);
        }
    }

    /** Lấy danh sách ví (theo ô tìm kiếm) từ database rồi đổ lên bảng. */
    private void napBang(boolean baoNeuRong) {
        try {
            danhSach = Database.layVi(nguoiDungId, txtTuKhoa.getText().trim());
        } catch (SQLException e) {
            baoLoi("Lỗi khi đọc danh sách ví:\n" + e.getMessage());
            return;
        }
        bangModel.setRowCount(0);
        for (int i = 0; i < danhSach.size(); i++) {
            Database.Vi vi = danhSach.get(i);
            Object[] dong = {vi.id(), vi.tenVi(), dinhDangTien.format(vi.soDuBanDau())};
            bangModel.addRow(dong);
        }
        if (baoNeuRong && danhSach.size() == 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy ví nào phù hợp.");
        }
    }

    /** Bấm vào một dòng: đổ dữ liệu của dòng đó lên form. */
    private void chonDong() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            return;
        }
        Database.Vi vi = danhSach.get(dong);
        double soDu = vi.soDuBanDau();
        txtTenVi.setText(vi.tenVi());
        txtSoDu.setText(soDu == Math.floor(soDu) ? String.valueOf((long) soDu) : String.valueOf(soDu)); // bỏ ".0"
    }

    private void themVi() {
        String ten = docTenVi(-1);
        double soDu = docSoDu();
        if (ten == null || soDu < 0) {
            return; // dữ liệu chưa hợp lệ (đã báo lỗi rồi)
        }
        try {
            ketQua(Database.themVi(nguoiDungId, ten, soDu), "Thêm ví");
        } catch (SQLException e) {
            baoLoi("Lỗi khi thêm ví:\n" + e.getMessage());
        }
    }

    private void capNhatVi() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            baoLoi("Hãy chọn một ví trên bảng trước khi cập nhật.");
            return;
        }
        int id = danhSach.get(dong).id();
        String ten = docTenVi(id);
        double soDu = docSoDu();
        if (ten == null || soDu < 0) {
            return;
        }
        try {
            ketQua(Database.capNhatVi(id, nguoiDungId, ten, soDu), "Cập nhật ví");
        } catch (SQLException e) {
            baoLoi("Lỗi khi cập nhật ví:\n" + e.getMessage());
        }
    }

    private void xoaVi() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            baoLoi("Hãy chọn một ví trên bảng trước khi xóa.");
            return;
        }
        Database.Vi vi = danhSach.get(dong);
        try {
            // Không cho xóa ví đang có giao dịch (xóa sẽ vi phạm khóa ngoại)
            int soGiaoDich = Database.demGiaoDichCuaVi(vi.id());
            if (soGiaoDich > 0) {
                baoLoi("Ví \"" + vi.tenVi() + "\" đang có " + soGiaoDich + " giao dịch nên không thể xóa.");
                return;
            }
            int traLoi = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa ví \"" + vi.tenVi() + "\" không?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (traLoi == JOptionPane.YES_OPTION) {
                ketQua(Database.xoaVi(vi.id(), nguoiDungId), "Xóa ví");
            }
        } catch (SQLException e) {
            baoLoi("Lỗi khi xóa ví:\n" + e.getMessage());
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
     * Đọc và kiểm tra tên ví: không trống, tối đa 100 ký tự, không trùng ví khác.
     * idBoQua = id của ví đang sửa (để không so sánh với chính nó), -1 nếu đang thêm mới.
     * Trả về null (và báo lỗi) nếu chưa hợp lệ.
     */
    private String docTenVi(int idBoQua) {
        String ten = txtTenVi.getText().trim();
        if (ten.equals("")) {
            baoLoi("Vui lòng nhập tên ví.");
            return null;
        }
        if (ten.length() > 100) {
            baoLoi("Tên ví tối đa 100 ký tự.");
            return null;
        }
        try {
            ArrayList<Database.Vi> tatCa = Database.layVi(nguoiDungId, "");
            for (int i = 0; i < tatCa.size(); i++) {
                Database.Vi vi = tatCa.get(i);
                if (vi.id() != idBoQua && vi.tenVi().equalsIgnoreCase(ten)) {
                    baoLoi("Đã có ví tên \"" + vi.tenVi() + "\".");
                    return null;
                }
            }
        } catch (SQLException e) {
            baoLoi("Lỗi khi kiểm tra tên ví:\n" + e.getMessage());
            return null;
        }
        return ten;
    }

    /** Đọc số dư ban đầu. Trả về -1 (và báo lỗi) nếu không hợp lệ. */
    private double docSoDu() {
        double soDu = 0;
        try {
            soDu = Double.parseDouble(txtSoDu.getText().trim());
        } catch (NumberFormatException e) {
            baoLoi("Số dư phải là số, ví dụ: 500000 (nhập 0 nếu chưa có tiền).");
            return -1;
        }
        if (!(soDu >= 0) || soDu > 9999999999999.0) {
            baoLoi("Số dư phải từ 0 trở lên.");
            return -1;
        }
        return soDu;
    }

    private void xoaForm() {
        txtTenVi.setText("");
        txtSoDu.setText("");
        bang.clearSelection();
    }

    private void baoLoi(String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }
}
