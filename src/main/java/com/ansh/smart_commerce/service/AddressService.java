package com.ansh.smart_commerce.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.dto.AddressRequest;
import com.ansh.smart_commerce.dto.AddressResponse;
import com.ansh.smart_commerce.entity.Address;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.exception.AddressNotFoundException;
import com.ansh.smart_commerce.repository.AddressRepository;
import com.ansh.smart_commerce.security.SecurityHelper;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;
    private final SecurityHelper securityHelper;

    public AddressService(AddressRepository addressRepository, SecurityHelper securityHelper) {
        this.addressRepository = addressRepository;
        this.securityHelper = securityHelper;
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        // Fallback for compatibility, but resolve securely
        User user = securityHelper.getCurrentUser();
        log.info("Adding address for user {}", user.getId());

        Address address = new Address();
        mapRequestToEntity(request, address);
        address.setUser(user);

        if (request.isDefault()) {
            clearExistingDefault(user);
        }

        Address saved = addressRepository.save(address);
        log.info("Address {} saved for user {}", saved.getId(), user.getId());
        return AddressResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesByUser(Long userId) {
        User user = securityHelper.getCurrentUser();
        log.info("Fetching addresses for user {}", user.getId());
        return addressRepository.findByUser(user).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        User user = securityHelper.getCurrentUser();
        log.info("Updating address {} for user {}", addressId, user.getId());
        Address address = resolveAddress(addressId);
        
        if (address.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You do not own this address");
        }

        mapRequestToEntity(request, address);

        if (request.isDefault()) {
            clearExistingDefault(address.getUser());
        }

        Address saved = addressRepository.save(address);
        log.info("Address {} updated", addressId);
        return AddressResponse.from(saved);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        User user = securityHelper.getCurrentUser();
        log.info("Deleting address {} for user {}", addressId, user.getId());
        Address address = resolveAddress(addressId);
        
        if (address.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You do not own this address");
        }

        addressRepository.delete(address);
        log.info("Address {} deleted", addressId);
    }

    @Transactional
    public AddressResponse setDefault(Long addressId) {
        User user = securityHelper.getCurrentUser();
        log.info("Setting address {} as default for user {}", addressId, user.getId());
        Address address = resolveAddress(addressId);
        
        if (address.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You do not own this address");
        }

        clearExistingDefault(address.getUser());
        address.setDefault(true);
        Address saved = addressRepository.save(address);
        log.info("Address {} is now the default for user {}", addressId, address.getUser().getId());
        return AddressResponse.from(saved);
    }

    private void clearExistingDefault(User user) {
        addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(existing -> {
            existing.setDefault(false);
            addressRepository.save(existing);
        });
    }

    private void mapRequestToEntity(AddressRequest request, Address address) {
        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setHouseNumber(request.getHouseNumber());
        address.setStreet(request.getStreet());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());
    }

    private Address resolveAddress(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address not found: {}", addressId);
                    return new AddressNotFoundException(addressId);
                });
    }
}
