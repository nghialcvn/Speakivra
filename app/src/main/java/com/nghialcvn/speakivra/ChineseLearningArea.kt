package com.nghialcvn.speakivra

import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Language navigation is outside the course; no English/Chinese tabs inside a lesson. */
@Composable
internal fun ChineseLearningArea(preferences: SharedPreferences, learner: Learner, initialPage: String,
    onSwitchLanguage: () -> Unit, onSettings: () -> Unit) {
    var page by rememberSaveable(initialPage) { mutableStateOf(initialPage) }
    var courseTopic by rememberSaveable { mutableStateOf<String?>(null) }
    var coursePack by rememberSaveable { mutableStateOf("") }
    var courseMode by rememberSaveable { mutableStateOf("") }
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> revision++ }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val topics = remember(learner.name) { ChineseCurriculum.topics.map { it.personalize(learner.name) } }
    val store = remember(preferences) { ChineseProgress(preferences) }
    val active = remember(revision, topics) { topics.firstOrNull { it.id == preferences.getString("zh.active_lesson", null) } ?: topics.first() }
    fun resume(topic: ChineseTopic = active) {
        courseTopic = topic.id
        val pending = store.resume(topic)
        courseMode = pending.mode; coursePack = pending.packId
        page = "course"
    }
    BackHandler(page != "home" && page != "course") { page = "home" }
    Column(Modifier.fillMaxSize()) {
        if (page == "home") StudyLanguageBar(StudyLanguage.CHINESE, onSwitchLanguage)
        Box(Modifier.weight(1f)) {
            when (page) {
                "home" -> {
                    val pending = store.firstPending(active, "repeat")
                    HomeScreen(active.lesson, LessonProgress(phrase = pending), preferences.getInt("daily_goal", 20),
                        topics.sumOf { preferences.getInt("lesson.${it.id}.rounds", 0) }, learner.name, learner, preferences,
                        onContinue = { resume() }, onLessons = { courseTopic = null; courseMode = ""; coursePack = ""; page = "course" }, onProfile = onSettings,
                        lessonHeading = if (active.level in listOf("FO", "HK")) "Khách sạn • ${active.title}" else
                            "Bài ${topics.filter { it.level == active.level }.indexOf(active) + 1} • ${if (active.id == "zh_a1_greetings") "Chào hỏi" else active.title}",
                        lessonNote = "Pinyin • Nghĩa tiếng Việt",
                        courseSummary = "A1 • A2 • B1 • B2 • C1 • C2 • Hội thoại • Đọc hiểu • Bài tập • Khách sạn")
                }
                "course", "lessons", "repeat" -> ChineseCourseScreen(preferences, learner, onBack = { page = "home" },
                    initialTopic = if (page == "repeat") active.id else courseTopic,
                    initialMode = if (page == "repeat") courseMode.ifEmpty { "repeat" } else courseMode, initialPackId = coursePack)
                "progress" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PageHeading("Tiến độ học tập", "Tiếng Trung • Lưu riêng theo hồ sơ", onBack = { page = "home" })
                    StudioCard {
                        Text("${topics.count { store.finished(it) }}/${topics.size} chủ đề hoàn thành", fontWeight = FontWeight.ExtraBold)
                        AchievementSummary(topics.sumOf { store.diamonds(it) })
                    }
                    (CourseLevels.all + listOf("FO", "HK")).forEach { level ->
                        val items = topics.filter { it.level == level }
                        StudioCard {
                            Text(if (level in CourseLevels.all) CourseLevels.title(level) else HotelCurriculum.roleLabel(level), fontWeight = FontWeight.Bold)
                            Text("${items.sumOf { store.done(it, "repeat") }}/${items.sumOf { it.phrases.size }} câu luyện nói")
                            Text("${items.sumOf { store.done(it, "chat") }}/${items.sumOf { it.turns.size }} lượt hội thoại")
                            Text("${items.sumOf { store.done(it, "read") }}/${items.sumOf { it.reading.size }} câu bài đọc")
                            Text("${items.sumOf { store.done(it, "exercise") }}/${items.sumOf { it.exercises.size }} câu Việt → Trung")
                            Text("${items.count { store.finished(it) }}/${items.size} chủ đề hoàn thành")
                            TextButton(onClick = { resume(items.firstOrNull { !store.finished(it) } ?: items.first()) }) { Text("Tiếp tục học") }
                        }
                    }
                }
            }
        }
        StudioBottomNavigation(if (page == "course") "lessons" else page) { destination ->
            if (destination == "profile") onSettings()
            else {
                if (destination == "lessons") { courseTopic = null; courseMode = ""; coursePack = "" }
                if (destination == "repeat") { resume(); return@StudioBottomNavigation }
                page = destination
            }
        }
    }
}
