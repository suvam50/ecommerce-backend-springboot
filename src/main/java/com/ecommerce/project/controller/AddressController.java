package com.ecommerce.project.controller;

import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.service.*;
import com.ecommerce.project.util.*;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;
    @Autowired
    AuthUtil authUtil;

    @PostMapping("/addresses")
    public ResponseEntity<?> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO, user);
        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getAllAddresses() {
        //User user=authUtil.loggedInUser();
        AddressResponse addresses = addressService.getAllAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<?> getSpecificAddress(@PathVariable Long addressId) {
        //User user=authUtil.loggedInUser();
        AddressDTO address = addressService.getSpecificAddress(addressId);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @GetMapping("/addresses/user/address")
    public ResponseEntity<?> getUserAddressList() {
        User user = authUtil.loggedInUser();
        AddressResponse address = addressService.getListOfAddressesOfUser(user.getUserId());
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(@Valid @RequestBody AddressDTO addressDTO, @PathVariable Long addressId) {
        User user = authUtil.loggedInUser();
        AddressDTO updatedAddressDTO = addressService.updateAddress(addressDTO, addressId);
        return new ResponseEntity<>(updatedAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        //User user=authUtil.loggedInUser();
        AddressDTO address = addressService.deleteAddress(addressId);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }
}