# QuanLyOto

## Khởi tạo cơ sở dữ liệu SQL Server

Để tạo database mới và nạp dữ liệu mẫu, chạy lần lượt hai file trong SQL Server Management Studio:

1. `database/QuanLyGaraOto.sql` — tạo database `a1` và toàn bộ bảng.
2. `database/DuLieuMau.sql` — nạp dữ liệu mẫu vào database `a1`.

Chỉ chạy file dữ liệu mẫu sau khi tạo schema thành công. File dữ liệu mẫu không cho phép chạy lặp để tránh trùng dữ liệu; muốn nạp lại, hãy dùng một database mới.

Bộ dữ liệu mẫu mở rộng bao gồm khách hàng và nhiều xe, tài khoản/nhân viên/lịch làm việc, phụ tùng/tồn kho, dịch vụ/khuyến mãi, tiếp nhận/kiểm tra/sửa chữa, hóa đơn và giao dịch thanh toán, bảo hành/đổi trả/bàn giao. Dữ liệu có nhiều trạng thái và mốc thời gian để thử báo cáo, công nợ, tồn kho và các luồng xử lý.

Tài khoản demo: `NV001` đến `NV008` dùng mật khẩu `123456`; `NV009` và `NV010` dùng mật khẩu `654321`.

Nếu database `a1` đã có schema cũ, không chạy lại file tạo database. Chạy `database/Migration_ChiTietHoaDon.sql` một lần để cập nhật cấu trúc chi tiết phiếu sửa chữa và hóa đơn.

Các bảng chính sách bảo hành và phiếu yêu cầu bảo hành chỉ được tạo trong script khởi tạo database mới; nếu đang dùng database cũ, hãy tạo lại database và nạp lại dữ liệu mẫu để sử dụng đầy đủ các chức năng này.

Các ràng buộc `NOT NULL` mới cũng chỉ áp dụng khi khởi tạo schema mới. Hãy tạo lại database `a1`, sau đó chạy lại `QuanLyGaraOto.sql` và `DuLieuMau.sql` theo đúng thứ tự; không chạy script tạo schema trên database đã có bảng.
