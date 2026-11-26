package com.nghialcvn.speakivra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable
internal fun ExtraCatalogueScreen(passages: Boolean, level: String, completed: Set<String>,
    load: (Lesson) -> LessonProgress, onLevel: (String) -> Unit, onBack: () -> Unit,
    onPassage: (ReadingPassage) -> Unit, onExercise: (SpeakingExercise) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PageHeading(if (passages) "Luyện nói theo đoạn văn" else "Tiếng Anh nói thế nào?", "A1 • A2 • B1", onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("A1", "A2", "B1").forEach {
                FilterChip(selected = it == level, onClick = { onLevel(it) }, label = { Text(it) })
            }
        }
        if (passages) ExtraPracticeData.passages.filter { it.level == level }.forEach { item ->
            ExtraCatalogueCard(item.title, item.vietnamese,
                "${item.sentences.count { it.id in completed }}/${item.sentences.size} câu • ${item.english.split(' ').size} từ",
                extraTopicOpen(item.lessonId, load), item.id in completed) { onPassage(item) }
        } else ExtraPracticeData.exercises.filter { it.level == level }.forEach { item ->
            ExtraCatalogueCard(item.topic, "Tiếng Anh nói thế nào?",
                "${item.questions.count { it.id in completed }}/${item.questions.size} câu",
                extraTopicOpen(item.lessonId, load), item.questions.all { it.id in completed }) { onExercise(item) }
        }
        Text("Học xong chủ đề 3 để mở chủ đề 4, rồi tiếp tục theo thứ tự bài học.", color = studio.muted, fontSize = 12.sp, lineHeight = 15.sp)
    }
}
@Composable
private fun ExtraCatalogueCard(title: String, subtitle: String, progress: String, open: Boolean,
    done: Boolean, onClick: () -> Unit) {
    StudioCard(Modifier.clickable(enabled = open, role = Role.Button, onClick = onClick)
        .semantics { if (!open) disabled() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, lineHeight = 23.sp)
                Text(subtitle, color = studio.muted, fontSize = 14.sp, lineHeight = 18.sp)
                Text(if (open) progress else "Đang khóa", color = studio.coral, fontSize = 12.sp, lineHeight = 15.sp)
            }
            StudioIcon(if (!open) "lock" else if (done) "check" else "chevron", Modifier.size(22.dp), studio.coral)
        }
    }
}
@Composable
private fun ExtraMicrophone(phase: String, prompt: String, onSpeak: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly) {
            DecorativeWave()
            Microphone(onSpeak, Modifier.size(94.dp), large = true, active = phase == "listening")
            DecorativeWave()
        }
        Text(captureLabel(phase, prompt), color = studio.muted, fontSize = 14.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
    }
}
@Composable
private fun ExtraSpeed(speed: Int, onSpeed: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ExtraOutline("${speedLabel(speed)} ⌄", onClick = { expanded = true })
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            listOf(75, 100, 125).forEach { value ->
                DropdownMenuItem(text = { Text(speedLabel(value)) }, onClick = { expanded = false; onSpeed(value) })
            }
        }
    }
}
@Composable
internal fun PassagePracticeScreen(item: ReadingPassage, selected: Int, whole: Boolean, translation: Boolean,
    speed: Int, voice: VoiceChoice, learner: Learner, finalTranscript: String?, transcript: String,
    phase: String, status: String, onBack: () -> Unit, onSentence: (Int) -> Unit, onWhole: (Boolean) -> Unit,
    onSpeak: () -> Unit, onListen: (String) -> Unit, onSpeed: (Int) -> Unit, onTranslation: () -> Unit,
    onNext: () -> Unit) {
    val p = studio
    val sentence = item.sentences[selected]
    val result = matchWords(if (whole) item.english else sentence.english, finalTranscript)
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeading("Luyện nói theo đoạn văn", "${item.level} • ${item.topic}", onBack)
        StudioProgress((selected + 1f) / item.sentences.size)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TwoChoices("Từng câu", "Cả đoạn", !whole, { onWhole(!it) }, Modifier.fillMaxWidth())
            StudioCard(contentPadding = 10.dp, spacing = 4.dp) {
                Text(item.title, fontSize = 21.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold)
                val annotated = buildAnnotatedString {
                    var tokenOffset = 0
                    item.sentences.forEachIndexed { index, part ->
                        if (index > 0) append(" ")
                        pushStringAnnotation("sentence", index.toString())
                        pushStyle(SpanStyle(background = if (index == selected) p.coral.copy(alpha = .15f) else Color.Transparent))
                        val tokens = matchWords(part.english, null).tokens
                        val vocabularyRanges = (item.vocabulary.map { it.english } + "Cook dinner").flatMap { phrase ->
                            Regex(Regex.escape(phrase), RegexOption.IGNORE_CASE).findAll(part.english).map { it.range }.toList()
                        }
                        var characterOffset = 0
                        tokens.forEachIndexed { wordIndex, word ->
                            if (wordIndex > 0) { append(" "); characterOffset++ }
                            val inVocabulary = vocabularyRanges.any { characterOffset in it }
                            val scoreIndex = if (whole) tokenOffset + wordIndex else wordIndex
                            val scored = result.hasResult && (whole || index == selected)
                            withStyle(SpanStyle(color = if (scored) {
                                if (result.matched.getOrElse(scoreIndex) { false }) p.correctWord else p.incorrectWord
                            } else if (inVocabulary) p.coral else p.ink,
                                fontWeight = if (inVocabulary) FontWeight.Bold else FontWeight.Normal)) { append(word) }
                            characterOffset += word.length
                        }
                        tokenOffset += tokens.size
                        pop(); pop()
                    }
                }
                @Suppress("DEPRECATION")
                ClickableText(annotated, style = MaterialTheme.typography.bodyLarge.copy(color = p.ink,
                    fontSize = 16.sp, lineHeight = 23.sp), onClick = { offset ->
                    annotated.getStringAnnotations("sentence", offset, offset).firstOrNull()?.let { onSentence(it.item.toInt()) }
                }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Chọn câu trong đoạn văn" })
                IpaLine(sentence.english, voice.accent, Modifier.fillMaxWidth())
                if (translation) Text(sentence.vietnamese, color = p.muted, fontSize = 14.sp, lineHeight = 18.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                var listenMenu by remember { mutableStateOf(false) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Box {
                        ExtraOutline("Nghe đoạn", speaker = true, onClick = { listenMenu = true })
                        DropdownMenu(listenMenu, onDismissRequest = { listenMenu = false }) {
                            DropdownMenuItem(text = { Text("Nghe cả đoạn") }, onClick = { listenMenu = false; onListen(item.english) })
                            DropdownMenuItem(text = { Text("Nghe câu đang chọn") }, onClick = { listenMenu = false; onListen(sentence.english) })
                        }
                    }
                    ExtraSpeed(speed, onSpeed)
                    ExtraOutline(if (translation) "Ẩn dịch" else "Hiện dịch", onTranslation)
                }
            }
            StudioCard(contentPadding = 8.dp, spacing = 4.dp) {
                Text("Từ vựng & cụm từ", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 19.sp)
                item.vocabulary.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { vocab -> StudioCard(Modifier.weight(1f).clickable(role = Role.Button) { onListen(vocab.english) }, border = false, contentPadding = 6.dp, spacing = 2.dp) {
                            Text(vocab.english, color = p.coral, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp)
                            Text(vocab.vietnamese, color = p.muted, fontSize = 12.sp, lineHeight = 15.sp)
                        } }
                    }
                }
            }
            ExtraMicrophone(phase, if (whole) "Chạm để đọc cả đoạn" else "Chạm để đọc câu đang chọn", onSpeak)
            SpokenResult(learner, if (phase == "idle") finalTranscript.orEmpty() else transcript,
                if (phase == "idle" && result.hasResult) result.canContinue else null,
                reference = if (phase == "idle") { if (whole) item.english else sentence.english } else null)
            if (result.hasResult) Text("Khớp ${result.correct}/${result.tokens.size} từ · ${result.matchPercent}%", color = if (result.incorrect == 0) p.correctWord else p.incorrectWord, fontSize = 13.sp, lineHeight = 16.sp)
            if (status.isNotBlank() && (phase != "idle" || finalTranscript.isNullOrBlank())) Text(status, color = p.muted, fontSize = 12.sp, lineHeight = 15.sp)
        }
        if (!whole) {
            CoralButton(if (result.canContinue && phase == "idle") "Câu tiếp theo" else "Luyện cả đoạn",
                chevron = false, labelColor = if (p == NeonStudio) Color(0xFF071B3F) else null,
                onClick = { if (result.canContinue && phase == "idle") onNext() else onWhole(true) })
        } else CoralButton(if (result.canContinue && phase == "idle") "Hoàn thành đoạn văn" else "Cần nói lại",
            enabled = result.canContinue && phase == "idle", chevron = false,
            labelColor = if (p == NeonStudio) Color(0xFF071B3F) else null, onClick = onNext)
    }
}
@Composable
internal fun SpeakingExerciseScreen(item: SpeakingExercise, selected: Int, completed: Set<String>, voice: VoiceChoice,
    learner: Learner, finalTranscript: String?, transcript: String, phase: String, status: String,
    onBack: () -> Unit, onSpeak: () -> Unit, onListen: (String) -> Unit, onNext: () -> Unit) {
    val p = studio; val question = item.questions[selected]
    val evaluation = evaluateExercise(question, finalTranscript)
    val accepted = evaluation.accepted && phase == "idle"
    var hint by remember(question.id) { mutableStateOf(false) }
    var reveal by remember(question.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PageHeading("Tiếng Anh nói thế nào?", "${item.level} • ${item.topic} • Câu ${selected + 1}/${item.questions.size}", onBack)
        StudioProgress((selected + 1f) / item.questions.size)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item.questions.forEachIndexed { index, row ->
                val active = index == selected
                val saved = row.id in completed && !active
                StudioCard(border = active, contentPadding = 6.dp, spacing = 4.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(23.dp).clip(RoundedCornerShape(5.dp)).background(p.coral.copy(alpha = .16f)), Alignment.Center) {
                            Text("${index + 1}", color = p.ink, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(row.vietnamese, Modifier.weight(1f), fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp, lineHeight = 18.sp)
                        val shown = saved || (active && (accepted || reveal))
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(6.dp))
                            .background(if (shown) p.coral.copy(alpha = .12f) else Color.Transparent)
                            .then(if (shown) Modifier.border(1.dp, p.border, RoundedCornerShape(6.dp)) else Modifier.drawBehind {
                                drawRoundRect(p.border, cornerRadius = CornerRadius(6.dp.toPx()),
                                    style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))))
                            }).padding(horizontal = 7.dp, vertical = 4.dp), Alignment.Center) {
                            val displayed = if (shown) { if (active && accepted) evaluation.reference else row.answer } else "?"
                            val feedback = if (shown && active && phase == "idle" && !finalTranscript.isNullOrBlank())
                                matchWords(displayed, finalTranscript) else null
                            Text(if (shown) wordFeedback(displayed, feedback) else androidx.compose.ui.text.AnnotatedString("?"),
                                color = if (shown) p.ink else p.muted,
                                fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                        if (saved || (active && accepted)) StudioIcon("check", Modifier.size(18.dp), p.correctWord)
                    }
                }
                if (active) {
                        if (accepted || reveal) IpaLine(if (accepted) evaluation.reference else row.answer, voice.accent, Modifier.fillMaxWidth())
                        if (hint && !accepted && !reveal) Text("Gợi ý: ${row.answer.substringBefore(' ')} …", color = p.muted, fontSize = 12.sp, lineHeight = 15.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                            ExtraOutline("Gợi ý", onClick = { hint = true }, bulb = true)
                            ExtraOutline("Nghe đáp án", speaker = true, onClick = { reveal = true; onListen(row.answer) })
                        }
                }
            }
            ExtraMicrophone(phase, if (accepted) "Chạm để nói lại" else "Chạm để trả lời", onSpeak)
            SpokenResult(learner, if (phase == "idle") finalTranscript.orEmpty() else transcript,
                if (phase == "idle" && !finalTranscript.isNullOrBlank()) accepted else null,
                reference = if (phase == "idle") evaluation.reference else null)
            if (!finalTranscript.isNullOrBlank() && phase == "idle") Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                if (accepted) StudioIcon("check", Modifier.size(19.dp).padding(end = 4.dp), p.correctWord)
                Text(if (accepted) "Cách diễn đạt phù hợp" else "Chưa khớp đáp án mẫu. Hãy nghe và thử lại nhé.",
                    color = if (accepted) p.correctWord else p.incorrectWord, fontSize = 13.sp, lineHeight = 16.sp)
            }
            if (status.isNotBlank() && (phase != "idle" || finalTranscript.isNullOrBlank())) Text(status, color = p.muted, fontSize = 12.sp, lineHeight = 15.sp)
        }
        CoralButton(if (accepted) "Câu tiếp theo" else "Cần nói lại", enabled = accepted, chevron = false,
            labelColor = if (p == NeonStudio) Color(0xFF071B3F) else null, onClick = onNext)
    }
}

@Composable
private fun ExtraOutline(label: String, onClick: () -> Unit, speaker: Boolean = false, bulb: Boolean = false) {
    Row(Modifier.clip(RoundedCornerShape(8.dp))
        .border(1.dp, studio.border, RoundedCornerShape(8.dp))
        .clickable(role = Role.Button, onClick = onClick).heightIn(min = 32.dp)
        .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        if (speaker || bulb) StudioIcon(if (speaker) "speaker" else "bulb", Modifier.size(17.dp), studio.coral)
        Text(label, color = if (speaker) studio.coral else studio.ink,
            fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
    }
}
