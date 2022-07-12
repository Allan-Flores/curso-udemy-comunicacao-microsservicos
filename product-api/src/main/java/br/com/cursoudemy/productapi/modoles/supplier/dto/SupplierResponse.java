package br.com.cursoudemy.productapi.modoles.supplier.dto;

import br.com.cursoudemy.productapi.modoles.category.model.Category;
import br.com.cursoudemy.productapi.modoles.supplier.model.Supplier;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class SupplierResponse {

    private Integer id;

    private String name;

    public static SupplierResponse of(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        BeanUtils.copyProperties(supplier, response);
        return response;
    }
}
