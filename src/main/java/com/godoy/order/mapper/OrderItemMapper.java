package com.godoy.order.mapper;

import com.godoy.order.dto.request.OrderItemRequest;
import com.godoy.order.dto.response.OrderItemResponse;
import com.godoy.order.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemMapper {

    @Mapping(source = "product.name", target = "productName")
    OrderItemResponse toResponse(OrderItem orderItem);
}
