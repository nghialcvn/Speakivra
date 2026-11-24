package com.nghialcvn.speakivra

internal data class Phrase(val english: String, val vietnamese: String, val tip: String, val id: String = english)
internal data class ConversationTurn(val prompt: String, val translation: String, val reply: String, val replyTranslation: String, val id: String = prompt)
internal data class Lesson(val id: String, val title: String, val vietnamese: String, val phrases: List<Phrase>, val conversation: List<ConversationTurn>)

/** Reviewed A1 practice content. Conversations are fixed scripts, not an AI service. */
internal object Lessons {
    val all = listOf(
        Lesson("greetings", "Greetings & Introductions", "Chào hỏi và giới thiệu", listOf(
            Phrase("Hello! How are you?", "Xin chào! Bạn khỏe không?", "Nối nhẹ how và are, lên giọng ở lời chào.", id = "0bda053b271a2d426961"),
            Phrase("I'm fine, thank you.", "Tôi khỏe, cảm ơn bạn.", "Đặt đầu lưỡi nhẹ giữa hai hàm răng ở âm /θ/ trong thank.", id = "d8510d4cc11d6d6456d2"),
            Phrase("My name is Alex.", "Tên tôi là Alex.", "Nối name và is; đừng bỏ âm /m/ cuối name.", id = "81284e80aa8fecde16e1"),
            Phrase("Nice to meet you!", "Rất vui được gặp bạn!", "Kéo dài âm /iː/ trong meet, giọng vui khi chào.", id = "1160a5ae1cde058c44e5"),
            Phrase("I'm from Vietnam.", "Tôi đến từ Việt Nam.", "Giữ âm /m/ cuối from; nhấn Vietnam.", id = "5f71c6264a3f4a8c73be"),
            Phrase("I live in Hanoi.", "Tôi sống ở Hà Nội.", "Nối live và in, giữ âm /v/ bằng môi dưới và răng trên.", id = "4bdf836a78526735514f"),
            Phrase("I'm a student.", "Tôi là học sinh.", "Nhấn âm đầu của student, không thêm âm vào cụm /st/.", id = "6b657556a0442c4ef21a"),
            Phrase("See you tomorrow!", "Hẹn gặp bạn ngày mai!", "Nhấn âm giữa của tomorrow, nói thân thiện.", id = "04a83fcf89fa37d08bc0")
        ), listOf(
            ConversationTurn("Hello! What's your name?", "Xin chào! Bạn tên là gì?", "My name is Alex.", "Tên tôi là Alex.", id = "5dcee9bdca119172f74e"),
            ConversationTurn("Nice to meet you! Where are you from?", "Rất vui được gặp bạn! Bạn đến từ đâu?", "I'm from Vietnam.", "Tôi đến từ Việt Nam.", id = "59f29906a2ec3a3ba51f"),
            ConversationTurn("Where do you live?", "Bạn sống ở đâu?", "I live in Hanoi.", "Tôi sống ở Hà Nội.", id = "8a38c81b46381d269366")
        )),
        Lesson("family", "My Family", "Gia đình tôi", listOf(
            Phrase("This is my family.", "Đây là gia đình tôi.", "Rung nhẹ cổ họng với âm /ð/ trong this; nhấn âm đầu family.", id = "1bd6c8cd3809be1dd066"),
            Phrase("I have one brother.", "Tôi có một anh trai.", "Giữ âm /v/ cuối have và /ð/ giữa brother.", id = "0a9ae5455692cf721355"),
            Phrase("I have two sisters.", "Tôi có hai chị gái.", "Đọc rõ âm /z/ cuối sisters, không thêm một âm tiết.", id = "f2693ebe61da3af92db0"),
            Phrase("My mother is a teacher.", "Mẹ tôi là giáo viên.", "Nối mother và is; nhấn âm đầu teacher.", id = "c5df970de4d42e93e4e6"),
            Phrase("My father is a doctor.", "Bố tôi là bác sĩ.", "Nhấn âm đầu doctor, giữ âm /ð/ trong father.", id = "d493c3fa246e6142c147"),
            Phrase("We live together.", "Chúng tôi sống cùng nhau.", "Nhấn âm giữa together; nối live và together nhẹ nhàng.", id = "7c5810e836f2e92cccdd"),
            Phrase("My grandmother is very kind.", "Bà tôi rất tốt bụng.", "Giữ âm /d/ cuối kind, nhấn very kind.", id = "f9779786bf4a0ef616ad"),
            Phrase("I love my family.", "Tôi yêu gia đình mình.", "Đọc âm /v/ cuối love, giọng ấm áp.", id = "342cd6ad40ba5bb2fd27")
        ), listOf(
            ConversationTurn("Do you have any brothers?", "Bạn có anh em trai không?", "I have one brother.", "Tôi có một anh trai.", id = "037b8e4340cb2ac0b417"),
            ConversationTurn("What does your mother do?", "Mẹ bạn làm nghề gì?", "My mother is a teacher.", "Mẹ tôi là giáo viên.", id = "cf7f7ca79e715a954bf6"),
            ConversationTurn("Do you live with your family?", "Bạn có sống cùng gia đình không?", "We live together.", "Chúng tôi sống cùng nhau.", id = "dee42e03a7a1bb550ee6")
        )),
        Lesson("routine", "Daily Routine", "Sinh hoạt hằng ngày", listOf(
            Phrase("I wake up at seven.", "Tôi thức dậy lúc bảy giờ.", "Nối wake up và up at; giữ âm /n/ cuối seven.", id = "b7709c3cc590c682adf6"),
            Phrase("I brush my teeth.", "Tôi đánh răng.", "Giữ âm /ʃ/ cuối brush và /θ/ cuối teeth.", id = "888ec92eeaeb8111839d"),
            Phrase("I have breakfast at home.", "Tôi ăn sáng ở nhà.", "Nhấn âm đầu breakfast; nối breakfast at.", id = "4719a559a2fb02ec92dd"),
            Phrase("I go to school by bus.", "Tôi đi học bằng xe buýt.", "Giữ âm /s/ cuối bus; không thêm âm vào school.", id = "c3e8b6110b8115836089"),
            Phrase("I start work at nine.", "Tôi bắt đầu làm việc lúc chín giờ.", "Giữ âm /t/ cuối start, nối work at.", id = "4d070b87bbd147dde4ab"),
            Phrase("I have lunch at noon.", "Tôi ăn trưa lúc mười hai giờ.", "Kết thúc lunch bằng /tʃ/, kéo dài /uː/ trong noon.", id = "47994f7debdcef498676"),
            Phrase("I go home at five.", "Tôi về nhà lúc năm giờ.", "Nối home at; đọc rõ âm /v/ cuối five.", id = "d9d0e0203803f73a0ee4"),
            Phrase("I go to bed at ten.", "Tôi đi ngủ lúc mười giờ.", "Giữ âm /d/ cuối bed; nhấn ten.", id = "f4740fbaa904ea81d569")
        ), listOf(
            ConversationTurn("What time do you wake up?", "Bạn thức dậy lúc mấy giờ?", "I wake up at seven.", "Tôi thức dậy lúc bảy giờ.", id = "a8c1a10d5cc6862f3504"),
            ConversationTurn("How do you go to school?", "Bạn đến trường bằng cách nào?", "I go to school by bus.", "Tôi đi học bằng xe buýt.", id = "641287da970add269801"),
            ConversationTurn("What time do you go to bed?", "Bạn đi ngủ lúc mấy giờ?", "I go to bed at ten.", "Tôi đi ngủ lúc mười giờ.", id = "8de2b5136ee2ca97cd06")
        )),
        Lesson("food", "Food & Drinks", "Đồ ăn và thức uống", listOf(
            Phrase("I'd like some water, please.", "Cho tôi xin một ít nước.", "Nói please nhẹ nhàng; giữ âm /d/ trong I'd.", id = "6e53ec411d18e9365b95"),
            Phrase("Can I have the menu, please?", "Cho tôi xem thực đơn được không?", "Nhấn âm đầu menu, lên giọng lịch sự ở câu hỏi.", id = "8c6352646872343462eb"),
            Phrase("I like chicken and rice.", "Tôi thích thịt gà và cơm.", "Giữ âm /s/ cuối rice; and có thể đọc nhẹ.", id = "ae978dee3a2bdb9d0641"),
            Phrase("I don't eat meat.", "Tôi không ăn thịt.", "Giữ /t/ cuối don't và meat, kéo dài /iː/ trong eat.", id = "d074dd3a05d971f5ba4c"),
            Phrase("This soup is delicious!", "Món súp này ngon quá!", "Nhấn âm giữa delicious, giọng hài lòng.", id = "bdae2a0f2f756e7f7faf"),
            Phrase("I'd like a cup of tea.", "Tôi muốn một tách trà.", "Nối cup of; giữ âm /p/ của cup.", id = "14bccbcd7d72cea40c2b"),
            Phrase("No sugar, please.", "Không đường, làm ơn.", "Sugar bắt đầu bằng /ʃ/, nhấn âm đầu.", id = "71e87a62c589a767df04"),
            Phrase("Can I have the bill, please?", "Cho tôi xin hóa đơn được không?", "Bill dùng âm /ɪ/ ngắn, giọng hỏi lịch sự.", id = "a90c116e470019002b03")
        ), listOf(
            ConversationTurn("What would you like to drink?", "Bạn muốn uống gì?", "I'd like some water, please.", "Cho tôi xin một ít nước.", id = "93b5933e96d40edd9f4c"),
            ConversationTurn("What food do you like?", "Bạn thích món ăn nào?", "I like chicken and rice.", "Tôi thích thịt gà và cơm.", id = "b72714f6eaf296b843f1"),
            ConversationTurn("Would you like sugar in your tea?", "Bạn muốn thêm đường vào trà không?", "No sugar, please.", "Không đường, làm ơn.", id = "1d46ecf8e624520ca896")
        )),
        Lesson("shopping", "Shopping", "Mua sắm", listOf(
            Phrase("How much is this shirt?", "Chiếc áo sơ mi này giá bao nhiêu?", "Nối much is, giữ cụm /ʃ/ đầu shirt.", id = "b92650aecac9e243945f"),
            Phrase("I'm looking for a blue shirt.", "Tôi đang tìm một chiếc áo sơ mi màu xanh.", "Giữ /ŋ/ cuối looking, kéo dài /uː/ trong blue.", id = "c13a14e3b1e8c9e5e205"),
            Phrase("Do you have a smaller size?", "Bạn có cỡ nhỏ hơn không?", "Size kết thúc bằng /z/, không đọc thành /s/.", id = "519793a4e35c341be511"),
            Phrase("Can I try it on?", "Tôi có thể thử nó không?", "Nối try it on thành một cụm liền mạch.", id = "1442c32134ebe6ee58fa"),
            Phrase("It's too expensive.", "Nó đắt quá.", "Nhấn âm thứ hai của expensive, kéo dài too.", id = "d18fef7557ad3ed5378b"),
            Phrase("I'll take this one.", "Tôi sẽ lấy cái này.", "Nối take this; giữ âm /ð/ trong this.", id = "f1b6d60ddb6573c8512b"),
            Phrase("Can I pay by card?", "Tôi có thể trả bằng thẻ không?", "Giữ /d/ cuối card, nhấn pay và card.", id = "73a8c75d9fc0608c5377"),
            Phrase("Thank you for your help.", "Cảm ơn bạn đã giúp đỡ.", "Giữ âm /p/ cuối help, nói với giọng cảm ơn.", id = "a5b98d0c736b7fa8ebf4")
        ), listOf(
            ConversationTurn("Can I help you?", "Tôi có thể giúp gì cho bạn?", "I'm looking for a blue shirt.", "Tôi đang tìm một chiếc áo sơ mi màu xanh.", id = "1f24795de945138fd3ca"),
            ConversationTurn("Is this size okay?", "Cỡ này có vừa không?", "Do you have a smaller size?", "Bạn có cỡ nhỏ hơn không?", id = "868a24fb8ac2ee11707b"),
            ConversationTurn("How would you like to pay?", "Bạn muốn thanh toán bằng cách nào?", "Can I pay by card?", "Tôi có thể trả bằng thẻ không?", id = "4d84eb6139790d65c7d9")
        )),
        Lesson("directions", "Directions", "Hỏi và chỉ đường", listOf(
            Phrase("Excuse me, where is the station?", "Xin lỗi, nhà ga ở đâu?", "Nhấn âm đầu station, mở lời nhẹ nhàng.", id = "7fa743afcb7e175f3b6e"),
            Phrase("Go straight ahead.", "Đi thẳng về phía trước.", "Không thêm âm vào /str/ trong straight; nhấn ahead.", id = "548cf4e63a1d065d213e"),
            Phrase("Turn left at the traffic lights.", "Rẽ trái ở đèn giao thông.", "Giữ /t/ cuối left, nhấn âm đầu traffic.", id = "86302f9d9d357f8a16a6"),
            Phrase("Turn right at the bank.", "Rẽ phải ở ngân hàng.", "Giữ /t/ cuối right, kết thúc bank bằng /ŋk/.", id = "2598c8b06429f76dbe3a"),
            Phrase("It's next to the supermarket.", "Nó ở cạnh siêu thị.", "Đọc chậm cụm next to, nhấn âm đầu supermarket.", id = "6276ce2d07a6efbe3909"),
            Phrase("It's across from the park.", "Nó ở đối diện công viên.", "Nhấn âm thứ hai across, giữ /k/ cuối park.", id = "c71a44a59e0114d9a947"),
            Phrase("Is it far from here?", "Nó có xa đây không?", "Nối is it; kéo dài nguyên âm trong far.", id = "ca30a5afb5127a970f56"),
            Phrase("Thank you. I understand.", "Cảm ơn. Tôi hiểu rồi.", "Ngắt nhẹ sau thank you; nhấn âm cuối understand.", id = "246a6c281f1ca397ab08")
        ), listOf(
            ConversationTurn("Excuse me, how do I get to the bank?", "Xin lỗi, tôi đến ngân hàng bằng cách nào?", "Go straight ahead.", "Đi thẳng về phía trước.", id = "31c264436eb061031e15"),
            ConversationTurn("Where do I turn?", "Tôi rẽ ở đâu?", "Turn left at the traffic lights.", "Rẽ trái ở đèn giao thông.", id = "1b0738f2c97b593a26db"),
            ConversationTurn("Where is the supermarket?", "Siêu thị ở đâu?", "It's across from the park.", "Nó ở đối diện công viên.", id = "2ce77cb4157d89b3fafa")
        )),
        Lesson("hobbies", "My Hobbies", "Sở thích của tôi", listOf(
            Phrase("I like listening to music.", "Tôi thích nghe nhạc.", "Listening có chữ t câm; nhấn âm đầu music.", id = "69abb3a19ec6dd315ded"),
            Phrase("I enjoy reading books.", "Tôi thích đọc sách.", "Nhấn âm sau enjoy, giữ /s/ cuối books.", id = "575bccf7e5b277dd8f93"),
            Phrase("I play football on Sundays.", "Tôi chơi bóng đá vào các Chủ nhật.", "Nhấn âm đầu football; Sundays kết thúc bằng /z/.", id = "28ed69c22314cdb91dc2"),
            Phrase("I like cooking with my family.", "Tôi thích nấu ăn cùng gia đình.", "Cooking dùng âm /ʊ/ ngắn; giữ /ŋ/ cuối.", id = "e8581d38e312cae06c50"),
            Phrase("I watch movies at home.", "Tôi xem phim ở nhà.", "Movies kết thúc bằng /z/; nối movies at.", id = "5c4f6826613913e65c22"),
            Phrase("I can swim.", "Tôi biết bơi.", "Không thêm nguyên âm vào /sw/ đầu swim.", id = "4dfec7b6fe858196c65d"),
            Phrase("I draw pictures in my free time.", "Tôi vẽ tranh trong thời gian rảnh.", "Nhấn âm đầu pictures, kéo dài /iː/ trong free.", id = "1a59aad5b881d50681af"),
            Phrase("Let's play together!", "Chúng ta cùng chơi nhé!", "Giữ /s/ cuối let's; giọng mời thân thiện.", id = "f930edb64a435acc1557")
        ), listOf(
            ConversationTurn("What do you like doing?", "Bạn thích làm gì?", "I like listening to music.", "Tôi thích nghe nhạc.", id = "dbb57009a2a28b499bb8"),
            ConversationTurn("What do you do on Sundays?", "Bạn làm gì vào các Chủ nhật?", "I play football on Sundays.", "Tôi chơi bóng đá vào các Chủ nhật.", id = "7a3d1325abb293dc0c27"),
            ConversationTurn("Can you swim?", "Bạn có biết bơi không?", "I can swim.", "Tôi biết bơi.", id = "06f58d32cca1370a25d9")
        )),
        Lesson("time", "Time & Dates", "Giờ và ngày tháng", listOf(
            Phrase("What time is it?", "Bây giờ là mấy giờ?", "Nối time is it, giữ /t/ cuối câu.", id = "cd61bf0cc67fc2233111"),
            Phrase("It's half past eight.", "Bây giờ là tám giờ rưỡi.", "Half có chữ l câm; nối past eight.", id = "238115931fe42fdcf789"),
            Phrase("Today is Monday.", "Hôm nay là thứ Hai.", "Nhấn âm đầu Monday, nối today is.", id = "1c729b249b61a429743e"),
            Phrase("My birthday is in June.", "Sinh nhật tôi vào tháng Sáu.", "Birthday có âm /θ/; June kết thúc bằng /n/.", id = "e9dd103caacfd82b7a23"),
            Phrase("The meeting is at two.", "Cuộc họp lúc hai giờ.", "Nối meeting is at, kéo dài /uː/ trong two.", id = "c5567aa2a61c027a4d2e"),
            Phrase("See you on Friday!", "Hẹn gặp bạn vào thứ Sáu!", "Nhấn âm đầu Friday; giọng hẹn gặp vui vẻ.", id = "2e50b1676ed7f7d035ce"),
            Phrase("I'm free this afternoon.", "Chiều nay tôi rảnh.", "Nhấn âm cuối afternoon; giữ /ð/ trong this.", id = "89306129dc2e6a903bbf"),
            Phrase("Let's meet at ten tomorrow.", "Hãy gặp nhau lúc mười giờ ngày mai.", "Nối meet at; nhấn ten và âm giữa tomorrow.", id = "67a2a43b3e1576bd1819")
        ), listOf(
            ConversationTurn("What time is it now?", "Bây giờ là mấy giờ?", "It's half past eight.", "Bây giờ là tám giờ rưỡi.", id = "1742f43b5de3205687a3"),
            ConversationTurn("When is your birthday?", "Sinh nhật bạn vào khi nào?", "My birthday is in June.", "Sinh nhật tôi vào tháng Sáu.", id = "62620d13ffd38e38d04b"),
            ConversationTurn("When can we meet?", "Khi nào chúng ta có thể gặp nhau?", "Let's meet at ten tomorrow.", "Hãy gặp nhau lúc mười giờ ngày mai.", id = "bc51c1cd5b3fbcc53c01")
        )),
        Lesson("travel", "Travel", "Du lịch", listOf(
            Phrase("I'd like a ticket to Hanoi.", "Tôi muốn một vé đến Hà Nội.", "Nhấn âm đầu ticket; nối like a.", id = "ee52cb6bd429ef26a826"),
            Phrase("Is this seat free?", "Chỗ ngồi này còn trống không?", "Seat dùng /iː/ dài; giữ /t/ cuối seat.", id = "3abbbd91e05fc05c9af9"),
            Phrase("I have a reservation.", "Tôi đã đặt chỗ.", "Nhấn âm tiết va trong reservation.", id = "51769290a6bb8e6c0c85"),
            Phrase("Can I see your passport, please?", "Cho tôi xem hộ chiếu của bạn được không?", "Nhấn âm đầu passport, dùng giọng hỏi lịch sự.", id = "e5309fc42eb634f4c27f"),
            Phrase("Where is the bus stop?", "Trạm xe buýt ở đâu?", "Giữ /s/ cuối bus và /p/ cuối stop.", id = "5b763a76d38ca86d3af9"),
            Phrase("What time does the train leave?", "Tàu khởi hành lúc mấy giờ?", "Giữ /v/ cuối leave, nhấn train và leave.", id = "d2ab7e3ff8170a00d556"),
            Phrase("I'd like a room for two nights.", "Tôi muốn một phòng trong hai đêm.", "Nights kết thúc bằng /ts/; kéo dài /uː/ trong room.", id = "74056f3f5fe54fc21e1d"),
            Phrase("Have a nice trip!", "Chúc bạn có chuyến đi vui vẻ!", "Giữ /p/ cuối trip; giọng chúc thân thiện.", id = "b1cee704b7e105acadf4")
        ), listOf(
            ConversationTurn("Where would you like to go?", "Bạn muốn đi đâu?", "I'd like a ticket to Hanoi.", "Tôi muốn một vé đến Hà Nội.", id = "c022fa54258553ea7c1b"),
            ConversationTurn("Do you have a reservation?", "Bạn đã đặt chỗ chưa?", "I have a reservation.", "Tôi đã đặt chỗ.", id = "bc2b5daf45a11a259bc8"),
            ConversationTurn("How long would you like to stay?", "Bạn muốn ở bao lâu?", "I'd like a room for two nights.", "Tôi muốn một phòng trong hai đêm.", id = "46b1bf1bece0758a014a")
        )),
        Lesson("conversation", "Real Conversation", "Giao tiếp thường ngày", listOf(
            Phrase("Could you say that again, please?", "Bạn có thể nói lại được không?", "Nối say that, giọng đề nghị lịch sự.", id = "1534c878b6ccec86d348"),
            Phrase("Please speak more slowly.", "Làm ơn nói chậm hơn.", "Nhấn âm đầu slowly; không thêm âm trước speak.", id = "4c8e6b7346d9192729b2"),
            Phrase("I don't understand.", "Tôi không hiểu.", "Nhấn âm cuối understand, giữ /d/ cuối.", id = "906248d9ffd63d8b8c54"),
            Phrase("How do you spell your name?", "Bạn đánh vần tên mình như thế nào?", "Giữ cụm /sp/ đầu spell, nối spell your.", id = "edeeedb79da0d54fbe2e"),
            Phrase("I'm learning English.", "Tôi đang học tiếng Anh.", "Giữ /ŋ/ cuối learning; nhấn âm đầu English.", id = "d8233c949473e75c2c2e"),
            Phrase("Can you help me, please?", "Bạn có thể giúp tôi không?", "Giữ /p/ cuối help, dùng giọng nhờ giúp.", id = "8266f9d07e530dee2fa9"),
            Phrase("Sorry, I'm late.", "Xin lỗi, tôi đến muộn.", "Giọng xin lỗi nhẹ; giữ /t/ cuối late.", id = "cf068eaeb9a2ce9cc48f"),
            Phrase("It was nice talking to you.", "Rất vui được trò chuyện với bạn.", "Nối talking to, giọng kết thúc thân thiện.", id = "7ebb30823026eeefeb2d")
        ), listOf(
            ConversationTurn("Hi! How is your English class?", "Chào bạn! Lớp tiếng Anh của bạn thế nào?", "I'm learning English.", "Tôi đang học tiếng Anh.", id = "4c219690fe59d5de0fc9"),
            ConversationTurn("Let's meet at a quarter past nine.", "Hãy gặp nhau lúc chín giờ mười lăm.", "Could you say that again, please?", "Bạn có thể nói lại được không?", id = "bd99c09cb76e85439d53"),
            ConversationTurn("Thanks for the chat. See you soon!", "Cảm ơn cuộc trò chuyện. Hẹn sớm gặp lại!", "It was nice talking to you.", "Rất vui được trò chuyện với bạn.", id = "574023d1f8114d662c0d")
        )),
    ).map(ExpandedLessons::append).map(MoreA1Practice::append) + A1NewTopics.all + A2Lessons.all

    val a1: List<Lesson> get() = all.filterNot { it.id.startsWith("a2_") }
    val a2: List<Lesson> get() = all.filter { it.id.startsWith("a2_") }
    fun levelOf(lesson: Lesson): String = if (lesson.id.startsWith("a2_")) "A2" else "A1"
}
