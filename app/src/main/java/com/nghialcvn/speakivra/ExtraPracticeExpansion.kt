package com.nghialcvn.speakivra

/** Append-only reading content and translation practice with stable IDs. */
internal object ExtraPracticeExpansion {
    private val additions = mapOf(
"passage_a2_experiences" to listOf(
ReadingSentence("passage_a2_experiences_s5", "I usually pack my lunch before leaving home.", "Tôi thường chuẩn bị bữa trưa mang theo trước khi ra khỏi nhà."),
ReadingSentence("passage_a2_experiences_s6", "The bus journey takes about twenty minutes.", "Chuyến xe buýt mất khoảng hai mươi phút."),
ReadingSentence("passage_a2_experiences_s7", "At lunchtime, I eat with two colleagues.", "Vào giờ trưa, tôi ăn cùng hai đồng nghiệp."),
ReadingSentence("passage_a2_experiences_s8", "We sometimes take a short walk after eating.", "Đôi khi chúng tôi đi bộ một lát sau khi ăn."),
ReadingSentence("passage_a2_experiences_s9", "Before bed, I prepare my clothes for the next day.", "Trước khi ngủ, tôi chuẩn bị quần áo cho ngày hôm sau."),
ReadingSentence("passage_a2_experiences_s10", "This routine helps me feel ready in the morning.", "Thói quen này giúp tôi thấy sẵn sàng vào buổi sáng.")
),
"passage_a2_plans" to listOf(
ReadingSentence("passage_a2_plans_s5", "We have checked the train times online.", "Chúng tôi đã kiểm tra giờ tàu trên mạng."),
ReadingSentence("passage_a2_plans_s6", "Everyone will bring a small bag and some water.", "Mỗi người sẽ mang túi nhỏ và ít nước."),
ReadingSentence("passage_a2_plans_s7", "I am going to buy the tickets this evening.", "Tối nay tôi sẽ mua vé."),
ReadingSentence("passage_a2_plans_s8", "We will meet outside the station at seven.", "Chúng tôi sẽ gặp bên ngoài ga lúc bảy giờ."),
ReadingSentence("passage_a2_plans_s9", "Before returning, we hope to try a local dessert.", "Trước khi về, chúng tôi hy vọng thử món tráng miệng địa phương."),
ReadingSentence("passage_a2_plans_s10", "We do not want to plan every minute of the day.", "Chúng tôi không muốn lên lịch cho từng phút trong ngày.")
),
"passage_a2_opinions" to listOf(
ReadingSentence("passage_a2_opinions_s5", "I called the restaurant to ask about a table.", "Tôi gọi nhà hàng để hỏi về bàn."),
ReadingSentence("passage_a2_opinions_s6", "The staff said they could seat us near a window.", "Nhân viên nói có thể xếp chúng tôi ngồi gần cửa sổ."),
ReadingSentence("passage_a2_opinions_s7", "My brother checked whether there were vegetarian dishes.", "Anh trai tôi kiểm tra xem có món chay không."),
ReadingSentence("passage_a2_opinions_s8", "We agreed to arrive at half past six.", "Chúng tôi thống nhất đến lúc sáu giờ rưỡi."),
ReadingSentence("passage_a2_opinions_s9", "The food was fresh, and the service was friendly.", "Đồ ăn tươi ngon và nhân viên phục vụ thân thiện."),
ReadingSentence("passage_a2_opinions_s10", "Next time, we would like to invite our grandparents too.", "Lần sau, chúng tôi cũng muốn mời ông bà.")
),
"passage_a2_problems" to listOf(
ReadingSentence("passage_a2_problems_s5", "The assistant asked me to check the label on the package.", "Nhân viên nhờ tôi kiểm tra nhãn trên gói hàng."),
ReadingSentence("passage_a2_problems_s6", "I sent the order number through the shop's official form.", "Tôi gửi mã đơn qua biểu mẫu chính thức của cửa hàng."),
ReadingSentence("passage_a2_problems_s7", "They explained how to return the wrong jacket.", "Họ giải thích cách trả áo khoác bị gửi nhầm."),
ReadingSentence("passage_a2_problems_s8", "I did not have to pay for the return delivery.", "Tôi không phải trả phí gửi hàng trả lại."),
ReadingSentence("passage_a2_problems_s9", "A few days later, the blue jacket arrived.", "Vài ngày sau, áo khoác xanh dương được giao đến."),
ReadingSentence("passage_a2_problems_s10", "I checked the colour and size before removing the label.", "Tôi kiểm tra màu và cỡ trước khi tháo nhãn.")
),
"passage_a2_travel" to listOf(
ReadingSentence("passage_a2_travel_s5", "The receptionist explained the breakfast hours before we went upstairs.", "Lễ tân giải thích giờ ăn sáng trước khi chúng tôi lên lầu."),
ReadingSentence("passage_a2_travel_s6", "We asked for a quiet room because we needed some rest.", "Chúng tôi xin phòng yên tĩnh vì cần nghỉ ngơi."),
ReadingSentence("passage_a2_travel_s7", "In the morning, we bought a map at a nearby shop.", "Buổi sáng, chúng tôi mua bản đồ ở cửa hàng gần đó."),
ReadingSentence("passage_a2_travel_s8", "The weather was warm enough to walk around the centre.", "Trời đủ ấm để đi bộ quanh trung tâm."),
ReadingSentence("passage_a2_travel_s9", "We stopped at a small cafe for lunch.", "Chúng tôi ghé quán cà phê nhỏ để ăn trưa."),
ReadingSentence("passage_a2_travel_s10", "By the evening, we had found several places to visit again.", "Đến tối, chúng tôi đã tìm được vài nơi muốn quay lại.")
),
"passage_b1_change" to listOf(
ReadingSentence("passage_b1_change_s6", "To see whether the routine was working, I kept a short note about how each morning felt.", "Để xem thói quen có hiệu quả không, tôi ghi ngắn gọn cảm nhận mỗi sáng."),
ReadingSentence("passage_b1_change_s7", "The notes showed that preparation mattered more than getting up much earlier.", "Ghi chép cho thấy chuẩn bị quan trọng hơn việc thức dậy sớm hơn nhiều."),
ReadingSentence("passage_b1_change_s8", "On a particularly busy evening, I reduced the preparation to the two most useful tasks.", "Vào một tối đặc biệt bận, tôi giảm việc chuẩn bị còn hai việc hữu ích nhất."),
ReadingSentence("passage_b1_change_s9", "That made the habit easier to maintain instead of turning it into another source of pressure.", "Điều đó giúp duy trì thói quen dễ hơn thay vì biến nó thành áp lực khác."),
ReadingSentence("passage_b1_change_s10", "I learned that a realistic routine can be more valuable than an ambitious plan that I rarely follow.", "Tôi nhận ra thói quen thực tế có thể giá trị hơn kế hoạch tham vọng mà tôi hiếm khi làm theo.")
),
"passage_b1_relationships" to listOf(
ReadingSentence("passage_b1_relationships_s6", "Before making suggestions, I asked whether she wanted advice or simply someone to listen.", "Trước khi góp ý, tôi hỏi cô ấy muốn lời khuyên hay chỉ cần người lắng nghe."),
ReadingSentence("passage_b1_relationships_s7", "She explained that an unclear handover was making her responsibilities difficult to understand.", "Cô ấy giải thích việc bàn giao không rõ khiến cô ấy khó hiểu trách nhiệm."),
ReadingSentence("passage_b1_relationships_s8", "Together, we considered how she could request a short meeting with her supervisor.", "Chúng tôi cùng cân nhắc cách cô ấy đề nghị gặp ngắn với giám sát."),
ReadingSentence("passage_b1_relationships_s9", "I avoided promising that a single conversation would solve every problem.", "Tôi tránh hứa một cuộc trao đổi sẽ giải quyết mọi vấn đề."),
ReadingSentence("passage_b1_relationships_s10", "A week later, she told me that asking specific questions had helped her feel more confident.", "Một tuần sau, cô ấy nói việc hỏi cụ thể đã giúp cô ấy tự tin hơn.")
),
"passage_b1_work" to listOf(
ReadingSentence("passage_b1_work_s6", "We agreed who would contact the customer and when the next update should be given.", "Chúng tôi thống nhất ai sẽ liên hệ khách và khi nào cần cập nhật tiếp."),
ReadingSentence("passage_b1_work_s7", "I separated confirmed information from details that still needed to be checked.", "Tôi tách thông tin đã xác nhận khỏi chi tiết vẫn cần kiểm tra."),
ReadingSentence("passage_b1_work_s8", "This prevented the next shift from treating an estimate as a promise.", "Điều này ngăn ca sau coi thời gian dự kiến là lời hứa."),
ReadingSentence("passage_b1_work_s9", "Before leaving, I asked my colleague to summarise the most important action in their own words.", "Trước khi về, tôi nhờ đồng nghiệp tóm tắt hành động quan trọng nhất bằng lời của họ."),
ReadingSentence("passage_b1_work_s10", "The brief check gave us a chance to correct a misunderstanding while we were both present.", "Việc kiểm tra ngắn giúp sửa hiểu lầm khi cả hai vẫn có mặt.")
),
"passage_b1_learning" to listOf(
ReadingSentence("passage_b1_learning_s6", "I chose one difficult phrase instead of trying to correct every mistake at once.", "Tôi chọn một cụm khó thay vì cố sửa mọi lỗi cùng lúc."),
ReadingSentence("passage_b1_learning_s7", "Then I compared a new recording with the earlier one to hear whether the ending was clearer.", "Sau đó tôi so bản ghi mới với bản trước để nghe âm cuối có rõ hơn không."),
ReadingSentence("passage_b1_learning_s8", "The comparison helped me notice progress that was difficult to remember from day to day.", "Việc so sánh giúp tôi nhận ra tiến bộ khó nhớ qua từng ngày."),
ReadingSentence("passage_b1_learning_s9", "I also practised the phrase in a different sentence so that I was not only memorising a script.", "Tôi cũng luyện cụm đó trong câu khác để không chỉ học thuộc kịch bản."),
ReadingSentence("passage_b1_learning_s10", "By focusing on one useful change, I made practice more manageable and easier to repeat.", "Nhờ tập trung một thay đổi hữu ích, tôi khiến việc luyện dễ thực hiện và lặp lại hơn.")
),
"passage_b1_travel" to listOf(
ReadingSentence("passage_b1_travel_s6", "The museum was smaller than the famous attractions, but its displays explained local life in detail.", "Bảo tàng nhỏ hơn các điểm nổi tiếng nhưng trưng bày giải thích chi tiết đời sống địa phương."),
ReadingSentence("passage_b1_travel_s7", "We spent longer there than expected because the guide answered our questions so thoughtfully.", "Chúng tôi ở lâu hơn dự kiến vì hướng dẫn viên trả lời rất chu đáo."),
ReadingSentence("passage_b1_travel_s8", "Afterwards, we discussed which part of the visit had surprised each of us most.", "Sau đó, chúng tôi bàn phần nào của chuyến thăm làm mỗi người bất ngờ nhất."),
ReadingSentence("passage_b1_travel_s9", "We still hoped to see the old town, so we kept the map for a future visit.", "Chúng tôi vẫn muốn xem phố cổ nên giữ bản đồ cho lần sau."),
ReadingSentence("passage_b1_travel_s10", "The change of plan taught us to protect the purpose of a trip rather than follow every detail rigidly.", "Việc đổi kế hoạch dạy chúng tôi giữ mục đích chuyến đi thay vì cứng nhắc theo từng chi tiết.")
)
    )
    private val newPassages = listOf(
ReadingPassage("passage_a2_neighbours", "A2", "a2_experiences", "Meeting a new neighbour", "Gặp hàng xóm mới", "Quan hệ hằng ngày", listOf(
ReadingSentence("passage_a2_neighbours_s1", "A new neighbour moved into the flat next to mine last week.", "Tuần trước, một hàng xóm mới chuyển vào căn hộ cạnh tôi."),
ReadingSentence("passage_a2_neighbours_s2", "I met her near the lift on Saturday morning.", "Tôi gặp cô ấy gần thang máy vào sáng thứ Bảy."),
ReadingSentence("passage_a2_neighbours_s3", "She was carrying several boxes, so I offered to hold the door.", "Cô ấy mang vài thùng đồ nên tôi đề nghị giữ cửa."),
ReadingSentence("passage_a2_neighbours_s4", "She thanked me and introduced herself as Mai.", "Cô ấy cảm ơn và giới thiệu tên là Mai."),
ReadingSentence("passage_a2_neighbours_s5", "We talked about the shops near our building.", "Chúng tôi nói về các cửa hàng gần tòa nhà."),
ReadingSentence("passage_a2_neighbours_s6", "I told her where she could buy fresh vegetables.", "Tôi chỉ nơi cô ấy có thể mua rau tươi."),
ReadingSentence("passage_a2_neighbours_s7", "She asked whether the nearby park was open in the evening.", "Cô ấy hỏi công viên gần đó có mở buổi tối không."),
ReadingSentence("passage_a2_neighbours_s8", "I said I often walked there after dinner.", "Tôi nói mình thường đi bộ ở đó sau bữa tối."),
ReadingSentence("passage_a2_neighbours_s9", "We agreed to say hello again when we met downstairs.", "Chúng tôi hẹn sẽ chào nhau khi gặp ở tầng dưới."),
ReadingSentence("passage_a2_neighbours_s10", "It was a simple conversation, but it made the building feel friendlier.", "Đó là cuộc trò chuyện đơn giản nhưng khiến tòa nhà thân thiện hơn.")
), listOf(PracticeVocabulary("Neighbour", "Hàng xóm"), PracticeVocabulary("Hold the door", "Giữ cửa"), PracticeVocabulary("Vegetables", "Rau"), PracticeVocabulary("Introduce yourself", "Tự giới thiệu"))),
ReadingPassage("passage_a2_class", "A2", "a2_plans", "Joining an evening class", "Tham gia lớp buổi tối", "Học tập", listOf(
ReadingSentence("passage_a2_class_s1", "I wanted to improve my cooking, so I looked for an evening class.", "Tôi muốn nấu ăn tốt hơn nên tìm lớp buổi tối."),
ReadingSentence("passage_a2_class_s2", "The community centre offered a course on Wednesdays.", "Trung tâm cộng đồng có khóa học vào thứ Tư."),
ReadingSentence("passage_a2_class_s3", "I called to ask about the price and starting date.", "Tôi gọi hỏi giá và ngày bắt đầu."),
ReadingSentence("passage_a2_class_s4", "The receptionist explained that I could try one lesson first.", "Lễ tân giải thích tôi có thể thử một buổi trước."),
ReadingSentence("passage_a2_class_s5", "I booked a place and wrote the time in my calendar.", "Tôi đăng ký chỗ và ghi giờ vào lịch."),
ReadingSentence("passage_a2_class_s6", "On the first evening, I arrived ten minutes early.", "Tối đầu tiên, tôi đến sớm mười phút."),
ReadingSentence("passage_a2_class_s7", "The teacher showed us how to prepare a simple soup.", "Giáo viên chỉ cách nấu món súp đơn giản."),
ReadingSentence("passage_a2_class_s8", "I worked with another student and learned several new words.", "Tôi làm cùng học viên khác và học vài từ mới."),
ReadingSentence("passage_a2_class_s9", "We tasted the soup together at the end of the lesson.", "Cuối buổi, chúng tôi cùng nếm súp."),
ReadingSentence("passage_a2_class_s10", "I enjoyed the class and decided to attend the full course.", "Tôi thích lớp học và quyết định theo cả khóa.")
), listOf(PracticeVocabulary("Evening class", "Lớp buổi tối"), PracticeVocabulary("Starting date", "Ngày bắt đầu"), PracticeVocabulary("Book a place", "Đăng ký chỗ"), PracticeVocabulary("Attend a course", "Theo học một khóa"))),
ReadingPassage("passage_a2_market", "A2", "a2_opinions", "A careful shopping choice", "Lựa chọn mua sắm cẩn thận", "Mua sắm", listOf(
ReadingSentence("passage_a2_market_s1", "I needed a small bag for work, but I did not want to spend too much.", "Tôi cần túi nhỏ đi làm nhưng không muốn tiêu quá nhiều."),
ReadingSentence("passage_a2_market_s2", "I compared two bags in a shop near my office.", "Tôi so hai chiếc túi ở cửa hàng gần văn phòng."),
ReadingSentence("passage_a2_market_s3", "The first one was cheaper and had a bright colour.", "Chiếc đầu rẻ hơn và có màu sáng."),
ReadingSentence("passage_a2_market_s4", "The second one had stronger handles and more pockets.", "Chiếc thứ hai có quai chắc hơn và nhiều ngăn hơn."),
ReadingSentence("passage_a2_market_s5", "I asked the assistant whether both bags had a guarantee.", "Tôi hỏi nhân viên cả hai túi có được bảo hành không."),
ReadingSentence("passage_a2_market_s6", "She explained the conditions without asking me to decide quickly.", "Cô ấy giải thích điều kiện mà không giục tôi quyết định."),
ReadingSentence("passage_a2_market_s7", "I checked whether my notebook would fit inside.", "Tôi kiểm tra xem sổ tay có vừa bên trong không."),
ReadingSentence("passage_a2_market_s8", "Then I chose the stronger bag because I would use it every day.", "Sau đó tôi chọn túi chắc hơn vì sẽ dùng hằng ngày."),
ReadingSentence("passage_a2_market_s9", "I kept the receipt in case there was a problem.", "Tôi giữ hóa đơn phòng khi có vấn đề."),
ReadingSentence("passage_a2_market_s10", "The extra cost seemed reasonable for something I needed often.", "Khoản tiền thêm có vẻ hợp lý cho món đồ tôi cần thường xuyên.")
), listOf(PracticeVocabulary("Compare", "So sánh"), PracticeVocabulary("Guarantee", "Bảo hành"), PracticeVocabulary("Receipt", "Hóa đơn"), PracticeVocabulary("Reasonable", "Hợp lý"))),
ReadingPassage("passage_a2_repair", "A2", "a2_problems", "Reporting a small repair", "Báo việc sửa chữa nhỏ", "Nhà ở", listOf(
ReadingSentence("passage_a2_repair_s1", "The light above my desk stopped working on Monday.", "Đèn trên bàn tôi ngừng hoạt động vào thứ Hai."),
ReadingSentence("passage_a2_repair_s2", "I checked the switch but did not open the fitting.", "Tôi kiểm tra công tắc nhưng không mở bộ đèn."),
ReadingSentence("passage_a2_repair_s3", "I sent a repair request to the building office.", "Tôi gửi yêu cầu sửa cho văn phòng tòa nhà."),
ReadingSentence("passage_a2_repair_s4", "The assistant asked me which room was affected.", "Nhân viên hỏi phòng nào bị ảnh hưởng."),
ReadingSentence("passage_a2_repair_s5", "I described the problem and included a photograph.", "Tôi mô tả vấn đề và gửi kèm ảnh."),
ReadingSentence("passage_a2_repair_s6", "We agreed on a time when I would be at home.", "Chúng tôi thống nhất giờ tôi có nhà."),
ReadingSentence("passage_a2_repair_s7", "The repair worker arrived the next afternoon.", "Thợ sửa đến vào chiều hôm sau."),
ReadingSentence("passage_a2_repair_s8", "He checked the fitting and explained what needed replacing.", "Anh ấy kiểm tra bộ đèn và giải thích cần thay gì."),
ReadingSentence("passage_a2_repair_s9", "After the repair, we tested the light together.", "Sau khi sửa, chúng tôi cùng thử đèn."),
ReadingSentence("passage_a2_repair_s10", "I thanked him and confirmed that the request was complete.", "Tôi cảm ơn và xác nhận yêu cầu đã hoàn tất.")
), listOf(PracticeVocabulary("Fitting", "Bộ phận lắp đặt"), PracticeVocabulary("Repair request", "Yêu cầu sửa chữa"), PracticeVocabulary("Affected", "Bị ảnh hưởng"), PracticeVocabulary("Replace", "Thay thế"))),
ReadingPassage("passage_a2_bus", "A2", "a2_travel", "Finding another way home", "Tìm đường khác về nhà", "Đi lại", listOf(
ReadingSentence("passage_a2_bus_s1", "I was waiting for my usual bus when I saw a service notice.", "Tôi đang chờ xe buýt thường đi thì thấy thông báo dịch vụ."),
ReadingSentence("passage_a2_bus_s2", "The route was closed for road repairs that evening.", "Tuyến đường bị đóng để sửa vào tối đó."),
ReadingSentence("passage_a2_bus_s3", "I asked a station assistant how I could get home.", "Tôi hỏi nhân viên trạm cách về nhà."),
ReadingSentence("passage_a2_bus_s4", "She showed me another bus that stopped near my street.", "Cô ấy chỉ xe buýt khác dừng gần phố tôi."),
ReadingSentence("passage_a2_bus_s5", "The journey was longer, but I would not need a taxi.", "Chuyến đi lâu hơn nhưng tôi không cần đi taxi."),
ReadingSentence("passage_a2_bus_s6", "I checked the stop name before getting on.", "Tôi kiểm tra tên điểm dừng trước khi lên xe."),
ReadingSentence("passage_a2_bus_s7", "Then I sent my family a message about the delay.", "Sau đó tôi nhắn gia đình về việc về muộn."),
ReadingSentence("passage_a2_bus_s8", "A passenger helped me notice the correct stop.", "Một hành khách giúp tôi nhận ra đúng điểm dừng."),
ReadingSentence("passage_a2_bus_s9", "I walked the last few minutes in light rain.", "Tôi đi bộ mấy phút cuối dưới mưa nhẹ."),
ReadingSentence("passage_a2_bus_s10", "Next time, I will check service updates before leaving work.", "Lần sau, tôi sẽ xem cập nhật dịch vụ trước khi tan làm.")
), listOf(PracticeVocabulary("Service notice", "Thông báo dịch vụ"), PracticeVocabulary("Route", "Tuyến đường"), PracticeVocabulary("Delay", "Sự chậm trễ"), PracticeVocabulary("Passenger", "Hành khách"))),
ReadingPassage("passage_b1_housing", "B1", "b1_housing", "Agreeing on shared space", "Thống nhất không gian chung", "Nhà ở và thỏa thuận", listOf(
ReadingSentence("passage_b1_housing_s1", "When a new flatmate moved in, we discovered that our daily routines were quite different.", "Khi bạn cùng nhà mới chuyển đến, chúng tôi nhận ra nếp sinh hoạt khá khác nhau."),
ReadingSentence("passage_b1_housing_s2", "I usually cooked late, whereas she needed to sleep early before her morning shift.", "Tôi thường nấu muộn, còn cô ấy cần ngủ sớm trước ca sáng."),
ReadingSentence("passage_b1_housing_s3", "Rather than wait until one of us became annoyed, we arranged a short conversation.", "Thay vì chờ một người bực bội, chúng tôi sắp xếp nói chuyện ngắn."),
ReadingSentence("passage_b1_housing_s4", "We each explained which parts of the routine were flexible and which were difficult to change.", "Mỗi người giải thích phần nào của nếp sinh hoạt có thể linh hoạt và phần nào khó đổi."),
ReadingSentence("passage_b1_housing_s5", "I offered to prepare noisy ingredients earlier and wash up more quietly afterwards.", "Tôi đề nghị sơ chế những thứ gây ồn sớm hơn và rửa dọn nhẹ nhàng hơn sau đó."),
ReadingSentence("passage_b1_housing_s6", "She agreed to keep the shared table clear so that I could prepare food without moving her work.", "Cô ấy đồng ý để bàn chung trống để tôi chuẩn bị đồ ăn mà không phải di chuyển việc của cô ấy."),
ReadingSentence("passage_b1_housing_s7", "We wrote down the main points because small details are easy to remember differently.", "Chúng tôi ghi các ý chính vì chi tiết nhỏ dễ được mỗi người nhớ khác nhau."),
ReadingSentence("passage_b1_housing_s8", "After a week, we checked whether the arrangement was practical for both of us.", "Sau một tuần, chúng tôi kiểm tra thỏa thuận có thực tế với cả hai không."),
ReadingSentence("passage_b1_housing_s9", "One part needed adjusting, but neither of us treated that as a failure.", "Một phần cần điều chỉnh nhưng không ai coi đó là thất bại."),
ReadingSentence("passage_b1_housing_s10", "The experience showed us that a useful agreement leaves room for honest feedback.", "Trải nghiệm cho thấy thỏa thuận hữu ích cần dành chỗ cho phản hồi thật lòng.")
), listOf(PracticeVocabulary("Flatmate", "Bạn cùng nhà"), PracticeVocabulary("Flexible", "Linh hoạt"), PracticeVocabulary("Shared space", "Không gian chung"), PracticeVocabulary("Maintain", "Duy trì"))),
ReadingPassage("passage_b1_budget", "B1", "b1_budget", "Planning a realistic budget", "Lập ngân sách thực tế", "Chi tiêu", listOf(
ReadingSentence("passage_b1_budget_s1", "I wanted to save for a short course, but I had never looked closely at my monthly spending.", "Tôi muốn tiết kiệm để học khóa ngắn nhưng chưa xem kỹ chi tiêu hằng tháng."),
ReadingSentence("passage_b1_budget_s2", "Instead of guessing where the money went, I recorded my expenses for several weeks.", "Thay vì đoán tiền đi đâu, tôi ghi chi phí trong vài tuần."),
ReadingSentence("passage_b1_budget_s3", "The record showed that many small convenience purchases added up to a considerable amount.", "Ghi chép cho thấy nhiều khoản mua nhỏ cho tiện cộng lại thành số tiền đáng kể."),
ReadingSentence("passage_b1_budget_s4", "I separated essential costs from things that I could reduce without causing serious difficulty.", "Tôi tách chi phí thiết yếu khỏi những khoản có thể giảm mà không gây khó khăn lớn."),
ReadingSentence("passage_b1_budget_s5", "Then I chose a savings target that left some room for unexpected expenses.", "Sau đó tôi chọn mục tiêu tiết kiệm vẫn chừa khoản cho chi phí bất ngờ."),
ReadingSentence("passage_b1_budget_s6", "A friend suggested cutting every leisure activity, but I knew that plan would be hard to maintain.", "Bạn gợi ý cắt mọi hoạt động giải trí nhưng tôi biết kế hoạch đó khó duy trì."),
ReadingSentence("passage_b1_budget_s7", "I decided to cook at home more often while keeping one inexpensive outing each week.", "Tôi quyết định nấu ở nhà thường xuyên hơn nhưng vẫn giữ một buổi đi chơi ít tốn mỗi tuần."),
ReadingSentence("passage_b1_budget_s8", "At the end of the month, I compared the actual spending with my original estimate.", "Cuối tháng, tôi so chi tiêu thực với dự kiến ban đầu."),
ReadingSentence("passage_b1_budget_s9", "Although I had saved less than I hoped, I understood which changes were working.", "Dù tiết kiệm ít hơn mong đợi, tôi hiểu thay đổi nào có hiệu quả."),
ReadingSentence("passage_b1_budget_s10", "That information helped me make the next month's plan more realistic.", "Thông tin đó giúp tôi lập kế hoạch tháng sau thực tế hơn.")
), listOf(PracticeVocabulary("Essential costs", "Chi phí thiết yếu"), PracticeVocabulary("Savings target", "Mục tiêu tiết kiệm"), PracticeVocabulary("Estimate", "Dự kiến"), PracticeVocabulary("Unexpected expenses", "Chi phí bất ngờ"))),
ReadingPassage("passage_b1_digital", "B1", "b1_digital", "Checking a suspicious message", "Kiểm tra tin nhắn đáng ngờ", "Đời sống số", listOf(
ReadingSentence("passage_b1_digital_s1", "While travelling, I received a message claiming that my hotel payment had failed.", "Khi đi du lịch, tôi nhận tin nhắn nói thanh toán khách sạn bị lỗi."),
ReadingSentence("passage_b1_digital_s2", "It included a link and warned that my room would be cancelled unless I acted immediately.", "Tin có đường dẫn và cảnh báo phòng sẽ bị hủy nếu tôi không hành động ngay."),
ReadingSentence("passage_b1_digital_s3", "The pressure to respond quickly made me suspicious, so I did not open the link.", "Sự thúc ép trả lời nhanh khiến tôi nghi ngờ nên không mở đường dẫn."),
ReadingSentence("passage_b1_digital_s4", "I found the hotel's contact details in the booking confirmation I already had.", "Tôi tìm thông tin liên hệ khách sạn trong xác nhận đặt phòng đã có."),
ReadingSentence("passage_b1_digital_s5", "The receptionist checked my reservation through the normal verification process.", "Lễ tân kiểm tra đặt phòng qua quy trình xác minh thông thường."),
ReadingSentence("passage_b1_digital_s6", "She confirmed that the booking was valid and asked me to report the message through the proper channel.", "Cô ấy xác nhận đặt phòng còn hiệu lực và nhờ tôi báo tin qua kênh phù hợp."),
ReadingSentence("passage_b1_digital_s7", "I shared only the details necessary for the investigation and covered unrelated personal information.", "Tôi chỉ chia sẻ thông tin cần thiết để kiểm tra và che thông tin cá nhân không liên quan."),
ReadingSentence("passage_b1_digital_s8", "Afterwards, I warned my travelling companion without forwarding the suspicious link.", "Sau đó tôi cảnh báo người đi cùng mà không chuyển tiếp đường dẫn đáng ngờ."),
ReadingSentence("passage_b1_digital_s9", "The experience reminded me to verify urgent requests through an independent contact method.", "Trải nghiệm nhắc tôi xác minh yêu cầu gấp bằng cách liên hệ độc lập."),
ReadingSentence("passage_b1_digital_s10", "Taking a few extra minutes prevented a rushed decision based on an unverified claim.", "Dành thêm vài phút giúp tránh quyết định vội dựa trên lời khẳng định chưa xác minh.")
), listOf(PracticeVocabulary("Suspicious", "Đáng ngờ"), PracticeVocabulary("Verify", "Xác minh"), PracticeVocabulary("Independent", "Độc lập"), PracticeVocabulary("Personal information", "Thông tin cá nhân"))),
ReadingPassage("passage_b1_community", "B1", "b1_community", "Organising a shared activity", "Tổ chức hoạt động chung", "Cộng đồng", listOf(
ReadingSentence("passage_b1_community_s1", "Our neighbourhood group wanted to organise a weekend clean-up, but volunteers had different schedules.", "Nhóm khu phố muốn tổ chức dọn vệ sinh cuối tuần nhưng tình nguyện viên có lịch khác nhau."),
ReadingSentence("passage_b1_community_s2", "At first, we tried to find one time that suited everybody and made very little progress.", "Ban đầu chúng tôi cố tìm một giờ hợp tất cả nên tiến triển rất ít."),
ReadingSentence("passage_b1_community_s3", "Someone suggested dividing the activity into two shorter sessions with a clear handover.", "Có người đề nghị chia hoạt động thành hai buổi ngắn hơn có bàn giao rõ ràng."),
ReadingSentence("passage_b1_community_s4", "We listed the tasks and asked volunteers to choose work that matched their availability.", "Chúng tôi liệt kê công việc và nhờ mọi người chọn việc hợp thời gian rảnh."),
ReadingSentence("passage_b1_community_s5", "The organiser checked which materials were available before asking anyone to buy more.", "Người tổ chức kiểm tra vật dụng sẵn có trước khi nhờ ai mua thêm."),
ReadingSentence("passage_b1_community_s6", "We also agreed that unsafe objects should be left for the appropriate service team.", "Chúng tôi cũng thống nhất để vật không an toàn cho bộ phận phù hợp xử lý."),
ReadingSentence("passage_b1_community_s7", "On the day, a short briefing helped new volunteers understand where to start.", "Hôm đó, việc phổ biến ngắn giúp người mới hiểu nên bắt đầu ở đâu."),
ReadingSentence("passage_b1_community_s8", "The second team used the notes from the first session instead of repeating completed tasks.", "Nhóm thứ hai dùng ghi chép buổi đầu thay vì làm lại việc đã xong."),
ReadingSentence("passage_b1_community_s9", "Not every planned area was finished, so we recorded what remained for another day.", "Không phải khu vực dự kiến nào cũng xong nên chúng tôi ghi phần còn lại cho hôm khác."),
ReadingSentence("passage_b1_community_s10", "The flexible plan allowed more people to contribute without promising an unrealistic result.", "Kế hoạch linh hoạt cho nhiều người đóng góp mà không hứa kết quả thiếu thực tế.")
), listOf(PracticeVocabulary("Volunteer", "Tình nguyện viên"), PracticeVocabulary("Availability", "Thời gian sẵn có"), PracticeVocabulary("Handover", "Bàn giao"), PracticeVocabulary("Contribute", "Đóng góp"))),
ReadingPassage("passage_b1_services", "B1", "b1_services", "Following up a service problem", "Theo dõi vấn đề dịch vụ", "Dịch vụ và phản hồi", listOf(
ReadingSentence("passage_b1_services_s1", "A delivery company told me that my parcel had arrived, although I could not find it.", "Công ty giao hàng báo bưu kiện đã đến dù tôi không tìm thấy."),
ReadingSentence("passage_b1_services_s2", "Before making a complaint, I checked the delivery address and asked the building reception.", "Trước khi khiếu nại, tôi kiểm tra địa chỉ và hỏi lễ tân tòa nhà."),
ReadingSentence("passage_b1_services_s3", "When those checks did not resolve the issue, I contacted the company's official support channel.", "Khi kiểm tra đó không giải quyết được, tôi liên hệ kênh hỗ trợ chính thức của công ty."),
ReadingSentence("passage_b1_services_s4", "I described the facts in order and provided the reference number without including unnecessary personal details.", "Tôi mô tả sự việc theo thứ tự và cung cấp mã tham chiếu mà không kèm thông tin cá nhân không cần thiết."),
ReadingSentence("passage_b1_services_s5", "The agent explained which team would investigate and when I could expect an update.", "Nhân viên giải thích đội nào sẽ kiểm tra và khi nào tôi có thể nhận cập nhật."),
ReadingSentence("passage_b1_services_s6", "I asked whether that time was a confirmed deadline or only an estimate.", "Tôi hỏi thời gian đó là hạn đã xác nhận hay chỉ dự kiến."),
ReadingSentence("passage_b1_services_s7", "We agreed that I would quote the same case number if I needed to contact them again.", "Chúng tôi thống nhất tôi sẽ nêu cùng mã vụ việc nếu cần liên hệ lại."),
ReadingSentence("passage_b1_services_s8", "The parcel was later found at another entrance, and the company explained the collection process.", "Sau đó bưu kiện được tìm ở lối vào khác và công ty giải thích cách nhận."),
ReadingSentence("passage_b1_services_s9", "I confirmed receipt so that the investigation could be closed with an accurate record.", "Tôi xác nhận nhận hàng để việc kiểm tra được kết thúc với ghi chép chính xác."),
ReadingSentence("passage_b1_services_s10", "Clear information and a specific follow-up plan helped us solve the problem without repeated explanations.", "Thông tin rõ và kế hoạch theo dõi cụ thể giúp giải quyết mà không phải giải thích lặp lại.")
), listOf(PracticeVocabulary("Reference number", "Mã tham chiếu"), PracticeVocabulary("Investigation", "Việc kiểm tra"), PracticeVocabulary("Deadline", "Thời hạn"), PracticeVocabulary("Confirm receipt", "Xác nhận đã nhận")))
    )
    fun passages(original: List<ReadingPassage>) = original.map {
        it.copy(sentences = it.sentences + additions[it.id].orEmpty())
    } + newPassages
    // Existing questions and alternatives remain untouched. New drills use reviewed bilingual
    // curriculum lines, avoiding duplicate prompts already used in this exercise level.
    fun exercises(original: List<SpeakingExercise>): List<SpeakingExercise> = original +
        listOf("A2", "B1").flatMap { level ->
            val lessons = if (level == "A2") Lessons.a2 else Lessons.b1
            val used = original.filter { it.level == level }.flatMap { it.questions.flatMap { q -> q.answers } }.toMutableSet()
            (if (level == "B1") lessons.drop(5) else lessons).take(15).mapIndexed { index, lesson ->
                val count = if (index < 10) 5 else 4
                val phrases = lesson.phrases.filter { it.english !in used }.distinctBy { it.english }.take(count)
                check(phrases.size == count)
                used.addAll(phrases.map { it.english })
                SpeakingExercise("exercise_extended_${lesson.id}", level, lesson.id, "${lesson.vietnamese} • Luyện thêm",
                    phrases.map { ExerciseQuestion("exercise_extended_${it.id}", it.vietnamese, listOf(it.english)) })
            }
        }
}
