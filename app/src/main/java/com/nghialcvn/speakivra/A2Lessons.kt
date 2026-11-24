package com.nghialcvn.speakivra

/** Introductory A2 curriculum: real bilingual phrases and scripted exchanges, no fake AI dialogue. */
internal object A2Lessons {
    val all: List<Lesson> = listOf(
        Lesson("a2_experiences", "Life Experiences", "Trải nghiệm cá nhân", listOf(
            Phrase("I have visited Da Nang twice.", "Tôi đã đến Đà Nẵng hai lần.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("I have never tried skiing.", "Tôi chưa từng thử trượt tuyết.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("Have you ever flown alone?", "Bạn đã từng bay một mình chưa?", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("I went to Hue last summer.", "Tôi đã đến Huế mùa hè năm ngoái.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("The trip was better than I expected.", "Chuyến đi tốt hơn tôi mong đợi.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("I learned to cook when I was young.", "Tôi học nấu ăn khi còn nhỏ.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng."),
            Phrase("I haven't finished that book yet.", "Tôi vẫn chưa đọc xong cuốn sách đó.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("She has already seen that film.", "Cô ấy đã xem bộ phim đó rồi.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("We stayed there for three days.", "Chúng tôi đã ở đó ba ngày.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("I met a very friendly guide.", "Tôi đã gặp một hướng dẫn viên rất thân thiện.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("What was the best part of your trip?", "Phần tuyệt nhất trong chuyến đi của bạn là gì?", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("I would love to go back someday.", "Tôi rất muốn quay lại vào một ngày nào đó.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.")
        ), listOf(
            ConversationTurn("Have you ever visited Da Nang?", "Bạn đã từng đến Đà Nẵng chưa?", "I have visited Da Nang twice.", "Tôi đã đến Đà Nẵng hai lần."),
            ConversationTurn("Have you ever tried skiing?", "Bạn đã từng thử trượt tuyết chưa?", "I have never tried skiing.", "Tôi chưa từng thử trượt tuyết."),
            ConversationTurn("Where did you go last summer?", "Mùa hè năm ngoái bạn đã đi đâu?", "I went to Hue last summer.", "Tôi đã đến Huế mùa hè năm ngoái."),
            ConversationTurn("Have you finished the book?", "Bạn đã đọc xong sách chưa?", "I haven't finished that book yet.", "Tôi vẫn chưa đọc xong cuốn sách đó."),
            ConversationTurn("Would you like to visit again?", "Bạn có muốn đến đó lần nữa không?", "I would love to go back someday.", "Tôi rất muốn quay lại vào một ngày nào đó.")
        )),
        Lesson("a2_plans", "Plans & Arrangements", "Kế hoạch và sắp xếp", listOf(
            Phrase("I'm going to study tonight.", "Tối nay tôi định học bài.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("We are meeting at the station tomorrow.", "Ngày mai chúng tôi sẽ gặp nhau ở nhà ga.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("I might visit my cousin this weekend.", "Có thể cuối tuần này tôi sẽ thăm anh họ.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("Could we move the meeting to Friday?", "Chúng ta có thể chuyển cuộc họp sang thứ Sáu không?", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("I need to book the tickets in advance.", "Tôi cần đặt vé trước.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("Let me check my schedule first.", "Để tôi kiểm tra lịch trước đã.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng."),
            Phrase("I'm free after four in the afternoon.", "Tôi rảnh sau bốn giờ chiều.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("We should leave early to avoid traffic.", "Chúng ta nên đi sớm để tránh tắc đường.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("I will send you the details later.", "Tôi sẽ gửi bạn thông tin chi tiết sau.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("If it rains, we'll stay at home.", "Nếu trời mưa, chúng tôi sẽ ở nhà.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("What time shall we meet?", "Chúng ta gặp nhau lúc mấy giờ?", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("That plan sounds good to me.", "Kế hoạch đó nghe ổn với tôi.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.")
        ), listOf(
            ConversationTurn("What are you going to do tonight?", "Tối nay bạn định làm gì?", "I'm going to study tonight.", "Tối nay tôi định học bài."),
            ConversationTurn("Can you meet on Thursday?", "Bạn có thể gặp vào thứ Năm không?", "Could we move the meeting to Friday?", "Chúng ta có thể chuyển cuộc họp sang thứ Sáu không?"),
            ConversationTurn("When are you available?", "Khi nào bạn rảnh?", "I'm free after four in the afternoon.", "Tôi rảnh sau bốn giờ chiều."),
            ConversationTurn("What if the weather is bad?", "Nếu thời tiết xấu thì sao?", "If it rains, we'll stay at home.", "Nếu trời mưa, chúng tôi sẽ ở nhà."),
            ConversationTurn("Do you agree with the plan?", "Bạn có đồng ý với kế hoạch không?", "That plan sounds good to me.", "Kế hoạch đó nghe ổn với tôi.")
        )),
        Lesson("a2_opinions", "Opinions & Comparisons", "Ý kiến và so sánh", listOf(
            Phrase("In my opinion, this option is better.", "Theo ý tôi, lựa chọn này tốt hơn.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("I prefer the quieter restaurant.", "Tôi thích nhà hàng yên tĩnh hơn.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("This phone is cheaper than mine.", "Điện thoại này rẻ hơn của tôi.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("The new course is more interesting.", "Khóa học mới thú vị hơn.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("I don't completely agree with that.", "Tôi không hoàn toàn đồng ý với điều đó.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("Could you explain your opinion?", "Bạn có thể giải thích ý kiến của mình không?", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng."),
            Phrase("I think both choices have advantages.", "Tôi nghĩ cả hai lựa chọn đều có ưu điểm.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("This is the most useful feature.", "Đây là tính năng hữu ích nhất.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("The second idea seems more practical.", "Ý tưởng thứ hai có vẻ thực tế hơn.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("I agree with you about the price.", "Tôi đồng ý với bạn về giá cả.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("It's difficult to choose between them.", "Thật khó để chọn giữa hai cái.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("Let's compare the two options carefully.", "Chúng ta hãy so sánh kỹ hai lựa chọn.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.")
        ), listOf(
            ConversationTurn("Which restaurant do you prefer?", "Bạn thích nhà hàng nào hơn?", "I prefer the quieter restaurant.", "Tôi thích nhà hàng yên tĩnh hơn."),
            ConversationTurn("How do you feel about my idea?", "Bạn nghĩ sao về ý tưởng của tôi?", "I don't completely agree with that.", "Tôi không hoàn toàn đồng ý với điều đó."),
            ConversationTurn("What do you think of these choices?", "Bạn nghĩ gì về các lựa chọn này?", "I think both choices have advantages.", "Tôi nghĩ cả hai lựa chọn đều có ưu điểm."),
            ConversationTurn("Which feature matters most?", "Tính năng nào quan trọng nhất?", "This is the most useful feature.", "Đây là tính năng hữu ích nhất."),
            ConversationTurn("Should we compare them first?", "Chúng ta nên so sánh trước không?", "Let's compare the two options carefully.", "Chúng ta hãy so sánh kỹ hai lựa chọn.")
        )),
        Lesson("a2_problems", "Problems & Solutions", "Vấn đề và giải pháp", listOf(
            Phrase("I seem to have lost my wallet.", "Có vẻ tôi đã làm mất ví.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("The air conditioner isn't working properly.", "Máy điều hòa hoạt động không đúng.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("Could you help me solve this problem?", "Bạn có thể giúp tôi giải quyết vấn đề này không?", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("I have already reported the issue.", "Tôi đã báo cáo sự cố rồi.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("We should check the instructions first.", "Chúng ta nên kiểm tra hướng dẫn trước.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("The package arrived two days late.", "Gói hàng đến muộn hai ngày.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng."),
            Phrase("I would like to request a refund.", "Tôi muốn yêu cầu hoàn tiền.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("I'm sorry for the inconvenience.", "Tôi xin lỗi vì sự bất tiện.", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("Please let me know when it is fixed.", "Vui lòng báo tôi khi việc đó được khắc phục.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("I can try a different approach.", "Tôi có thể thử cách khác.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("The problem has been solved.", "Vấn đề đã được giải quyết.", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("Thank you for being so patient.", "Cảm ơn bạn đã rất kiên nhẫn.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.")
        ), listOf(
            ConversationTurn("What seems to be the problem?", "Có vẻ đang có vấn đề gì?", "The air conditioner isn't working properly.", "Máy điều hòa hoạt động không đúng."),
            ConversationTurn("Have you reported the issue?", "Bạn đã báo cáo sự cố chưa?", "I have already reported the issue.", "Tôi đã báo cáo sự cố rồi."),
            ConversationTurn("Would you like your money back?", "Bạn có muốn nhận lại tiền không?", "I would like to request a refund.", "Tôi muốn yêu cầu hoàn tiền."),
            ConversationTurn("What will you do next?", "Tiếp theo bạn sẽ làm gì?", "I can try a different approach.", "Tôi có thể thử cách khác."),
            ConversationTurn("Is everything working now?", "Bây giờ mọi thứ hoạt động chưa?", "The problem has been solved.", "Vấn đề đã được giải quyết.")
        )),
        Lesson("a2_travel", "Travel & Hospitality", "Du lịch và dịch vụ", listOf(
            Phrase("I'd like to confirm my reservation.", "Tôi muốn xác nhận đặt phòng của mình.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("Could I check in a little earlier?", "Tôi có thể nhận phòng sớm hơn một chút không?", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("Is there an extra charge for breakfast?", "Có tính thêm phí cho bữa sáng không?", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("Our flight has been cancelled.", "Chuyến bay của chúng tôi đã bị hủy.", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("Can you suggest a nearby attraction?", "Bạn có thể gợi ý điểm tham quan gần đây không?", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("The room isn't as quiet as I expected.", "Phòng không yên tĩnh như tôi mong đợi.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng."),
            Phrase("I'd prefer a room on a higher floor.", "Tôi muốn phòng ở tầng cao hơn.", "Chú ý thì và từ chỉ thời gian; nhấn từ mang nghĩa chính."),
            Phrase("Do I need to book the tour in advance?", "Tôi có cần đặt tour trước không?", "Luyện nối âm ở tốc độ vừa phải, không nuốt phụ âm cuối."),
            Phrase("The hotel provides a shuttle service.", "Khách sạn có dịch vụ xe đưa đón.", "Ngắt câu thành từng cụm ý để giữ ngữ điệu tự nhiên."),
            Phrase("How long does it take to reach the airport?", "Mất bao lâu để đến sân bay?", "Nhấn từ so sánh hoặc từ diễn tả yêu cầu lịch sự."),
            Phrase("Could you arrange a taxi for six o'clock?", "Bạn có thể sắp xếp taxi lúc sáu giờ không?", "Thử nói chậm rồi tăng tốc, vẫn giữ rõ trọng âm."),
            Phrase("Thank you for making our stay comfortable.", "Cảm ơn bạn đã giúp kỳ nghỉ của chúng tôi thoải mái.", "Hỏi lịch sự với ngữ điệu nhẹ và kết thúc rõ ràng.")
        ), listOf(
            ConversationTurn("How can I help with your booking?", "Tôi có thể giúp gì về đặt chỗ của bạn?", "I'd like to confirm my reservation.", "Tôi muốn xác nhận đặt phòng của mình."),
            ConversationTurn("Would you like to check in now?", "Bạn muốn nhận phòng ngay bây giờ chứ?", "Could I check in a little earlier?", "Tôi có thể nhận phòng sớm hơn một chút không?"),
            ConversationTurn("What kind of room would you prefer?", "Bạn muốn loại phòng như thế nào?", "I'd prefer a room on a higher floor.", "Tôi muốn phòng ở tầng cao hơn."),
            ConversationTurn("Do you need transport to the airport?", "Bạn có cần xe ra sân bay không?", "Could you arrange a taxi for six o'clock?", "Bạn có thể sắp xếp taxi lúc sáu giờ không?"),
            ConversationTurn("How was your stay with us?", "Kỳ lưu trú của bạn ở đây thế nào?", "Thank you for making our stay comfortable.", "Cảm ơn bạn đã giúp kỳ nghỉ của chúng tôi thoải mái.")
        )),
    )
}
