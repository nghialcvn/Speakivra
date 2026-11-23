# Ký phát hành và cập nhật Speakivra

## Trạng thái chuyển tiếp

V0.1.2 là APK debug, applicationId `com.nghialcvn.speakivra`, versionCode 3.
Chứng thư SHA-256 của APK đã kiểm tra:

`b53550efd24a5457adf150465144b45104a979ae3465475e45030d9ba2c7d9fe`

Không có khóa riêng tương ứng trong phiên làm việc này. Không thể tái tạo khóa riêng từ APK hoặc chứng thư. Chuyển sang một khóa mới không bảo đảm cài đè V0.1.2. Không gỡ ứng dụng hoặc xóa dữ liệu để thử cập nhật. Nếu còn khóa debug gốc, cần so khớp chứng thư trước khi sử dụng. Không thay khóa của một bản release đã phát hành.

## Thiết lập một lần trên Windows

Cần JDK 17+ và GitHub CLI, đã `gh auth login` bằng tài khoản có quyền ghi repository/secrets.
Chạy từ thư mục mã nguồn:

```powershell
powershell -File .\scripts\setup-signing.ps1
```

Script kiểm tra cấu hình Secrets hiện có trước; nếu đã có cấu hình nhưng thiếu khóa sao lưu tại máy, script dừng để tránh tạo nhầm khóa mới. Script hỏi mật khẩu bằng ô nhập ẩn; tạo một khóa RSA cố định ngoài repository, mặc định tại `%USERPROFILE%\Speakivra-Signing`. Nếu đã có khóa tại đó, script sử dụng lại và không ghi đè. Script lưu 5 GitHub Actions Secrets qua stdin, không in nội dung khóa hay mật khẩu:

- `SPEAKIVRA_KEYSTORE_BASE64`
- `SPEAKIVRA_STORE_PASSWORD`
- `SPEAKIVRA_KEY_ALIAS`
- `SPEAKIVRA_KEY_PASSWORD`
- `SPEAKIVRA_SIGNING_SHA256`

Sao lưu khóa và mật khẩu vào nơi riêng được mã hóa. SHA-256 chứng thư không phải mật khẩu, dùng để phát hiện ký nhầm khóa. Khi dùng khóa có sẵn ở vị trí khác, tự cấu hình 5 secret tương ứng; không chạy tạo khóa mới thay thế khóa cũ.

## Build và phát hành

1. Mỗi commit thay mã trên main chạy đúng một workflow kiểm tra debug: unit test, lint, build, xác minh APK.
2. Sau khi cấu hình secrets, mở Actions → Android APK → Run workflow, bật `release`. Chỉ chạy sau khi cần một APK phát hành. Không push thêm tag để kích hoạt lượt build thứ hai.
3. Release build kiểm tra secrets và chứng thư của keystore trước khi tải SDK, dùng cache Gradle, ký bằng khóa cố định, so khớp chứng thư với fingerprint đã ghim và kiểm tra icon/quyền.
4. Workflow tạo release nháp, tải lên APK và `speakivra-update.json`, rồi mới công bố beta. Không thay thế APK của cùng một phiên bản.
5. Người dùng tải trực tiếp tệp `.apk` trong Releases; artifact Actions vẫn là ZIP phục vụ kiểm tra.

`versionCode` phải tăng ở mỗi bản mới. Giữ applicationId, khóa ký và tên SharedPreferences `speakivra_progress`, các khóa `phrase`/`completed`. Không có mã xóa hoặc reset dữ liệu trong luồng cập nhật.

## Luồng cập nhật

App kiểm tra danh sách release công khai khi mở, bao gồm prerelease beta. Có thể kiểm tra lại trong mục Cập nhật. Mất mạng không chặn học. Hộp thoại bản mới hiện ở Trang chủ để tránh ngắt lượt luyện nói.

Metadata phải thuộc asset của chính repository và tag đó. App kiểm tra applicationId, versionCode và SHA-256 chứng thư theo metadata trước khi hiện nút cài đè. Đây là kiểm tra thông tin do nhà phát hành cung cấp, không phải xác minh mật mã tệp APK từ xa. Android xác minh chữ ký APK thực tế khi cài.

Nút cập nhật mở trực tiếp URL APK trong trình duyệt. Sau khi tải, người dùng mở tệp và xác nhận cài đặt với Android. App không dùng `REQUEST_INSTALL_PACKAGES`, không cài ngầm. Không cần tìm hoặc giải nén ZIP trên GitHub.

## Kiểm tra trên Samsung S24 Ultra

- Chụp icon launcher sau khi cài; xác nhận đúng logo gốc.
- Kiểm tra quyền và kết quả Google Play Protect trên APK thực tế. Không thể bảo đảm mọi bản đều được chấp nhận từ kiểm tra mã nguồn.
- Với hai bản release liên tiếp cùng khóa: học đến một câu, ghi số lượt hoàn thành, cập nhật bằng nút trong app; kiểm tra cả hai giá trị vẫn còn.
- Thử mất mạng, kiểm tra thủ công, Để sau và quay lại sau khi tải APK.
- Với V0.1.2 khác khóa: xác nhận app báo không thể cài đè; không gỡ app. Chưa có luồng di chuyển dữ liệu sang khóa khác.
