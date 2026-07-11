package com.ansh.smart_commerce.dto;

import com.ansh.smart_commerce.entity.Address;

public class AddressResponse {

    private Long id;
    private String fullName;
    private String phoneNumber;
    private String houseNumber;
    private String street;
    private String landmark;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean isDefault;

    public static AddressResponse from(Address address) {
        AddressResponse r = new AddressResponse();
        r.id = address.getId();
        r.fullName = address.getFullName();
        r.phoneNumber = address.getPhoneNumber();
        r.houseNumber = address.getHouseNumber();
        r.street = address.getStreet();
        r.landmark = address.getLandmark();
        r.city = address.getCity();
        r.state = address.getState();
        r.postalCode = address.getPostalCode();
        r.country = address.getCountry();
        r.isDefault = address.isDefault();
        return r;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getHouseNumber() { return houseNumber; }
    public String getStreet() { return street; }
    public String getLandmark() { return landmark; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public boolean isDefault() { return isDefault; }
}
