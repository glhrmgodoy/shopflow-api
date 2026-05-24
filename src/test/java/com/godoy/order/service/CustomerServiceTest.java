package com.godoy.order.service;

import com.godoy.order.dto.request.CustomerRequest;
import com.godoy.order.dto.response.CustomerResponse;
import com.godoy.order.exception.BusinessException;
import com.godoy.order.exception.NotFoundException;
import com.godoy.order.mapper.CustomerMapper;
import com.godoy.order.model.entity.Customer;
import com.godoy.order.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest request;
    private CustomerResponse response;

    @BeforeEach
    void setUp() {
        request = new CustomerRequest(
                "Maria Clara",
                "maria.clara@email.com",
                "99999999999",
                "11888888888",
                "Av Angélica"
        );

        customer = Customer.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .email(request.email())
                .cpf(request.cpf())
                .phone(request.phone())
                .address(request.address())
                .active(true)
                .build();

        response = new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getCpf(),
                customer.getPhone(),
                customer.getAddress(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar cliente com sucesso")
        void shouldCreateCustomerSuccessfully() {
            when(customerRepository.existsByEmail(request.email())).thenReturn(false);
            when(customerRepository.existsByCpf(request.cpf())).thenReturn(false);
            when(customerMapper.toEntity(request)).thenReturn(customer);
            when(customerRepository.save(any())).thenReturn(customer);
            when(customerMapper.toResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.create(request);

            assertThat(result).isNotNull();

            assertThat(result.name()).isEqualTo(customer.getName());
            assertThat(result.email()).isEqualTo(customer.getEmail());
            verify(customerRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando email já cadastrado")
        void shouldThrowBusinessExceptionWhenEmailAlreadyExists() {
            when(customerRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email já cadastrado");

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando CPF já cadastrado")
        void shouldThrowBusinessExceptionWhenCpfAlreadyExists() {
            when(customerRepository.existsByCpf(request.cpf())).thenReturn(true);

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("CPF já cadastrado");

            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retorna lista de clientes")
        void shouldReturnListOfCustomers() {
            when(customerRepository.findAll()).thenReturn(List.of(customer));
            when(customerMapper.toResponse(customer)).thenReturn(response);

            List<CustomerResponse> result = customerService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo(customer.getName());
        }


        @Test
        @DisplayName("Deve retornar lista vazia quando não há clientes")
        void shouldReturnEmptyListWhenNoClients() {
            when(customerRepository.findAll()).thenReturn(List.of());

            List<CustomerResponse> result = customerService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar cliente por ID com sucesso")
        void shouldFindCustomerByIdSuccessfully() {
            when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
            when(customerMapper.toResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.findById(customer.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(customer.getId());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando cliente não encontrado")
        void shouldThrowNotFoundWhenCustomerNotFound() {
            when(customerRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar cliente com sucesso")
        void shouldUpdateCustomerSuccessfully() {
            customer.setEmail("email@email.com");
            customer.setCpf("88888888888");

            when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
            when(customerRepository.existsByEmail(request.email())).thenReturn(false);
            when(customerRepository.existsByCpf(request.cpf())).thenReturn(false);
            when(customerRepository.save(any())).thenReturn(customer);
            when(customerMapper.toResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.update(customer.getId(), request);

            assertThat(result).isNotNull();
            verify(customerRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar cliente inexistente")
        void shouldThrowNotFoundWhenUpdatingNonExistentCustomer() {
            when(customerRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar email já usado por outro cliente")
        void shouldThrowBusinessExceptionWhenEmailUsedByAnotherCustomer() {
            customer.setEmail("outro.email@email.com");

            when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
            when(customerRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email já cadastrado");

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar com CPF já usado por outro cliente")
        void shouldThrowBusinessExceptionWhenCpfUsedByAnotherCustomer() {
            customer.setCpf("88888888888");

            when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
            when(customerRepository.existsByCpf(request.cpf())).thenReturn(true);

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("CPF já cadastrado");

            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve inativar cliente com sucesso")
        void shouldDeleteCustomerSuccessfully() {
            when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

            customerService.delete(customer.getId());

            verify(customerRepository).save(argThat(c -> !c.getActive()));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao inativar cliente inexistente")
        void shouldThrowNotFoundWhenDeletingNonExistentCustomer() {
            when(customerRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");

            verify(customerRepository, never()).save(any());
        }
    }
}