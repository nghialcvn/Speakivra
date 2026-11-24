package com.nghialcvn.speakivra

internal data class Phrase(val english: String, val vietnamese: String, val tip: String)
internal data class ConversationTurn(val prompt: String, val translation: String, val reply: String, val replyTranslation: String)
internal data class Lesson(val id: String, val title: String, val vietnamese: String, val phrases: List<Phrase>, val conversation: List<ConversationTurn>)

/** Reviewed A1 practice content. Conversations are fixed scripts, not an AI service. */
internal object Lessons {
    val all = listOf(
        Lesson("greetings", "Greetings & Introductions", "Chào hỏi và giới thiệu", listOf(
            Phrase("Hello! How are you?", "Xin chào! Bạn khỏe không?", "Nối nhẹ how và are, lên giọng ở lời chào."),
            Phrase("I'm fine, thank you.", "Tôi khỏe, cảm ơn bạn.", "Đặt đầu lưỡi nhẹ giữa hai hàm răng ở âm /θ/ trong thank."),
            Phrase("My name is Alex.", "Tên tôi là Alex.", "Nối name và is; đừng bỏ âm /m/ cuối name."),
            Phrase("Nice to meet you!", "Rất vui được gặp bạn!", "Kéo dài âm /iː/ trong meet, giọng vui khi chào."),
            Phrase("I'm from Vietnam.", "Tôi đến từ Việt Nam.", "Giữ âm /m/ cuối from; nhấn Vietnam."),
            Phrase("I live in Hanoi.", "Tôi sống ở Hà Nội.", "Nối live và in, giữ âm /v/ bằng môi dưới và răng trên."),
            Phrase("I'm a student.", "Tôi là học sinh.", "Nhấn âm đầu của student, không thêm âm vào cụm /st/."),
            Phrase("See you tomorrow!", "Hẹn gặp bạn ngày mai!", "Nhấn âm giữa của tomorrow, nói thân thiện.")
        ), listOf(
            ConversationTurn("Hello! What's your name?", "Xin chào! Bạn tên là gì?", "My name is Alex.", "Tên tôi là Alex."),
            ConversationTurn("Nice to meet you! Where are you from?", "Rất vui được gặp bạn! Bạn đến từ đâu?", "I'm from Vietnam.", "Tôi đến từ Việt Nam."),
            ConversationTurn("Where do you live?", "Bạn sống ở đâu?", "I live in Hanoi.", "Tôi sống ở Hà Nội.")
        )),
        Lesson("family", "My Family", "Gia đình tôi", listOf(
            Phrase("This is my family.", "Đây là gia đình tôi.", "Rung nhẹ cổ họng với âm /ð/ trong this; nhấn âm đầu family."),
            Phrase("I have one brother.", "Tôi có một anh trai.", "Giữ âm /v/ cuối have và /ð/ giữa brother."),
            Phrase("I have two sisters.", "Tôi có hai chị gái.", "Đọc rõ âm /z/ cuối sisters, không thêm một âm tiết."),
            Phrase("My mother is a teacher.", "Mẹ tôi là giáo viên.", "Nối mother và is; nhấn âm đầu teacher."),
            Phrase("My father is a doctor.", "Bố tôi là bác sĩ.", "Nhấn âm đầu doctor, giữ âm /ð/ trong father."),
            Phrase("We live together.", "Chúng tôi sống cùng nhau.", "Nhấn âm giữa together; nối live và together nhẹ nhàng."),
            Phrase("My grandmother is very kind.", "Bà tôi rất tốt bụng.", "Giữ âm /d/ cuối kind, nhấn very kind."),
            Phrase("I love my family.", "Tôi yêu gia đình mình.", "Đọc âm /v/ cuối love, giọng ấm áp.")
        ), listOf(
            ConversationTurn("Do you have any brothers?", "Bạn có anh em trai không?", "I have one brother.", "Tôi có một anh trai."),
            ConversationTurn("What does your mother do?", "Mẹ bạn làm nghề gì?", "My mother is a teacher.", "Mẹ tôi là giáo viên."),
            ConversationTurn("Do you live with your family?", "Bạn có sống cùng gia đình không?", "We live together.", "Chúng tôi sống cùng nhau.")
        )),
        Lesson("routine", "Daily Routine", "Sinh hoạt hằng ngày", listOf(
            Phrase("I wake up at seven.", "Tôi thức dậy lúc bảy giờ.", "Nối wake up và up at; giữ âm /n/ cuối seven."),
            Phrase("I brush my teeth.", "Tôi đánh răng.", "Giữ âm /ʃ/ cuối brush và /θ/ cuối teeth."),
            Phrase("I have breakfast at home.", "Tôi ăn sáng ở nhà.", "Nhấn âm đầu breakfast; nối breakfast at."),
            Phrase("I go to school by bus.", "Tôi đi học bằng xe buýt.", "Giữ âm /s/ cuối bus; không thêm âm vào school."),
            Phrase("I start work at nine.", "Tôi bắt đầu làm việc lúc chín giờ.", "Giữ âm /t/ cuối start, nối work at."),
            Phrase("I have lunch at noon.", "Tôi ăn trưa lúc mười hai giờ.", "Kết thúc lunch bằng /tʃ/, kéo dài /uː/ trong noon."),
            Phrase("I go home at five.", "Tôi về nhà lúc năm giờ.", "Nối home at; đọc rõ âm /v/ cuối five."),
            Phrase("I go to bed at ten.", "Tôi đi ngủ lúc mười giờ.", "Giữ âm /d/ cuối bed; nhấn ten.")
        ), listOf(
            ConversationTurn("What time do you wake up?", "Bạn thức dậy lúc mấy giờ?", "I wake up at seven.", "Tôi thức dậy lúc bảy giờ."),
            ConversationTurn("How do you go to school?", "Bạn đến trường bằng cách nào?", "I go to school by bus.", "Tôi đi học bằng xe buýt."),
            ConversationTurn("What time do you go to bed?", "Bạn đi ngủ lúc mấy giờ?", "I go to bed at ten.", "Tôi đi ngủ lúc mười giờ.")
        )),
        Lesson("food", "Food & Drinks", "Đồ ăn và thức uống", listOf(
            Phrase("I'd like some water, please.", "Cho tôi xin một ít nước.", "Nói please nhẹ nhàng; giữ âm /d/ trong I'd."),
            Phrase("Can I have the menu, please?", "Cho tôi xem thực đơn được không?", "Nhấn âm đầu menu, lên giọng lịch sự ở câu hỏi."),
            Phrase("I like chicken and rice.", "Tôi thích thịt gà và cơm.", "Giữ âm /s/ cuối rice; and có thể đọc nhẹ."),
            Phrase("I don't eat meat.", "Tôi không ăn thịt.", "Giữ /t/ cuối don't và meat, kéo dài /iː/ trong eat."),
            Phrase("This soup is delicious!", "Món súp này ngon quá!", "Nhấn âm giữa delicious, giọng hài lòng."),
            Phrase("I'd like a cup of tea.", "Tôi muốn một tách trà.", "Nối cup of; giữ âm /p/ của cup."),
            Phrase("No sugar, please.", "Không đường, làm ơn.", "Sugar bắt đầu bằng /ʃ/, nhấn âm đầu."),
            Phrase("Can I have the bill, please?", "Cho tôi xin hóa đơn được không?", "Bill dùng âm /ɪ/ ngắn, giọng hỏi lịch sự.")
        ), listOf(
            ConversationTurn("What would you like to drink?", "Bạn muốn uống gì?", "I'd like some water, please.", "Cho tôi xin một ít nước."),
            ConversationTurn("What food do you like?", "Bạn thích món ăn nào?", "I like chicken and rice.", "Tôi thích thịt gà và cơm."),
            ConversationTurn("Would you like sugar in your tea?", "Bạn muốn thêm đường vào trà không?", "No sugar, please.", "Không đường, làm ơn.")
        )),
        Lesson("shopping", "Shopping", "Mua sắm", listOf(
            Phrase("How much is this shirt?", "Chiếc áo sơ mi này giá bao nhiêu?", "Nối much is, giữ cụm /ʃ/ đầu shirt."),
            Phrase("I'm looking for a blue shirt.", "Tôi đang tìm một chiếc áo sơ mi màu xanh.", "Giữ /ŋ/ cuối looking, kéo dài /uː/ trong blue."),
            Phrase("Do you have a smaller size?", "Bạn có cỡ nhỏ hơn không?", "Size kết thúc bằng /z/, không đọc thành /s/."),
            Phrase("Can I try it on?", "Tôi có thể thử nó không?", "Nối try it on thành một cụm liền mạch."),
            Phrase("It's too expensive.", "Nó đắt quá.", "Nhấn âm thứ hai của expensive, kéo dài too."),
            Phrase("I'll take this one.", "Tôi sẽ lấy cái này.", "Nối take this; giữ âm /ð/ trong this."),
            Phrase("Can I pay by card?", "Tôi có thể trả bằng thẻ không?", "Giữ /d/ cuối card, nhấn pay và card."),
            Phrase("Thank you for your help.", "Cảm ơn bạn đã giúp đỡ.", "Giữ âm /p/ cuối help, nói với giọng cảm ơn.")
        ), listOf(
            ConversationTurn("Can I help you?", "Tôi có thể giúp gì cho bạn?", "I'm looking for a blue shirt.", "Tôi đang tìm một chiếc áo sơ mi màu xanh."),
            ConversationTurn("Is this size okay?", "Cỡ này có vừa không?", "Do you have a smaller size?", "Bạn có cỡ nhỏ hơn không?"),
            ConversationTurn("How would you like to pay?", "Bạn muốn thanh toán bằng cách nào?", "Can I pay by card?", "Tôi có thể trả bằng thẻ không?")
        )),
        Lesson("directions", "Directions", "Hỏi và chỉ đường", listOf(
            Phrase("Excuse me, where is the station?", "Xin lỗi, nhà ga ở đâu?", "Nhấn âm đầu station, mở lời nhẹ nhàng."),
            Phrase("Go straight ahead.", "Đi thẳng về phía trước.", "Không thêm âm vào /str/ trong straight; nhấn ahead."),
            Phrase("Turn left at the traffic lights.", "Rẽ trái ở đèn giao thông.", "Giữ /t/ cuối left, nhấn âm đầu traffic."),
            Phrase("Turn right at the bank.", "Rẽ phải ở ngân hàng.", "Giữ /t/ cuối right, kết thúc bank bằng /ŋk/."),
            Phrase("It's next to the supermarket.", "Nó ở cạnh siêu thị.", "Đọc chậm cụm next to, nhấn âm đầu supermarket."),
            Phrase("It's across from the park.", "Nó ở đối diện công viên.", "Nhấn âm thứ hai across, giữ /k/ cuối park."),
            Phrase("Is it far from here?", "Nó có xa đây không?", "Nối is it; kéo dài nguyên âm trong far."),
            Phrase("Thank you. I understand.", "Cảm ơn. Tôi hiểu rồi.", "Ngắt nhẹ sau thank you; nhấn âm cuối understand.")
        ), listOf(
            ConversationTurn("Excuse me, how do I get to the bank?", "Xin lỗi, tôi đến ngân hàng bằng cách nào?", "Go straight ahead.", "Đi thẳng về phía trước."),
            ConversationTurn("Where do I turn?", "Tôi rẽ ở đâu?", "Turn left at the traffic lights.", "Rẽ trái ở đèn giao thông."),
            ConversationTurn("Where is the supermarket?", "Siêu thị ở đâu?", "It's across from the park.", "Nó ở đối diện công viên.")
        )),
        Lesson("hobbies", "My Hobbies", "Sở thích của tôi", listOf(
            Phrase("I like listening to music.", "Tôi thích nghe nhạc.", "Listening có chữ t câm; nhấn âm đầu music."),
            Phrase("I enjoy reading books.", "Tôi thích đọc sách.", "Nhấn âm sau enjoy, giữ /s/ cuối books."),
            Phrase("I play football on Sundays.", "Tôi chơi bóng đá vào các Chủ nhật.", "Nhấn âm đầu football; Sundays kết thúc bằng /z/."),
            Phrase("I like cooking with my family.", "Tôi thích nấu ăn cùng gia đình.", "Cooking dùng âm /ʊ/ ngắn; giữ /ŋ/ cuối."),
            Phrase("I watch movies at home.", "Tôi xem phim ở nhà.", "Movies kết thúc bằng /z/; nối movies at."),
            Phrase("I can swim.", "Tôi biết bơi.", "Không thêm nguyên âm vào /sw/ đầu swim."),
            Phrase("I draw pictures in my free time.", "Tôi vẽ tranh trong thời gian rảnh.", "Nhấn âm đầu pictures, kéo dài /iː/ trong free."),
            Phrase("Let's play together!", "Chúng ta cùng chơi nhé!", "Giữ /s/ cuối let's; giọng mời thân thiện.")
        ), listOf(
            ConversationTurn("What do you like doing?", "Bạn thích làm gì?", "I like listening to music.", "Tôi thích nghe nhạc."),
            ConversationTurn("What do you do on Sundays?", "Bạn làm gì vào các Chủ nhật?", "I play football on Sundays.", "Tôi chơi bóng đá vào các Chủ nhật."),
            ConversationTurn("Can you swim?", "Bạn có biết bơi không?", "I can swim.", "Tôi biết bơi.")
        )),
        Lesson("time", "Time & Dates", "Giờ và ngày tháng", listOf(
            Phrase("What time is it?", "Bây giờ là mấy giờ?", "Nối time is it, giữ /t/ cuối câu."),
            Phrase("It's half past eight.", "Bây giờ là tám giờ rưỡi.", "Half có chữ l câm; nối past eight."),
            Phrase("Today is Monday.", "Hôm nay là thứ Hai.", "Nhấn âm đầu Monday, nối today is."),
            Phrase("My birthday is in June.", "Sinh nhật tôi vào tháng Sáu.", "Birthday có âm /θ/; June kết thúc bằng /n/."),
            Phrase("The meeting is at two.", "Cuộc họp lúc hai giờ.", "Nối meeting is at, kéo dài /uː/ trong two."),
            Phrase("See you on Friday!", "Hẹn gặp bạn vào thứ Sáu!", "Nhấn âm đầu Friday; giọng hẹn gặp vui vẻ."),
            Phrase("I'm free this afternoon.", "Chiều nay tôi rảnh.", "Nhấn âm cuối afternoon; giữ /ð/ trong this."),
            Phrase("Let's meet at ten tomorrow.", "Hãy gặp nhau lúc mười giờ ngày mai.", "Nối meet at; nhấn ten và âm giữa tomorrow.")
        ), listOf(
            ConversationTurn("What time is it now?", "Bây giờ là mấy giờ?", "It's half past eight.", "Bây giờ là tám giờ rưỡi."),
            ConversationTurn("When is your birthday?", "Sinh nhật bạn vào khi nào?", "My birthday is in June.", "Sinh nhật tôi vào tháng Sáu."),
            ConversationTurn("When can we meet?", "Khi nào chúng ta có thể gặp nhau?", "Let's meet at ten tomorrow.", "Hãy gặp nhau lúc mười giờ ngày mai.")
        )),
        Lesson("travel", "Travel", "Du lịch", listOf(
            Phrase("I'd like a ticket to Hanoi.", "Tôi muốn một vé đến Hà Nội.", "Nhấn âm đầu ticket; nối like a."),
            Phrase("Is this seat free?", "Chỗ ngồi này còn trống không?", "Seat dùng /iː/ dài; giữ /t/ cuối seat."),
            Phrase("I have a reservation.", "Tôi đã đặt chỗ.", "Nhấn âm tiết va trong reservation."),
            Phrase("Can I see your passport, please?", "Cho tôi xem hộ chiếu của bạn được không?", "Nhấn âm đầu passport, dùng giọng hỏi lịch sự."),
            Phrase("Where is the bus stop?", "Trạm xe buýt ở đâu?", "Giữ /s/ cuối bus và /p/ cuối stop."),
            Phrase("What time does the train leave?", "Tàu khởi hành lúc mấy giờ?", "Giữ /v/ cuối leave, nhấn train và leave."),
            Phrase("I'd like a room for two nights.", "Tôi muốn một phòng trong hai đêm.", "Nights kết thúc bằng /ts/; kéo dài /uː/ trong room."),
            Phrase("Have a nice trip!", "Chúc bạn có chuyến đi vui vẻ!", "Giữ /p/ cuối trip; giọng chúc thân thiện.")
        ), listOf(
            ConversationTurn("Where would you like to go?", "Bạn muốn đi đâu?", "I'd like a ticket to Hanoi.", "Tôi muốn một vé đến Hà Nội."),
            ConversationTurn("Do you have a reservation?", "Bạn đã đặt chỗ chưa?", "I have a reservation.", "Tôi đã đặt chỗ."),
            ConversationTurn("How long would you like to stay?", "Bạn muốn ở bao lâu?", "I'd like a room for two nights.", "Tôi muốn một phòng trong hai đêm.")
        )),
        Lesson("conversation", "Real Conversation", "Giao tiếp thường ngày", listOf(
            Phrase("Could you say that again, please?", "Bạn có thể nói lại được không?", "Nối say that, giọng đề nghị lịch sự."),
            Phrase("Please speak more slowly.", "Làm ơn nói chậm hơn.", "Nhấn âm đầu slowly; không thêm âm trước speak."),
            Phrase("I don't understand.", "Tôi không hiểu.", "Nhấn âm cuối understand, giữ /d/ cuối."),
            Phrase("How do you spell your name?", "Bạn đánh vần tên mình như thế nào?", "Giữ cụm /sp/ đầu spell, nối spell your."),
            Phrase("I'm learning English.", "Tôi đang học tiếng Anh.", "Giữ /ŋ/ cuối learning; nhấn âm đầu English."),
            Phrase("Can you help me, please?", "Bạn có thể giúp tôi không?", "Giữ /p/ cuối help, dùng giọng nhờ giúp."),
            Phrase("Sorry, I'm late.", "Xin lỗi, tôi đến muộn.", "Giọng xin lỗi nhẹ; giữ /t/ cuối late."),
            Phrase("It was nice talking to you.", "Rất vui được trò chuyện với bạn.", "Nối talking to, giọng kết thúc thân thiện.")
        ), listOf(
            ConversationTurn("Hi! How is your English class?", "Chào bạn! Lớp tiếng Anh của bạn thế nào?", "I'm learning English.", "Tôi đang học tiếng Anh."),
            ConversationTurn("Let's meet at a quarter past nine.", "Hãy gặp nhau lúc chín giờ mười lăm.", "Could you say that again, please?", "Bạn có thể nói lại được không?"),
            ConversationTurn("Thanks for the chat. See you soon!", "Cảm ơn cuộc trò chuyện. Hẹn sớm gặp lại!", "It was nice talking to you.", "Rất vui được trò chuyện với bạn.")
        )),
    ).map(ExpandedLessons::append).map(MoreA1Practice::append) + A1NewTopics.all + A2Lessons.all

    val a1: List<Lesson> get() = all.filterNot { it.id.startsWith("a2_") }
    val a2: List<Lesson> get() = all.filter { it.id.startsWith("a2_") }
    fun levelOf(lesson: Lesson): String = if (lesson.id.startsWith("a2_")) "A2" else "A1"
}
