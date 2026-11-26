package com.nghialcvn.speakivra

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun GuestIdentityLabel(learner: Learner) {
    val context = LocalContext.current
    var identity by remember(learner.id) { mutableStateOf<GuestIdentity?>(null) }
    LaunchedEffect(learner.id) {
        identity = withContext(Dispatchers.IO) { runCatching { GuestIdentityStore(context).ensure(learner) }.getOrNull() }
    }
    Text(if (identity?.ownerUid == null) "Hồ sơ khách • Chưa liên kết" else "Hồ sơ đã liên kết",
        color = studio.muted, fontSize = 11.sp)
    identity?.let { Text(it.serial, color = studio.muted, fontSize = 10.sp) }
}

/** Live cloud wiring is deliberately unavailable until a project and providers are configured.
 * This draft cannot report a successful login, ownership claim or sync without a gateway receipt. */
@Composable
internal fun AccountFlow(learner: Learner?, onBack: () -> Unit,
    gateway: AccountGateway = rememberAccountGateway(), onAuthScreen: (Boolean) -> Unit = {}, onChooseLearner: (Learner) -> Unit = { onBack() }) {
    val context = LocalContext.current
    val identityStore = remember { GuestIdentityStore(context.applicationContext) }
    val store = remember { LearnerStore(context.applicationContext) }
    var page by rememberSaveable { mutableStateOf(if (gateway.session == null) "login" else if (gateway.session?.verified != true) "verify" else "account") }
    var session by remember { mutableStateOf(gateway.session) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val runtime = remember { AccountRuntime.get(context) }
    val cloudRevision by runtime.revision.collectAsState()
    var showMethods by rememberSaveable { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var synced by remember { mutableStateOf(false) }
    var identity by remember(learner?.id) { mutableStateOf<GuestIdentity?>(null) }
    LaunchedEffect(learner?.id) {
        if (learner != null) identity = withContext(Dispatchers.IO) { runCatching { identityStore.ensure(learner) }.getOrNull() }
    }
    val liveUid = gateway.session?.uid
    LaunchedEffect(liveUid) {
        if (session?.uid != liveUid) {
            session = gateway.session
            page = if (session == null) "login" else if (session?.verified != true) "verify" else "account"
            synced = false
        }
    }
    val displaySynced = if (gateway is FirebaseAccountGateway) remember(cloudRevision, learner?.id, synced) { runtime.isSynced(learner) } else synced
    val effectivePage = if (page !in listOf("login", "register", "reset") && session?.uid != liveUid) "login" else page
    SideEffect { onAuthScreen(effectivePage != "account") }
    fun back() { if (page == "login" || page == "account") onBack() else { message = null; page = "login" } }
    BackHandler(onBack = ::back)
    fun loggedIn(result: Result<AccountSession>) {
        busy = false
        result.fold(onSuccess = { session = it; message = null; page = if (!it.verified) "verify" else if (identity?.ownerUid == null && learner != null) "link" else "account" },
            onFailure = { message = it.message ?: "Chưa đăng nhập được. Hãy thử lại." })
    }
    Column(Modifier.fillMaxSize().background(studio.background).verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when (effectivePage) {
            "login", "register", "reset" -> {
                Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.brand_logo_clear), "Logo Speakivra", Modifier.size(48.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Speakivra", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                }
                if (page == "login") AccountDevices(Modifier.fillMaxWidth().height(115.dp))
                Text(when (page) { "register" -> "Tạo tài khoản"; "reset" -> "Quên mật khẩu?"; else -> "Học tiếp trên mọi thiết bị" },
                    fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text("Đăng nhập để đồng bộ hồ sơ, tiến độ và kim cương.", color = studio.muted, fontSize = 14.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                AccountCredentialsForm(page, busy,
                    onProvider = { busy = true; message = null; gateway.signIn(it, "", "", ::loggedIn) },
                    onSubmit = { email, password, repeat ->
                        val error = if (page == "reset") { if (validAccountEmail(email)) null else "Nhập địa chỉ email hợp lệ." }
                            else accountFormError(email, password, if (page == "register") repeat else null)
                        if (error != null) message = error else {
                            busy = true; message = null
                            when (page) {
                                "register" -> gateway.register(email, password) { result ->
                                    busy = false
                                    result.fold(onSuccess = { session = gateway.session; page = "verify" }, onFailure = { message = it.message; if (gateway.session != null) { session = gateway.session; page = "verify" } })
                                }
                                "reset" -> gateway.resetPassword(email) { result ->
                                    busy = false
                                    message = result.fold(onSuccess = { "Nếu email này đã đăng ký, hãy kiểm tra thư hướng dẫn đặt lại mật khẩu." }, onFailure = { it.message })
                                }
                                else -> gateway.signIn(LoginProvider.EMAIL, email, password, ::loggedIn)
                            }
                        }
                    }, onPage = { page = it; message = null })
                if (page == "login") {
                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Tiếp tục học trên máy này") }
                    Text("Bạn vẫn có thể học khi không có mạng.", color = studio.muted, fontSize = 12.sp)
                } else TextButton(onClick = { page = "login"; message = null }) { Text("Quay lại đăng nhập") }
            }
            "verify" -> {
                PageHeading("Xác minh email", "Xác minh trước khi liên kết hồ sơ", ::back)
                Text("Mở email và làm theo hướng dẫn xác minh, sau đó quay lại Speakivra.")
                CoralButton("Tôi đã xác minh", chevron = false, enabled = !busy) { busy = true; gateway.refreshSession(::loggedIn) }
                OutlinedButton(onClick = { busy = true; gateway.verifyEmail { busy = false; message = it.fold(onSuccess = { "Đã gửi email xác minh." }, onFailure = { it.message }) } }, enabled = !busy) { Text("Gửi lại email xác minh") }
            }
            "link" -> {
                PageHeading("Liên kết dữ liệu hiện tại", "Đưa hồ sơ trên máy này lên tài khoản của bạn.", ::back)
                learner?.let { GuestLinkSummary(it, identity) }
                StudioCard {
                    Text("Giữ nguyên những gì đã học", fontWeight = FontWeight.ExtraBold)
                    Text("Hồ sơ và tiến độ hiện tại được giữ lại khi liên kết.")
                    Text("Hồ sơ này chỉ được liên kết một lần với một tài khoản Speakivra. Không thể chuyển sang tài khoản khác.", color = studio.muted)
                    Text("Mã hồ sơ chỉ để nhận diện, không phải mã đăng nhập hay mã khôi phục.", color = studio.muted, fontSize = 12.sp)
                    HorizontalDivider()
                    Text("Tài khoản nhận hồ sơ", fontWeight = FontWeight.ExtraBold)
                    Text(session?.email ?: session?.name.orEmpty(), color = studio.muted)
                    val existing = store.all().filter { item -> item.id != learner?.id && identityStore.peek(item)?.ownerUid == session?.uid && session != null }
                    if (existing.isNotEmpty()) {
                        Text("Tài khoản đã có ${existing.size} hồ sơ: ${existing.joinToString { it.name }}.")
                        Text("Giữ cả hồ sơ hiện có và hồ sơ đang liên kết. Mỗi hồ sơ có tiến độ riêng.", color = studio.muted)
                    }
                }
                CoralButton("Liên kết và đồng bộ", chevron = false, enabled = !busy && learner != null && identity != null && session?.verified == true) { confirm = true }
                TextButton(onClick = onBack) { Text("Để sau") }
            }
            "account" -> {
                PageHeading("Tài khoản & đồng bộ", onBack = onBack)
                StudioCard {
                    Text(session?.name ?: "Chưa đăng nhập", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(session?.provider ?: "Hồ sơ vẫn được giữ trên máy", color = studio.muted)
                    session?.email?.let { Text(it, color = studio.muted, fontSize = 13.sp) }
                    TextButton(onClick = { showMethods = !showMethods }) { Text("Phương thức đăng nhập") }
                    if (showMethods) {
                        if (session?.provider?.contains("Google") != true) TextButton(onClick = { busy = true; gateway.linkProvider(LoginProvider.GOOGLE, ::loggedIn) }, enabled = !busy) { Text("Liên kết Google") }
                        if (session?.provider?.contains("Microsoft") != true) TextButton(onClick = { busy = true; gateway.linkProvider(LoginProvider.MICROSOFT, ::loggedIn) }, enabled = !busy) { Text("Liên kết Microsoft") }
                    }
                }
                StudioCard {
                    Text(if (displaySynced) "Đã đồng bộ" else "Chưa đồng bộ", fontWeight = FontWeight.ExtraBold,
                        color = if (displaySynced) studio.green else studio.muted)
                    Text("Hồ sơ, tiến độ và kim cương", color = studio.muted)
                    OutlinedButton(onClick = {
                        val value = identity
                        if (session?.verified != true) message = "Hãy đăng nhập và xác minh tài khoản để đồng bộ."
                        else {
                            busy = true
                            val payload = if (value != null && value.ownerUid == session?.uid && learner != null)
                                profileSyncPayload(learner, value, store.preferences(learner).all) else org.json.JSONObject()
                            gateway.sync(payload) { result ->
                                busy = false; synced = result.isSuccess
                                result.exceptionOrNull()?.let { message = it.message }
                            }
                        }
                    }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Đồng bộ ngay") }
                    if (learner != null && identity?.ownerUid == null) CoralButton("Liên kết dữ liệu hiện tại", chevron = false) { page = "link" }
                }
                StudioCard {
                    Text("Hồ sơ người học", fontWeight = FontWeight.ExtraBold)
                    store.visible(gateway.session?.uid).forEach { item ->
                        Row(Modifier.fillMaxWidth().clickable { onChooseLearner(item) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LearnerAvatar(item.avatar, Modifier.size(36.dp)); Text(item.name, Modifier.weight(1f), fontWeight = FontWeight.Bold); StudioIcon("chevron")
                        }
                    }
                    Text("Mỗi hồ sơ có tiến độ riêng", color = studio.muted, fontSize = 12.sp)
                }
                Text("Khi mất mạng, tiến độ được lưu trên máy và đồng bộ khi có mạng trở lại.", color = studio.muted, fontSize = 12.sp)
                Text("Bản ghi giọng nói chỉ lưu tạm trên máy.", color = studio.muted, fontSize = 12.sp)
                TextButton(onClick = { gateway.signOut(); session = null; synced = false; page = "login" }) { Text("Đăng xuất") }
            }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        message?.let { Text(it, color = studio.coral, fontSize = 13.sp) }
        if (!gateway.configured) Text("Chưa kết nối đám mây", color = studio.muted, fontSize = 11.sp)
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Xác nhận liên kết một lần") },
        text = { Text("Hồ sơ ${learner?.name.orEmpty()} (${identity?.serial.orEmpty()}) sẽ thuộc tài khoản ${session?.email ?: session?.name.orEmpty()} và không thể chuyển sang tài khoản khác.") },
        confirmButton = { TextButton(onClick = {
            confirm = false
            val current = session; val guest = identity; val person = learner
            if (current == null || !current.verified || guest == null || person == null) message = "Hãy đăng nhập và xác minh tài khoản trước."
            else {
                busy = true
                runCatching { identityStore.proof(person, current.uid) }.fold(onSuccess = { proof ->
                    gateway.claim(guest, proof) { result ->
                        busy = false
                        result.fold(onSuccess = { receipt ->
                            runCatching { identityStore.recordServerReceipt(person, receipt, current.uid) }.fold(
                                onSuccess = {
                                    identity = it; synced = false; page = "account"; busy = true
                                    runtime.markPending(person)
                                    val payload = profileSyncPayload(person, it, store.preferences(person).all)
                                    gateway.sync(payload) { syncResult ->
                                        busy = false; synced = syncResult.isSuccess
                                        message = syncResult.exceptionOrNull()?.message
                                    }
                                },
                                onFailure = { message = it.message })
                        }, onFailure = { message = it.message })
                    }
                }, onFailure = { busy = false; message = it.message })
            }
        }, enabled = !busy) { Text("Xác nhận liên kết") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Quay lại") } })
}

@Composable
private fun AccountCredentialsForm(page: String, busy: Boolean, onProvider: (LoginProvider) -> Unit,
    onSubmit: (String, String, String) -> Unit, onPage: (String) -> Unit) {
    // Never save passwords in savedInstanceState, preferences, logs or a sync payload.
    var email by remember(page) { mutableStateOf("") }
    var password by remember(page) { mutableStateOf("") }
    var repeat by remember(page) { mutableStateOf("") }
    StudioCard {
        if (page == "login") {
            OutlinedButton(onClick = { onProvider(LoginProvider.GOOGLE) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.ExtraBold); Spacer(Modifier.width(10.dp)); Text("Tiếp tục với Google")
            }
            OutlinedButton(onClick = { onProvider(LoginProvider.MICROSOFT) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.size(7.dp).background(Color(0xFFF25022)))
                        Box(Modifier.size(7.dp).background(Color(0xFF00A4EF)))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box(Modifier.size(7.dp).background(Color(0xFF7FBA00)))
                        Box(Modifier.size(7.dp).background(Color(0xFFFFB900)))
                    }
                }
                Spacer(Modifier.width(10.dp)); Text("Tiếp tục với Microsoft")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HorizontalDivider(Modifier.weight(1f)); Text("hoặc", color = studio.muted); HorizontalDivider(Modifier.weight(1f))
            }
        }
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, enabled = !busy, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        if (page != "reset") OutlinedTextField(password, { password = it }, label = { Text("Mật khẩu") }, enabled = !busy,
            singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        if (page == "register") OutlinedTextField(repeat, { repeat = it }, label = { Text("Nhập lại mật khẩu") }, enabled = !busy,
            singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        if (page == "login") TextButton(onClick = { onPage("reset") }, modifier = Modifier.align(Alignment.End)) { Text("Quên mật khẩu?") }
        CoralButton(when (page) { "register" -> "Tạo tài khoản"; "reset" -> "Gửi hướng dẫn"; else -> "Đăng nhập" }, chevron = false, enabled = !busy) { onSubmit(email.trim(), password, repeat) }
        if (page == "login") Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Chưa có tài khoản?", color = studio.muted, fontSize = 13.sp)
            TextButton(onClick = { onPage("register") }) { Text("Tạo tài khoản") }
        }
    }
}

@Composable
internal fun GuestLinkSummary(learner: Learner, identity: GuestIdentity?) {
    StudioCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LearnerAvatar(learner.avatar, Modifier.size(60.dp))
            Column {
                Text(learner.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Hồ sơ khách trên máy này", color = studio.muted, fontSize = 12.sp)
            }
        }
        identity?.let { Text(it.serial, color = studio.coral, fontSize = 11.sp) }
        HorizontalDivider()
        listOf("book" to "Bài học đã học", "progress" to "Tiến độ từng câu", "diamond" to "Thành tích kim cương").forEach { (icon, label) ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioIcon(icon, color = studio.coral); Text(label, Modifier.weight(1f)); StudioIcon("check", color = studio.green)
            }
        }
    }
}

/** Native illustration keeps the approved three-device composition sharp in every appearance. */
@Composable
private fun AccountDevices(modifier: Modifier) {
    val outline = studio.ink
    val screen = studio.card
    val accent = studio.coral
    Canvas(modifier) {
        val unit = size.height / 115f
        val origin = Offset(size.width / 2f - 110f * unit, 0f)
        fun point(x: Float, y: Float) = origin + Offset(x * unit, y * unit)
        fun device(x: Float, y: Float, w: Float, h: Float) {
            drawRoundRect(screen, point(x, y), Size(w * unit, h * unit), CornerRadius(7 * unit))
            drawRoundRect(outline, point(x, y), Size(w * unit, h * unit), CornerRadius(7 * unit), style = Stroke(3 * unit))
            val center = point(x + w / 2, y + h / 2)
            drawCircle(accent.copy(alpha = .2f), 14 * unit, center)
            val play = Path().apply { moveTo(center.x - 3 * unit, center.y - 6 * unit); lineTo(center.x + 6 * unit, center.y); lineTo(center.x - 3 * unit, center.y + 6 * unit); close() }
            drawPath(play, accent)
        }
        device(55f, 13f, 112f, 77f)
        drawLine(outline, point(43f, 93f), point(179f, 93f), 5 * unit)
        device(14f, 40f, 43f, 65f)
        device(164f, 48f, 52f, 54f)
        listOf(24f to 8f, 38f to 4f, 184f to 12f, 199f to 18f).forEach { (x,y) ->
            drawLine(accent, point(x, y), point(x + 3, y + 9), 2 * unit)
        }
    }
}
