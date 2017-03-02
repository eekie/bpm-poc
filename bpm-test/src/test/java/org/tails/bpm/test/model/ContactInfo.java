package org.tails.bpm.test.model;

import java.io.Serializable;

public class ContactInfo implements Serializable {

    //static final long serialVersionUID = 2L;

    private String phone;
    private String email;
    private String aaaa;
    private String ccc;
//    public String getAasd() {
//        return aasd;
//    }
//
//    public void setAasd(String aasd) {
//        this.aasd = aasd;
//    }

    //private String aasd;

    public ContactInfo(String phone, String email) {
        this.phone = phone;
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

}
