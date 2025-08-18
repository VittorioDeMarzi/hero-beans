package techcourse.herobeans.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import techcourse.herobeans.annotation.LoginMember
import techcourse.herobeans.dto.AddressDto
import techcourse.herobeans.dto.AddressRequest
import techcourse.herobeans.dto.MemberDto
import techcourse.herobeans.dto.UpdateAddressRequest
import techcourse.herobeans.service.AddressService

@RestController
@RequestMapping("/api/address")
class AddressController(
    private val addressService: AddressService,
) {
    @PostMapping
    fun createAddress(
        @LoginMember member: MemberDto,
        @RequestBody request: AddressRequest,
    ): ResponseEntity<AddressDto> {
        val newAddress = addressService.createAddress(request, member.id!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(newAddress)
    }

    @DeleteMapping("/{addressId}")
    fun removeAddress(
        @LoginMember member: MemberDto,
        @PathVariable addressId: Long,
    ): ResponseEntity<Void> {
        addressService.removeAddress(addressId, member.id!!)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{addressId}")
    fun updateAddress(
        @LoginMember member: MemberDto,
        @RequestBody request: UpdateAddressRequest,
        @PathVariable addressId: Long,
    ): ResponseEntity<AddressDto> {
        val updatedAddress = addressService.updateAddress(addressId, member.id!!, request)
        return ResponseEntity.ok(updatedAddress)
    }

    @GetMapping()
    fun getAllAddresses(
        @LoginMember member: MemberDto,
    ): ResponseEntity<List<AddressDto>> {
        val addresses = addressService.getAllAddresses(member.id!!)
        return ResponseEntity.ok(addresses)
    }
}
