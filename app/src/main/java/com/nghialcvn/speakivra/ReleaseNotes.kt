package com.nghialcvn.speakivra

/** Installed-version notes are bundled with that APK, independent of a newer online release. */
internal object InstalledReleaseNotes {
    private val notes = mapOf(
        "1.2.4" to listOf(
            "layers" to "Diện mạo Sáng–Tối mới theo tông navy–tím–cyan",
            "book" to "Logo Speakivra mới: chữ S, mũ tốt nghiệp và sóng âm",
            "profile" to "Trang chủ, điểm danh và luyện nói đồng bộ theo mẫu đã duyệt"),
        "1.2.3" to listOf(
            "speaker" to "Sửa câu giới thiệu tên đạt 75% nhưng vẫn bị chặn",
            "profile" to "Gợi ý tên có dấu và không dấu; ưu tiên phương án nhận dạng có đúng tên hồ sơ",
            "check" to "Giữ xanh–đỏ theo chữ nhận dạng và thống nhất điều kiện chuyển câu"),
        "1.2.2" to listOf(
            "bell" to "Điểm danh mỗi ngày, theo dõi chuỗi ngày riêng từng hồ sơ",
            "profile" to "Khung chào và điểm danh theo demo, avatar nổi trên logo chìm",
            "cloud-check" to "Đồng bộ ngày điểm danh khi hồ sơ đã liên kết tài khoản",
            "tools" to "Sửa Tiếp tục bài học và giữ trạng thái liên kết trên một hàng"),
        "1.2.1" to listOf(
            "layers" to "Tiến độ thu gọn từng mục; Khách sạn có nhãn Bài học riêng",
            "book" to "B2, C1 và C2 • Chuyên nghiệp: 20 chủ đề, 700 câu và 300 lượt hội thoại mỗi cấp",
            "lock" to "Hoàn thành B1 mở B2, hoàn thành B2 mở C1, hoàn thành C1 mở C2",
            "bell" to "Nhắc học khoảng 20:00 hằng ngày"),
        "1.2.0" to listOf(
            "cloud-check" to "Nhắc liên kết khi bắt đầu học với hồ sơ chưa liên kết",
            "profile" to "Chọn Liên kết ngay hoặc Tiếp tục học; hồ sơ đã liên kết vào học ngay"),
        "1.1.9" to listOf(
            "profile" to "Sửa luồng đăng nhập Google và thoát chờ khi kết nối quá lâu",
            "layers" to "Đồng bộ hồ sơ chạy riêng, không chặn đăng nhập",
            "info" to "Rút gọn mã hiển thị còn SPK và ba nhóm ký tự; giữ nguyên mã gốc và dữ liệu học"),
        "1.1.8" to listOf(
            "profile" to "Hồ sơ khách có mã riêng, liên kết một lần với tài khoản",
            "layers" to "Đồng bộ hồ sơ, tiến độ và kim cương trên nhiều thiết bị",
            "tools" to "Đăng nhập Google/email, đăng ký và xác minh email",
            "info" to "Microsoft tạm thời chưa khả dụng; bản ghi giọng nói chỉ lưu trên máy"),
        "1.1.3" to listOf("layers" to "Thành tích kim cương, căn giữa và hiển thị trên một hàng"),
        "1.1.4" to listOf("speaker" to "Sửa đối chiếu giờ đọc thành số", "chat" to "Xanh–đỏ theo từng từ trong lời nói và đáp án"),
        "1.1.5" to listOf("reception" to "Tiếng Anh Khách sạn: 15 chủ đề mỗi nghiệp vụ", "chat" to "240 câu mẫu, 180 lượt hội thoại và 240 câu Việt → Anh"),
        "1.1.6" to listOf("speaker" to "Gộp nghiệp vụ trong khung Nghe & nhắc lại", "reception" to "Tab Lễ tân / Buồng phòng trong Học theo tình huống", "chat" to "Chọn chế độ để mở ngay chủ đề tương ứng"),
        "1.1.7" to listOf(
            "book" to "A2/B1: mở rộng bài đọc và Việt → Anh",
            "layers" to "FO/HK: 20 chủ đề, 500 câu mỗi nhánh",
            "speaker" to "Việt → Anh khách sạn: 20 bài, 500 câu mỗi nhánh",
            "tools" to "Sửa đối chiếu tên riêng có dấu",
            "info" to "Nội dung cập nhật đúng từng phiên bản")
    )
    fun forVersion(version: String): List<Pair<String, String>> = notes[version].orEmpty()
}
