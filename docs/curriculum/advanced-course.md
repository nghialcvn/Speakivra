# B2, C1 và C2 theo phương án đã duyệt

Mỗi cấp có 20 chủ đề, 700 đơn vị luyện nói và 300 lượt hội thoại. Luyện thêm gồm 10 bài đọc/100 đơn vị và 20 bài Việt → Anh/100 câu. Các đáp án nhiều câu được giữ trong một lượt luyện, giúp học cách triển khai một ý hoàn chỉnh.

Ngân hàng song ngữ gồm 100 trường hợp cụ thể, chia thành 20 chủ đề. Mỗi trường hợp có vấn đề, nguyên nhân và hướng xử lý được biên soạn sẵn. Các cấp quay lại tình huống với chức năng giao tiếp khác nhau để luyện tiến bộ theo mức, không gọi dịch vụ AI tạo đáp án.

B2 luyện đề xuất, nêu cơ sở, hỏi làm rõ và thương lượng. C1 triển khai câu trả lời nhiều phần: vấn đề, bằng chứng, giới hạn, lợi ích liên quan và bước tiếp theo. C2 luyện hàm ý, diễn đạt thận trọng, thành ngữ trong ngữ cảnh, phản biện lịch sự, nhượng bộ có điều kiện và xác nhận chính xác mức cam kết. Không dùng hệ số độ dài cho C2.

Các ID có tiền tố cấp độ, chủ đề và số thứ tự, không phụ thuộc tên người học. Nội dung A1/A2/B1 và khách sạn được giữ nguyên. Các tài khoản và tiến độ cũ không bị đặt lại.

Điều kiện mở khóa xét toàn bộ 20 chủ đề của cấp trước có ít nhất một lượt hoàn thành, giữ việc đã hoàn thành khi học lại. B2/C1/C2 chưa mở sẽ bị chặn ở danh mục, từng chủ đề, luyện thêm và đường khôi phục bài. Trong một cấp đã mở, giữ quy tắc ba chủ đề đầu mở sẵn rồi học tiếp theo thứ tự hiện có. Khách sạn độc lập.

Phiên âm được sinh ngoại tuyến bằng phiên bản cố định của phonemizer/eSpeak và đóng gói trong `advanced-ipa.bin`. Không thay các từ điển IPA cũ. Chạy `AdvancedCurriculumTest` để xuất `app/build/reports/tests/advanced-content.tsv`, sau đó dùng `scripts/generate_advanced_ipa.py` để tái tạo từ điển mới. Các lượt có nhiều câu được cấp thời gian thu âm theo số từ; giới hạn tối đa 15 phút, vẫn dừng khi nói xong, bấm dừng, đổi câu hoặc rời màn hình.
