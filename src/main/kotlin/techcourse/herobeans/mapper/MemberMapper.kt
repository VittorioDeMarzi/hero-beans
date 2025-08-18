package techcourse.herobeans.mapper

import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.entity.Member

object MemberMapper {
    fun Member.toDto(): MemberDto {
        return MemberDto(id, email, role)
    }
}
