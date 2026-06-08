package com.example.demoStep3.controller;

import com.example.demoStep3.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.demoStep3.dto.CustomerDto;
import com.example.demoStep3.dto.CustomerViewDto;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private DataSource dataSource;

    private final List<Customer> customers = new ArrayList<>(List.of(
            new Customer(1, "박지성", "영국 맨체스터", "000-5000-0001"),
            new Customer(2, "김연아", "대한민국 서울", "000-6000-0001"),
            new Customer(3, "김연경", "대한민국 경기도", "000-7000-0001"),
            new Customer(4, "추신수", "미국 클리블랜드", "000-8000-0001"),
            new Customer(5, "박세리", "대한민국 대전", null)
    ));

    @GetMapping("/all")
    public List<Customer> getCustomersFromMySQL() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT custid, name, address, phone FROM Customer";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
        ) {
            while (rs.next()) {
                Customer c = new Customer(
                        rs.getInt("custid"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone")
                );
                customers.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    @GetMapping
    public List<CustomerViewDto> getCustomersWithViewDto() {
        List<CustomerViewDto> result = new ArrayList<>();
        String sql = "SELECT custid, name, address FROM Customer";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                result.add(new CustomerViewDto(
                        rs.getInt("custid"),
                        rs.getString("name"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @GetMapping("/memory")
    public List<CustomerDto> getAllCustomers() {
        return customers.stream()
                .map(c -> new CustomerDto(c.getCustid(), c.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CustomerDto getCustomerById(@PathVariable int id) {
        return customers.stream()
                .filter(c -> c.getCustid() == id)
                .findFirst()
                .map(c -> new CustomerDto(c.getCustid(), c.getName()))
                .orElse(null);
    }

    @PutMapping("/{id}")
    public String updateCustomer(@PathVariable int id, @RequestBody CustomerDto dto) {
        for (Customer c : customers) {
            if (c.getCustid() == id) {
                c.setName(dto.getName());
                return "수정 완료";
            }
        }
        return "고객을 찾을 수 없습니다";
    }

    @PostMapping
    public String addCustomer(@RequestBody CustomerDto dto) {
        Customer newCustomer = new Customer(
                dto.getCustid(),
                dto.getName(),
                "주소 미입력",
                null
        );
        customers.add(newCustomer);
        return "추가 완료";
    }

    @DeleteMapping("/{id}")
    public String deleteCustomer(@PathVariable int id) {
        boolean removed = customers.removeIf(c -> c.getCustid() == id);
        return removed ? "삭제 완료" : "고객을 찾을 수 없습니다.";
    }
}


/*
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final List<Customer> customers = List.of(
            new Customer(1, "박지성", "영국 맨체스터", "000-5000-0001"),
            new Customer(2, "김연아", "대한민국 서울", "000-6000-0001"),
            new Customer(3, "김연경", "대한민국 경기도", "000-7000-0001"),
            new Customer(4, "추신수", "미국 클리블랜드", "000-8000-0001"),
            new Customer(5, "박세리", "대한민국 대전", null)
    );

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customers;
    }

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable int id) {
        return customers.stream()
                .filter(c -> c.getCustid() == id)
                .findFirst()
                .orElse(null);
    }
}
*/