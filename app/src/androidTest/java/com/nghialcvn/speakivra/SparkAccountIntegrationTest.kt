package com.nghialcvn.speakivra

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withTimeout
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** Real Android SDK + emulated Auth/Firestore. Never creates users or data in the live project. */
@RunWith(AndroidJUnit4::class)
class SparkAccountIntegrationTest {
    private lateinit var runtime: AccountRuntime
    @get:Rule(order = 0) val seed = TestRule { base, _: Description -> object : Statement() {
        override fun evaluate() {
            Assume.assumeTrue("Emulated account setup is debug-only", BuildConfig.DEBUG)
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.getSharedPreferences(CURRICULUM_MIGRATIONS,0).edit().putBoolean(RESET_108,true).commit()
            context.getSharedPreferences("speakivra_learners",0).edit().clear().putString("profiles",encodeLearners(listOf(Learner(ORIGINAL_LEARNER,"Nghĩa",0)))).commit()
            context.getSharedPreferences("speakivra_guest_identity",0).edit().clear().commit()
            context.getSharedPreferences("speakivra_cloud_sync",0).edit().clear().commit()
            context.getSharedPreferences("speakivra_progress",0).edit().clear().putBoolean("automatic_mode",false)
                .putString("lesson.greetings.phase","repeat").putInt("lesson.greetings.phrase",2).commit()
            val app = FirebaseApp.getApps(context).firstOrNull { it.name == "spark-emulator-tests" } ?: FirebaseApp.initializeApp(context,
                FirebaseOptions.Builder().setProjectId("demo-speakivra-spark").setApplicationId("1:123456:android:test")
                    .setApiKey("fake-api-key").build(),"spark-emulator-tests")
            val auth = FirebaseAuth.getInstance(app).apply { useEmulator("10.0.2.2",9099); signOut() }
            val db = FirebaseFirestore.getInstance(app).apply { useEmulator("10.0.2.2",8080) }
            runtime = AccountRuntime.installEmulated(context,auth,db)
            AccountRuntime.testGateway = null
            try { base.evaluate() } finally { auth.signOut(); runtime.changed() }
        }
    } }
    @get:Rule(order = 1) val ui = createAndroidComposeRule<MainActivity>()
    @org.junit.Before fun enterEnglishArea() { ui.selectEnglishAreaIfNeeded() }
    private fun request(path: String, body: JSONObject? = null): JSONObject {
        val connection = URL("http://10.0.2.2:9099/$path").openConnection() as HttpURLConnection
        connection.connectTimeout=10000; connection.readTimeout=10000
        if(body!=null) { connection.requestMethod="POST";connection.doOutput=true;connection.setRequestProperty("Content-Type","application/json");connection.outputStream.use { it.write(body.toString().toByteArray()) } }
        return try { JSONObject(connection.inputStream.bufferedReader().use { it.readText() }) } finally { connection.disconnect() }
    }
    private fun verifyEmail(email: String) = runBlocking {
        requireNotNull(runtime.auth.currentUser).sendEmailVerification().cloudAwait()
        val codes = request("emulator/v1/projects/demo-speakivra-spark/oobCodes").getJSONArray("oobCodes")
        val code = (0 until codes.length()).map { codes.getJSONObject(it) }.last { it.optString("email")==email && it.optString("requestType")=="VERIFY_EMAIL" }.getString("oobCode")
        request("identitytoolkit.googleapis.com/v1/accounts:update?key=fake-api-key",JSONObject().put("oobCode",code))
        requireNotNull(runtime.auth.currentUser).reload().cloudAwait()
        requireNotNull(runtime.auth.currentUser).getIdToken(true).cloudAwait()
        assertTrue(runtime.auth.currentUser!!.isEmailVerified)
    }
    private fun shot(name: String) {
        ui.waitForIdle()
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        dismissUnrelatedLauncherAnr(instrumentation)
        instrumentation.uiAutomation.waitForIdle(500,5000)
        Thread.sleep(150)
        val file=File(ui.activity.getExternalFilesDir(null),"ui-screenshots/$name.png");file.parentFile!!.mkdirs()
        val bitmap=requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
    }
    @Test fun registrationVerificationOneTimeClaimOfflineSyncAccountIsolationAndAnotherDeviceRestore() {
        val email="speakivra-${UUID.randomUUID()}@example.test";val password="Emulator-only-long-password-123"
        ui.onNodeWithText("Tài khoản & đồng bộ",substring=false).performClick()
        ui.onNodeWithText("Tiếp tục với Google").assertExists()
        val request = googleButtonRequest(ui.activity.getString(R.string.default_web_client_id))
        assertEquals(1, request.credentialOptions.size)
        assertTrue(request.credentialOptions.single() is GetSignInWithGoogleOption)
        shot("84-login-live-sdk")
        ui.onNodeWithText("Tiếp tục với Microsoft").assertDoesNotExist()
        ui.onNode(hasText("Tạo tài khoản") and hasClickAction()).performScrollTo().performClick()
        ui.onNodeWithText("Email",substring=false).performTextInput(email)
        ui.onNodeWithText("Mật khẩu",substring=false).performTextInput(password)
        ui.onNodeWithText("Nhập lại mật khẩu",substring=false).performTextInput(password)
        shot("85-register-live-sdk")
        ui.onNode(hasText("Tạo tài khoản") and hasClickAction()).performScrollTo().performClick()
        ui.waitUntil(20000) { ui.onAllNodesWithText("Tôi đã xác minh").fetchSemanticsNodes().isNotEmpty() }
        assertFalse(runtime.auth.currentUser!!.isEmailVerified)
        verifyEmail(email)
        ui.onNodeWithText("Tôi đã xác minh").performScrollTo().performClick()
        ui.waitUntil(20000) { ui.onAllNodesWithText("Liên kết và đồng bộ").fetchSemanticsNodes().isNotEmpty() }
        val store=LearnerStore(ui.activity);val original=store.all().single();val identities=GuestIdentityStore(ui.activity)
        val guest=identities.ensure(original);val owner=runtime.uid!!;val proof=identities.proof(original,owner)
        shot("86-link-live-sdk")
        ui.onNodeWithText("Liên kết và đồng bộ").performScrollTo().performClick()
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Xác nhận liên kết", substring=false).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Xác nhận liên kết",substring=false).performClick()
        ui.waitUntil(25000) { ui.onAllNodesWithText("Đã đồng bộ",substring=false).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(owner,identities.peek(original)!!.ownerUid)
        val listener = runtime.watch(original)
        val achievement="achievement.greetings.${Lessons.a1.first().phrases.first().id}"
        store.preferences(original).edit().putInt(achievement,95).putString("active_lesson",Lessons.a1[2].id).commit()
        runBlocking { runtime.syncAll(owner);runtime.db.disableNetwork().cloudAwait() }
        assertTrue(runtime.isSynced(original))
        store.preferences(original).edit().putInt("lesson.greetings.rounds",2).commit()
        assertEquals(2,store.preferences(original).getInt("lesson.greetings.rounds",0))
        ui.waitUntil(10000) { !runtime.isSynced(original) }
        runBlocking { runtime.db.enableNetwork().cloudAwait();runtime.syncPending(owner) }
        assertTrue(runtime.isSynced(original))
        store.preferences(original).unregisterOnSharedPreferenceChangeListener(listener)
        val other="other-${UUID.randomUUID()}@example.test"
        runBlocking { runtime.auth.createUserWithEmailAndPassword(other,password).cloudAwait() };verifyEmail(other)
        val otherUid=runtime.uid!!
        assertNotEquals(owner,otherUid);assertTrue(store.visible(otherUid).none { it.id==original.id })
        assertTrue(runBlocking { runCatching { runtime.claim(guest,proof) }.isFailure })
        runtime.auth.signOut();assertTrue(store.visible(null).none { it.id==original.id })
        // Simulate a different installation: no local profiles, identity metadata or progress.
        ui.activity.getSharedPreferences("speakivra_learners",0).edit().putString("profiles","[]").commit()
        ui.activity.getSharedPreferences("speakivra_guest_identity",0).edit().clear().commit()
        ui.activity.getSharedPreferences("speakivra_progress",0).edit().clear().commit()
        runBlocking { runtime.auth.signInWithEmailAndPassword(email,password).cloudAwait() };runtime.changed()
        ui.waitUntil(20000) { ui.onAllNodesWithText("Đồng bộ ngay").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Đồng bộ ngay").performScrollTo().performClick()
        ui.waitUntil(25000) { store.visible(owner).isNotEmpty() }
        val restored=store.visible(owner).single();assertNotEquals(original.id,restored.id)
        ui.waitUntil(25000) { runtime.isSynced(restored) }
        ui.onNodeWithText("Đã đồng bộ",substring=false).assertExists()
        assertTrue(ui.onAllNodesWithText("Nghĩa",substring=false).fetchSemanticsNodes().isNotEmpty())
        assertEquals("Nghĩa",restored.name);assertEquals(guest.profileId,identities.peek(restored)!!.profileId)
        assertEquals(owner,identities.peek(restored)!!.ownerUid)
        assertEquals(95,store.preferences(restored).getInt(achievement,0))
        assertEquals(2,store.preferences(restored).getInt("lesson.greetings.rounds",0))
        assertEquals(Lessons.a1[2].id,store.preferences(restored).getString("active_lesson",null))
        ui.onNodeWithText("Cài đặt", substring=false).assertIsSelected()
        shot("83-spark-cloud-restored")
        ui.onNodeWithText("Trang chủ", substring=false).performClick()
        ui.onNodeWithText("Tiếp tục học", substring=false).performScrollTo().assertExists()
        // Sign-in must return even if Firestore cannot reach its server. A second app instance
        // keeps Auth on the real emulator but points Firestore at an unused local port.
        val isolated = FirebaseApp.initializeApp(ui.activity,
            FirebaseOptions.Builder().setProjectId("demo-speakivra-spark")
                .setApplicationId("1:123456:android:test").setApiKey("fake-api-key").build(),
            "auth-with-unreachable-firestore-${UUID.randomUUID()}")
        val slowAuth = FirebaseAuth.getInstance(isolated).apply { useEmulator("10.0.2.2", 9099) }
        val unreachable = FirebaseFirestore.getInstance(isolated).apply { useEmulator("10.0.2.2", 1) }
        AccountRuntime.installEmulated(ui.activity, slowAuth, unreachable)
        val loginScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val completed = CompletableDeferred<Result<AccountSession>>()
        try {
            ui.runOnUiThread {
                FirebaseAccountGateway(ui.activity, loginScope).signIn(LoginProvider.EMAIL, email, password) { completed.complete(it) }
            }
            val signedIn = runBlocking { withTimeout(5_000L) { completed.await() } }.getOrThrow()
            assertEquals(owner, signedIn.uid)
            assertTrue(signedIn.verified)
        } finally {
            slowAuth.signOut(); loginScope.cancel()
            runBlocking { unreachable.terminate().cloudAwait() }
            AccountRuntime.installEmulated(ui.activity, runtime.auth, runtime.db)
            isolated.delete()
        }
    }
}
