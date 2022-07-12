package br.com.cursoudemy.productapi.modoles.supplier.repository;

import br.com.cursoudemy.productapi.modoles.supplier.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    List<Supplier> findByNameIgnoreCaseContaining(String name);
}
