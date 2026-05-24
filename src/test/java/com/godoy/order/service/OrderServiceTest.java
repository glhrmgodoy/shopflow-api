package com.godoy.order.service;

import com.godoy.order.dto.request.OrderItemRequest;
import com.godoy.order.dto.request.OrderRequest;
import com.godoy.order.dto.response.OrderItemResponse;
import com.godoy.order.dto.response.OrderResponse;
import com.godoy.order.exception.BusinessException;
import com.godoy.order.exception.NotFoundException;
import com.godoy.order.mapper.OrderMapper;
import com.godoy.order.model.entity.Customer;
import com.godoy.order.model.entity.Order;
import com.godoy.order.model.entity.OrderItem;
import com.godoy.order.model.entity.Product;
import com.godoy.order.model.enums.OrderStatus;
import com.godoy.order.model.enums.PaymentMethod;
import com.godoy.order.repository.CustomerRepository;
import com.godoy.order.repository.OrderRepository;
import com.godoy.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private Customer customer;
    private Product product;
    private OrderRequest request;
    private OrderResponse response;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Aitana Bonmatí")
                .email("aitana.bonmati@email.com")
                .cpf("77777777777")
                .phone("11234567890")
                .address("Rua Camp Nou, 30")
                .active(true)
                .build();

        product = Product.builder()
                .id(UUID.randomUUID())
                .name("PlayStation 5")
                .description("PlayStation 5 1TB")
                .price(new BigDecimal("2500.00"))
                .stockQuantity(50)
                .active(true)
                .build();

        request = new OrderRequest(
                customer.getId(),
                PaymentMethod.PIX,
                List.of(new OrderItemRequest(product.getId(), 2)),
                "Entregar até dia 30/05 até às 18h"
        );

        OrderItem  item = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("2500.00"))
                .totalPrice(new BigDecimal("5000.00"))
                .build();

        order = Order.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .paymentMethod(PaymentMethod.PIX)
                .totalAmount(new BigDecimal("5000.00"))
                .status(OrderStatus.PENDING)
                .notes(request.notes())
                .items(List.of(item))
                .build();

        item.setOrder(order);

        response = new OrderResponse(
                order.getId(),
                customer.getName(),
                order.getPaymentMethod(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getNotes(),
                List.of(new OrderItemResponse(
                        item.getId(),
                        product.getName(),
                        2,
                        new BigDecimal("2500.00"),
                        new BigDecimal("5000.00")
                )),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar pedido com sucesso")
        void shouldCreateOrderSuccessfully() {
            when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(customer));
            when(productService.findProductForOrder(product.getId())).thenReturn(product);
            when(orderRepository.save(any())).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(response);

            OrderResponse result = orderService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.totalAmount()).isEqualByComparingTo("5000.00");
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.items()).hasSize(1);
            verify(productRepository).save(argThat(p -> p.getStockQuantity().equals(48)));
            verify(orderRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando cliente não encontrado")
        void shouldThrowNotFoundWhenClientNotFound() {
            when(customerRepository.findById(request.customerId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando cliente está inativo")
        void shouldThrowBusinessExceptionWhenCustomerInactive() {
            customer.setActive(false);
            when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(customer));

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Cliente inativo não pode realizar pedidos");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando produto está inativo")
        void shouldThrowBusinessExceptionWhenProductInactive() {
            product.setActive(false);
            when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(customer));
            when(productService.findProductForOrder(product.getId())).thenReturn(product);

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Produto" + product.getName() + "está inativo");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando estoque insuficiente")
        void shouldThrowBusinessExceptionWhenInsufficientStock() {
            product.setStockQuantity(1);
            when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(customer));
            when(productService.findProductForOrder(product.getId())).thenReturn(product);

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Estoque insuficiente para o produto" + product.getName());

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de pedidos")
        void shouldReturnListOfOrders() {
            when(orderRepository.findAll()).thenReturn(List.of(order));
            when(orderMapper.toResponse(order)).thenReturn(response);

            List<OrderResponse> result = orderService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().customerName()).isEqualTo(customer.getName());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há pedidos")
        void shouldReturnEmptyListWhenNoOrders() {
            when(orderRepository.findAll()).thenReturn(List.of());

            List<OrderResponse> result = orderService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar pedido por ID com sucesso")
        void shouldFindOrderByIdSuccessfully() {
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(orderMapper.toResponse(order)).thenReturn(response);

            OrderResponse result = orderService.findById(order.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(order.getId());
            assertThat(result.customerName()).isEqualTo(customer.getName());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não encontrado")
        void shouldThrowNotFoundWhenOrderNotFound() {
            when(orderRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Pedido não encontrado");
        }
    }

    @Nested
    @DisplayName("findAllByCustomerId")
    class FindAllByCustomerId {

        @Test
        @DisplayName("Deve retornar pedidos do cliente")
        void shouldReturnOrdersByCustomerId() {
            when(customerRepository.existsById(customer.getId())).thenReturn(true);
            when(orderRepository.findAllByCustomerId(customer.getId())).thenReturn(List.of(order));
            when(orderMapper.toResponse(order)).thenReturn(response);

            List<OrderResponse> result = orderService.findAllByCustomerId(customer.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().customerName()).isEqualTo(customer.getName());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando cliente não encontrado")
        void shouldThrowNotFoundWhenCustomerNotFound() {
            when(customerRepository.existsById(any())).thenReturn(false);

            assertThatThrownBy(() -> orderService.findAllByCustomerId(customer.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");
        }
    }

    @Nested
    @DisplayName("confirm")
    class Confirm {

        @Test
        @DisplayName("Deve confirmar pedido com sucesso")
        void shouldConfirmOrderSuccessfully() {
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);
            when(orderMapper.toResponse(any())).thenReturn(response);

            orderService.confirm(order.getId());

            verify(orderRepository).save(argThat(o ->
                    o.getStatus().equals(OrderStatus.CONFIRMED
            )));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não encontrado")
        void shouldThrowNotFoundWhenOrderNotFound() {
            when(orderRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Pedido não encontrado");
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao confirmar pedido não PENDING")
        void shouldThrowBusinessExceptionWhenConfirmingNonPendingOrder() {
            order.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.confirm(order.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Apenas pedidos PENDING podem ser confirmados");

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("ship")
    class Ship {

        @Test
        @DisplayName("Deve enviar pedido com sucesso")
        void shouldShipOrderSuccessfully() {
            order.setStatus(OrderStatus.CONFIRMED);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);
            when(orderMapper.toResponse(any())).thenReturn(response);

            orderService.ship(order.getId());

            verify(orderRepository).save(argThat(o ->
                    o.getStatus().equals(OrderStatus.SHIPPED
                    )));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não encontrado")
        void shouldThrowNotFoundWhenOrderNotFound() {
            when(orderRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Pedido não encontrado");
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao enviar pedido não CONFIRMED")
        void shouldThrowBusinessExceptionWhenShippingNonConfirmedOrder() {
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.ship(order.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Apenas pedidos CONFIRMED podem ser enviados");

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deliver")
    class Deliver {

        @Test
        @DisplayName("Deve entregar pedido com sucesso")
        void shouldDeliverOrderSuccessfully() {
            order.setStatus(OrderStatus.SHIPPED);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);
            when(orderMapper.toResponse(any())).thenReturn(response);

            orderService.deliver(order.getId());

            verify(orderRepository).save(argThat(o ->
                    o.getStatus().equals(OrderStatus.DELIVERED
            )));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não encontrado")
        void shouldThrowNotFoundWhenOrderNotFound() {
            when(orderRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Pedido não encontrado");
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao entregar pedido não SHIPPED")
        void shouldThrowBusinessExceptionWhenDeliveringNonShippedOrder() {
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.deliver(order.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Apenas pedidos SHIPPED podem ser entregues");

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("Deve cancelar pedido PENDING com sucesso e devolver ao estoque")
        void shouldCancelOrderSuccessfully() {
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            orderService.cancel(order.getId());

            verify(productRepository).save(argThat(p -> p.getStockQuantity().equals(52)));
            verify(orderRepository).save(argThat(o ->
                    o.getStatus().equals(OrderStatus.CANCELLED
            )));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando pedido não encontrado")
        void shouldThrowNotFoundWhenOrderNotFound() {
            when(orderRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Pedido não encontrado");
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar pedido SHIPPED")
        void shouldThrowBusinessExceptionWhenCancellingShippedOrder() {
            order.setStatus(OrderStatus.SHIPPED);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancel(order.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Pedidos SHIPPED ou DELIVERED não podem ser cancelados");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar pedido DELIVERED")
        void shouldThrowBusinessExceptionWhenCancellingDeliveredOrder() {
            order.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancel(order.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Pedidos SHIPPED ou DELIVERED não podem ser cancelados");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar pedido CANCELLED")
        void shouldThrowBusinessExceptionWhenCancellingAlreadyCancelledOrder() {
            order.setStatus(OrderStatus.CANCELLED);
            when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancel(order.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Pedido já está cancelado");

            verify(orderRepository, never()).save(any());
        }
    }
}