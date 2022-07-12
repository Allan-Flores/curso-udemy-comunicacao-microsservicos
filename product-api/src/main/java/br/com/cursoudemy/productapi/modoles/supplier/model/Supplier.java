package br.com.cursoudemy.productapi.modoles.supplier.model;

import br.com.cursoudemy.productapi.modoles.category.dto.CategoryRequest;
import br.com.cursoudemy.productapi.modoles.category.model.Category;
import br.com.cursoudemy.productapi.modoles.supplier.dto.SupplierRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SUPPLIER")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String name;

    public static Supplier of(SupplierRequest request) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(request, supplier);
        return supplier;
    }
}
