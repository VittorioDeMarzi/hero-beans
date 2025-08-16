package techcourse.herobeans.service

import org.springframework.stereotype.Service
import techcourse.herobeans.dto.AddressDto
import techcourse.herobeans.dto.AddressRequest
import techcourse.herobeans.dto.UpdateAddressRequest
import techcourse.herobeans.exception.ForbiddenAccessException
import techcourse.herobeans.exception.MaxAddressesExceededException
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.mapper.AddressMapper.toDto
import techcourse.herobeans.mapper.AddressMapper.toEntity
import techcourse.herobeans.repository.AddressJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository

@Service
class AddressService(
    private val addressRepository: AddressJpaRepository,
    private val memberJpaRepository: MemberJpaRepository,
) {
    fun createAddress(
        request: AddressRequest,
        memberId: Long,
    ): AddressDto {
        val member = memberJpaRepository.findById(memberId).orElseThrow { NotFoundException("Member with id: $memberId not found") }
        if (addressRepository.findAllByMemberId(memberId).size >= 5) {
            throw MaxAddressesExceededException("Max 5 addresses allowed")
        }
        val address = request.toEntity(member)
        return addressRepository.save(address).toDto()
    }

    fun removeAddress(
        id: Long,
        memberId: Long,
    ) {
        val address = addressRepository.findById(id).orElseThrow { NotFoundException("Address with id: $id not found") }
        if (address.member.id != memberId) {
            throw ForbiddenAccessException("You are not allowed to delete this address")
        }
        addressRepository.delete(address)
    }

    fun updateAddress(
        id: Long,
        memberId: Long,
        request: UpdateAddressRequest,
    ): AddressDto {
        val address = addressRepository.findById(id).orElseThrow { NotFoundException("Address with id: $id not found") }
        if (address.member.id != memberId) {
            throw ForbiddenAccessException("You are not allowed to modify this address")
        }

        request.street?.let { address.street = it }
        request.number?.let { address.number = it }
        request.postalCode?.let { address.postalCode = it }
        request.label?.let { address.label = it }

        return addressRepository.save(address).toDto()
    }

    fun getAllAddresses(memberId: Long): List<AddressDto> {
        return addressRepository.findAllByMemberId(memberId).map { it.toDto() }
    }
}
