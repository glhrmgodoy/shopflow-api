package com.godoy.order.mapper;

import com.godoy.order.dto.request.ProductRequest;
import com.godoy.order.dto.response.ProductResponse;
import com.godoy.order.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toResponse(Product product);
}
