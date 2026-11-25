package com.nghialcvn.speakivra

/** Authored bilingual A2 expansion. Legacy order/IDs are retained; appended IDs never depend on list position at runtime. */
internal object ExpandedA2Lessons {
    fun expand(original: List<Lesson>): List<Lesson> = original.map { lesson ->
        when (lesson.id) {
            "a2_experiences" -> a2_experiences(lesson)
            "a2_plans" -> a2_plans(lesson)
            "a2_opinions" -> a2_opinions(lesson)
            "a2_problems" -> a2_problems(lesson)
            "a2_travel" -> a2_travel(lesson)
            else -> lesson
        }
    } + listOf(a2_health(), a2_work(), a2_learning(), a2_community(), a2_stories())

    private fun a2_experiences(original: Lesson): Lesson {
        val phrases = original.phrases + listOf(
            Phrase("I have just joined a cooking class.", "Tôi vừa tham gia một lớp nấu ăn.", "Đọc rõ have, has và âm cuối động từ; nhấn thời gian hoặc trải nghiệm chính.", id = "a2_experiences_phrase_013"),
            Phrase("We explored the old town on foot.", "Chúng tôi đi bộ khám phá phố cổ.", "Đọc rõ have, has và âm cuối động từ; nhấn thời gian hoặc trải nghiệm chính.", id = "a2_experiences_phrase_014"),
            Phrase("My brother has lived abroad for a year.", "Anh trai tôi đã sống ở nước ngoài được một năm.", "Đọc rõ have, has và âm cuối động từ; nhấn thời gian hoặc trải nghiệm chính.", id = "a2_experiences_phrase_015"),
            Phrase("I took my first train journey yesterday.", "Hôm qua tôi đi tàu hỏa lần đầu tiên.", "Đọc rõ have, has và âm cuối động từ; nhấn thời gian hoặc trải nghiệm chính.", id = "a2_experiences_phrase_016"),
            Phrase("Have you ever stayed with a local family?", "Bạn đã từng ở cùng một gia đình địa phương chưa?", "Đọc rõ have, has và âm cuối động từ; nhấn thời gian hoặc trải nghiệm chính.", id = "a2_experiences_phrase_017"),
            Phrase("I used to spend summers with my grandparents.", "Trước đây tôi thường nghỉ hè với ông bà.", "Đọc rõ have, has và âm cuối động từ; nhấn thời gian hoặc trải nghiệm chính.", id = "a2_experiences_phrase_018"),
            Phrase("I felt nervous at first, but everyone was kind.", "Lúc đầu tôi thấy lo, nhưng mọi người đều tốt bụng.", "Ngắt nhẹ trước but, so hoặc when; giữ rõ động từ quá khứ.", id = "a2_experiences_phrase_019"),
            Phrase("We missed the bus, so we walked back to the hotel.", "Chúng tôi lỡ xe buýt nên đã đi bộ về khách sạn.", "Ngắt nhẹ trước but, so hoặc when; giữ rõ động từ quá khứ.", id = "a2_experiences_phrase_020"),
            Phrase("I have tried this dish before, but I can't remember its name.", "Tôi đã thử món này rồi, nhưng không nhớ tên món.", "Ngắt nhẹ trước but, so hoặc when; giữ rõ động từ quá khứ.", id = "a2_experiences_phrase_021"),
            Phrase("When I visited the village, a farmer showed me around.", "Khi tôi đến thăm ngôi làng, một bác nông dân dẫn tôi đi xem.", "Ngắt nhẹ trước but, so hoặc when; giữ rõ động từ quá khứ.", id = "a2_experiences_phrase_022"),
            Phrase("I didn't understand the guide, so I asked her to speak slowly.", "Tôi không hiểu hướng dẫn viên nên đã nhờ cô ấy nói chậm.", "Ngắt nhẹ trước but, so hoặc when; giữ rõ động từ quá khứ.", id = "a2_experiences_phrase_023"),
            Phrase("My favorite memory is watching the sunset with my family.", "Kỷ niệm tôi thích nhất là ngắm hoàng hôn cùng gia đình.", "Ngắt nhẹ trước but, so hoặc when; giữ rõ động từ quá khứ.", id = "a2_experiences_phrase_024"),
            Phrase("Although the journey was long, we enjoyed talking to the other passengers.", "Mặc dù hành trình dài, chúng tôi vẫn thích trò chuyện với những hành khách khác.", "Chia câu thành cụm ý; đọc nhẹ từ nối, nhấn sự kiện và thời gian chính.", id = "a2_experiences_phrase_025"),
            Phrase("I have become more confident because I have spoken English with several visitors.", "Tôi đã tự tin hơn vì đã nói tiếng Anh với vài du khách.", "Chia câu thành cụm ý; đọc nhẹ từ nối, nhấn sự kiện và thời gian chính.", id = "a2_experiences_phrase_026"),
            Phrase("While we were waiting for the train, we met a couple from Australia.", "Trong lúc chờ tàu, chúng tôi gặp một cặp đôi đến từ Úc.", "Chia câu thành cụm ý; đọc nhẹ từ nối, nhấn sự kiện và thời gian chính.", id = "a2_experiences_phrase_027"),
            Phrase("I have kept the photos on my phone so I can show them to my friends.", "Tôi giữ ảnh trong điện thoại để có thể cho bạn bè xem.", "Chia câu thành cụm ý; đọc nhẹ từ nối, nhấn sự kiện và thời gian chính.", id = "a2_experiences_phrase_028"),
            Phrase("The first time I travelled alone, I called my parents when I arrived safely.", "Lần đầu đi du lịch một mình, tôi đã gọi cho bố mẹ khi đến nơi an toàn.", "Chia câu thành cụm ý; đọc nhẹ từ nối, nhấn sự kiện và thời gian chính.", id = "a2_experiences_phrase_029"),
            Phrase("If I visit that town again, I would like to spend more time with the local people.", "Nếu lại đến thị trấn đó, tôi muốn dành thêm thời gian với người dân địa phương.", "Chia câu thành cụm ý; đọc nhẹ từ nối, nhấn sự kiện và thời gian chính.", id = "a2_experiences_phrase_030"),
        )
        val turns = original.conversation + listOf(
            turn("a2_experiences_turn_006", "What have you just started doing?", "Bạn vừa bắt đầu làm việc gì?", phrases[12]),
            turn("a2_experiences_turn_007", "How long has your brother lived abroad?", "Anh trai bạn đã sống ở nước ngoài bao lâu?", phrases[14]),
            turn("a2_experiences_turn_008", "What would you like to ask about staying with local people?", "Bạn muốn hỏi gì về việc ở cùng người dân địa phương?", phrases[16]),
            turn("a2_experiences_turn_009", "How did you feel when the class started?", "Bạn cảm thấy thế nào khi lớp học bắt đầu?", phrases[18]),
            turn("a2_experiences_turn_010", "Do you remember the name of that dish?", "Bạn có nhớ tên món ăn đó không?", phrases[20]),
            turn("a2_experiences_turn_011", "What did you do when the guide spoke too quickly?", "Bạn đã làm gì khi hướng dẫn viên nói quá nhanh?", phrases[22]),
            turn("a2_experiences_turn_012", "Did you enjoy the journey even though it took a long time?", "Bạn có thích hành trình đó dù mất nhiều thời gian không?", phrases[24]),
            turn("a2_experiences_turn_013", "Who did you meet while you were waiting?", "Bạn đã gặp ai trong lúc chờ?", phrases[26]),
            turn("a2_experiences_turn_014", "What did you do after arriving on your first solo trip?", "Bạn đã làm gì sau khi đến nơi trong chuyến đi một mình đầu tiên?", phrases[28]),
            turn("a2_experiences_turn_015", "What would you like to do if you go back?", "Bạn muốn làm gì nếu quay lại?", phrases[29]),
        )
        return original.copy(phrases = phrases, conversation = turns)
    }

    private fun a2_plans(original: Lesson): Lesson {
        val phrases = original.phrases + listOf(
            Phrase("I am planning a short trip next month.", "Tôi đang lên kế hoạch cho một chuyến đi ngắn vào tháng tới.", "Nhấn thời gian hẹn; nối âm trong going to và would you.", id = "a2_plans_phrase_013"),
            Phrase("Let's choose a place near the station.", "Chúng ta hãy chọn một địa điểm gần nhà ga.", "Nhấn thời gian hẹn; nối âm trong going to và would you.", id = "a2_plans_phrase_014"),
            Phrase("Would Saturday morning be convenient for you?", "Sáng thứ Bảy có tiện cho bạn không?", "Nhấn thời gian hẹn; nối âm trong going to và would you.", id = "a2_plans_phrase_015"),
            Phrase("I have to finish my homework before dinner.", "Tôi phải làm xong bài tập trước bữa tối.", "Nhấn thời gian hẹn; nối âm trong going to và would you.", id = "a2_plans_phrase_016"),
            Phrase("We can invite a few friends from work.", "Chúng ta có thể mời vài người bạn ở chỗ làm.", "Nhấn thời gian hẹn; nối âm trong going to và would you.", id = "a2_plans_phrase_017"),
            Phrase("Please remind me to bring the tickets tomorrow.", "Ngày mai hãy nhắc tôi mang vé nhé.", "Nhấn thời gian hẹn; nối âm trong going to và would you.", id = "a2_plans_phrase_018"),
            Phrase("I can pick you up if you don't have a car.", "Tôi có thể đón bạn nếu bạn không có ô tô.", "Ngắt nhẹ giữa kế hoạch và lý do; giữ nhịp đều trong câu hỏi lịch sự.", id = "a2_plans_phrase_019"),
            Phrase("We are going to have lunch after we visit the museum.", "Chúng ta sẽ ăn trưa sau khi tham quan bảo tàng.", "Ngắt nhẹ giữa kế hoạch và lý do; giữ nhịp đều trong câu hỏi lịch sự.", id = "a2_plans_phrase_020"),
            Phrase("I haven't decided where to stay, so I'm comparing a few hotels.", "Tôi chưa quyết định ở đâu nên đang so sánh vài khách sạn.", "Ngắt nhẹ giữa kế hoạch và lý do; giữ nhịp đều trong câu hỏi lịch sự.", id = "a2_plans_phrase_021"),
            Phrase("Could you tell me how many people are coming to the party?", "Bạn có thể cho tôi biết có bao nhiêu người đến dự tiệc không?", "Ngắt nhẹ giữa kế hoạch và lý do; giữ nhịp đều trong câu hỏi lịch sự.", id = "a2_plans_phrase_022"),
            Phrase("I'll call the restaurant when you confirm the number of guests.", "Tôi sẽ gọi nhà hàng khi bạn xác nhận số khách.", "Ngắt nhẹ giữa kế hoạch và lý do; giữ nhịp đều trong câu hỏi lịch sự.", id = "a2_plans_phrase_023"),
            Phrase("We should take an earlier train because the station may be busy.", "Chúng ta nên đi chuyến tàu sớm hơn vì nhà ga có thể đông.", "Ngắt nhẹ giữa kế hoạch và lý do; giữ nhịp đều trong câu hỏi lịch sự.", id = "a2_plans_phrase_024"),
            Phrase("If you finish work early, we can meet outside the library at half past five.", "Nếu bạn tan làm sớm, chúng ta có thể gặp nhau ngoài thư viện lúc năm giờ rưỡi.", "Ngắt sau mệnh đề if, when hoặc before; nhấn giờ hẹn và việc cần làm.", id = "a2_plans_phrase_025"),
            Phrase("I'm going to make a list so that we don't forget anything important for the trip.", "Tôi sẽ lập danh sách để chúng ta không quên đồ quan trọng cho chuyến đi.", "Ngắt sau mệnh đề if, when hoặc before; nhấn giờ hẹn và việc cần làm.", id = "a2_plans_phrase_026"),
            Phrase("Before we book the tickets, let's check whether everyone is free on the same day.", "Trước khi đặt vé, hãy kiểm tra xem mọi người có cùng rảnh vào ngày đó không.", "Ngắt sau mệnh đề if, when hoặc before; nhấn giờ hẹn và việc cần làm.", id = "a2_plans_phrase_027"),
            Phrase("We can have the picnic in the park, but we'll need another plan if it rains.", "Chúng ta có thể dã ngoại trong công viên, nhưng cần phương án khác nếu trời mưa.", "Ngắt sau mệnh đề if, when hoặc before; nhấn giờ hẹn và việc cần làm.", id = "a2_plans_phrase_028"),
            Phrase("I would rather leave on Friday evening because it gives us more time to explore the city.", "Tôi muốn đi vào tối thứ Sáu hơn vì như vậy có thêm thời gian khám phá thành phố.", "Ngắt sau mệnh đề if, when hoặc before; nhấn giờ hẹn và việc cần làm.", id = "a2_plans_phrase_029"),
            Phrase("When you have checked the timetable, please send me a message so I can book our seats.", "Khi đã kiểm tra lịch tàu, hãy nhắn cho tôi để tôi đặt chỗ cho chúng ta.", "Ngắt sau mệnh đề if, when hoặc before; nhấn giờ hẹn và việc cần làm.", id = "a2_plans_phrase_030"),
        )
        val turns = original.conversation + listOf(
            turn("a2_plans_turn_006", "What are you planning for next month?", "Bạn đang lên kế hoạch gì cho tháng tới?", phrases[12]),
            turn("a2_plans_turn_007", "Would you like to suggest a time for our meeting?", "Bạn muốn đề xuất giờ gặp mặt không?", phrases[14]),
            turn("a2_plans_turn_008", "Who else can we invite?", "Chúng ta có thể mời thêm ai?", phrases[16]),
            turn("a2_plans_turn_009", "How can you help me get there without a car?", "Bạn có thể giúp tôi đến đó thế nào nếu tôi không có ô tô?", phrases[18]),
            turn("a2_plans_turn_010", "Have you chosen a hotel yet?", "Bạn đã chọn khách sạn chưa?", phrases[20]),
            turn("a2_plans_turn_011", "When will you contact the restaurant?", "Khi nào bạn sẽ liên hệ nhà hàng?", phrases[22]),
            turn("a2_plans_turn_012", "What can we do if I leave work early?", "Chúng ta có thể làm gì nếu tôi tan làm sớm?", phrases[24]),
            turn("a2_plans_turn_013", "What should we check before buying the tickets?", "Chúng ta nên kiểm tra gì trước khi mua vé?", phrases[26]),
            turn("a2_plans_turn_014", "Why would you prefer to travel on Friday?", "Tại sao bạn muốn đi vào thứ Sáu hơn?", phrases[28]),
            turn("a2_plans_turn_015", "What would you like me to do after checking the timetable?", "Bạn muốn tôi làm gì sau khi kiểm tra lịch tàu?", phrases[29]),
        )
        return original.copy(phrases = phrases, conversation = turns)
    }

    private fun a2_opinions(original: Lesson): Lesson {
        val phrases = original.phrases + listOf(
            Phrase("I find this chair more comfortable.", "Tôi thấy chiếc ghế này thoải mái hơn.", "Nhấn từ so sánh và tính từ chính; giữ rõ âm cuối better, cheaper.", id = "a2_opinions_phrase_013"),
            Phrase("The blue jacket fits me better.", "Chiếc áo khoác màu xanh vừa với tôi hơn.", "Nhấn từ so sánh và tính từ chính; giữ rõ âm cuối better, cheaper.", id = "a2_opinions_phrase_014"),
            Phrase("For me, the location is more important.", "Với tôi, vị trí quan trọng hơn.", "Nhấn từ so sánh và tính từ chính; giữ rõ âm cuối better, cheaper.", id = "a2_opinions_phrase_015"),
            Phrase("I would rather walk than take a taxi.", "Tôi thích đi bộ hơn là đi taxi.", "Nhấn từ so sánh và tính từ chính; giữ rõ âm cuối better, cheaper.", id = "a2_opinions_phrase_016"),
            Phrase("Which of these courses is easier to follow?", "Khóa nào trong số này dễ theo học hơn?", "Nhấn từ so sánh và tính từ chính; giữ rõ âm cuối better, cheaper.", id = "a2_opinions_phrase_017"),
            Phrase("I see your point, but I prefer the first one.", "Tôi hiểu ý bạn, nhưng tôi thích cái đầu tiên hơn.", "Nhấn từ so sánh và tính từ chính; giữ rõ âm cuối better, cheaper.", id = "a2_opinions_phrase_018"),
            Phrase("The train is faster, but the bus costs much less.", "Tàu hỏa nhanh hơn, nhưng xe buýt rẻ hơn nhiều.", "Đổi trọng âm nhẹ giữa hai lựa chọn; ngắt trước but hoặc although.", id = "a2_opinions_phrase_019"),
            Phrase("I think we should ask the others before making a decision.", "Tôi nghĩ chúng ta nên hỏi những người khác trước khi quyết định.", "Đổi trọng âm nhẹ giữa hai lựa chọn; ngắt trước but hoặc although.", id = "a2_opinions_phrase_020"),
            Phrase("This apartment is smaller than ours, although it has a nicer view.", "Căn hộ này nhỏ hơn căn của chúng tôi, dù có tầm nhìn đẹp hơn.", "Đổi trọng âm nhẹ giữa hai lựa chọn; ngắt trước but hoặc although.", id = "a2_opinions_phrase_021"),
            Phrase("I like the idea because it would save us both time and money.", "Tôi thích ý tưởng đó vì nó sẽ tiết kiệm cả thời gian lẫn tiền bạc.", "Đổi trọng âm nhẹ giữa hai lựa chọn; ngắt trước but hoặc although.", id = "a2_opinions_phrase_022"),
            Phrase("The meal was tasty, but the service was slower than last time.", "Bữa ăn ngon, nhưng phục vụ chậm hơn lần trước.", "Đổi trọng âm nhẹ giữa hai lựa chọn; ngắt trước but hoặc although.", id = "a2_opinions_phrase_023"),
            Phrase("Could you tell me why you think the second option is better?", "Bạn có thể cho tôi biết tại sao bạn nghĩ lựa chọn thứ hai tốt hơn không?", "Đổi trọng âm nhẹ giữa hai lựa chọn; ngắt trước but hoặc although.", id = "a2_opinions_phrase_024"),
            Phrase("Although the cheaper phone looks attractive, I think the battery life is more important.", "Mặc dù điện thoại rẻ hơn trông hấp dẫn, tôi nghĩ thời lượng pin quan trọng hơn.", "Chia rõ ý kiến và lý do; nhấn điểm khác nhau giữa hai lựa chọn.", id = "a2_opinions_phrase_025"),
            Phrase("I agree that online classes are convenient, but I learn better when I meet the teacher.", "Tôi đồng ý lớp trực tuyến tiện lợi, nhưng tôi học tốt hơn khi gặp giáo viên.", "Chia rõ ý kiến và lý do; nhấn điểm khác nhau giữa hai lựa chọn.", id = "a2_opinions_phrase_026"),
            Phrase("If we share a taxi with two friends, it won't be much more expensive than the bus.", "Nếu đi chung taxi với hai người bạn, chi phí sẽ không đắt hơn xe buýt nhiều.", "Chia rõ ý kiến và lý do; nhấn điểm khác nhau giữa hai lựa chọn.", id = "a2_opinions_phrase_027"),
            Phrase("The restaurant near the river is quieter, so it may be a better place for our conversation.", "Nhà hàng gần sông yên tĩnh hơn nên có thể phù hợp hơn cho cuộc trò chuyện của chúng ta.", "Chia rõ ý kiến và lý do; nhấn điểm khác nhau giữa hai lựa chọn.", id = "a2_opinions_phrase_028"),
            Phrase("I used to prefer busy cities, but now I enjoy places where I can spend time outdoors.", "Trước đây tôi thích thành phố nhộn nhịp, nhưng giờ tôi thích nơi có thể dành thời gian ngoài trời.", "Chia rõ ý kiến và lý do; nhấn điểm khác nhau giữa hai lựa chọn.", id = "a2_opinions_phrase_029"),
            Phrase("Let's try both activities before we decide which one we would like to do every weekend.", "Hãy thử cả hai hoạt động trước khi quyết định sẽ làm hoạt động nào mỗi cuối tuần.", "Chia rõ ý kiến và lý do; nhấn điểm khác nhau giữa hai lựa chọn.", id = "a2_opinions_phrase_030"),
        )
        val turns = original.conversation + listOf(
            turn("a2_opinions_turn_006", "Which chair do you find more comfortable?", "Bạn thấy chiếc ghế nào thoải mái hơn?", phrases[12]),
            turn("a2_opinions_turn_007", "What matters more to you when choosing a home?", "Điều gì quan trọng hơn với bạn khi chọn nhà?", phrases[14]),
            turn("a2_opinions_turn_008", "What would you like to ask about these courses?", "Bạn muốn hỏi gì về những khóa học này?", phrases[16]),
            turn("a2_opinions_turn_009", "How do the train and the bus compare?", "Tàu hỏa và xe buýt khác nhau thế nào?", phrases[18]),
            turn("a2_opinions_turn_010", "What do you think of this apartment compared with yours?", "Bạn thấy căn hộ này thế nào so với căn của mình?", phrases[20]),
            turn("a2_opinions_turn_011", "How was the restaurant this time?", "Lần này nhà hàng thế nào?", phrases[22]),
            turn("a2_opinions_turn_012", "What matters more than the price of the phone?", "Điều gì quan trọng hơn giá điện thoại?", phrases[24]),
            turn("a2_opinions_turn_013", "Could sharing a taxi make it affordable?", "Đi chung taxi có thể giúp giảm chi phí không?", phrases[26]),
            turn("a2_opinions_turn_014", "Have your preferences about places changed?", "Sở thích của bạn về nơi chốn đã thay đổi chưa?", phrases[28]),
            turn("a2_opinions_turn_015", "How can we choose a regular weekend activity?", "Chúng ta có thể chọn hoạt động cuối tuần thường xuyên như thế nào?", phrases[29]),
        )
        return original.copy(phrases = phrases, conversation = turns)
    }

    private fun a2_problems(original: Lesson): Lesson {
        val phrases = original.phrases + listOf(
            Phrase("My phone has run out of battery.", "Điện thoại của tôi đã hết pin.", "Nhấn đồ vật và sự cố; giữ âm cuối trong các từ chỉ vấn đề.", id = "a2_problems_phrase_013"),
            Phrase("There is a mistake on the bill.", "Hóa đơn có một chỗ sai.", "Nhấn đồ vật và sự cố; giữ âm cuối trong các từ chỉ vấn đề.", id = "a2_problems_phrase_014"),
            Phrase("I can't open the file you sent.", "Tôi không mở được tệp bạn gửi.", "Nhấn đồ vật và sự cố; giữ âm cuối trong các từ chỉ vấn đề.", id = "a2_problems_phrase_015"),
            Phrase("The tap in the bathroom keeps leaking.", "Vòi nước trong phòng tắm cứ bị rò rỉ.", "Nhấn đồ vật và sự cố; giữ âm cuối trong các từ chỉ vấn đề.", id = "a2_problems_phrase_016"),
            Phrase("Could I exchange this for a larger size?", "Tôi có thể đổi món này sang cỡ lớn hơn không?", "Nhấn đồ vật và sự cố; giữ âm cuối trong các từ chỉ vấn đề.", id = "a2_problems_phrase_017"),
            Phrase("I left my bag on the bus this morning.", "Sáng nay tôi để quên túi trên xe buýt.", "Nhấn đồ vật và sự cố; giữ âm cuối trong các từ chỉ vấn đề.", id = "a2_problems_phrase_018"),
            Phrase("The screen goes black whenever I open the camera.", "Màn hình bị đen mỗi khi tôi mở máy ảnh.", "Nói rõ yêu cầu và nguyên nhân; lên giọng nhẹ khi nhờ kiểm tra.", id = "a2_problems_phrase_019"),
            Phrase("I tried restarting the computer, but the same message appeared again.", "Tôi đã thử khởi động lại máy tính, nhưng thông báo đó lại hiện ra.", "Nói rõ yêu cầu và nguyên nhân; lên giọng nhẹ khi nhờ kiểm tra.", id = "a2_problems_phrase_020"),
            Phrase("Could you check the bill? I think we were charged for an extra drink.", "Bạn kiểm tra hóa đơn giúp nhé? Tôi nghĩ chúng tôi bị tính thêm một đồ uống.", "Nói rõ yêu cầu và nguyên nhân; lên giọng nhẹ khi nhờ kiểm tra.", id = "a2_problems_phrase_021"),
            Phrase("I need to change my delivery address because I won't be at home tomorrow.", "Tôi cần đổi địa chỉ giao hàng vì ngày mai tôi không ở nhà.", "Nói rõ yêu cầu và nguyên nhân; lên giọng nhẹ khi nhờ kiểm tra.", id = "a2_problems_phrase_022"),
            Phrase("The shoes arrived yesterday, but they are a different color from the ones I ordered.", "Giày được giao hôm qua, nhưng khác màu với đôi tôi đặt.", "Nói rõ yêu cầu và nguyên nhân; lên giọng nhẹ khi nhờ kiểm tra.", id = "a2_problems_phrase_023"),
            Phrase("I will keep the receipt in case I need to return the item later.", "Tôi sẽ giữ hóa đơn phòng khi cần trả lại món hàng sau này.", "Nói rõ yêu cầu và nguyên nhân; lên giọng nhẹ khi nhờ kiểm tra.", id = "a2_problems_phrase_024"),
            Phrase("If the internet still doesn't work after you restart the router, please call the service team.", "Nếu mạng vẫn không hoạt động sau khi khởi động lại bộ định tuyến, hãy gọi đội hỗ trợ.", "Ngắt câu thành vấn đề, hoàn cảnh và đề nghị; giữ giọng lịch sự.", id = "a2_problems_phrase_025"),
            Phrase("I'm sorry to bother you, but our room hasn't been cleaned and we are expecting a guest.", "Xin lỗi làm phiền bạn, nhưng phòng chúng tôi chưa được dọn và chúng tôi sắp có khách.", "Ngắt câu thành vấn đề, hoàn cảnh và đề nghị; giữ giọng lịch sự.", id = "a2_problems_phrase_026"),
            Phrase("While I was checking the order, I noticed that one of the items was missing from the box.", "Trong khi kiểm tra đơn hàng, tôi nhận thấy trong hộp thiếu một món.", "Ngắt câu thành vấn đề, hoàn cảnh và đề nghị; giữ giọng lịch sự.", id = "a2_problems_phrase_027"),
            Phrase("Could you send me written confirmation so that I know when the replacement is going to arrive?", "Bạn có thể gửi xác nhận bằng văn bản để tôi biết khi nào hàng thay thế sẽ đến không?", "Ngắt câu thành vấn đề, hoàn cảnh và đề nghị; giữ giọng lịch sự.", id = "a2_problems_phrase_028"),
            Phrase("I understand that you are busy, but could you let me know how long the repair will take?", "Tôi hiểu bạn đang bận, nhưng bạn có thể cho biết việc sửa chữa sẽ mất bao lâu không?", "Ngắt câu thành vấn đề, hoàn cảnh và đề nghị; giữ giọng lịch sự.", id = "a2_problems_phrase_029"),
            Phrase("We couldn't fix the problem ourselves, so we asked someone with more experience to help us.", "Chúng tôi không tự sửa được sự cố nên đã nhờ người có nhiều kinh nghiệm hơn giúp.", "Ngắt câu thành vấn đề, hoàn cảnh và đề nghị; giữ giọng lịch sự.", id = "a2_problems_phrase_030"),
        )
        val turns = original.conversation + listOf(
            turn("a2_problems_turn_006", "Why can't you use your phone right now?", "Tại sao lúc này bạn không dùng điện thoại được?", phrases[12]),
            turn("a2_problems_turn_007", "Were you able to open my attachment?", "Bạn có mở được tệp đính kèm của tôi không?", phrases[14]),
            turn("a2_problems_turn_008", "What would you like to ask the shop assistant about the size?", "Bạn muốn hỏi nhân viên bán hàng điều gì về kích cỡ?", phrases[16]),
            turn("a2_problems_turn_009", "When does the screen stop working?", "Khi nào màn hình ngừng hoạt động?", phrases[18]),
            turn("a2_problems_turn_010", "What would you like me to check on the bill?", "Bạn muốn tôi kiểm tra gì trên hóa đơn?", phrases[20]),
            turn("a2_problems_turn_011", "Was there a problem with the shoes you received?", "Đôi giày bạn nhận có vấn đề gì không?", phrases[22]),
            turn("a2_problems_turn_012", "What should I do if restarting the router doesn't help?", "Tôi nên làm gì nếu khởi động lại bộ định tuyến không có tác dụng?", phrases[24]),
            turn("a2_problems_turn_013", "What did you notice while checking the order?", "Bạn nhận thấy điều gì khi kiểm tra đơn hàng?", phrases[26]),
            turn("a2_problems_turn_014", "What would you like to ask about the repair time?", "Bạn muốn hỏi gì về thời gian sửa chữa?", phrases[28]),
            turn("a2_problems_turn_015", "How did you solve the problem in the end?", "Cuối cùng bạn đã giải quyết sự cố thế nào?", phrases[29]),
        )
        return original.copy(phrases = phrases, conversation = turns)
    }

    private fun a2_travel(original: Lesson): Lesson {
        val phrases = original.phrases + listOf(
            Phrase("I would like a map of the area.", "Tôi muốn có một bản đồ khu vực này.", "Nhấn địa điểm và dịch vụ cần hỏi; đọc could you nhẹ và lịch sự.", id = "a2_travel_phrase_013"),
            Phrase("Where can I collect my luggage?", "Tôi có thể nhận hành lý ở đâu?", "Nhấn địa điểm và dịch vụ cần hỏi; đọc could you nhẹ và lịch sự.", id = "a2_travel_phrase_014"),
            Phrase("We are looking for the departure gate.", "Chúng tôi đang tìm cửa khởi hành.", "Nhấn địa điểm và dịch vụ cần hỏi; đọc could you nhẹ và lịch sự.", id = "a2_travel_phrase_015"),
            Phrase("Does this ticket include a return journey?", "Vé này có bao gồm lượt về không?", "Nhấn địa điểm và dịch vụ cần hỏi; đọc could you nhẹ và lịch sự.", id = "a2_travel_phrase_016"),
            Phrase("Could you recommend somewhere to eat nearby?", "Bạn có thể giới thiệu chỗ ăn gần đây không?", "Nhấn địa điểm và dịch vụ cần hỏi; đọc could you nhẹ và lịch sự.", id = "a2_travel_phrase_017"),
            Phrase("I need a quiet room away from the lift.", "Tôi cần phòng yên tĩnh, xa thang máy.", "Nhấn địa điểm và dịch vụ cần hỏi; đọc could you nhẹ và lịch sự.", id = "a2_travel_phrase_018"),
            Phrase("We would like to leave our bags here until the afternoon.", "Chúng tôi muốn gửi túi ở đây đến chiều.", "Ngắt giữa yêu cầu và thông tin bổ sung; đọc rõ giờ, số lượng và loại phòng.", id = "a2_travel_phrase_019"),
            Phrase("Could you let us know if a room with a balcony becomes available?", "Bạn có thể báo cho chúng tôi nếu có phòng với ban công không?", "Ngắt giữa yêu cầu và thông tin bổ sung; đọc rõ giờ, số lượng và loại phòng.", id = "a2_travel_phrase_020"),
            Phrase("I booked a double room online, but the confirmation says it has two single beds.", "Tôi đặt phòng một giường đôi trên mạng, nhưng xác nhận ghi hai giường đơn.", "Ngắt giữa yêu cầu và thông tin bổ sung; đọc rõ giờ, số lượng và loại phòng.", id = "a2_travel_phrase_021"),
            Phrase("We need to leave before breakfast, so could you prepare something for us to take away?", "Chúng tôi cần đi trước giờ ăn sáng, vậy bạn chuẩn bị đồ ăn mang theo giúp được không?", "Ngắt giữa yêu cầu và thông tin bổ sung; đọc rõ giờ, số lượng và loại phòng.", id = "a2_travel_phrase_022"),
            Phrase("How much would it cost to stay for another night if we keep the same room?", "Nếu giữ nguyên phòng, ở thêm một đêm sẽ tốn bao nhiêu?", "Ngắt giữa yêu cầu và thông tin bổ sung; đọc rõ giờ, số lượng và loại phòng.", id = "a2_travel_phrase_023"),
            Phrase("I would like to know whether the last bus leaves before or after the concert finishes.", "Tôi muốn biết chuyến xe buýt cuối rời đi trước hay sau khi buổi hòa nhạc kết thúc.", "Ngắt giữa yêu cầu và thông tin bổ sung; đọc rõ giờ, số lượng và loại phòng.", id = "a2_travel_phrase_024"),
            Phrase("If the train is delayed, could you please keep our room because we may arrive after midnight?", "Nếu tàu bị trễ, bạn vui lòng giữ phòng vì chúng tôi có thể đến sau nửa đêm nhé?", "Chia rõ điều kiện và yêu cầu; nhấn thời gian di chuyển, tránh nói dồn cuối câu.", id = "a2_travel_phrase_025"),
            Phrase("We enjoyed the tour, especially the market where the guide gave us time to try some local food.", "Chúng tôi thích chuyến tham quan, nhất là khu chợ nơi hướng dẫn viên cho thời gian thử món địa phương.", "Chia rõ điều kiện và yêu cầu; nhấn thời gian di chuyển, tránh nói dồn cuối câu.", id = "a2_travel_phrase_026"),
            Phrase("Before we leave, could you check that we haven't forgotten anything in the safe or the wardrobe?", "Trước khi đi, bạn kiểm tra giúp xem chúng tôi có quên gì trong két hoặc tủ quần áo không?", "Chia rõ điều kiện và yêu cầu; nhấn thời gian di chuyển, tránh nói dồn cuối câu.", id = "a2_travel_phrase_027"),
            Phrase("I would prefer to take public transport, but I need to know how to get there with two heavy bags.", "Tôi muốn đi phương tiện công cộng hơn, nhưng cần biết cách đến đó khi mang hai túi nặng.", "Chia rõ điều kiện và yêu cầu; nhấn thời gian di chuyển, tránh nói dồn cuối câu.", id = "a2_travel_phrase_028"),
            Phrase("When we get to the station, let's find the correct platform first and buy some water if we have time.", "Khi đến ga, hãy tìm đúng sân ga trước rồi mua nước nếu còn thời gian.", "Chia rõ điều kiện và yêu cầu; nhấn thời gian di chuyển, tránh nói dồn cuối câu.", id = "a2_travel_phrase_029"),
            Phrase("Although our flight was cancelled, the airline helped us find another flight and a hotel near the airport.", "Dù chuyến bay bị hủy, hãng hàng không đã giúp chúng tôi tìm chuyến khác và khách sạn gần sân bay.", "Chia rõ điều kiện và yêu cầu; nhấn thời gian di chuyển, tránh nói dồn cuối câu.", id = "a2_travel_phrase_030"),
        )
        val turns = original.conversation + listOf(
            turn("a2_travel_turn_006", "What would help you find your way around the area?", "Điều gì sẽ giúp bạn tìm đường trong khu vực này?", phrases[12]),
            turn("a2_travel_turn_007", "What are you trying to find in the airport?", "Bạn đang cố tìm gì trong sân bay?", phrases[14]),
            turn("a2_travel_turn_008", "What would you like to ask about local restaurants?", "Bạn muốn hỏi gì về nhà hàng địa phương?", phrases[16]),
            turn("a2_travel_turn_009", "What would you like to do with your bags after checking out?", "Sau khi trả phòng, bạn muốn làm gì với túi của mình?", phrases[18]),
            turn("a2_travel_turn_010", "What is different about your room confirmation?", "Xác nhận phòng của bạn có gì khác?", phrases[20]),
            turn("a2_travel_turn_011", "What would you like to ask about extending your stay?", "Bạn muốn hỏi gì về việc ở thêm?", phrases[22]),
            turn("a2_travel_turn_012", "What would you like the hotel to do if your train is late?", "Bạn muốn khách sạn làm gì nếu tàu của bạn đến trễ?", phrases[24]),
            turn("a2_travel_turn_013", "What should we check before leaving the room?", "Chúng ta nên kiểm tra gì trước khi rời phòng?", phrases[26]),
            turn("a2_travel_turn_014", "What should we do first when we reach the station?", "Chúng ta nên làm gì trước khi đến ga?", phrases[28]),
            turn("a2_travel_turn_015", "How did the airline help after the cancellation?", "Sau khi hủy chuyến, hãng hàng không đã giúp thế nào?", phrases[29]),
        )
        return original.copy(phrases = phrases, conversation = turns)
    }

    private fun a2_health(): Lesson {
        val phrases = listOf(
            Phrase("I haven't been sleeping well.", "Gần đây tôi ngủ không ngon.", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_001"),
            Phrase("My back hurts after work.", "Lưng tôi đau sau giờ làm.", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_002"),
            Phrase("I would like to make an appointment.", "Tôi muốn đặt lịch hẹn.", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_003"),
            Phrase("The pain started yesterday morning.", "Cơn đau bắt đầu sáng hôm qua.", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_004"),
            Phrase("How often should I take this medicine?", "Tôi nên uống thuốc này bao lâu một lần?", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_005"),
            Phrase("I try to drink enough water.", "Tôi cố gắng uống đủ nước.", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_006"),
            Phrase("A short walk helps me relax.", "Đi bộ một lúc giúp tôi thư giãn.", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_007"),
            Phrase("I feel better after some rest.", "Tôi thấy đỡ hơn sau khi nghỉ ngơi.", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_008"),
            Phrase("I am allergic to some medicines.", "Tôi bị dị ứng với một số loại thuốc.", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_009"),
            Phrase("Could you speak a little more slowly?", "Bạn có thể nói chậm hơn một chút không?", "Nhấn triệu chứng và thời gian; đọc rõ âm cuối trong hurts và started.", id = "a2_health_phrase_010"),
            Phrase("I have a sore throat, so it hurts to swallow.", "Tôi bị đau họng nên nuốt thấy đau.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_011"),
            Phrase("I would like an appointment in the morning if possible.", "Nếu được, tôi muốn có lịch hẹn vào buổi sáng.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_012"),
            Phrase("I spend less time on my phone before going to bed.", "Tôi dành ít thời gian dùng điện thoại hơn trước khi đi ngủ.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_013"),
            Phrase("Can you tell me whether I should take this with food?", "Bạn có thể cho tôi biết có nên uống thuốc này cùng thức ăn không?", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_014"),
            Phrase("I have been feeling tired since I started working late.", "Tôi cảm thấy mệt từ khi bắt đầu làm việc đến muộn.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_015"),
            Phrase("I usually cook at home because I want to eat more vegetables.", "Tôi thường nấu ăn ở nhà vì muốn ăn nhiều rau hơn.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_016"),
            Phrase("My doctor asked me to come back in a week.", "Bác sĩ hẹn tôi quay lại sau một tuần.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_017"),
            Phrase("I take a break every hour when I work at a computer.", "Tôi nghỉ một lần mỗi giờ khi làm việc với máy tính.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_018"),
            Phrase("I can't attend the class today because I don't feel well.", "Hôm nay tôi không thể đến lớp vì thấy không khỏe.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_019"),
            Phrase("Please write down the instructions so I can remember them.", "Hãy viết hướng dẫn ra để tôi có thể nhớ.", "Ngắt giữa triệu chứng và lý do; giữ nhịp chậm, rõ trong câu hỏi.", id = "a2_health_phrase_020"),
            Phrase("I have had this cough for three days, and it gets worse when I lie down.", "Tôi ho ba ngày rồi, và ho nặng hơn khi nằm xuống.", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_021"),
            Phrase("Could you check whether this medicine is suitable for me, because I take another medicine every day?", "Bạn kiểm tra giúp thuốc này có phù hợp với tôi không, vì tôi đang uống một thuốc khác hằng ngày?", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_022"),
            Phrase("If I don't feel better tomorrow, I will call the clinic and ask for an appointment.", "Nếu ngày mai không đỡ, tôi sẽ gọi phòng khám để đặt lịch hẹn.", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_023"),
            Phrase("I used to skip breakfast when I was busy, but now I make time for it.", "Trước đây tôi thường bỏ bữa sáng khi bận, nhưng giờ tôi dành thời gian ăn sáng.", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_024"),
            Phrase("When I feel stressed, I like to take a quiet walk instead of looking at my phone.", "Khi căng thẳng, tôi thích đi bộ yên tĩnh thay vì nhìn điện thoại.", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_025"),
            Phrase("I am trying to go to bed earlier so that I have more energy in the morning.", "Tôi đang cố đi ngủ sớm hơn để buổi sáng có nhiều năng lượng hơn.", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_026"),
            Phrase("Could you explain the instructions again and show me where the next appointment time is written?", "Bạn có thể giải thích lại hướng dẫn và chỉ chỗ ghi giờ hẹn tiếp theo không?", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_027"),
            Phrase("I felt dizzy while I was standing in the queue, so I sat down and asked for help.", "Tôi thấy chóng mặt trong lúc xếp hàng nên đã ngồi xuống và nhờ giúp đỡ.", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_028"),
            Phrase("Although I enjoy my job, I need to find a better balance between work and rest.", "Dù thích công việc, tôi cần cân bằng tốt hơn giữa làm việc và nghỉ ngơi.", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_029"),
            Phrase("Before I leave the clinic, I would like to check that I have understood everything correctly.", "Trước khi rời phòng khám, tôi muốn kiểm tra xem mình đã hiểu đúng mọi thứ chưa.", "Chia câu thành triệu chứng, hoàn cảnh và yêu cầu; nhấn thời gian và thông tin quan trọng.", id = "a2_health_phrase_030"),
        )
        val turns = listOf(
            turn("a2_health_turn_001", "How have you been sleeping recently?", "Gần đây bạn ngủ thế nào?", phrases[0]),
            turn("a2_health_turn_002", "What would you like to arrange with the clinic?", "Bạn muốn sắp xếp việc gì với phòng khám?", phrases[2]),
            turn("a2_health_turn_003", "What do you need to ask about the medicine?", "Bạn cần hỏi gì về thuốc?", phrases[4]),
            turn("a2_health_turn_004", "What helps you relax after a busy day?", "Điều gì giúp bạn thư giãn sau một ngày bận rộn?", phrases[6]),
            turn("a2_health_turn_005", "Is there anything the doctor should know about allergies?", "Bác sĩ cần biết gì về dị ứng của bạn không?", phrases[8]),
            turn("a2_health_turn_006", "What happens when you swallow?", "Điều gì xảy ra khi bạn nuốt?", phrases[10]),
            turn("a2_health_turn_007", "Have you changed your bedtime routine?", "Bạn đã thay đổi thói quen trước giờ ngủ chưa?", phrases[12]),
            turn("a2_health_turn_008", "When did you start feeling tired?", "Bạn bắt đầu thấy mệt từ khi nào?", phrases[14]),
            turn("a2_health_turn_009", "When did the doctor ask you to return?", "Bác sĩ hẹn bạn quay lại khi nào?", phrases[16]),
            turn("a2_health_turn_010", "Why can't you come to class today?", "Tại sao hôm nay bạn không đến lớp được?", phrases[18]),
            turn("a2_health_turn_011", "How long have you had the cough, and when is it worse?", "Bạn đã ho bao lâu và khi nào ho nặng hơn?", phrases[20]),
            turn("a2_health_turn_012", "What will you do if you still feel unwell tomorrow?", "Bạn sẽ làm gì nếu ngày mai vẫn không khỏe?", phrases[22]),
            turn("a2_health_turn_013", "What do you like to do when you feel stressed?", "Bạn thích làm gì khi cảm thấy căng thẳng?", phrases[24]),
            turn("a2_health_turn_014", "What would you like the clinic staff to explain again?", "Bạn muốn nhân viên phòng khám giải thích lại điều gì?", phrases[26]),
            turn("a2_health_turn_015", "What would you like to check before leaving the clinic?", "Bạn muốn kiểm tra gì trước khi rời phòng khám?", phrases[29]),
        )
        return Lesson("a2_health", "Health & Well-being", "Sức khỏe và sinh hoạt", phrases, turns)
    }

    private fun a2_work(): Lesson {
        val phrases = listOf(
            Phrase("I work in the customer service team.", "Tôi làm việc trong nhóm dịch vụ khách hàng.", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_001"),
            Phrase("My shift starts at half past eight.", "Ca làm của tôi bắt đầu lúc tám giờ rưỡi.", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_002"),
            Phrase("I am responsible for checking the rooms.", "Tôi phụ trách kiểm tra các phòng.", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_003"),
            Phrase("Could you show me how this works?", "Bạn có thể chỉ tôi cách dùng cái này không?", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_004"),
            Phrase("I need a copy of the schedule.", "Tôi cần một bản sao lịch làm việc.", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_005"),
            Phrase("The manager is in a meeting.", "Người quản lý đang họp.", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_006"),
            Phrase("Let's check the list together first.", "Chúng ta hãy cùng kiểm tra danh sách trước.", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_007"),
            Phrase("I have already sent the email.", "Tôi đã gửi email rồi.", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_008"),
            Phrase("Could you cover my shift tomorrow?", "Ngày mai bạn có thể làm thay ca cho tôi không?", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_009"),
            Phrase("We still have a few tasks left.", "Chúng ta vẫn còn vài việc chưa làm.", "Nhấn công việc và giờ làm; nối nhẹ từ chức năng với từ bên cạnh.", id = "a2_work_phrase_010"),
            Phrase("I will finish the report before I leave this afternoon.", "Tôi sẽ hoàn thành báo cáo trước khi về chiều nay.", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_011"),
            Phrase("Could you send me the updated schedule when you have time?", "Khi có thời gian, bạn gửi cho tôi lịch đã cập nhật nhé?", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_012"),
            Phrase("I couldn't answer your call because I was helping a customer.", "Tôi không thể nghe cuộc gọi của bạn vì đang hỗ trợ khách hàng.", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_013"),
            Phrase("We need to check the details before we confirm the booking.", "Chúng ta cần kiểm tra chi tiết trước khi xác nhận đặt chỗ.", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_014"),
            Phrase("I have worked with this team for nearly two years.", "Tôi đã làm việc cùng nhóm này gần hai năm.", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_015"),
            Phrase("Let me know if you need any help with the new system.", "Hãy báo tôi nếu bạn cần giúp với hệ thống mới.", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_016"),
            Phrase("I made a mistake in the report, but I have corrected it.", "Tôi đã mắc lỗi trong báo cáo, nhưng đã sửa rồi.", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_017"),
            Phrase("Could we discuss this after lunch when everyone is available?", "Chúng ta có thể bàn việc này sau bữa trưa khi mọi người đều rảnh không?", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_018"),
            Phrase("The customer asked for a quiet room on a higher floor.", "Khách yêu cầu một phòng yên tĩnh ở tầng cao hơn.", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_019"),
            Phrase("I am taking notes so I can explain the changes to my colleagues.", "Tôi đang ghi chú để giải thích những thay đổi cho đồng nghiệp.", "Ngắt nhẹ trước because, before hoặc when; nhấn nhiệm vụ và thời hạn.", id = "a2_work_phrase_020"),
            Phrase("If the delivery arrives while I am at lunch, could you ask the driver to wait at reception?", "Nếu hàng đến trong lúc tôi ăn trưa, bạn nhờ tài xế đợi ở lễ tân giúp nhé?", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_021"),
            Phrase("We were preparing the meeting room when the manager called to say that the guests were arriving early.", "Chúng tôi đang chuẩn bị phòng họp thì người quản lý gọi báo khách sẽ đến sớm.", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_022"),
            Phrase("I would like to change my shift next week because I have an important appointment on Tuesday morning.", "Tôi muốn đổi ca tuần tới vì có một cuộc hẹn quan trọng vào sáng thứ Ba.", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_023"),
            Phrase("Before you send the report, please check that the dates and the room numbers are correct.", "Trước khi gửi báo cáo, hãy kiểm tra ngày tháng và số phòng cho đúng.", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_024"),
            Phrase("Although we were short of staff, we finished the work on time by helping each other.", "Dù thiếu nhân viên, chúng tôi đã hoàn thành đúng giờ nhờ giúp đỡ lẫn nhau.", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_025"),
            Phrase("I haven't used this system before, so I would appreciate it if someone could show me the first steps.", "Tôi chưa dùng hệ thống này bao giờ nên rất mong có người chỉ những bước đầu.", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_026"),
            Phrase("When you speak to the customer, please explain the delay clearly and let them know what we can do.", "Khi nói chuyện với khách, hãy giải thích rõ sự chậm trễ và cho họ biết chúng ta có thể làm gì.", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_027"),
            Phrase("I think we should write down the main tasks so that the next team knows what still needs to be done.", "Tôi nghĩ nên ghi các việc chính để nhóm tiếp theo biết việc gì vẫn cần làm.", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_028"),
            Phrase("Could we arrange a short meeting tomorrow to discuss what went well and what we need to improve?", "Chúng ta có thể sắp xếp cuộc họp ngắn ngày mai để bàn điều đã làm tốt và điều cần cải thiện không?", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_029"),
            Phrase("By checking the details together before the guests arrive, we can avoid mistakes and make their visit more comfortable.", "Bằng cách cùng kiểm tra chi tiết trước khi khách đến, chúng ta có thể tránh lỗi và giúp họ có chuyến thăm thoải mái hơn.", "Chia lời nhờ thành từng cụm; nhấn việc cần làm, lý do và thời gian.", id = "a2_work_phrase_030"),
        )
        val turns = listOf(
            turn("a2_work_turn_001", "Which team do you work in?", "Bạn làm việc trong nhóm nào?", phrases[0]),
            turn("a2_work_turn_002", "What are you responsible for at work?", "Bạn phụ trách việc gì ở chỗ làm?", phrases[2]),
            turn("a2_work_turn_003", "What document do you need for next week?", "Bạn cần tài liệu gì cho tuần tới?", phrases[4]),
            turn("a2_work_turn_004", "How should we start checking the tasks?", "Chúng ta nên bắt đầu kiểm tra công việc thế nào?", phrases[6]),
            turn("a2_work_turn_005", "What would you like to ask a colleague about tomorrow's shift?", "Bạn muốn hỏi đồng nghiệp gì về ca làm ngày mai?", phrases[8]),
            turn("a2_work_turn_006", "When will you finish the report?", "Khi nào bạn sẽ hoàn thành báo cáo?", phrases[10]),
            turn("a2_work_turn_007", "Why couldn't you answer my call?", "Tại sao bạn không thể nghe cuộc gọi của tôi?", phrases[12]),
            turn("a2_work_turn_008", "How long have you worked with this team?", "Bạn đã làm việc cùng nhóm này bao lâu?", phrases[14]),
            turn("a2_work_turn_009", "What happened to the mistake in the report?", "Lỗi trong báo cáo đã được xử lý thế nào?", phrases[16]),
            turn("a2_work_turn_010", "What kind of room did the customer ask for?", "Khách đã yêu cầu phòng như thế nào?", phrases[18]),
            turn("a2_work_turn_011", "What should I do if the delivery comes during your lunch break?", "Tôi nên làm gì nếu hàng đến trong giờ nghỉ trưa của bạn?", phrases[20]),
            turn("a2_work_turn_012", "Why would you like to change your shift next week?", "Tại sao bạn muốn đổi ca tuần tới?", phrases[22]),
            turn("a2_work_turn_013", "How did your team finish despite having fewer people?", "Nhóm bạn đã hoàn thành thế nào dù ít người hơn?", phrases[24]),
            turn("a2_work_turn_014", "What should I explain to the customer about the delay?", "Tôi nên giải thích gì với khách về việc chậm trễ?", phrases[26]),
            turn("a2_work_turn_015", "How can checking together help the guests?", "Cùng kiểm tra có thể giúp khách như thế nào?", phrases[29]),
        )
        return Lesson("a2_work", "Work & Teamwork", "Công việc và phối hợp", phrases, turns)
    }

    private fun a2_learning(): Lesson {
        val phrases = listOf(
            Phrase("I am taking an evening course.", "Tôi đang theo học một khóa buổi tối.", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_001"),
            Phrase("This exercise is quite challenging.", "Bài tập này khá khó.", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_002"),
            Phrase("Could you explain that word again?", "Bạn có thể giải thích lại từ đó không?", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_003"),
            Phrase("I learn best by listening first.", "Tôi học tốt nhất khi nghe trước.", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_004"),
            Phrase("I have a test next Friday.", "Thứ Sáu tới tôi có bài kiểm tra.", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_005"),
            Phrase("Let's practise the dialogue in pairs.", "Chúng ta hãy luyện hội thoại theo cặp.", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_006"),
            Phrase("I need to improve my pronunciation.", "Tôi cần cải thiện phát âm.", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_007"),
            Phrase("My goal is to speak more confidently.", "Mục tiêu của tôi là nói tự tin hơn.", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_008"),
            Phrase("I review my notes after each class.", "Tôi ôn lại ghi chú sau mỗi buổi học.", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_009"),
            Phrase("Could I borrow your dictionary today?", "Hôm nay tôi có thể mượn từ điển của bạn không?", "Nhấn kỹ năng hoặc mục tiêu học; đọc rõ các cụm phụ âm cuối.", id = "a2_learning_phrase_010"),
            Phrase("I didn't understand the question, so I asked for an example.", "Tôi không hiểu câu hỏi nên đã xin một ví dụ.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_011"),
            Phrase("I spend about twenty minutes practising English on my phone every day.", "Tôi dành khoảng hai mươi phút mỗi ngày luyện tiếng Anh trên điện thoại.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_012"),
            Phrase("My teacher says that I should pay more attention to word endings.", "Giáo viên nói tôi nên chú ý hơn đến âm cuối của từ.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_013"),
            Phrase("I find it easier to remember words when I use them in sentences.", "Tôi thấy dễ nhớ từ hơn khi dùng chúng trong câu.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_014"),
            Phrase("Could you check whether I have used the past tense correctly?", "Bạn có thể kiểm tra xem tôi đã dùng thì quá khứ đúng chưa?", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_015"),
            Phrase("I have learned a lot since I started listening to short conversations.", "Tôi đã học được nhiều từ khi bắt đầu nghe hội thoại ngắn.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_016"),
            Phrase("We are working on a project about life in our town.", "Chúng tôi đang làm dự án về cuộc sống trong thị trấn.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_017"),
            Phrase("I want to join the speaking club, but I feel a little nervous.", "Tôi muốn tham gia câu lạc bộ nói, nhưng thấy hơi lo.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_018"),
            Phrase("The course is more difficult than I expected, although I enjoy it.", "Khóa học khó hơn tôi nghĩ, dù tôi vẫn thích.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_019"),
            Phrase("I will ask my teacher for advice before I choose the next course.", "Tôi sẽ xin lời khuyên của giáo viên trước khi chọn khóa tiếp theo.", "Ngắt giữa cách học và lý do; nhấn từ mang ý chính thay vì mọi từ.", id = "a2_learning_phrase_020"),
            Phrase("If I don't know a word during a conversation, I try to explain it using words I already know.", "Nếu không biết một từ khi trò chuyện, tôi cố giải thích bằng những từ mình đã biết.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_021"),
            Phrase("I used to translate every word in my head, but now I try to understand the whole sentence.", "Trước đây tôi thường dịch từng từ trong đầu, nhưng giờ tôi cố hiểu cả câu.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_022"),
            Phrase("When I listen to a new dialogue, I play it once for the main idea and then listen again for details.", "Khi nghe hội thoại mới, tôi nghe một lần để hiểu ý chính rồi nghe lại chi tiết.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_023"),
            Phrase("Although I still make mistakes, I can now have longer conversations without stopping to think about every word.", "Dù vẫn mắc lỗi, giờ tôi có thể trò chuyện dài hơn mà không dừng để nghĩ từng từ.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_024"),
            Phrase("Could we practise this conversation one more time so that I can work on the parts I found difficult?", "Chúng ta có thể luyện hội thoại này thêm lần nữa để tôi luyện những phần thấy khó không?", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_025"),
            Phrase("I am planning to study a little every day because regular practice works better for me than one long session.", "Tôi định học một ít mỗi ngày vì luyện đều hiệu quả với tôi hơn một buổi học dài.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_026"),
            Phrase("Before I move to the next lesson, I like to listen to my recording and check which words I can improve.", "Trước khi sang bài tiếp theo, tôi thích nghe bản ghi của mình và xem có thể cải thiện từ nào.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_027"),
            Phrase("My friend and I take turns asking questions, and we help each other when one of us gets stuck.", "Bạn tôi và tôi thay phiên hỏi, và giúp nhau khi một trong hai gặp khó.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_028"),
            Phrase("I would like to use English at work, so I am learning how to explain simple problems and ask polite questions.", "Tôi muốn dùng tiếng Anh ở chỗ làm nên đang học cách giải thích vấn đề đơn giản và hỏi lịch sự.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_029"),
            Phrase("At the end of each week, I look back at what I have learned and decide what to practise next.", "Cuối mỗi tuần, tôi xem lại những gì đã học và quyết định sẽ luyện gì tiếp theo.", "Chia câu dài thành cách làm, mục đích và kết quả; giữ ngữ điệu tự nhiên.", id = "a2_learning_phrase_030"),
        )
        val turns = listOf(
            turn("a2_learning_turn_001", "What course are you taking at the moment?", "Hiện tại bạn đang theo học khóa nào?", phrases[0]),
            turn("a2_learning_turn_002", "What would you like the teacher to explain again?", "Bạn muốn giáo viên giải thích lại điều gì?", phrases[2]),
            turn("a2_learning_turn_003", "When is your next test?", "Bài kiểm tra tiếp theo của bạn vào khi nào?", phrases[4]),
            turn("a2_learning_turn_004", "Which part of your English do you need to improve?", "Bạn cần cải thiện phần nào trong tiếng Anh?", phrases[6]),
            turn("a2_learning_turn_005", "What do you do after each class?", "Bạn làm gì sau mỗi buổi học?", phrases[8]),
            turn("a2_learning_turn_006", "What did you do when the question was unclear?", "Bạn đã làm gì khi câu hỏi chưa rõ?", phrases[10]),
            turn("a2_learning_turn_007", "What does your teacher want you to focus on?", "Giáo viên muốn bạn tập trung vào điều gì?", phrases[12]),
            turn("a2_learning_turn_008", "What would you like me to check in your writing?", "Bạn muốn tôi kiểm tra gì trong bài viết?", phrases[14]),
            turn("a2_learning_turn_009", "What is your class project about?", "Dự án của lớp bạn về chủ đề gì?", phrases[16]),
            turn("a2_learning_turn_010", "Is the course as easy as you expected?", "Khóa học có dễ như bạn mong đợi không?", phrases[18]),
            turn("a2_learning_turn_011", "What do you do if you forget a word while speaking?", "Bạn làm gì nếu quên một từ trong lúc nói?", phrases[20]),
            turn("a2_learning_turn_012", "How do you listen to a new dialogue?", "Bạn nghe một đoạn hội thoại mới như thế nào?", phrases[22]),
            turn("a2_learning_turn_013", "What would help you practise the difficult parts?", "Điều gì sẽ giúp bạn luyện những phần khó?", phrases[24]),
            turn("a2_learning_turn_014", "What do you like to check before starting the next lesson?", "Bạn thích kiểm tra gì trước khi bắt đầu bài tiếp theo?", phrases[26]),
            turn("a2_learning_turn_015", "How do you decide what to practise each week?", "Bạn quyết định luyện gì mỗi tuần như thế nào?", phrases[29]),
        )
        return Lesson("a2_learning", "Learning & Personal Goals", "Học tập và mục tiêu", phrases, turns)
    }

    private fun a2_community(): Lesson {
        val phrases = listOf(
            Phrase("I would like to join the library.", "Tôi muốn đăng ký thư viện.", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_001"),
            Phrase("Where should I sign this form?", "Tôi nên ký mẫu đơn này ở đâu?", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_002"),
            Phrase("The lift is out of order.", "Thang máy đang bị hỏng.", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_003"),
            Phrase("Our street is quieter at weekends.", "Phố chúng tôi yên tĩnh hơn vào cuối tuần.", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_004"),
            Phrase("I usually separate paper from plastic.", "Tôi thường phân loại giấy riêng với nhựa.", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_005"),
            Phrase("Could you help me carry this box?", "Bạn có thể giúp tôi bê chiếc hộp này không?", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_006"),
            Phrase("The community center opens at nine.", "Trung tâm cộng đồng mở cửa lúc chín giờ.", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_007"),
            Phrase("I need to renew my library card.", "Tôi cần gia hạn thẻ thư viện.", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_008"),
            Phrase("We are collecting clothes for charity.", "Chúng tôi đang quyên góp quần áo từ thiện.", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_009"),
            Phrase("Is there a fee for this service?", "Dịch vụ này có mất phí không?", "Đọc rõ tên dịch vụ và từ chỉ yêu cầu; hỏi với giọng lịch sự.", id = "a2_community_phrase_010"),
            Phrase("Could you tell me what documents I need to bring?", "Bạn có thể cho tôi biết cần mang giấy tờ gì không?", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_011"),
            Phrase("I have lived in this neighborhood since I started my new job.", "Tôi sống ở khu này từ khi bắt đầu công việc mới.", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_012"),
            Phrase("The new bus route makes it easier to reach the hospital.", "Tuyến xe buýt mới giúp đi đến bệnh viện dễ hơn.", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_013"),
            Phrase("We are meeting on Saturday to clean up the local park.", "Thứ Bảy chúng tôi sẽ gặp nhau để dọn công viên địa phương.", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_014"),
            Phrase("I would like to report a street light that isn't working.", "Tôi muốn báo một đèn đường không hoạt động.", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_015"),
            Phrase("Please let me know when the road is going to reopen.", "Hãy cho tôi biết khi nào đường sẽ mở lại.", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_016"),
            Phrase("I can't return the books today, so could I renew them online?", "Hôm nay tôi không trả sách được, vậy tôi gia hạn trên mạng được không?", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_017"),
            Phrase("Our neighbors offered to water the plants while we were away.", "Hàng xóm đề nghị tưới cây giúp trong lúc chúng tôi đi vắng.", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_018"),
            Phrase("This area is becoming busier because several new shops have opened.", "Khu này ngày càng đông đúc vì vài cửa hàng mới đã mở.", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_019"),
            Phrase("Could you explain how to pay the bill without coming to the office?", "Bạn có thể giải thích cách thanh toán hóa đơn mà không đến văn phòng không?", "Ngắt giữa thông tin và lý do; nhấn giấy tờ, địa điểm hoặc thời gian.", id = "a2_community_phrase_020"),
            Phrase("If we put a notice in the entrance hall, more residents will know about the meeting on Sunday.", "Nếu đặt thông báo ở sảnh vào, nhiều cư dân hơn sẽ biết cuộc họp vào Chủ nhật.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_021"),
            Phrase("I would like to volunteer at the event, but I need to know what time it starts and finishes.", "Tôi muốn tình nguyện tại sự kiện, nhưng cần biết giờ bắt đầu và kết thúc.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_022"),
            Phrase("When we moved into this building, our neighbors helped us find the nearest shops and bus stops.", "Khi chuyển vào tòa nhà này, hàng xóm đã giúp chúng tôi tìm cửa hàng và trạm xe buýt gần nhất.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_023"),
            Phrase("Although the park is small, it gives people a safe place to walk and meet their neighbors.", "Dù công viên nhỏ, nó cho mọi người nơi an toàn để đi bộ và gặp hàng xóm.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_024"),
            Phrase("Could you check whether I can use a copy of this document, or do I need to bring the original?", "Bạn kiểm tra giúp tôi có thể dùng bản sao giấy tờ này hay phải mang bản gốc nhé?", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_025"),
            Phrase("We should ask everyone for ideas before we decide how to spend the money for our community project.", "Chúng ta nên hỏi ý tưởng của mọi người trước khi quyết định dùng tiền cho dự án cộng đồng thế nào.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_026"),
            Phrase("I am happy to help older neighbors with their shopping if they find it difficult to carry heavy bags.", "Tôi sẵn lòng giúp hàng xóm lớn tuổi mua sắm nếu họ khó mang túi nặng.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_027"),
            Phrase("Before you leave the office, please keep a copy of the form so you can check the details later.", "Trước khi rời văn phòng, hãy giữ một bản sao mẫu đơn để kiểm tra chi tiết sau này.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_028"),
            Phrase("The library has started an evening group where people can practise languages and learn about each other's cultures.", "Thư viện đã mở nhóm buổi tối để mọi người luyện ngôn ngữ và tìm hiểu văn hóa của nhau.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_029"),
            Phrase("If the event is successful, we would like to organize it again next year and invite people from nearby neighborhoods.", "Nếu sự kiện thành công, chúng tôi muốn tổ chức lại năm tới và mời người dân từ các khu lân cận.", "Ngắt sau if, when hoặc before; nhấn việc cộng đồng cần làm và mục đích.", id = "a2_community_phrase_030"),
        )
        val turns = listOf(
            turn("a2_community_turn_001", "What would you like to register for?", "Bạn muốn đăng ký gì?", phrases[0]),
            turn("a2_community_turn_002", "Why aren't you using the lift?", "Tại sao bạn không dùng thang máy?", phrases[2]),
            turn("a2_community_turn_003", "How do you sort your waste at home?", "Bạn phân loại rác tại nhà như thế nào?", phrases[4]),
            turn("a2_community_turn_004", "What time does the community center open?", "Trung tâm cộng đồng mở cửa lúc mấy giờ?", phrases[6]),
            turn("a2_community_turn_005", "What are you collecting for the charity?", "Bạn đang quyên góp gì cho tổ chức từ thiện?", phrases[8]),
            turn("a2_community_turn_006", "What would you like to ask before bringing your documents?", "Bạn muốn hỏi gì trước khi mang giấy tờ đến?", phrases[10]),
            turn("a2_community_turn_007", "How has the new bus route helped people?", "Tuyến xe buýt mới giúp mọi người thế nào?", phrases[12]),
            turn("a2_community_turn_008", "What problem would you like to report?", "Bạn muốn báo vấn đề gì?", phrases[14]),
            turn("a2_community_turn_009", "What would you like to ask about returning library books?", "Bạn muốn hỏi gì về việc trả sách thư viện?", phrases[16]),
            turn("a2_community_turn_010", "Why is your neighborhood getting busier?", "Tại sao khu phố của bạn ngày càng đông đúc?", phrases[18]),
            turn("a2_community_turn_011", "How can we tell more residents about the meeting?", "Chúng ta có thể báo cuộc họp cho nhiều cư dân hơn bằng cách nào?", phrases[20]),
            turn("a2_community_turn_012", "Did anyone help when you moved into the building?", "Có ai giúp khi bạn chuyển vào tòa nhà không?", phrases[22]),
            turn("a2_community_turn_013", "What would you like to check about this document?", "Bạn muốn kiểm tra gì về giấy tờ này?", phrases[24]),
            turn("a2_community_turn_014", "How could you help older people in your neighborhood?", "Bạn có thể giúp người lớn tuổi trong khu phố thế nào?", phrases[26]),
            turn("a2_community_turn_015", "What would you like to do if the event goes well?", "Bạn muốn làm gì nếu sự kiện diễn ra tốt đẹp?", phrases[29]),
        )
        return Lesson("a2_community", "Community & Everyday Services", "Cộng đồng và dịch vụ hằng ngày", phrases, turns)
    }

    private fun a2_stories(): Lesson {
        val phrases = listOf(
            Phrase("Something unusual happened yesterday.", "Hôm qua đã xảy ra một chuyện lạ.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_001"),
            Phrase("I was waiting outside the station.", "Tôi đang đợi bên ngoài nhà ga.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_002"),
            Phrase("Suddenly, I heard someone call my name.", "Bỗng nhiên tôi nghe ai đó gọi tên mình.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_003"),
            Phrase("It was an old friend from school.", "Đó là một người bạn học cũ.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_004"),
            Phrase("We hadn't planned to meet there.", "Chúng tôi đã không có kế hoạch gặp nhau ở đó.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_005"),
            Phrase("I almost missed the last bus.", "Tôi suýt lỡ chuyến xe buýt cuối.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_006"),
            Phrase("Luckily, the driver waited for me.", "May mắn là tài xế đã đợi tôi.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_007"),
            Phrase("Everyone was surprised by the news.", "Mọi người đều ngạc nhiên trước tin đó.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_008"),
            Phrase("In the end, everything was fine.", "Cuối cùng, mọi việc đều ổn.", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_009"),
            Phrase("What happened after you got home?", "Chuyện gì xảy ra sau khi bạn về nhà?", "Nhấn sự kiện chính; đọc rõ âm cuối của động từ quá khứ.", id = "a2_stories_phrase_010"),
            Phrase("I was cooking dinner when the lights suddenly went out.", "Tôi đang nấu bữa tối thì đèn bỗng tắt.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_011"),
            Phrase("At first I was worried, but then I found my torch.", "Lúc đầu tôi lo lắng, nhưng sau đó tìm được đèn pin.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_012"),
            Phrase("We decided to wait in the cafe until the rain stopped.", "Chúng tôi quyết định chờ trong quán cà phê đến khi mưa tạnh.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_013"),
            Phrase("While I was looking for my keys, my phone started ringing.", "Trong khi tìm chìa khóa, điện thoại của tôi bắt đầu reo.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_014"),
            Phrase("I thought I had lost my ticket, but it was in my coat.", "Tôi tưởng mình đã làm mất vé, nhưng vé ở trong áo khoác.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_015"),
            Phrase("The bus was so crowded that I couldn't find a seat.", "Xe buýt đông đến mức tôi không tìm được chỗ ngồi.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_016"),
            Phrase("A woman noticed that I was lost and offered to help.", "Một phụ nữ nhận thấy tôi bị lạc và đề nghị giúp.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_017"),
            Phrase("After we reached the hotel, we realized that we had the wrong bag.", "Sau khi đến khách sạn, chúng tôi nhận ra đã cầm nhầm túi.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_018"),
            Phrase("I couldn't believe it when I saw my name on the list.", "Tôi không thể tin được khi thấy tên mình trong danh sách.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_019"),
            Phrase("The experience taught me to check the address before leaving home.", "Trải nghiệm đó dạy tôi kiểm tra địa chỉ trước khi rời nhà.", "Ngắt sau cụm thời gian; phân biệt nhịp hành động đang diễn ra và sự kiện xen vào.", id = "a2_stories_phrase_020"),
            Phrase("While I was walking home from work, I found a wallet and took it to the nearest police station.", "Trong lúc đi bộ từ chỗ làm về nhà, tôi thấy một chiếc ví và mang đến đồn cảnh sát gần nhất.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_021"),
            Phrase("I was about to leave the house when my sister called to say that she needed a lift.", "Tôi đang định ra khỏi nhà thì chị gái gọi nói cần tôi chở đi.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_022"),
            Phrase("Although we arrived late because of the traffic, the staff were friendly and helped us find our seats.", "Dù đến muộn vì tắc đường, nhân viên vẫn thân thiện và giúp chúng tôi tìm chỗ ngồi.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_023"),
            Phrase("When I opened the parcel, I was surprised to find a birthday present from a friend who lives abroad.", "Khi mở bưu kiện, tôi ngạc nhiên thấy quà sinh nhật của một người bạn sống ở nước ngoài.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_024"),
            Phrase("We were enjoying our lunch outside when it started raining, so we quickly carried everything into the house.", "Chúng tôi đang ăn trưa vui vẻ ngoài trời thì mưa bắt đầu, nên vội mang mọi thứ vào nhà.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_025"),
            Phrase("I didn't recognize my old teacher at first, but I remembered her as soon as she started speaking.", "Lúc đầu tôi không nhận ra cô giáo cũ, nhưng nhớ ra ngay khi cô bắt đầu nói.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_026"),
            Phrase("The journey took longer than usual because we stopped to help a family whose car had broken down.", "Hành trình lâu hơn bình thường vì chúng tôi dừng giúp một gia đình có ô tô bị hỏng.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_027"),
            Phrase("After I explained what had happened, the receptionist called the other guest and arranged for us to exchange our bags.", "Sau khi tôi giải thích chuyện xảy ra, lễ tân gọi vị khách kia và sắp xếp để chúng tôi đổi lại túi.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_028"),
            Phrase("If something like that happens again, I will stay calm and ask someone for help instead of trying to do everything alone.", "Nếu chuyện tương tự xảy ra nữa, tôi sẽ bình tĩnh và nhờ giúp thay vì cố làm mọi thứ một mình.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_029"),
            Phrase("Looking back, I am glad that we changed our plans, because the unexpected visit became the best part of our holiday.", "Nhìn lại, tôi vui vì chúng tôi đã đổi kế hoạch, bởi chuyến thăm bất ngờ thành phần tuyệt nhất của kỳ nghỉ.", "Chia câu theo trình tự sự kiện; ngắt trước when, while hoặc because, nhấn kết quả.", id = "a2_stories_phrase_030"),
        )
        val turns = listOf(
            turn("a2_stories_turn_001", "Did anything interesting happen yesterday?", "Hôm qua có chuyện gì thú vị không?", phrases[0]),
            turn("a2_stories_turn_002", "What did you hear all of a sudden?", "Bạn đã bất chợt nghe thấy gì?", phrases[2]),
            turn("a2_stories_turn_003", "Were you expecting to meet your friend there?", "Bạn có định gặp bạn mình ở đó không?", phrases[4]),
            turn("a2_stories_turn_004", "Did you manage to catch the bus?", "Bạn có kịp bắt xe buýt không?", phrases[6]),
            turn("a2_stories_turn_005", "How did things turn out in the end?", "Cuối cùng mọi chuyện thế nào?", phrases[8]),
            turn("a2_stories_turn_006", "What were you doing when the lights went out?", "Bạn đang làm gì khi đèn tắt?", phrases[10]),
            turn("a2_stories_turn_007", "What did you do while it was raining?", "Bạn đã làm gì khi trời mưa?", phrases[12]),
            turn("a2_stories_turn_008", "Where did you find your ticket in the end?", "Cuối cùng bạn tìm thấy vé ở đâu?", phrases[14]),
            turn("a2_stories_turn_009", "How did you get help when you were lost?", "Bạn đã được giúp thế nào khi bị lạc?", phrases[16]),
            turn("a2_stories_turn_010", "How did you feel when you saw your name?", "Bạn cảm thấy thế nào khi thấy tên mình?", phrases[18]),
            turn("a2_stories_turn_011", "What did you do with the wallet you found?", "Bạn đã làm gì với chiếc ví tìm thấy?", phrases[20]),
            turn("a2_stories_turn_012", "How did the staff respond when you arrived late?", "Nhân viên phản ứng thế nào khi bạn đến muộn?", phrases[22]),
            turn("a2_stories_turn_013", "What happened during your lunch outside?", "Chuyện gì xảy ra trong bữa trưa ngoài trời?", phrases[24]),
            turn("a2_stories_turn_014", "Why did the journey take longer than normal?", "Tại sao hành trình lâu hơn bình thường?", phrases[26]),
            turn("a2_stories_turn_015", "How do you feel about changing your holiday plans now?", "Bây giờ bạn thấy thế nào về việc thay đổi kế hoạch nghỉ lễ?", phrases[29]),
        )
        return Lesson("a2_stories", "Stories & Unexpected Events", "Kể chuyện và tình huống bất ngờ", phrases, turns)
    }

    private fun turn(id: String, prompt: String, translation: String, phrase: Phrase) =
        ConversationTurn(prompt, translation, phrase.english, phrase.vietnamese, id)
}
