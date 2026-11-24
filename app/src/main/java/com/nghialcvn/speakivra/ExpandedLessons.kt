package com.nghialcvn.speakivra

/** Additive A1 practice pack. Existing lesson indices and audio clips remain unchanged.
 * New prompts use the existing on-device US-English TTS fallback until neural clips are reviewed.
 */
internal object ExpandedLessons {
    fun append(base: Lesson): Lesson {
        val extra = when (base.id) {
            "greetings" -> Pair(
                listOf(
                    Phrase("I slept well, thank you.", "Tôi ngủ ngon, cảm ơn bạn.", "Nhấn slept well và thank you.", id = "7f83385d3323e6554311"),
                    Phrase("It's good to see you again.", "Thật vui khi gặp lại bạn.", "Nhấn good và again tự nhiên.", id = "a81ae3d55e4a8463ee69"),
                    Phrase("What do you do for work?", "Bạn làm nghề gì?", "Nối do you, nhấn work.", id = "5d30c21e973a830e66d5"),
                    Phrase("I work at a hotel.", "Tôi làm việc ở một khách sạn.", "Nối work at, giữ âm /k/ trong work.", id = "df239fd46dd53fd2eda8")
                ),
                listOf(
                    ConversationTurn("How did you sleep?", "Bạn ngủ có ngon không?", "I slept well, thank you.", "Tôi ngủ ngon, cảm ơn bạn.", id = "677fa13016ed6019c8da"),
                    ConversationTurn("Where do you work?", "Bạn làm việc ở đâu?", "I work at a hotel.", "Tôi làm việc ở một khách sạn.", id = "0201d4d8b5a557144a5a")
                )
            )
            "family" -> Pair(
                listOf(
                    Phrase("How many people are in your family?", "Gia đình bạn có bao nhiêu người?", "Nhấn how many và family.", id = "22abe7814243a9b86dac"),
                    Phrase("There are four people in my family.", "Gia đình tôi có bốn người.", "Nối there are, nhấn four.", id = "912b04e581e14da6c27a"),
                    Phrase("My parents live in another city.", "Bố mẹ tôi sống ở một thành phố khác.", "Nhấn parents và city.", id = "4d388164ca5e761d5bad"),
                    Phrase("We visit our grandparents on weekends.", "Chúng tôi thăm ông bà vào cuối tuần.", "Giữ /s/ cuối weekends.", id = "c6363f9191f51bc8bb49")
                ),
                listOf(
                    ConversationTurn("How many people are in your family?", "Gia đình bạn có bao nhiêu người?", "There are four people in my family.", "Gia đình tôi có bốn người.", id = "22abe7814243a9b86dac"),
                    ConversationTurn("When do you visit your grandparents?", "Khi nào bạn thăm ông bà?", "We visit our grandparents on weekends.", "Chúng tôi thăm ông bà vào cuối tuần.", id = "3a40bbb5e6cb2a6d6611")
                )
            )
            "routine" -> Pair(
                listOf(
                    Phrase("I usually get up early.", "Tôi thường dậy sớm.", "Nhấn usually và early.", id = "a7b8198b31f07e5d4c38"),
                    Phrase("I take a shower before breakfast.", "Tôi tắm trước khi ăn sáng.", "Nối take a và before breakfast.", id = "a5e94d20b4019632e23b"),
                    Phrase("I sometimes work at night.", "Thỉnh thoảng tôi làm việc vào ban đêm.", "Nhấn sometimes và night.", id = "bd82e733e9f8f1bb64b1"),
                    Phrase("I relax after a long day.", "Tôi nghỉ ngơi sau một ngày dài.", "Nối relax after tự nhiên.", id = "46879aa382bf79fef8c3")
                ),
                listOf(
                    ConversationTurn("Do you get up early?", "Bạn có dậy sớm không?", "I usually get up early.", "Tôi thường dậy sớm.", id = "cf1e5612810b05beb0cb"),
                    ConversationTurn("What do you do after work?", "Bạn làm gì sau giờ làm?", "I relax after a long day.", "Tôi nghỉ ngơi sau một ngày dài.", id = "ce1e3bc38a8447a77c04")
                )
            )
            "food" -> Pair(
                listOf(
                    Phrase("Do you have any vegetarian dishes?", "Bạn có món chay nào không?", "Nhấn vegetarian và dishes.", id = "c16735265fa1285bbc69"),
                    Phrase("I'm allergic to peanuts.", "Tôi bị dị ứng với đậu phộng.", "Nhấn allergic và peanuts.", id = "84dcb7c274909558057e"),
                    Phrase("Could I have some more water?", "Cho tôi thêm chút nước được không?", "Nối could I, giọng lịch sự.", id = "37be726bd8fb457d28b8"),
                    Phrase("The food is a little spicy.", "Món ăn hơi cay.", "Nhấn little và spicy.", id = "f2cf259b3030f14dc988")
                ),
                listOf(
                    ConversationTurn("Do you have any food allergies?", "Bạn có dị ứng thực phẩm không?", "I'm allergic to peanuts.", "Tôi bị dị ứng với đậu phộng.", id = "f8e7c542990f1658165f"),
                    ConversationTurn("How is your food?", "Món ăn của bạn thế nào?", "The food is a little spicy.", "Món ăn hơi cay.", id = "6b41907fb853a7d13c63")
                )
            )
            "shopping" -> Pair(
                listOf(
                    Phrase("Is there a discount today?", "Hôm nay có giảm giá không?", "Nhấn discount và today.", id = "9b5ae00691a76906130a"),
                    Phrase("Do you have this in black?", "Bạn có mẫu này màu đen không?", "Nối have this và nhấn black.", id = "1045d9abab65173d1002"),
                    Phrase("Can I return this item?", "Tôi có thể trả lại món hàng này không?", "Nhấn return và item.", id = "93b8a786611c2da4b647"),
                    Phrase("Could I get a receipt, please?", "Cho tôi xin hóa đơn được không?", "Nối could I và nhấn receipt.", id = "b9462adb598fd238f0d2")
                ),
                listOf(
                    ConversationTurn("Would you like a receipt?", "Bạn có muốn nhận hóa đơn không?", "Could I get a receipt, please?", "Cho tôi xin hóa đơn được không?", id = "4d78c8be4fdba967fca5"),
                    ConversationTurn("What color would you like?", "Bạn muốn màu gì?", "Do you have this in black?", "Bạn có mẫu này màu đen không?", id = "4f0d94d8fd76bfc1c8cd")
                )
            )
            "directions" -> Pair(
                listOf(
                    Phrase("Is there a pharmacy nearby?", "Gần đây có hiệu thuốc không?", "Nhấn pharmacy và nearby.", id = "aa5c578d4929c1a85eed"),
                    Phrase("It's about a ten-minute walk.", "Đi bộ khoảng mười phút.", "Nhấn ten-minute và walk.", id = "cf857773cd28042246f3"),
                    Phrase("Take the second street on the right.", "Rẽ vào con phố thứ hai bên phải.", "Nhấn second và right.", id = "918133bdd6be3405a196"),
                    Phrase("Could you show me on the map?", "Bạn chỉ giúp tôi trên bản đồ được không?", "Nối show me và nhấn map.", id = "e50423f1bf6a7db0696c")
                ),
                listOf(
                    ConversationTurn("How far is the pharmacy?", "Hiệu thuốc cách đây bao xa?", "It's about a ten-minute walk.", "Đi bộ khoảng mười phút.", id = "254d1046a13f8ce07059"),
                    ConversationTurn("I can't find the place.", "Tôi không tìm thấy địa điểm đó.", "Could you show me on the map?", "Bạn chỉ giúp tôi trên bản đồ được không?", id = "cd8624bdd95c566746b6")
                )
            )
            "hobbies" -> Pair(
                listOf(
                    Phrase("I like taking photos when I travel.", "Tôi thích chụp ảnh khi đi du lịch.", "Nhấn taking photos và travel.", id = "6af14852ac8e22a27675"),
                    Phrase("I go cycling on weekends.", "Tôi đạp xe vào cuối tuần.", "Nhấn cycling và weekends.", id = "35a4cdf749a4d5460bb2"),
                    Phrase("I'm interested in learning new things.", "Tôi thích tìm hiểu những điều mới.", "Nối interested in nhẹ.", id = "e91250b382db82116748"),
                    Phrase("Would you like to join us?", "Bạn có muốn tham gia cùng chúng tôi không?", "Nối like to, lên giọng hỏi.", id = "884b32aa1457cab2c415")
                ),
                listOf(
                    ConversationTurn("What do you do on weekends?", "Bạn làm gì vào cuối tuần?", "I go cycling on weekends.", "Tôi đạp xe vào cuối tuần.", id = "fdd64ef35b1c712062d7"),
                    ConversationTurn("Can I come with you?", "Tôi có thể đi cùng không?", "Would you like to join us?", "Bạn có muốn tham gia cùng chúng tôi không?", id = "7d3cd057c66553bdd11e")
                )
            )
            "time" -> Pair(
                listOf(
                    Phrase("What day is it today?", "Hôm nay là thứ mấy?", "Nối day is it nhẹ.", id = "c42e1e4ac7da5f018a15"),
                    Phrase("The appointment is next Tuesday.", "Lịch hẹn vào thứ Ba tuần tới.", "Nhấn appointment và Tuesday.", id = "e9ae0466f453552c2366"),
                    Phrase("I'm available after three o'clock.", "Tôi rảnh sau ba giờ.", "Nhấn available và three.", id = "5cedfd201fc41c66e23a"),
                    Phrase("Can we move it to tomorrow?", "Chúng ta dời sang ngày mai được không?", "Nối move it to.", id = "ad913e07f8582319d20e")
                ),
                listOf(
                    ConversationTurn("When are you available?", "Khi nào bạn rảnh?", "I'm available after three o'clock.", "Tôi rảnh sau ba giờ.", id = "46cc985b7119e62ba4fe"),
                    ConversationTurn("Can we reschedule the meeting?", "Chúng ta đổi lịch họp được không?", "Can we move it to tomorrow?", "Chúng ta dời sang ngày mai được không?", id = "19e426902c10a561f065")
                )
            )
            "travel" -> Pair(
                listOf(
                    Phrase("Where can I check in?", "Tôi có thể làm thủ tục nhận phòng ở đâu?", "Nhấn check in rõ hai từ.", id = "75f738cf6ca415d5a165"),
                    Phrase("Does the room include breakfast?", "Phòng có bao gồm bữa sáng không?", "Nhấn include và breakfast.", id = "d5c769bb362b27fb3863"),
                    Phrase("My flight has been delayed.", "Chuyến bay của tôi bị hoãn.", "Nhấn flight và delayed.", id = "ea67dcd30217a78de110"),
                    Phrase("Could you call a taxi for me?", "Bạn gọi giúp tôi một chiếc taxi được không?", "Nối call a, giọng lịch sự.", id = "0b8bbccc6e941f6fd5fe")
                ),
                listOf(
                    ConversationTurn("How can I help you at the hotel?", "Tôi có thể giúp gì tại khách sạn?", "Where can I check in?", "Tôi có thể làm thủ tục nhận phòng ở đâu?", id = "e4182bd0f79213022499"),
                    ConversationTurn("What happened to your flight?", "Chuyến bay của bạn gặp chuyện gì?", "My flight has been delayed.", "Chuyến bay của tôi bị hoãn.", id = "416043e259233854aae4")
                )
            )
            "conversation" -> Pair(
                listOf(
                    Phrase("I'm sorry, I didn't catch that.", "Xin lỗi, tôi chưa nghe rõ.", "Nhấn didn't catch that.", id = "bf58f1bfe54edba7e367"),
                    Phrase("Could you give me an example?", "Bạn có thể cho tôi một ví dụ không?", "Nối give me và nhấn example.", id = "6c7778a0e92ae9337970"),
                    Phrase("Let me think for a moment.", "Để tôi suy nghĩ một chút.", "Nối let me think.", id = "841eaadef0c932746119"),
                    Phrase("That sounds like a good idea!", "Nghe có vẻ là một ý hay!", "Nhấn sounds và good idea.", id = "ea1c11819f9e06cc5bb9")
                ),
                listOf(
                    ConversationTurn("Do you understand this word?", "Bạn hiểu từ này không?", "Could you give me an example?", "Bạn có thể cho tôi một ví dụ không?", id = "f741d2d27c333f903a01"),
                    ConversationTurn("Should we practice together?", "Chúng ta luyện tập cùng nhau nhé?", "That sounds like a good idea!", "Nghe có vẻ là một ý hay!", id = "8fde81fa178810690261")
                )
            )
            else -> return base
        }
        return base.copy(phrases = base.phrases + extra.first, conversation = base.conversation + extra.second)
    }
}
