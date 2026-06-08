package com.example.demoStep3.dto;

//package kr.co.hanbit.demo123456.dto;

public class CustomerDto {
    private int custid;
    private String name;

    public CustomerDto() {
    }

    public CustomerDto(int id, String name) {
        this.custid = id;
        this.name = name;
        // 주소, 전화번호는 DTO에서 제외
        // 보이고 싶은 정보만 남김
    }

    // Getter
    public int getCustid() {
        return custid;
    }

    public String getName() {
        return name;
    }

    // 2. Jackson이 JSON의 값을 필드에 바인딩할 수 있도록 Setter 추가
    public void setCustid(int custid) {
        this.custid = custid;
    }

    public void setName(String name) {
        this.name = name;
    }
}
