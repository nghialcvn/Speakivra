package com.nghialcvn.speakivra

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.CancellationException
import org.json.JSONObject

@Composable
internal fun rememberAccountGateway(): AccountGateway {
    val activity = LocalContext.current as ComponentActivity
    val scope = rememberCoroutineScope()
    return remember(activity, scope) {
        if (BuildConfig.DEBUG && AccountRuntime.testGateway != null) requireNotNull(AccountRuntime.testGateway)
        else FirebaseAccountGateway(activity, scope)
    }
}
internal class FirebaseAccountGateway(private val activity: ComponentActivity, private val scope: CoroutineScope): AccountGateway {
    private val runtime = AccountRuntime.get(activity)
    private val auth get() = runtime.auth
    override val configured = true
    override val session: AccountSession? get() = auth.currentUser?.let { user ->
        val provider = user.providerData.map { it.providerId }.filter { it != "firebase" }.joinToString(" + ") {
            when(it) { "google.com" -> "Google"; "microsoft.com" -> "Microsoft"; "password" -> "Email"; else -> it }
        }
        AccountSession(user.uid, user.displayName?.takeIf { it.isNotBlank() } ?: "Tài khoản Speakivra", provider, user.isEmailVerified, user.email)
    }
    private fun <T> run(result: (Result<T>) -> Unit, timeoutMillis: Long = 30_000L, action: suspend () -> T) {
        scope.launch {
            val value = try {
                Result.success(withTimeout(timeoutMillis) { action() })
            } catch (_: TimeoutCancellationException) {
                Result.failure(IllegalStateException("Kết nối quá lâu. Hãy kiểm tra mạng và thử lại; dữ liệu học vẫn được giữ trên máy."))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Result.failure(IllegalStateException(message(error), error))
            }
            result(value)
        }
    }
    private suspend fun finishSession(refresh: Boolean = false): AccountSession {
        val user = requireNotNull(auth.currentUser)
        // A completed sign-in already supplies the verified Firebase user and ID token.
        // Reload only after email verification; never wait for Firestore before returning login.
        if (refresh) { user.reload().cloudAwait(); user.getIdToken(true).cloudAwait() }
        runtime.changed()
        if (auth.currentUser?.isEmailVerified == true) runtime.schedule()
        return requireNotNull(session)
    }
    private suspend fun authenticateProvider(provider: LoginProvider, linking: Boolean): AccountSession {
        when(provider) {
            LoginProvider.GOOGLE -> {
                val response = withTimeout(60_000L) {
                    CredentialManager.create(activity).getCredential(activity,
                        googleButtonRequest(activity.getString(R.string.default_web_client_id)))
                }
                val credential = response.credential
                check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) { "Chưa nhận được tài khoản Google." }
                val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val google = GoogleAuthProvider.getCredential(token, null)
                withTimeout(25_000L) {
                    if (linking) requireNotNull(auth.currentUser).linkWithCredential(google).cloudAwait()
                    else auth.signInWithCredential(google).cloudAwait()
                }
            }
            LoginProvider.MICROSOFT -> {
                error("Đăng nhập Microsoft tạm thời chưa khả dụng. Hãy dùng Google hoặc email.")
            }
            LoginProvider.EMAIL -> error("Dùng email và mật khẩu để đăng nhập.")
        }
        return finishSession()
    }
    override fun signIn(provider: LoginProvider, email: String, password: String, result: (Result<AccountSession>) -> Unit) = run(result, if (provider == LoginProvider.GOOGLE) 90_000L else 30_000L) {
        if (provider == LoginProvider.EMAIL) { auth.signInWithEmailAndPassword(email, password).cloudAwait(); finishSession() }
        else authenticateProvider(provider, false)
    }
    override fun linkProvider(provider: LoginProvider, result: (Result<AccountSession>) -> Unit) = run(result, 90_000L) {
        val before = requireNotNull(session); check(before.verified)
        val linked = authenticateProvider(provider, true); check(linked.uid == before.uid); linked
    }
    override fun register(email: String, password: String, result: (Result<Unit>) -> Unit) = run(result) {
        auth.createUserWithEmailAndPassword(email, password).cloudAwait()
        requireNotNull(auth.currentUser).sendEmailVerification().cloudAwait(); runtime.changed()
    }
    override fun resetPassword(email: String, result: (Result<Unit>) -> Unit) = run(result) { auth.sendPasswordResetEmail(email).cloudAwait(); Unit }
    override fun refreshSession(result: (Result<AccountSession>) -> Unit) = run(result) { finishSession(refresh = true) }
    override fun verifyEmail(result: (Result<Unit>) -> Unit) = run(result) { requireNotNull(auth.currentUser).sendEmailVerification().cloudAwait(); Unit }
    override fun sync(payload: JSONObject, result: (Result<Unit>) -> Unit) = run(result) {
        val owner = requireNotNull(session); check(owner.verified)
        val id = payload.optString("profileId")
        if (id.isNotEmpty()) {
            val person = LearnerStore(activity).findCloud(id) ?: error("Không tìm thấy hồ sơ đã liên kết.")
            check(GuestIdentityStore(activity).peek(person)?.ownerUid == owner.uid)
        }
        runtime.syncAll(owner.uid)
    }
    override fun claim(identity: GuestIdentity, signature: ByteArray, result: (Result<GuestClaimReceipt>) -> Unit) = run(result) { runtime.claim(identity, signature) }
    override fun signOut() {
        val oldUid = auth.currentUser?.uid
        auth.signOut()
        if (oldUid != null) androidx.work.WorkManager.getInstance(activity).cancelUniqueWork("speakivra-sync-$oldUid")
        runtime.changed()
        scope.launch { runCatching { CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest()) } }
    }
    private fun message(error: Throwable): String = when(error) {
        is GetCredentialCancellationException -> "Đã hủy chọn tài khoản."
        is NoCredentialException -> "Chưa lấy được tài khoản Google. Hãy kiểm tra tài khoản Google và Dịch vụ Google Play trên máy rồi thử lại."
        is GetCredentialException -> "Google chưa trả về tài khoản. Hãy kiểm tra kết nối và thử lại."
        is FirebaseAuthUserCollisionException -> "Tài khoản này đã được dùng. Hãy đăng nhập bằng cách đã liên kết trước đó; hồ sơ không được chuyển sang tài khoản khác."
        is FirebaseAuthInvalidCredentialsException -> "Email hoặc mật khẩu chưa đúng. Hãy kiểm tra lại."
        is FirebaseAuthInvalidUserException -> "Không đăng nhập được tài khoản này. Hãy kiểm tra email hoặc đặt lại mật khẩu."
        is FirebaseAuthException -> when(error.errorCode) {
            "ERROR_OPERATION_NOT_ALLOWED", "ERROR_INVALID_OAUTH_CLIENT_ID" -> "Phương thức đăng nhập này chưa được cấu hình. Hiện có thể dùng Google hoặc email."
            "ERROR_TOO_MANY_REQUESTS" -> "Có quá nhiều lần thử. Hãy đợi một lúc rồi thử lại."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Chưa có kết nối. Tiến độ vẫn được giữ trên máy."
            else -> "Chưa đăng nhập được. Hãy thử lại hoặc dùng Google/email."
        }
        is FirebaseFirestoreException -> when(error.code) {
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> "Đã chạm hạn mức đồng bộ miễn phí. Tiến độ vẫn lưu trên máy; hãy thử lại sau."
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Không có quyền liên kết hoặc đồng bộ hồ sơ này. Hồ sơ có thể đã thuộc tài khoản khác."
            else -> "Chưa đồng bộ được. Tiến độ vẫn lưu trên máy và sẽ thử lại khi có mạng."
        }
        else -> error.message?.takeIf { error is IllegalStateException || error is IllegalArgumentException }
            ?: "Chưa kết nối được dịch vụ tài khoản. Tiến độ vẫn được giữ trên máy."
    }
}

/** Explicit Google button flow, rather than the automatic bottom-sheet option. */
internal fun googleButtonRequest(webClientId: String): GetCredentialRequest = GetCredentialRequest.Builder()
    .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build()).build()
