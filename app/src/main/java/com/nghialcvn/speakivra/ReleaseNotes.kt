package com.nghialcvn.speakivra

/** Installed-version notes are bundled with that APK, independent of a newer online release. */
internal object InstalledReleaseNotes {
    private val notes = mapOf(
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
