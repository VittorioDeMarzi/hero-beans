package techcourse.herobeans.exception

data class ErrorMessageModel(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
    val correlationId: String?,
)
