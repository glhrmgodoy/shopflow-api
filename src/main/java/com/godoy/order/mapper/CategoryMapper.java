package com.godoy.order.mapper;

import com.godoy.order.dto.request.CategoryRequest;
import com.godoy.order.dto.response.CategoryResponse;
import com.godoy.order.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);
}
