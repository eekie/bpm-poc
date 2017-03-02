package org.tails.bpm.test.model;

import java.io.Serializable;

public class Customer implements Serializable {

    //static final long serialVersionUID = 1L;

    private String name;
    private ContactInfo contactInfo;

    public Customer(String name, String phone, String email) {
        this.name = name;
        this.contactInfo = new ContactInfo(phone, email);
    }

    public String getName() {
        return name;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

}
