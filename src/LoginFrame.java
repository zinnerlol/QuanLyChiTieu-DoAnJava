import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * FORM 1: ĐĂNG NHẬP. Đây cũng là nơi chương trình bắt đầu chạy (hàm main).
 * Người dùng chọn một tài khoản trong bảng nguoi_dung rồi bấm "Đăng nhập".
 * (Mật khẩu mẫu trong database chỉ là chuỗi giữ chỗ giống nhau cho mọi người,
 * nên form này cho chọn tài khoản chứ không kiểm tra mật khẩu.)
 */
public class LoginFrame extends JFrame implements ActionListener {

    private JComboBox<Database.NguoiDung> cboNguoiDung = new JComboBox<Database.NguoiDung>();
    private JButton btnDangNhap = new JButton("Đăng nhập");
    private JButton btnThoat = new JButton("Thoát");

    public LoginFrame() {
        setTitle("Đăng nhập - Quản lý chi tiêu");
        setSize(460, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Tiêu đề ở trên, ô chọn tài khoản ở giữa, hai nút ở dưới
        JLabel lblTieuDe = new JLabel("QUẢN LÝ CHI TIÊU CÁ NHÂN", SwingConstants.CENTER);
        lblTieuDe.setFont(lblTieuDe.getFont().deriveFont(Font.BOLD, 20f));
        lblTieuDe.setBorder(BorderFactory.createEmptyBorder(20, 10, 5, 10));
        add(lblTieuDe, BorderLayout.NORTH);

        JPanel pnlGiua = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlGiua.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        pnlGiua.add(new JLabel("Chọn tài khoản:"));
        pnlGiua.add(cboNguoiDung);
        add(pnlGiua, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlNut.add(btnDangNhap);
        pnlNut.add(btnThoat);
        add(pnlNut, BorderLayout.SOUTH);

        // Đổ danh sách người dùng từ database vào ô chọn
        try {
            ArrayList<Database.NguoiDung> ds = Database.layNguoiDung();
            for (int i = 0; i < ds.size(); i++) {
                cboNguoiDung.addItem(ds.get(i));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Không đọc được danh sách tài khoản:\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        btnDangNhap.addActionListener(this);
        btnThoat.addActionListener(this);
        getRootPane().setDefaultButton(btnDangNhap); // nhấn Enter = bấm "Đăng nhập"
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnDangNhap) {
            Database.NguoiDung nd = (Database.NguoiDung) cboNguoiDung.getSelectedItem();
            if (nd == null) {
                JOptionPane.showMessageDialog(this, "Chưa có tài khoản nào để đăng nhập.");
                return;
            }
            new MainFrame(nd).setVisible(true); // mở form chính với tài khoản đã chọn
            dispose();                          // đóng form đăng nhập
        } else if (e.getSource() == btnThoat) {
            System.exit(0);
        }
    }

    // =================================================================
    // ĐIỂM BẮT ĐẦU CỦA CHƯƠNG TRÌNH
    // =================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // giao diện giống hệ điều hành
        } catch (Exception e) {
            // không đổi được thì dùng giao diện mặc định
        }

        // Kiểm tra file database có ở đúng chỗ không
        File fileDb = new File(Database.DUONG_DAN_FILE);
        if (!fileDb.exists()) {
            JOptionPane.showMessageDialog(null,
                    "Không tìm thấy file database:\n" + fileDb.getAbsolutePath()
                    + "\n\nHãy chạy chương trình từ THƯ MỤC GỐC của project,"
                    + "\nhoặc tạo lại database bằng lệnh trong HUONG_DAN.md.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Thử kết nối trước khi mở cửa sổ
        try {
            Connection conn = Database.moKetNoi();
            Database.dongKetNoi(conn, null, null);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Không kết nối được database!\n\n" + e.getMessage()
                    + "\n\nNếu chương trình đang mở ở một cửa sổ khác, hãy đóng nó lại rồi chạy lại.",
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mở form đăng nhập
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
