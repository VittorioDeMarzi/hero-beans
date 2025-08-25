package techcourse.herobeans.dto

import techcourse.herobeans.enums.MemberRole

class MemberDto(
    val id: Long,
    val email: String,
    val role: MemberRole,
)
