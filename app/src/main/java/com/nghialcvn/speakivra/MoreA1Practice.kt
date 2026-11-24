package com.nghialcvn.speakivra

/** Append-only extra A1 lines: old phrase/turn indices remain stable for saved progress. */
internal object MoreA1Practice {
    fun append(base: Lesson): Lesson = when (base.id) {
        "greetings" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I am twenty-five years old.", "Tôi hai mươi lăm tuổi.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "be2ed6d0a8cfb116de76"),
                Phrase("I live near the city center.", "Tôi sống gần trung tâm thành phố.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều.", id = "52007d6288ec8fdec6a2"),
                Phrase("I work in the morning.", "Tôi làm việc vào buổi sáng.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng.", id = "bbf6d101de023df88386"),
                Phrase("How old are you?", "Bạn bao nhiêu tuổi?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "9b181de483e0da61391c"),
                Phrase("What is your favorite color?", "Màu bạn yêu thích là gì?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính.", id = "dcc535df7b1cd1f8ee25"),
                Phrase("My favorite color is blue.", "Màu tôi yêu thích là màu xanh dương.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "5461e27808aa71006811"),
                Phrase("Welcome to our class!", "Chào mừng bạn đến với lớp học của chúng tôi!", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "dcffe4273deb2cdc800b"),
                Phrase("Please call me by my first name.", "Cứ gọi tôi bằng tên riêng nhé.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.", id = "d0c4eccc07b1b09ce9cb")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("Where do you live now?", "Bây giờ bạn sống ở đâu?", "I live near the city center.", "Tôi sống gần trung tâm thành phố.", id = "057c5f4c32887ecad1a7"),
                ConversationTurn("What is your favorite color?", "Màu bạn thích nhất là gì?", "My favorite color is blue.", "Màu tôi yêu thích là màu xanh dương.", id = "dcc535df7b1cd1f8ee25")
            )
        )
        "family" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("My sister is younger than me.", "Em gái tôi nhỏ tuổi hơn tôi.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "d1ee735df7cb5c168cc8"),
                Phrase("My brother goes to university.", "Anh trai tôi học đại học.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều.", id = "6fbbb289374d67a16a6f"),
                Phrase("We have dinner together on Sundays.", "Chúng tôi ăn tối cùng nhau vào Chủ nhật.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng.", id = "abb0ea5c4467ad79405a"),
                Phrase("My parents are very busy.", "Bố mẹ tôi rất bận.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên.", id = "03eb517c92177f9383f9"),
                Phrase("My aunt lives in Da Nang.", "Cô của tôi sống ở Đà Nẵng.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm.", id = "9e8c6cb27c9d43e524d5"),
                Phrase("I call my grandparents every week.", "Tôi gọi điện cho ông bà hằng tuần.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "dda9775b80ed5ec4ee61"),
                Phrase("Our family has a small dog.", "Gia đình tôi có một chú chó nhỏ.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "c68c1893e7f703d3bfa5"),
                Phrase("Who is the oldest in your family?", "Ai lớn tuổi nhất trong gia đình bạn?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "1654852dea0b773640c0")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What do you do with your family on Sundays?", "Bạn làm gì với gia đình vào Chủ nhật?", "We have dinner together on Sundays.", "Chúng tôi ăn tối cùng nhau vào Chủ nhật.", id = "f7d8515c076d895b8a22"),
                ConversationTurn("How often do you call your grandparents?", "Bạn gọi cho ông bà thường xuyên thế nào?", "I call my grandparents every week.", "Tôi gọi điện cho ông bà hằng tuần.", id = "ac903e58bada7a62e5a0")
            )
        )
        "routine" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I make my bed every morning.", "Tôi dọn giường mỗi sáng.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "658ef1668074604ea6ce"),
                Phrase("I drink a glass of water after waking up.", "Tôi uống một cốc nước sau khi thức dậy.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều.", id = "9d0c73075ca6668975e4"),
                Phrase("I leave home at half past seven.", "Tôi rời nhà lúc bảy giờ rưỡi.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng.", id = "1fbb34dd92581d4a9112"),
                Phrase("I walk to the bus stop.", "Tôi đi bộ tới trạm xe buýt.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên.", id = "eb52b8b48d23f58bffc0"),
                Phrase("I check my messages after lunch.", "Tôi kiểm tra tin nhắn sau bữa trưa.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm.", id = "f37e3eed9a1f0639edb8"),
                Phrase("I cook dinner at six.", "Tôi nấu bữa tối lúc sáu giờ.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "48cb31df0698a3a9f15a"),
                Phrase("I read for twenty minutes before bed.", "Tôi đọc sách hai mươi phút trước khi ngủ.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "d7f2a0c4596e0168af17"),
                Phrase("I don't watch television every day.", "Tôi không xem tivi hằng ngày.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.", id = "f6ae3063f1e25472d921")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What do you do first in the morning?", "Bạn làm gì đầu tiên vào buổi sáng?", "I make my bed every morning.", "Tôi dọn giường mỗi sáng.", id = "32473776cd634b10b0ff"),
                ConversationTurn("What do you do before going to bed?", "Bạn làm gì trước khi đi ngủ?", "I read for twenty minutes before bed.", "Tôi đọc sách hai mươi phút trước khi ngủ.", id = "02fc71cc5287cf8a91a0")
            )
        )
        "food" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I would like some orange juice.", "Tôi muốn một ít nước cam.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "12f3b1b4be838e63cba5"),
                Phrase("Could I order fried rice?", "Tôi có thể gọi cơm rang không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "a949300bf27370b6d7ca"),
                Phrase("Does this dish contain milk?", "Món này có chứa sữa không?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính.", id = "48ef99bf4df4dd7039f2"),
                Phrase("I prefer fish to chicken.", "Tôi thích cá hơn thịt gà.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên.", id = "ab2028b882f384ebcfb2"),
                Phrase("This restaurant is very clean.", "Nhà hàng này rất sạch sẽ.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm.", id = "7b52f571e9806ceb98e8"),
                Phrase("Please bring two spoons.", "Làm ơn mang cho tôi hai chiếc thìa.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "1cb288a7b1b13a75537f"),
                Phrase("The soup is too hot.", "Món súp nóng quá.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "4afa26164b81b40d2c2e"),
                Phrase("Everything tastes wonderful.", "Mọi món đều ngon tuyệt.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.", id = "c3ec100cb85faaba02e3")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What would you like to order?", "Bạn muốn gọi món gì?", "Could I order fried rice?", "Tôi có thể gọi cơm rang không?", id = "292da83fb89438b3b7c8"),
                ConversationTurn("How does everything taste?", "Mọi món có ngon không?", "Everything tastes wonderful.", "Mọi món đều ngon tuyệt.", id = "20a54cbf72e5ab3cc5b5")
            )
        )
        "shopping" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("Where is the fitting room?", "Phòng thử đồ ở đâu?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính.", id = "e42ba30a9a8890d5a5be"),
                Phrase("These shoes are too small.", "Đôi giày này quá nhỏ.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều.", id = "0ab94da2513621b3538a"),
                Phrase("Do you have a larger size?", "Bạn có cỡ lớn hơn không?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính.", id = "a8383f1828e1e57b5e16"),
                Phrase("I need a gift for my friend.", "Tôi cần món quà cho bạn mình.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên.", id = "07eec5aa805549675c6d"),
                Phrase("The red bag looks nice.", "Chiếc túi đỏ trông đẹp đấy.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm.", id = "0534642e29b4a85593d1"),
                Phrase("Is the shop open on Sunday?", "Cửa hàng có mở cửa Chủ nhật không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "4de7665495c1418d51f9"),
                Phrase("I want to compare the prices.", "Tôi muốn so sánh giá.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "8abc8f5ed46b6b0bf097"),
                Phrase("Can you wrap this as a gift?", "Bạn gói món này thành quà giúp tôi được không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "d4b96dd3f2865e5cadd0")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What are you looking for today?", "Hôm nay bạn đang tìm gì?", "I need a gift for my friend.", "Tôi cần món quà cho bạn mình.", id = "f65fdfec55e02617ea97"),
                ConversationTurn("Would you like this wrapped?", "Bạn muốn gói món này không?", "Can you wrap this as a gift?", "Bạn gói món này thành quà giúp tôi được không?", id = "7db1a39eda879c683e3c")
            )
        )
        "directions" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("The museum is behind the library.", "Bảo tàng nằm phía sau thư viện.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "073fd550ed1f325cf75a"),
                Phrase("Walk past the hospital.", "Đi qua bệnh viện.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều.", id = "043aa2e9162816d6b1b6"),
                Phrase("Cross the street at the crossing.", "Sang đường tại vạch qua đường.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng.", id = "b87c6ee6a348d3aa8c6e"),
                Phrase("Is the bus station on this road?", "Bến xe có nằm trên đường này không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "2128a80cec4d784dca06"),
                Phrase("You can take bus number five.", "Bạn có thể đi xe buýt số năm.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm.", id = "ce806d287693b5113eee"),
                Phrase("The entrance is on your left.", "Lối vào nằm bên trái bạn.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "9f0ac5927afa6a39c915"),
                Phrase("We are close to the river.", "Chúng ta đang ở gần con sông.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "5ba6ed1908426fc953d1"),
                Phrase("Please tell me when to get off.", "Làm ơn báo tôi khi nào xuống xe.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.", id = "f5b0ce0545c7f70c7b8d")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("Where is the museum?", "Bảo tàng ở đâu?", "The museum is behind the library.", "Bảo tàng nằm phía sau thư viện.", id = "01b5e8e333d3f3a09ab0"),
                ConversationTurn("Which bus should I take?", "Tôi nên đi xe buýt nào?", "You can take bus number five.", "Bạn có thể đi xe buýt số năm.", id = "12db29a88d3d3a940768")
            )
        )
        "hobbies" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I practice the guitar after school.", "Tôi tập đàn ghi-ta sau giờ học.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "a7be8efc81b1c12d6831"),
                Phrase("She enjoys dancing.", "Cô ấy thích khiêu vũ.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều.", id = "c1bf714d337854641995"),
                Phrase("We often go hiking together.", "Chúng tôi thường đi bộ đường dài cùng nhau.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng.", id = "0e16b968e60788d9f7d5"),
                Phrase("I collect postcards from different cities.", "Tôi sưu tầm bưu thiếp từ các thành phố khác nhau.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên.", id = "2689e28152bf87fa9744"),
                Phrase("Do you play any musical instruments?", "Bạn có chơi nhạc cụ nào không?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính.", id = "716f0c4f154bba6c6f28"),
                Phrase("I want to learn how to paint.", "Tôi muốn học cách vẽ tranh.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "8050d27248bc9bdb32ba"),
                Phrase("Chess is my favorite game.", "Cờ vua là trò chơi yêu thích của tôi.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "7a3fab86c44a55d7c738"),
                Phrase("Let's go swimming this weekend.", "Cuối tuần này mình đi bơi nhé.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.", id = "8a2cb25805678d90356b")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What do you do after school?", "Bạn làm gì sau giờ học?", "I practice the guitar after school.", "Tôi tập đàn ghi-ta sau giờ học.", id = "d3d8765bb76eeee1ae7f"),
                ConversationTurn("Do you have a favorite game?", "Bạn có trò chơi yêu thích không?", "Chess is my favorite game.", "Cờ vua là trò chơi yêu thích của tôi.", id = "e2d5a01ca77ba6733ace")
            )
        )
        "time" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("The class starts at eight fifteen.", "Lớp học bắt đầu lúc tám giờ mười lăm.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "8b1dbb7f7b4e5251b203"),
                Phrase("Today is the first of May.", "Hôm nay là ngày mùng một tháng Năm.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều.", id = "846da73d75736c3c507b"),
                Phrase("What date is the concert?", "Buổi hòa nhạc diễn ra ngày nào?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính.", id = "8bb2299a5897685e8e08"),
                Phrase("My appointment is at half past three.", "Lịch hẹn của tôi lúc ba giờ rưỡi.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên.", id = "df1e031d9f00ea685640"),
                Phrase("We have two hours left.", "Chúng ta còn hai giờ nữa.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm.", id = "47abb645c95b9cfd718b"),
                Phrase("I'm busy on Wednesday morning.", "Tôi bận vào sáng thứ Tư.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "72e36d1d0c38aeacd5f0"),
                Phrase("The shop closes at nine tonight.", "Cửa hàng đóng cửa lúc chín giờ tối nay.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "8d66c1a7c0c4cfd00e1f"),
                Phrase("Can you remind me tomorrow?", "Bạn có thể nhắc tôi vào ngày mai không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "155b8990d645ba23283e")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What time does the class start?", "Lớp học bắt đầu lúc mấy giờ?", "The class starts at eight fifteen.", "Lớp học bắt đầu lúc tám giờ mười lăm.", id = "745f205a7d9fa0579ead"),
                ConversationTurn("When is your appointment?", "Lịch hẹn của bạn khi nào?", "My appointment is at half past three.", "Lịch hẹn của tôi lúc ba giờ rưỡi.", id = "1b7f573fe7f269a35889")
            )
        )
        "travel" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I need a window seat, please.", "Tôi cần ghế gần cửa sổ.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "970805cd38d95eabf365"),
                Phrase("Where can I collect my luggage?", "Tôi có thể lấy hành lý ở đâu?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "d77eca9b0ef97cbf91f6"),
                Phrase("My train arrives at noon.", "Tàu của tôi đến lúc mười hai giờ.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng.", id = "9a3c98781bcda51969d3"),
                Phrase("Is breakfast included in the price?", "Giá đã bao gồm bữa sáng chưa?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "9761b7936e52384a8fbe"),
                Phrase("I would like to extend my stay.", "Tôi muốn gia hạn thời gian lưu trú.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm.", id = "e242580a8b2e95267839"),
                Phrase("The hotel is near the beach.", "Khách sạn ở gần bãi biển.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "7d26051138aaab4dc5e0"),
                Phrase("Can you recommend a place to visit?", "Bạn có thể gợi ý địa điểm tham quan không?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính.", id = "d16b6fe6d80cf968e6a2"),
                Phrase("I lost my room key.", "Tôi làm mất chìa khóa phòng.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.", id = "abfd7d67e1a987798cac")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("Where would you like to sit?", "Bạn muốn ngồi ở đâu?", "I need a window seat, please.", "Tôi cần ghế gần cửa sổ.", id = "ee541faf47df8a6b2803"),
                ConversationTurn("What happened to your room key?", "Chìa khóa phòng của bạn có chuyện gì?", "I lost my room key.", "Tôi làm mất chìa khóa phòng.", id = "fbd494a6974c04a54fee")
            )
        )
        "conversation" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("That's an interesting question.", "Đó là một câu hỏi thú vị.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm.", id = "1cb4c5fc6cd44d8af892"),
                Phrase("I agree with you.", "Tôi đồng ý với bạn.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều.", id = "5afba10f4f125d971e48"),
                Phrase("I'm not sure about that.", "Tôi không chắc về điều đó.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng.", id = "fbf02d06c14c9ea8dc54"),
                Phrase("Could you explain it again?", "Bạn có thể giải thích lại không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.", id = "ad17d7320ae49720211e"),
                Phrase("Let me write that down.", "Để tôi ghi điều đó lại.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm.", id = "5f736a23064788e4a760"),
                Phrase("I think you are right.", "Tôi nghĩ bạn đúng.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối.", id = "3b0654c7eb75927dc75e"),
                Phrase("We can talk about it later.", "Chúng ta có thể nói về chuyện đó sau.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời.", id = "4a4d87dd1dce50c3f6f2"),
                Phrase("Thanks for your time today.", "Cảm ơn bạn đã dành thời gian hôm nay.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.", id = "1132f1281949a679028f")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("Do you agree with my idea?", "Bạn có đồng ý với ý kiến của tôi không?", "I agree with you.", "Tôi đồng ý với bạn.", id = "cd0f4959b171b49f88af"),
                ConversationTurn("Can we discuss this tomorrow?", "Chúng ta có thể trao đổi việc này ngày mai không?", "We can talk about it later.", "Chúng ta có thể nói về chuyện đó sau.", id = "b9e613eebad622c5a981")
            )
        )
        else -> base
    }
}
