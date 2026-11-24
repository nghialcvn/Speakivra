# Speakivra curriculum: A1 and A2 (V0.1.18)

## A1: 15 chủ đề, 300 câu nói, 100 lượt hội thoại

- Mười chủ đề A1 gốc: Chào hỏi, Gia đình, Sinh hoạt, Đồ ăn, Mua sắm, Hỏi đường, Sở thích, Giờ & ngày tháng, Du lịch, Giao tiếp thường ngày. Mỗi bài có **20 câu nói, 7 lượt hội thoại**, tổng 200 câu và 70 lượt.
- Năm chủ đề A1 mới: Sức khỏe, Thời tiết, Phương tiện, Công việc & trường học, Công nghệ. Mỗi bài có **20 câu nói, 6 lượt hội thoại**, tổng 100 câu và 30 lượt.
- Tổng A1: **300 câu nói và 100 lượt hội thoại trên 15 chủ đề**. Mỗi câu có bản dịch, mẹo nói; mỗi lượt hội thoại có câu hỏi, bản dịch và câu trả lời mẫu.

## A2: Gói mở đầu, không phải toàn bộ A2

- Trải nghiệm; Lập kế hoạch; Nêu ý kiến; Giải quyết vấn đề; Du lịch & dịch vụ.
- Mỗi bài có 12 câu nói, 5 lượt hội thoại, 3 câu ôn tập. Tổng **5 chủ đề, 60 câu nói và 25 lượt hội thoại**.
- Các mẫu A2 luyện quá khứ, hiện tại hoàn thành ở mức cơ bản, kế hoạch, so sánh và lời đề nghị lịch sự. Hội thoại tiếp tục là **kịch bản cố định**, không phải AI ứng biến.

## Bảo toàn dữ liệu

Runtime là `Lessons.kt`, cùng `ExpandedLessons.kt`, `MoreA1Practice.kt`, `A1NewTopics.kt`, `A2Lessons.kt`.
- ID và thứ tự của **10 bài A1 đầu tiên** không đổi.
- **12 câu và 5 lượt hội thoại cũ của từng bài A1** giữ nguyên thứ tự, các câu mới chỉ nối thêm phía sau.
- ID A1 mới (`health`, `weather`, `transport`, `workplace`, `technology`) và ID A2 tiền tố `a2_` không trùng ID cũ.
- Tiến độ riêng từng người học và từng chủ đề vẫn ở `lesson.<stable-id>.*`; `active_lesson` được giữ nguyên. Học lại mới đặt lại lượt hiện tại, không xóa số lần hoàn thành.

## Âm thanh và kiểm tra

110 clip neural Anh-Mỹ có sẵn không bị thay đổi. Nội dung mới chưa có clip neural đã duyệt nên được đọc qua TTS được cài trên máy, theo biến thể US/UK và lựa chọn giọng hiện có. Nếu thiếu giọng ngoại tuyến, ứng dụng thông báo rõ.
Câu ôn tập kiểm tra nghĩa; nhận dạng giọng nói chỉ đối chiếu từ và **không phải chấm phát âm bằng âm học**. Tiến độ/độ tương thích được kiểm tra bằng unit test, Compose UI trên emulator và cuối cùng cần xác minh trên Samsung khi phát hành.
