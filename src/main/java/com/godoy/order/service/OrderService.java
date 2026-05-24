package com.godoy.order.service;

import com.godoy.order.dto.request.OrderItemRequest;
import com.godoy.order.dto.request.OrderRequest;
import com.godoy.order.dto.response.OrderResponse;
import com.godoy.order.exception.BusinessException;
import com.godoy.order.exception.NotFoundException;
import com.godoy.order.mapper.OrderMapper;
import com.godoy.order.model.entity.Customer;
import com.godoy.order.model.entity.Order;
import com.godoy.order.model.entity.OrderItem;
import com.godoy.order.model.entity.Product;
import com.godoy.order.model.enums.OrderStatus;
import com.godoy.order.repository.CustomerRepository;
import com.godoy.order.repository.OrderRepository;
import com.godoy.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    public OrderResponse create(OrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        if (!customer.getActive()) {
            throw new BusinessException("Cliente inativo não pode realizar pedidos");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productService.findProductForOrder(itemRequest.productId());

            if (!product.getActive()) {
                throw new BusinessException("Produto " + product.getName() + " está inativo");
            }

            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new BusinessException("Estoque insuficiente para o produto " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();

            items.add(item);
            totalAmount = totalAmount.add(totalPrice);

            product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());
            productRepository.save(product);
        }

        Order order = Order.builder()
                .customer(customer)
                .paymentMethod(request.paymentMethod())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .notes(request.notes())
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public OrderResponse findById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));
        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> findAllByCustomerId(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Cliente não encontrado");
        }

        return orderRepository.findAllByCustomerId(customerId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public OrderResponse confirm(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BusinessException("Apenas pedidos PENDING podem ser confirmados");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        Order updated = orderRepository.save(order);
        return orderMapper.toResponse(updated);
    }

    public OrderResponse ship(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));

        if (!order.getStatus().equals(OrderStatus.CONFIRMED)) {
            throw new BusinessException("Apenas pedidos CONFIRMED podem ser enviados");
        }

        order.setStatus(OrderStatus.SHIPPED);
        Order updated = orderRepository.save(order);
        return orderMapper.toResponse(updated);
    }

    public OrderResponse deliver(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));

        if (!order.getStatus().equals(OrderStatus.SHIPPED)) {
            throw new BusinessException("Apenas pedidos SHIPPED podem ser entregues");
        }

        order.setStatus(OrderStatus.DELIVERED);
        Order updated = orderRepository.save(order);
        return orderMapper.toResponse(updated);
    }

    public void cancel(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));

        if (order.getStatus().equals(OrderStatus.SHIPPED) ||
                order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new BusinessException("Pedidos SHIPPED ou DELIVERED não podem ser cancelados");
        }

        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            throw new BusinessException("Pedido já está cancelado");
        }

        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
