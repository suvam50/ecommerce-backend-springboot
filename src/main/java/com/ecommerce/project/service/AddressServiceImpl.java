package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.*;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.*;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {

        Address address=modelMapper.map(addressDTO, Address.class);

        List<Address> addresses=user.getAddresses();
        addresses.add(address);
        user.setAddresses(addresses);

        address.setUser(user);
        Address newAddress=addressRepository.save(address);

        return modelMapper.map(newAddress, AddressDTO.class);
    }

    @Override
    public AddressResponse getAllAddresses() {
        List<Address> addresses=addressRepository.findAll();
        if(addresses.isEmpty())
        {
            throw new APIException("No addresses were found");
        }
        List<AddressDTO>addressDTOS=addresses.stream().map(
                address -> modelMapper.map(address, AddressDTO.class))
                        .toList();
        AddressResponse addressResponse=new AddressResponse();
        addressResponse.setResponse(addressDTOS);
        return addressResponse;
    }

    @Override
    public AddressDTO getSpecificAddress(Long addressId) {
        Address address=addressRepository.findById(addressId).orElseThrow(
                () -> new ResourceNotFoundException("Address", "addressId", addressId));
        return modelMapper.map(address,AddressDTO.class);
    }

    @Override
    public AddressResponse getListOfAddressesOfUser(Long userId) {
        List<Address>userAddressesList=addressRepository.findAddressByUserId(userId);
        if(userAddressesList.isEmpty())
        {
            throw new APIException("No addresses found for the user with id: " + userId);
        }
        List<AddressDTO>addressDTOS=userAddressesList.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
        AddressResponse addressResponse=new AddressResponse();
        addressResponse.setResponse(addressDTOS);
        return addressResponse;
    }

    @Override
    public AddressDTO deleteAddress(Long addressId) {
        Address address=addressRepository.findById(addressId).orElseThrow(
                () -> new ResourceNotFoundException("Address", "addressId", addressId));
        User user=address.getUser();
        user.getAddresses().removeIf(addressToRemove -> addressToRemove.getAddressId().equals(addressId));
        userRepository.save(user);
        addressRepository.delete(address);
        return modelMapper.map(address,AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, Long addressId) {
        Address addressInDb=addressRepository.findById(addressId).orElseThrow(
                () -> new ResourceNotFoundException("Address", "addressId", addressId));

        addressInDb.setCity(addressDTO.getCity());
        addressInDb.setCountry(addressDTO.getCountry());
        addressInDb.setBuildingName(addressDTO.getBuildingName());
        addressInDb.setStreet(addressDTO.getStreet());
        addressInDb.setPincode(addressDTO.getPincode());
        addressInDb.setState(addressDTO.getState());

        Address updatedAddress=addressRepository.save(addressInDb);
        User user = addressInDb.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }
}
