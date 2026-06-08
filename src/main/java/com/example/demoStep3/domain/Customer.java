package com.example.demoStep3.domain;

public class Customer {
    private int custid;
    private String name;
    private String address;
    private String phone;

    public Customer(int custid, String name, String address, String phone) {
        this.custid = custid;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public int getCustid() { return custid; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }

    public void setName(String name) {
        this.name = name;
    }
}
