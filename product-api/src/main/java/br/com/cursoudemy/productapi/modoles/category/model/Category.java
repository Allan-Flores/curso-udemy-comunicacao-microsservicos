package br.com.cursoudemy.productapi.modoles.category.model;

import br.com.cursoudemy.productapi.modoles.category.dto.CategoryRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CATEGORY")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String description;

    public static Category of(CategoryRequest request) {
        Category category = new Category();
        BeanUtils.copyProperties(request, category);
        return category;
    }
}
