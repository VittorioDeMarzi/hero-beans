package techcourse.herobeans.mapper

import techcourse.herobeans.dto.AddressDto
import techcourse.herobeans.dto.AddressRequest
import techcourse.herobeans.entity.Address
import techcourse.herobeans.entity.Member

object AddressMapper {
    fun Address.toDto(): AddressDto {
        return AddressDto(
            street = this.street,
            number = this.number,
            city = this.city,
            postalCode = this.postalCode,
            countryCode = this.countryCode,
            label = this.label,
            id = this.id,
        )
    }

    fun AddressDto.toEntity(member: Member): Address {
        return Address(
            street = this.street,
            number = this.number,
            city = this.city,
            postalCode = this.postalCode,
            countryCode = this.countryCode,
            label = this.label,
            member = member,
        )
    }

    fun AddressRequest.toEntity(member: Member): Address {
        return Address(
            street = this.street,
            number = this.number,
            city = this.city,
            postalCode = this.postalCode,
            countryCode = this.countryCode,
            label = this.label,
            member = member,
        )
    }
}
