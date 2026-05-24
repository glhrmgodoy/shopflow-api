package com.godoy.order.mapper;

import com.godoy.order.dto.request.CustomerRequest;
import com.godoy.order.dto.response.CustomerResponse;
import com.godoy.order.model.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {

    Customer toEntity(CustomerRequest request);

    CustomerResponse toResponse(Customer customer);
}
