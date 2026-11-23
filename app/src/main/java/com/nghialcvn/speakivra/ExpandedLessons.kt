package com.nghialcvn.speakivra

/** Additive A1 practice pack. Existing lesson indices and audio clips remain unchanged.
 * New prompts use the existing on-device US-English TTS fallback until neural clips are reviewed.
 */
internal object ExpandedLessons {
    fun append(base: Lesson): Lesson {
        val extra = when (base.id) {
            "greetings" -> Pair(
                listOf(
                    Phrase("Good morning! How did you sleep?", "Chào buổi sáng! Bạn ngủ ngon không?", "Nối did you nhẹ và lên giọng cuối câu."),
                    Phrase("It's good to see you again.", "Thật vui khi gặp lại bạn.", "Nhấn good và again tự nhiên."),
                    Phrase("What do you do for work?", "Bạn làm nghề gì?", "Nối do you, nhấn work."),
                    Phrase("I work at a hotel.", "Tôi làm việc ở một khách sạn.", "Nối work at, giữ âm /k/ trong work.")
                ),
                listOf(
                    ConversationTurn("How did you sleep?", "Bạn ngủ có ngon không?", "Good morning! How did you sleep?", "Chào buổi sáng! Bạn ngủ ngon không?"),
                    ConversationTurn("Where do you work?", "Bạn làm việc ở đâu?", "I work at a hotel.", "Tôi làm việc ở một khách sạn.")
                )
            )
            "family" -> Pair(
                listOf(
                    Phrase("How many people are in your family?", "Gia đình bạn có bao nhiêu người?", "Nhấn how many và family."),
                    Phrase("There are four people in my family.", "Gia đình tôi có bốn người.", "Nối there are, nhấn four."),
                    Phrase("My parents live in another city.", "Bố mẹ tôi sống ở một thành phố khác.", "Nhấn parents và city."),
                    Phrase("We visit our grandparents on weekends.", "Chúng tôi thăm ông bà vào cuối tuần.", "Giữ /s/ cuối weekends.")
                ),
                listOf(
                    ConversationTurn("How many people are in your family?", "Gia đình bạn có bao nhiêu người?", "There are four people in my family.", "Gia đình tôi có bốn người."),
                    ConversationTurn("When do you visit your grandparents?", "Khi nào bạn thăm ông bà?", "We visit our grandparents on weekends.", "Chúng tôi thăm ông bà vào cuối tuần.")
                )
            )
            "routine" -> Pair(
                listOf(
                    Phrase("I usually get up early.", "Tôi thường dậy sớm.", "Nhấn usually và early."),
                    Phrase("I take a shower before breakfast.", "Tôi tắm trước khi ăn sáng.", "Nối take a và before breakfast."),
                    Phrase("I sometimes work at night.", "Thỉnh thoảng tôi làm việc vào ban đêm.", "Nhấn sometimes và night."),
                    Phrase("I relax after a long day.", "Tôi nghỉ ngơi sau một ngày dài.", "Nối relax after tự nhiên.")
                ),
                listOf(
                    ConversationTurn("Do you get up early?", "Bạn có dậy sớm không?", "I usually get up early.", "Tôi thường dậy sớm."),
                    ConversationTurn("What do you do after work?", "Bạn làm gì sau giờ làm?", "I relax after a long day.", "Tôi nghỉ ngơi sau một ngày dài.")
                )
            )
            "food" -> Pair(
                listOf(
                    Phrase("Do you have any vegetarian dishes?", "Bạn có món chay nào không?", "Nhấn vegetarian và dishes."),
                    Phrase("I'm allergic to peanuts.", "Tôi bị dị ứng với đậu phộng.", "Nhấn allergic và peanuts."),
                    Phrase("Could I have some more water?", "Cho tôi thêm chút nước được không?", "Nối could I, giọng lịch sự."),
                    Phrase("The food is a little spicy.", "Món ăn hơi cay.", "Nhấn little và spicy.")
                ),
                listOf(
                    ConversationTurn("Do you have any food allergies?", "Bạn có dị ứng thực phẩm không?", "I'm allergic to peanuts.", "Tôi bị dị ứng với đậu phộng."),
                    ConversationTurn("How is your food?", "Món ăn của bạn thế nào?", "The food is a little spicy.", "Món ăn hơi cay.")
                )
            )
            "shopping" -> Pair(
                listOf(
                    Phrase("Is there a discount today?", "Hôm nay có giảm giá không?", "Nhấn discount và today."),
                    Phrase("Do you have this in black?", "Bạn có mẫu này màu đen không?", "Nối have this và nhấn black."),
                    Phrase("Can I return this item?", "Tôi có thể trả lại món hàng này không?", "Nhấn return và item."),
                    Phrase("Could I get a receipt, please?", "Cho tôi xin hóa đơn được không?", "Nối could I và nhấn receipt.")
                ),
                listOf(
                    ConversationTurn("Would you like a receipt?", "Bạn có muốn nhận hóa đơn không?", "Could I get a receipt, please?", "Cho tôi xin hóa đơn được không?"),
                    ConversationTurn("What color would you like?", "Bạn muốn màu gì?", "Do you have this in black?", "Bạn có mẫu này màu đen không?")
                )
            )
            "directions" -> Pair(
                listOf(
                    Phrase("Is there a pharmacy nearby?", "Gần đây có hiệu thuốc không?", "Nhấn pharmacy và nearby."),
                    Phrase("It's about a ten-minute walk.", "Đi bộ khoảng mười phút.", "Nhấn ten-minute và walk."),
                    Phrase("Take the second street on the right.", "Rẽ vào con phố thứ hai bên phải.", "Nhấn second và right."),
                    Phrase("Could you show me on the map?", "Bạn chỉ giúp tôi trên bản đồ được không?", "Nối show me và nhấn map.")
                ),
                listOf(
                    ConversationTurn("How far is the pharmacy?", "Hiệu thuốc cách đây bao xa?", "It's about a ten-minute walk.", "Đi bộ khoảng mười phút."),
                    ConversationTurn("I can't find the place.", "Tôi không tìm thấy địa điểm đó.", "Could you show me on the map?", "Bạn chỉ giúp tôi trên bản đồ được không?")
                )
            )
            "hobbies" -> Pair(
                listOf(
                    Phrase("I like taking photos when I travel.", "Tôi thích chụp ảnh khi đi du lịch.", "Nhấn taking photos và travel."),
                    Phrase("I go cycling on weekends.", "Tôi đạp xe vào cuối tuần.", "Nhấn cycling và weekends."),
                    Phrase("I'm interested in learning new things.", "Tôi thích tìm hiểu những điều mới.", "Nối interested in nhẹ."),
                    Phrase("Would you like to join us?", "Bạn có muốn tham gia cùng chúng tôi không?", "Nối like to, lên giọng hỏi.")
                ),
                listOf(
                    ConversationTurn("What do you do on weekends?", "Bạn làm gì vào cuối tuần?", "I go cycling on weekends.", "Tôi đạp xe vào cuối tuần."),
                    ConversationTurn("Can I come with you?", "Tôi có thể đi cùng không?", "Would you like to join us?", "Bạn có muốn tham gia cùng chúng tôi không?")
                )
            )
            "time" -> Pair(
                listOf(
                    Phrase("What day is it today?", "Hôm nay là thứ mấy?", "Nối day is it nhẹ."),
                    Phrase("The appointment is next Tuesday.", "Lịch hẹn vào thứ Ba tuần tới.", "Nhấn appointment và Tuesday."),
                    Phrase("I'm available after three o'clock.", "Tôi rảnh sau ba giờ.", "Nhấn available và three."),
                    Phrase("Can we move it to tomorrow?", "Chúng ta dời sang ngày mai được không?", "Nối move it to.")
                ),
                listOf(
                    ConversationTurn("When are you available?", "Khi nào bạn rảnh?", "I'm available after three o'clock.", "Tôi rảnh sau ba giờ."),
                    ConversationTurn("Can we reschedule the meeting?", "Chúng ta đổi lịch họp được không?", "Can we move it to tomorrow?", "Chúng ta dời sang ngày mai được không?")
                )
            )
            "travel" -> Pair(
                listOf(
                    Phrase("Where can I check in?", "Tôi có thể làm thủ tục nhận phòng ở đâu?", "Nhấn check in rõ hai từ."),
                    Phrase("Does the room include breakfast?", "Phòng có bao gồm bữa sáng không?", "Nhấn include và breakfast."),
                    Phrase("My flight has been delayed.", "Chuyến bay của tôi bị hoãn.", "Nhấn flight và delayed."),
                    Phrase("Could you call a taxi for me?", "Bạn gọi giúp tôi một chiếc taxi được không?", "Nối call a, giọng lịch sự.")
                ),
                listOf(
                    ConversationTurn("How can I help you at the hotel?", "Tôi có thể giúp gì tại khách sạn?", "Where can I check in?", "Tôi có thể làm thủ tục nhận phòng ở đâu?"),
                    ConversationTurn("What happened to your flight?", "Chuyến bay của bạn gặp chuyện gì?", "My flight has been delayed.", "Chuyến bay của tôi bị hoãn.")
                )
            )
            "conversation" -> Pair(
                listOf(
                    Phrase("I'm sorry, I didn't catch that.", "Xin lỗi, tôi chưa nghe rõ.", "Nhấn didn't catch that."),
                    Phrase("Could you give me an example?", "Bạn có thể cho tôi một ví dụ không?", "Nối give me và nhấn example."),
                    Phrase("Let me think for a moment.", "Để tôi suy nghĩ một chút.", "Nối let me think."),
                    Phrase("That sounds like a good idea!", "Nghe có vẻ là một ý hay!", "Nhấn sounds và good idea.")
                ),
                listOf(
                    ConversationTurn("Do you understand this word?", "Bạn hiểu từ này không?", "Could you give me an example?", "Bạn có thể cho tôi một ví dụ không?"),
                    ConversationTurn("Should we practice together?", "Chúng ta luyện tập cùng nhau nhé?", "That sounds like a good idea!", "Nghe có vẻ là một ý hay!")
                )
            )
            else -> return base
        }
        return base.copy(phrases = base.phrases + extra.first, conversation = base.conversation + extra.second)
    }
}
