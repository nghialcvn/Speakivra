package com.nghialcvn.speakivra

/** Append-only extra A1 lines: old phrase/turn indices remain stable for saved progress. */
internal object MoreA1Practice {
    fun append(base: Lesson): Lesson = when (base.id) {
        "greetings" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I am twenty-five years old.", "Tôi hai mươi lăm tuổi.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("I live near the city center.", "Tôi sống gần trung tâm thành phố.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều."),
                Phrase("I work in the morning.", "Tôi làm việc vào buổi sáng.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng."),
                Phrase("How old are you?", "Bạn bao nhiêu tuổi?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên."),
                Phrase("What is your favorite color?", "Màu bạn yêu thích là gì?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính."),
                Phrase("My favorite color is blue.", "Màu tôi yêu thích là màu xanh dương.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("Welcome to our class!", "Chào mừng bạn đến với lớp học của chúng tôi!", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("Please call me by my first name.", "Cứ gọi tôi bằng tên riêng nhé.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("Where do you live now?", "Bây giờ bạn sống ở đâu?", "I live near the city center.", "Tôi sống gần trung tâm thành phố."),
                ConversationTurn("What is your favorite color?", "Màu bạn thích nhất là gì?", "My favorite color is blue.", "Màu tôi yêu thích là màu xanh dương.")
            )
        )
        "family" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("My sister is younger than me.", "Em gái tôi nhỏ tuổi hơn tôi.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("My brother goes to university.", "Anh trai tôi học đại học.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều."),
                Phrase("We have dinner together on Sundays.", "Chúng tôi ăn tối cùng nhau vào Chủ nhật.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng."),
                Phrase("My parents are very busy.", "Bố mẹ tôi rất bận.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên."),
                Phrase("My aunt lives in Da Nang.", "Cô của tôi sống ở Đà Nẵng.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm."),
                Phrase("I call my grandparents every week.", "Tôi gọi điện cho ông bà hằng tuần.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("Our family has a small dog.", "Gia đình tôi có một chú chó nhỏ.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("Who is the oldest in your family?", "Ai lớn tuổi nhất trong gia đình bạn?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What do you do with your family on Sundays?", "Bạn làm gì với gia đình vào Chủ nhật?", "We have dinner together on Sundays.", "Chúng tôi ăn tối cùng nhau vào Chủ nhật."),
                ConversationTurn("How often do you call your grandparents?", "Bạn gọi cho ông bà thường xuyên thế nào?", "I call my grandparents every week.", "Tôi gọi điện cho ông bà hằng tuần.")
            )
        )
        "routine" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I make my bed every morning.", "Tôi dọn giường mỗi sáng.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("I drink a glass of water after waking up.", "Tôi uống một cốc nước sau khi thức dậy.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều."),
                Phrase("I leave home at half past seven.", "Tôi rời nhà lúc bảy giờ rưỡi.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng."),
                Phrase("I walk to the bus stop.", "Tôi đi bộ tới trạm xe buýt.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên."),
                Phrase("I check my messages after lunch.", "Tôi kiểm tra tin nhắn sau bữa trưa.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm."),
                Phrase("I cook dinner at six.", "Tôi nấu bữa tối lúc sáu giờ.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("I read for twenty minutes before bed.", "Tôi đọc sách hai mươi phút trước khi ngủ.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("I don't watch television every day.", "Tôi không xem tivi hằng ngày.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What do you do first in the morning?", "Bạn làm gì đầu tiên vào buổi sáng?", "I make my bed every morning.", "Tôi dọn giường mỗi sáng."),
                ConversationTurn("What do you do before going to bed?", "Bạn làm gì trước khi đi ngủ?", "I read for twenty minutes before bed.", "Tôi đọc sách hai mươi phút trước khi ngủ.")
            )
        )
        "food" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I would like some orange juice.", "Tôi muốn một ít nước cam.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("Could I order fried rice?", "Tôi có thể gọi cơm rang không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên."),
                Phrase("Does this dish contain milk?", "Món này có chứa sữa không?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính."),
                Phrase("I prefer fish to chicken.", "Tôi thích cá hơn thịt gà.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên."),
                Phrase("This restaurant is very clean.", "Nhà hàng này rất sạch sẽ.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm."),
                Phrase("Please bring two spoons.", "Làm ơn mang cho tôi hai chiếc thìa.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("The soup is too hot.", "Món súp nóng quá.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("Everything tastes wonderful.", "Mọi món đều ngon tuyệt.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What would you like to order?", "Bạn muốn gọi món gì?", "Could I order fried rice?", "Tôi có thể gọi cơm rang không?"),
                ConversationTurn("How does everything taste?", "Mọi món có ngon không?", "Everything tastes wonderful.", "Mọi món đều ngon tuyệt.")
            )
        )
        "shopping" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("Where is the fitting room?", "Phòng thử đồ ở đâu?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính."),
                Phrase("These shoes are too small.", "Đôi giày này quá nhỏ.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều."),
                Phrase("Do you have a larger size?", "Bạn có cỡ lớn hơn không?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính."),
                Phrase("I need a gift for my friend.", "Tôi cần món quà cho bạn mình.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên."),
                Phrase("The red bag looks nice.", "Chiếc túi đỏ trông đẹp đấy.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm."),
                Phrase("Is the shop open on Sunday?", "Cửa hàng có mở cửa Chủ nhật không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên."),
                Phrase("I want to compare the prices.", "Tôi muốn so sánh giá.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("Can you wrap this as a gift?", "Bạn gói món này thành quà giúp tôi được không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What are you looking for today?", "Hôm nay bạn đang tìm gì?", "I need a gift for my friend.", "Tôi cần món quà cho bạn mình."),
                ConversationTurn("Would you like this wrapped?", "Bạn muốn gói món này không?", "Can you wrap this as a gift?", "Bạn gói món này thành quà giúp tôi được không?")
            )
        )
        "directions" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("The museum is behind the library.", "Bảo tàng nằm phía sau thư viện.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("Walk past the hospital.", "Đi qua bệnh viện.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều."),
                Phrase("Cross the street at the crossing.", "Sang đường tại vạch qua đường.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng."),
                Phrase("Is the bus station on this road?", "Bến xe có nằm trên đường này không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên."),
                Phrase("You can take bus number five.", "Bạn có thể đi xe buýt số năm.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm."),
                Phrase("The entrance is on your left.", "Lối vào nằm bên trái bạn.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("We are close to the river.", "Chúng ta đang ở gần con sông.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("Please tell me when to get off.", "Làm ơn báo tôi khi nào xuống xe.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("Where is the museum?", "Bảo tàng ở đâu?", "The museum is behind the library.", "Bảo tàng nằm phía sau thư viện."),
                ConversationTurn("Which bus should I take?", "Tôi nên đi xe buýt nào?", "You can take bus number five.", "Bạn có thể đi xe buýt số năm.")
            )
        )
        "hobbies" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I practice the guitar after school.", "Tôi tập đàn ghi-ta sau giờ học.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("She enjoys dancing.", "Cô ấy thích khiêu vũ.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều."),
                Phrase("We often go hiking together.", "Chúng tôi thường đi bộ đường dài cùng nhau.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng."),
                Phrase("I collect postcards from different cities.", "Tôi sưu tầm bưu thiếp từ các thành phố khác nhau.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên."),
                Phrase("Do you play any musical instruments?", "Bạn có chơi nhạc cụ nào không?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính."),
                Phrase("I want to learn how to paint.", "Tôi muốn học cách vẽ tranh.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("Chess is my favorite game.", "Cờ vua là trò chơi yêu thích của tôi.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("Let's go swimming this weekend.", "Cuối tuần này mình đi bơi nhé.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What do you do after school?", "Bạn làm gì sau giờ học?", "I practice the guitar after school.", "Tôi tập đàn ghi-ta sau giờ học."),
                ConversationTurn("Do you have a favorite game?", "Bạn có trò chơi yêu thích không?", "Chess is my favorite game.", "Cờ vua là trò chơi yêu thích của tôi.")
            )
        )
        "time" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("The class starts at eight fifteen.", "Lớp học bắt đầu lúc tám giờ mười lăm.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("Today is the first of May.", "Hôm nay là ngày mùng một tháng Năm.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều."),
                Phrase("What date is the concert?", "Buổi hòa nhạc diễn ra ngày nào?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính."),
                Phrase("My appointment is at half past three.", "Lịch hẹn của tôi lúc ba giờ rưỡi.", "Phát âm chậm lần đầu rồi tăng dần đến tốc độ tự nhiên."),
                Phrase("We have two hours left.", "Chúng ta còn hai giờ nữa.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm."),
                Phrase("I'm busy on Wednesday morning.", "Tôi bận vào sáng thứ Tư.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("The shop closes at nine tonight.", "Cửa hàng đóng cửa lúc chín giờ tối nay.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("Can you remind me tomorrow?", "Bạn có thể nhắc tôi vào ngày mai không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("What time does the class start?", "Lớp học bắt đầu lúc mấy giờ?", "The class starts at eight fifteen.", "Lớp học bắt đầu lúc tám giờ mười lăm."),
                ConversationTurn("When is your appointment?", "Lịch hẹn của bạn khi nào?", "My appointment is at half past three.", "Lịch hẹn của tôi lúc ba giờ rưỡi.")
            )
        )
        "travel" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("I need a window seat, please.", "Tôi cần ghế gần cửa sổ.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("Where can I collect my luggage?", "Tôi có thể lấy hành lý ở đâu?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên."),
                Phrase("My train arrives at noon.", "Tàu của tôi đến lúc mười hai giờ.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng."),
                Phrase("Is breakfast included in the price?", "Giá đã bao gồm bữa sáng chưa?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên."),
                Phrase("I would like to extend my stay.", "Tôi muốn gia hạn thời gian lưu trú.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm."),
                Phrase("The hotel is near the beach.", "Khách sạn ở gần bãi biển.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("Can you recommend a place to visit?", "Bạn có thể gợi ý địa điểm tham quan không?", "Hỏi rõ ràng, lên giọng nhẹ ở cuối câu; nhấn các từ chính."),
                Phrase("I lost my room key.", "Tôi làm mất chìa khóa phòng.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("Where would you like to sit?", "Bạn muốn ngồi ở đâu?", "I need a window seat, please.", "Tôi cần ghế gần cửa sổ."),
                ConversationTurn("What happened to your room key?", "Chìa khóa phòng của bạn có chuyện gì?", "I lost my room key.", "Tôi làm mất chìa khóa phòng.")
            )
        )
        "conversation" -> base.copy(
            phrases = base.phrases + listOf(
                Phrase("That's an interesting question.", "Đó là một câu hỏi thú vị.", "Giữ âm cuối rõ ràng, không thêm nguyên âm sau phụ âm."),
                Phrase("I agree with you.", "Tôi đồng ý với bạn.", "Nối âm giữa các từ quen thuộc, giữ nhịp đều."),
                Phrase("I'm not sure about that.", "Tôi không chắc về điều đó.", "Nhấn từ mang nghĩa chính; giảm nhẹ các từ chức năng."),
                Phrase("Could you explain it again?", "Bạn có thể giải thích lại không?", "Ngắt nhịp trước cụm thông tin quan trọng; nói tự nhiên."),
                Phrase("Let me write that down.", "Để tôi ghi điều đó lại.", "Đọc rõ từ chỉ thời gian, số lượng hoặc địa điểm."),
                Phrase("I think you are right.", "Tôi nghĩ bạn đúng.", "Chú ý nguyên âm dài và ngắn; tránh nuốt âm cuối."),
                Phrase("We can talk about it later.", "Chúng ta có thể nói về chuyện đó sau.", "Ngắt câu ở chỗ tự nhiên, tránh đọc từng chữ rời."),
                Phrase("Thanks for your time today.", "Cảm ơn bạn đã dành thời gian hôm nay.", "Giữ ngữ điệu thân thiện và rõ trọng âm trong câu.")
            ),
            conversation = base.conversation + listOf(
                ConversationTurn("Do you agree with my idea?", "Bạn có đồng ý với ý kiến của tôi không?", "I agree with you.", "Tôi đồng ý với bạn."),
                ConversationTurn("Can we discuss this tomorrow?", "Chúng ta có thể trao đổi việc này ngày mai không?", "We can talk about it later.", "Chúng ta có thể nói về chuyện đó sau.")
            )
        )
        else -> base
    }
}
