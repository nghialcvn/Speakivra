package com.nghialcvn.speakivra

/** Introductory A2 curriculum: real bilingual phrases and scripted exchanges, no fake AI dialogue. */
internal object A2Lessons {
    val all: List<Lesson> = listOf(
        Lesson("a2_experiences", "Life Experiences", "Trải nghiệm cá nhân", listOf(
            Phrase("I have visited Da Nang twice.", "Tôi đã đến Đà Nẵng hai lần.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "572ed8aa5593999df095"),
            Phrase("I have never tried skiing.", "Tôi chưa từng thử trượt tuyết.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "dd78148589581676b10d"),
            Phrase("Have you ever flown alone?", "Bạn đã từng bay một mình chưa?", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "07669273a04699434cd4"),
            Phrase("I went to Hue last summer.", "Tôi đã đến Huế mùa hè năm ngoái.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "40cf9cd638c32d4dbde9"),
            Phrase("The trip was better than I expected.", "Chuyến đi tốt hơn tôi mong đợi.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "0bbd38e383fb8edbfd3d"),
            Phrase("I learned to cook when I was young.", "Tôi học nấu ăn khi còn nhỏ.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "05f866b05b4ab66a29c5"),
            Phrase("I haven't finished that book yet.", "Tôi vẫn chưa đọc xong cuốn sách đó.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "136382449eede77c4987"),
            Phrase("She has already seen that film.", "Cô ấy đã xem bộ phim đó rồi.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "4b943c71afbaaa605158"),
            Phrase("We stayed there for three days.", "Chúng tôi đã ở đó ba ngày.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "74b7a45b91d781aef945"),
            Phrase("I met a very friendly guide.", "Tôi đã gặp một hướng dẫn viên rất thân thiện.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "8efbd5cbfa194b9e059d"),
            Phrase("What was the best part of your trip?", "Phần tuyệt nhất trong chuyến đi của bạn là gì?", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "84f1f99c78753f69034c"),
            Phrase("I would love to go back someday.", "Tôi rất muốn quay lại vào một ngày nào đó.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "c44853f01f18646cf61e")
        ), listOf(
            ConversationTurn("Have you ever visited Da Nang?", "Bạn đã từng đến Đà Nẵng chưa?", "I have visited Da Nang twice.", "Tôi đã đến Đà Nẵng hai lần.", id = "3461663c259e35f5a826"),
            ConversationTurn("Have you ever tried skiing?", "Bạn đã từng thử trượt tuyết chưa?", "I have never tried skiing.", "Tôi chưa từng thử trượt tuyết.", id = "9940b0192b7797a1c5a2"),
            ConversationTurn("Where did you go last summer?", "Mùa hè năm ngoái bạn đã đi đâu?", "I went to Hue last summer.", "Tôi đã đến Huế mùa hè năm ngoái.", id = "1ac33b52c860903c6a4b"),
            ConversationTurn("Have you finished the book?", "Bạn đã đọc xong sách chưa?", "I haven't finished that book yet.", "Tôi vẫn chưa đọc xong cuốn sách đó.", id = "c53fecbc3e8a7e25e270"),
            ConversationTurn("Would you like to visit again?", "Bạn có muốn đến đó lần nữa không?", "I would love to go back someday.", "Tôi rất muốn quay lại vào một ngày nào đó.", id = "dd24c93374c28a4cf22a")
        )),
        Lesson("a2_plans", "Plans & Arrangements", "Kế hoạch và sắp xếp", listOf(
            Phrase("I'm going to study tonight.", "Tối nay tôi định học bài.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "32d7938b5b9376f71216"),
            Phrase("We are meeting at the station tomorrow.", "Ngày mai chúng tôi sẽ gặp nhau ở nhà ga.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "2f25bd6d1e00e1d3104f"),
            Phrase("I might visit my cousin this weekend.", "Có thể cuối tuần này tôi sẽ thăm anh họ.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "9f29da910acbdbe36ee5"),
            Phrase("Could we move the meeting to Friday?", "Chúng ta có thể chuyển cuộc họp sang thứ Sáu không?", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "141d93debbe06c4626be"),
            Phrase("I need to book the tickets in advance.", "Tôi cần đặt vé trước.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "fcb4a40a54287e5cc790"),
            Phrase("Let me check my schedule first.", "Để tôi kiểm tra lịch trước đã.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "7f8935df31261be15ffa"),
            Phrase("I'm free after four in the afternoon.", "Tôi rảnh sau bốn giờ chiều.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "4b997e62cbaf684841e4"),
            Phrase("We should leave early to avoid traffic.", "Chúng ta nên đi sớm để tránh tắc đường.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "bd5949e3f6070b4b543a"),
            Phrase("I will send you the details later.", "Tôi sẽ gửi bạn thông tin chi tiết sau.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "822a535f099d84a103c6"),
            Phrase("If it rains, we'll stay at home.", "Nếu trời mưa, chúng tôi sẽ ở nhà.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "295c084e178c0fd27fb1"),
            Phrase("What time shall we meet?", "Chúng ta gặp nhau lúc mấy giờ?", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "800652229cab0d0d6180"),
            Phrase("That plan sounds good to me.", "Kế hoạch đó nghe ổn với tôi.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "03d1a84eb1ffd834c5f2")
        ), listOf(
            ConversationTurn("What are you going to do tonight?", "Tối nay bạn định làm gì?", "I'm going to study tonight.", "Tối nay tôi định học bài.", id = "09d074d0d4cf5a5fbeb4"),
            ConversationTurn("Can you meet on Thursday?", "Bạn có thể gặp vào thứ Năm không?", "Could we move the meeting to Friday?", "Chúng ta có thể chuyển cuộc họp sang thứ Sáu không?", id = "8960f656062469e7c6d7"),
            ConversationTurn("When are you available?", "Khi nào bạn rảnh?", "I'm free after four in the afternoon.", "Tôi rảnh sau bốn giờ chiều.", id = "46cc985b7119e62ba4fe"),
            ConversationTurn("What if the weather is bad?", "Nếu thời tiết xấu thì sao?", "If it rains, we'll stay at home.", "Nếu trời mưa, chúng tôi sẽ ở nhà.", id = "215dd9ce59ccfefcab75"),
            ConversationTurn("Do you agree with the plan?", "Bạn có đồng ý với kế hoạch không?", "That plan sounds good to me.", "Kế hoạch đó nghe ổn với tôi.", id = "c7a91c2e3bdffe6254ca")
        )),
        Lesson("a2_opinions", "Opinions & Comparisons", "Ý kiến và so sánh", listOf(
            Phrase("In my opinion, this option is better.", "Theo ý tôi, lựa chọn này tốt hơn.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "dceceb2345f1d06761dc"),
            Phrase("I prefer the quieter restaurant.", "Tôi thích nhà hàng yên tĩnh hơn.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "d84298d52acc62b15db7"),
            Phrase("This phone is cheaper than mine.", "Điện thoại này rẻ hơn của tôi.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "4baba281114d0c333850"),
            Phrase("The new course is more interesting.", "Khóa học mới thú vị hơn.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "8e30d84a8cdbb994c6f1"),
            Phrase("I don't completely agree with that.", "Tôi không hoàn toàn đồng ý với điều đó.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "5a1960369bea158a1912"),
            Phrase("Could you explain your opinion?", "Bạn có thể giải thích ý kiến của mình không?", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "6b03e88787e98be57e9f"),
            Phrase("I think both choices have advantages.", "Tôi nghĩ cả hai lựa chọn đều có ưu điểm.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "6096013cab494ed6d954"),
            Phrase("This is the most useful feature.", "Đây là tính năng hữu ích nhất.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "9f56944e61ff84e770c0"),
            Phrase("The second idea seems more practical.", "Ý tưởng thứ hai có vẻ thực tế hơn.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "8361b176f20bb9a4f678"),
            Phrase("I agree with you about the price.", "Tôi đồng ý với bạn về giá cả.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "d66318c8d461ae3f0827"),
            Phrase("It's difficult to choose between them.", "Thật khó để chọn giữa hai cái.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "d6afe3ae926a4c66efe8"),
            Phrase("Let's compare the two options carefully.", "Chúng ta hãy so sánh kỹ hai lựa chọn.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "46dcd40de5c192c24984")
        ), listOf(
            ConversationTurn("Which restaurant do you prefer?", "Bạn thích nhà hàng nào hơn?", "I prefer the quieter restaurant.", "Tôi thích nhà hàng yên tĩnh hơn.", id = "d014d6aacd9b6503df2d"),
            ConversationTurn("How do you feel about my idea?", "Bạn nghĩ sao về ý tưởng của tôi?", "I don't completely agree with that.", "Tôi không hoàn toàn đồng ý với điều đó.", id = "b6fd898e3767b91c9c20"),
            ConversationTurn("What do you think of these choices?", "Bạn nghĩ gì về các lựa chọn này?", "I think both choices have advantages.", "Tôi nghĩ cả hai lựa chọn đều có ưu điểm.", id = "3d52749597ca8f7edc45"),
            ConversationTurn("Which feature matters most?", "Tính năng nào quan trọng nhất?", "This is the most useful feature.", "Đây là tính năng hữu ích nhất.", id = "5af83c76488401bf1751"),
            ConversationTurn("Should we compare them first?", "Chúng ta nên so sánh trước không?", "Let's compare the two options carefully.", "Chúng ta hãy so sánh kỹ hai lựa chọn.", id = "cd00da0057ffaedeb71a")
        )),
        Lesson("a2_problems", "Problems & Solutions", "Vấn đề và giải pháp", listOf(
            Phrase("I seem to have lost my wallet.", "Có vẻ tôi đã làm mất ví.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "0003fc2f2561e678b043"),
            Phrase("The air conditioner isn't working properly.", "Máy điều hòa hoạt động không đúng.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "e40c305ca97f517e2244"),
            Phrase("Could you help me solve this problem?", "Bạn có thể giúp tôi giải quyết vấn đề này không?", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "9912ddc1119afedc4e6f"),
            Phrase("I have already reported the issue.", "Tôi đã báo cáo sự cố rồi.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "6629a0b2d1c7ec6426cb"),
            Phrase("We should check the instructions first.", "Chúng ta nên kiểm tra hướng dẫn trước.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "d92bbea4eeaf52a593ec"),
            Phrase("The package arrived two days late.", "Gói hàng đến muộn hai ngày.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "ae0ff471e578c816f6fb"),
            Phrase("I would like to request a refund.", "Tôi muốn yêu cầu hoàn tiền.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "fa84382dc2e686711cfe"),
            Phrase("I'm sorry for the inconvenience.", "Tôi xin lỗi vì sự bất tiện.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "c0c89338c0f642b1a08d"),
            Phrase("Please let me know when it is fixed.", "Vui lòng báo tôi khi việc đó được khắc phục.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "f16aab8779b6d272c9ea"),
            Phrase("I can try a different approach.", "Tôi có thể thử cách khác.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "d41a255762735e646679"),
            Phrase("The problem has been solved.", "Vấn đề đã được giải quyết.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "9e5e1cffd9251dcde53f"),
            Phrase("Thank you for being so patient.", "Cảm ơn bạn đã rất kiên nhẫn.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "fe01d00b10353a76ec58")
        ), listOf(
            ConversationTurn("What seems to be the problem?", "Có vẻ đang có vấn đề gì?", "The air conditioner isn't working properly.", "Máy điều hòa hoạt động không đúng.", id = "04a535e1dca7fb3b669d"),
            ConversationTurn("Have you reported the issue?", "Bạn đã báo cáo sự cố chưa?", "I have already reported the issue.", "Tôi đã báo cáo sự cố rồi.", id = "bbf32dfbafdea0f73616"),
            ConversationTurn("Would you like your money back?", "Bạn có muốn nhận lại tiền không?", "I would like to request a refund.", "Tôi muốn yêu cầu hoàn tiền.", id = "646279cef0a46b2ae483"),
            ConversationTurn("What will you do next?", "Tiếp theo bạn sẽ làm gì?", "I can try a different approach.", "Tôi có thể thử cách khác.", id = "59d568a08b1e002f2cad"),
            ConversationTurn("Is everything working now?", "Bây giờ mọi thứ hoạt động chưa?", "The problem has been solved.", "Vấn đề đã được giải quyết.", id = "5e012d5c6964069502a6")
        )),
        Lesson("a2_travel", "Travel & Hospitality", "Du lịch và dịch vụ", listOf(
            Phrase("I'd like to confirm my reservation.", "Tôi muốn xác nhận đặt phòng của mình.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "5d549c1d26428d9891a9"),
            Phrase("Could I check in a little earlier?", "Tôi có thể nhận phòng sớm hơn một chút không?", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "68eb4d2b4e65ffe488e9"),
            Phrase("Is there an extra charge for breakfast?", "Có tính thêm phí cho bữa sáng không?", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "312bcb72feab3d7b72e8"),
            Phrase("Our flight has been cancelled.", "Chuyến bay của chúng tôi đã bị hủy.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "101107337740ebeccd20"),
            Phrase("Can you suggest a nearby attraction?", "Bạn có thể gợi ý điểm tham quan gần đây không?", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "6c862fbe419e3341e570"),
            Phrase("The room isn't as quiet as I expected.", "Phòng không yên tĩnh như tôi mong đợi.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "2d58a0c9cfc117014521"),
            Phrase("I'd prefer a room on a higher floor.", "Tôi muốn phòng ở tầng cao hơn.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính.", id = "1d87f4d97fc1741570ca"),
            Phrase("Do I need to book the tour in advance?", "Tôi có cần đặt tour trước không?", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối.", id = "d47387805b8714397956"),
            Phrase("The hotel provides a shuttle service.", "Khách sạn có dịch vụ xe đưa đón.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên.", id = "07858a9e477a44002adc"),
            Phrase("How long does it take to reach the airport?", "Mất bao lâu để đến sân bay?", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự.", id = "8b1cccf1577e5e501d60"),
            Phrase("Could you arrange a taxi for six o'clock?", "Bạn có thể sắp xếp taxi lúc sáu giờ không?", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm.", id = "c61c4f64a20f6a8ac434"),
            Phrase("Thank you for making our stay comfortable.", "Cảm ơn bạn đã giúp kỳ nghỉ của chúng tôi thoải mái.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.", id = "1c3abc03b2099bed3756")
        ), listOf(
            ConversationTurn("How can I help with your booking?", "Tôi có thể giúp gì về đặt chỗ của bạn?", "I'd like to confirm my reservation.", "Tôi muốn xác nhận đặt phòng của mình.", id = "da3051cb3b12090c8406"),
            ConversationTurn("Would you like to check in now?", "Bạn muốn nhận phòng ngay bây giờ chứ?", "Could I check in a little earlier?", "Tôi có thể nhận phòng sớm hơn một chút không?", id = "65c63357ebf792fdb997"),
            ConversationTurn("What kind of room would you prefer?", "Bạn muốn loại phòng như thế nào?", "I'd prefer a room on a higher floor.", "Tôi muốn phòng ở tầng cao hơn.", id = "6758c73c6943f8c15ee5"),
            ConversationTurn("Do you need transport to the airport?", "Bạn có cần xe ra sân bay không?", "Could you arrange a taxi for six o'clock?", "Bạn có thể sắp xếp taxi lúc sáu giờ không?", id = "8ce333730b835d10a398"),
            ConversationTurn("How was your stay with us?", "Kỳ lưu trú của bạn ở đây thế nào?", "Thank you for making our stay comfortable.", "Cảm ơn bạn đã giúp kỳ nghỉ của chúng tôi thoải mái.", id = "57f334e5327945d88436")
        )),
    )
}
