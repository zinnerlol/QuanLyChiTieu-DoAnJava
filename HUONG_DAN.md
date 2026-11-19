# Quản lý chi tiêu - Java Swing + H2 Database

## 4 form của chương trình

| Form | File | Chức năng |
|---|---|---|
| 1. Đăng nhập | `src/LoginFrame.java` | Chọn tài khoản trong bảng `nguoi_dung` rồi đăng nhập. Chương trình bắt đầu chạy từ đây (hàm `main`). |
| 2. Giao dịch (form chính) | `src/MainFrame.java` | Thêm, sửa, xóa, tìm kiếm, lọc giao dịch (JTable). Có nút mở form 3, form 4 và nút Đăng xuất. |
| 3. Quản lý ví | `src/ViDialog.java` | Thêm, sửa, xóa, tìm kiếm ví. Không cho xóa ví đang có giao dịch. |
| 4. Quản lý danh mục | `src/DanhMucDialog.java` | Thêm, sửa tên, xóa, tìm kiếm danh mục (Thu / Chi). Không cho xóa danh mục còn giao dịch hoặc ngân sách. |

Và 1 file dữ liệu dùng chung cho cả 4 form: `src/Database.java` (kết nối database + các lớp dữ liệu + toàn bộ câu SQL).

Luồng: Đăng nhập -> Giao dịch -> (Quản lý ví | Quản lý danh mục) -> Đăng xuất -> Đăng nhập.
Mỗi người chỉ thấy và sửa được dữ liệu của tài khoản mình đã đăng nhập.

## Database

Database là MỘT FILE (`data/quanlychitieu.mv.db`) đi kèm project, driver (`lib/h2-2.2.220.jar`)
cũng đi kèm. Máy nào có **Java 17 trở lên** thì chép cả thư mục project sang là chạy được ngay,
không cần cài SQL Server, không cần cấu hình gì.

## Cách chạy

Luôn chạy từ THƯ MỤC GỐC của project (thư mục chứa `src`, `lib`, `data`).

**Cách 1 - gõ lệnh (Windows / macOS / Linux), không phụ thuộc file .bat:**

    javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d bin src/LoginFrame.java

    Windows:        java -cp "bin;lib/*" LoginFrame
    macOS / Linux:  java -cp "bin:lib/*" LoginFrame

**Cách 2 - Windows:** bấm đúp `chay.bat` (nếu Windows chặn file này bằng Smart App Control thì dùng Cách 1).

**Cách 3 - VS Code:** mở thư mục project, mở `src/LoginFrame.java` rồi bấm Run phía trên hàm `main`.

## Dữ liệu mẫu và khôi phục

- Mang sang máy khác: chép CẢ thư mục project, dữ liệu bạn đã thêm sẽ đi theo (nằm trong file `.mv.db`).
- Quay về dữ liệu mẫu ban đầu (XÓA hết dữ liệu hiện có), đóng app rồi gõ:

      Windows:     del data\quanlychitieu.mv.db
      macOS/Linux: rm data/quanlychitieu.mv.db

      java -Dfile.encoding=UTF-8 -cp "lib/h2-2.2.220.jar" org.h2.tools.RunScript -url "jdbc:h2:file:./data/quanlychitieu" -user sa -script sql/data_h2.sql

  (Trên Windows cũng có thể bấm đúp `khoi_phuc_du_lieu_mau.bat`.)
- Chỉ mở được MỘT chương trình tại một thời điểm với file database này. Đóng app trước khi mở bằng công cụ khác.
- `sql/data_h2.sql` là script tạo database (cú pháp H2). `sql/data.sql` là script gốc của SQL Server, để tham khảo.

## Xem dữ liệu bằng giao diện web của H2 (tùy chọn)

Đóng app, rồi chạy:

    java -cp lib/h2-2.2.220.jar org.h2.tools.Console

Trình duyệt sẽ mở ra. Điền: JDBC URL `jdbc:h2:file:./data/quanlychitieu`, User `sa`, mật khẩu để trống.

## Lỗi thường gặp

| Thông báo | Cách sửa |
|---|---|
| Không tìm thấy file database | Chạy từ thư mục gốc của project. Nếu mất file, tạo lại bằng lệnh ở mục "Dữ liệu mẫu". |
| Database may be already in use | Chương trình đang mở ở cửa sổ khác (hoặc H2 Console đang mở). Đóng nó rồi chạy lại. |
| javac không được nhận diện | Chưa cài JDK 17 trở lên. |
| No suitable driver | Thiếu file `lib/h2-2.2.220.jar`, hoặc thiếu `-cp "lib/*"` khi chạy. |
| Smart App Control blocked a file | Dùng Cách 1 (gõ lệnh) thay vì bấm đúp file .bat. |
