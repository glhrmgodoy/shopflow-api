package com.godoy.order.service;

import com.godoy.order.dto.request.CustomerRequest;
import com.godoy.order.dto.response.CustomerResponse;
import com.godoy.order.exception.BusinessException;
import com.godoy.order.exception.NotFoundException;
import com.godoy.order.mapper.CustomerMapper;
import com.godoy.order.model.entity.Customer;
import com.godoy.order.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (customerRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        Customer customer = customerMapper.toEntity(request);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    public CustomerResponse findById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        if (!customer.getEmail().equals(request.email()) &&
                customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (!customer.getCpf().equals(request.cpf()) &&
                customerRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setCpf(request.cpf());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());

        Customer update =  customerRepository.save(customer);
        return customerMapper.toResponse(update);
    }

    public void delete(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        customer.setActive(false);
        customerRepository.save(customer);
    }
}
