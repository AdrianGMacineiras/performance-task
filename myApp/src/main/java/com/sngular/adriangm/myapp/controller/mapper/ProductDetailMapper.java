package com.sngular.adriangm.myapp.controller.mapper;

import com.sngular.adriangm.myapp.dto.ProductDetailDTO;
import com.sngular.adriangm.myapp.model.ProductDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductDetailMapper {

	ProductDetailDTO toApiModel(ProductDetail productDetail);
}
