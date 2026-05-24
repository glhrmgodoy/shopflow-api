package com.godoy.order.mapper;

import com.godoy.order.dto.response.OrderResponse;
import com.godoy.order.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "items", target = "items")
    OrderResponse toResponse(Order order);
}
