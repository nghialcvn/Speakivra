package com.nghialcvn.speakivra

/** Provider-independent state; a successful UI state requires an actual service result. */
internal data class AccountSession(val uid: String, val name: String, val provider: String, val verified: Boolean)
internal enum class LoginProvider { GOOGLE, MICROSOFT, EMAIL }
internal interface AccountGateway {
    val session: AccountSession?
    val configured: Boolean
    fun signIn(provider: LoginProvider, email: String, password: String, result: (Result<AccountSession>) -> Unit)
    fun register(email: String, password: String, result: (Result<Unit>) -> Unit)
    fun resetPassword(email: String, result: (Result<Unit>) -> Unit)
    fun refreshSession(result: (Result<AccountSession>) -> Unit)
    fun sync(payload: org.json.JSONObject, result: (Result<Unit>) -> Unit)
    fun verifyEmail(result: (Result<Unit>) -> Unit)
    fun claim(identity: GuestIdentity, signature: ByteArray, result: (Result<GuestClaimReceipt>) -> Unit)
    fun signOut()
}
/** Never fake sign-in, verification email, cloud ownership or a completed sync. Not a release backend. */
internal class UnconfiguredAccountGateway : AccountGateway {
    override val session: AccountSession? = null
    override val configured = false
    private fun <T> unavailable(result: (Result<T>) -> Unit) = result(Result.failure(
        IllegalStateException("Chưa kết nối dịch vụ tài khoản. Hồ sơ và tiến độ vẫn lưu trên máy.")))
    override fun signIn(provider: LoginProvider, email: String, password: String, result: (Result<AccountSession>) -> Unit) = unavailable(result)
    override fun register(email: String, password: String, result: (Result<Unit>) -> Unit) = unavailable(result)
    override fun resetPassword(email: String, result: (Result<Unit>) -> Unit) = unavailable(result)
    override fun refreshSession(result: (Result<AccountSession>) -> Unit) = unavailable(result)
    override fun sync(payload: org.json.JSONObject, result: (Result<Unit>) -> Unit) = unavailable(result)
    override fun verifyEmail(result: (Result<Unit>) -> Unit) = unavailable(result)
    override fun claim(identity: GuestIdentity, signature: ByteArray, result: (Result<GuestClaimReceipt>) -> Unit) = unavailable(result)
    override fun signOut() = Unit
}
internal fun validAccountEmail(email: String) = email.length <= 254 &&
    Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+").matches(email)
internal fun accountFormError(email: String, password: String, repeat: String? = null): String? = when {
    !validAccountEmail(email.trim()) -> "Nhập địa chỉ email hợp lệ."
    password.length < 8 -> "Mật khẩu cần ít nhất 8 ký tự."
    repeat != null && password != repeat -> "Hai mật khẩu chưa khớp."
    else -> null
}
