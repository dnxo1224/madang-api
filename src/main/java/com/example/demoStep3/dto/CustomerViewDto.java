package com.example.demoStep3.dto;

public class CustomerViewDto {
    private int custid;
    private String name;
    private String address;

    public CustomerViewDto() {
    }

    public CustomerViewDto(int custid, String name, String address) {
        this.custid = custid;
        this.name = name;
        this.address = address;
    }

    public int getCustid() { return custid; }
    public String getName() { return name; }
    public String getAddress() { return address; }

    public void setCustid(int custid) { this.custid = custid; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
}
