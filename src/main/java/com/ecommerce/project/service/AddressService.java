package com.ecommerce.project.service;

import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import jakarta.validation.*;

public interface AddressService {
    AddressDTO createAddress( AddressDTO addressDTO, User user);

    AddressResponse getAllAddresses();

    AddressDTO getSpecificAddress(Long addressId);

    AddressResponse getListOfAddressesOfUser(Long userId);

    AddressDTO deleteAddress(Long addressId);

    AddressDTO updateAddress(@Valid AddressDTO addressDTO, Long addressId);
}
