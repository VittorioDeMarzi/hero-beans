package techcourse.herobeans.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import techcourse.herobeans.dto.AddressDto
import techcourse.herobeans.dto.AddressRequest
import techcourse.herobeans.dto.UpdateAddressRequest
import techcourse.herobeans.entity.Address
import techcourse.herobeans.exception.MaxAddressesExceededException
import techcourse.herobeans.exception.NotFoundException
import techcourse.herobeans.mapper.AddressMapper.toDto
import techcourse.herobeans.mapper.AddressMapper.toEntity
import techcourse.herobeans.repository.AddressJpaRepository
import techcourse.herobeans.repository.MemberJpaRepository

private val log = KotlinLogging.logger {}

@Service
@Transactional
class AddressService(
    private val addressRepository: AddressJpaRepository,
    private val memberJpaRepository: MemberJpaRepository,
) {
    fun createAddress(
        request: AddressRequest,
        memberId: Long,
    ): AddressDto {
        val member =
            memberJpaRepository.findById(memberId)
                .orElseThrow { NotFoundException("Member with id: $memberId not found") }
        if (addressRepository.findAllByMemberId(memberId).size >= 5) {
            throw MaxAddressesExceededException("Max 5 addresses allowed")
        }

        return addressRepository
            .save(request.toEntity(member))
            .also { saved -> log.info { "address.created memberId=$memberId addressId=${saved.id}" } }
            .toDto()
    }

    fun removeAddress(
        id: Long,
        memberId: Long,
    ) {
        val address = findMemberAddress(id, memberId)
        address
            .apply { addressRepository.delete(this) }
            .also { log.info { "address.removed memberId=$memberId addressId=$id" } }
    }

    fun updateAddress(
        id: Long,
        memberId: Long,
        request: UpdateAddressRequest,
    ): AddressDto {
        val address = findMemberAddress(id, memberId)

        request.street?.let { address.street = it }
        request.number?.let { address.number = it }
        request.postalCode?.let { address.postalCode = it }
        request.label?.let { address.label = it }

        return addressRepository
            .save(address)
            .also { log.info { "address.updated memberId=$memberId addressId=$id" } }
            .toDto()
    }

    fun getAllAddresses(memberId: Long): List<AddressDto> {
        return addressRepository.findAllByMemberId(memberId)
            .map { it.toDto() }
            .also { list -> log.info { "address.listed memberId=$memberId count=${list.size}" } }
    }

    @Transactional(readOnly = true)
    fun findMemberAddress(
        addressId: Long,
        memberId: Long,
    ): Address {
        return addressRepository.findByIdAndMemberId(addressId, memberId)
            ?: throw NotFoundException("Address with id=$addressId not found for memberId=$memberId")
    }

    fun findAddressByMemberId(memberId: Long): Address {
        return addressRepository.findByMemberId(memberId)
    }
}
