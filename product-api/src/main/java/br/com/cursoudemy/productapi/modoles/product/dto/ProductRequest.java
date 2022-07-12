package br.com.cursoudemy.productapi.modoles.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductRequest {

    private String name;
    @JsonProperty("created_at")
    private Integer quantityAvailable;
    private Integer supplierId;
    private Integer categoryId;
}
