package br.com.cursoudemy.productapi.modoles.product.service;

import br.com.cursoudemy.productapi.config.exception.SuccessResponse;
import br.com.cursoudemy.productapi.config.exception.ValidationException;
import br.com.cursoudemy.productapi.modoles.category.model.Category;
import br.com.cursoudemy.productapi.modoles.category.service.CategoryService;
import br.com.cursoudemy.productapi.modoles.product.dto.*;
import br.com.cursoudemy.productapi.modoles.product.model.Product;
import br.com.cursoudemy.productapi.modoles.product.repository.ProductRepository;
import br.com.cursoudemy.productapi.modoles.sales.client.SalesClient;
import br.com.cursoudemy.productapi.modoles.sales.dto.SalesConfirmationDTO;
import br.com.cursoudemy.productapi.modoles.sales.dto.SalesProductResponse;
import br.com.cursoudemy.productapi.modoles.sales.enums.SalesStatus;
import br.com.cursoudemy.productapi.modoles.sales.rebbitmq.SalesConfirmationSender;
import br.com.cursoudemy.productapi.modoles.supplier.model.Supplier;
import br.com.cursoudemy.productapi.modoles.supplier.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SalesConfirmationSender salesConfirmationSender;
    @Autowired
    private SalesClient salesClient;

    public List<ProductResponse> findAll() {
        return productRepository
                .findAll()
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findByName(String name) {
        if (isEmpty(name)) {
            throw new ValidationException("The product name must be informed.");
        }
        return productRepository
                .findByNameIgnoreCaseContaining(name)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public ProductResponse findById(Integer id) {
        validateInformedId(id);
        return ProductResponse
                .of(productRepository.findById(id)
                .orElseThrow(() -> new ValidationException("There's no product for the given ID,")));
    }

    public List<ProductResponse> findBySupplierId(Integer supplierId) {
        if (isEmpty(supplierId)) {
            throw new ValidationException("The product' supplier ID was not informed.");
        }
        return productRepository
                .findBySupplierId(supplierId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> findByCategoryId(Integer categoryId) {
        if (isEmpty(categoryId)) {
            throw new ValidationException("The product' category ID was not informed.");
        }
        return productRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public ProductResponse save(ProductRequest request) {
        validateProductNameInformed(request);
        validateCategoryAndSupplierIdInformed(request);

        Category category = categoryService.findById(request.getCategoryId());
        Supplier supplier = supplierService.findById(request.getSupplierId());

        Product product = productRepository.save(Product.of(request, supplier, category));
        return ProductResponse.of(product);
    }

    public ProductResponse update(ProductRequest request,
                                  Integer id) {
        validateProductNameInformed(request);
        validateCategoryAndSupplierIdInformed(request);
        validateInformedId(id);

        Category category = categoryService.findById(request.getCategoryId());
        Supplier supplier = supplierService.findById(request.getSupplierId());
        Product product = (Product.of(request, supplier, category));
        product.setId(id);
        productRepository.save(product);
        return ProductResponse.of(product);
    }

    private void validateProductNameInformed(ProductRequest request) {
        if (isEmpty(request.getName())) {
            throw new ValidationException("The product's name was not informed.");
        }
        if (isEmpty(request.getQuantityAvailable())) {
            throw new ValidationException("The product's quantity was not informed.");
        }
        if (request.getQuantityAvailable() <= 0) {
            throw new ValidationException("The quantity should not be less or equal to zero.");
        }
    }

    private void validateCategoryAndSupplierIdInformed(ProductRequest request) {
        if (isEmpty(request.getCategoryId())) {
            throw new ValidationException("The category ID was not informed.");
        }

        if (isEmpty(request.getSupplierId())) {
            throw new ValidationException("The supplier ID was not informed.");
        }
    }

    public Boolean existsByCategoryId(Integer categoryId){
        return productRepository.existsByCategoryId(categoryId);
    }

    public Boolean existsBySupplierId(Integer supplierId){
        return productRepository.existsBySupplierId(supplierId);
    }

    public SuccessResponse delete(Integer id) {
        validateInformedId(id);
        productRepository.deleteById(id);
        return SuccessResponse.create("The product was deleted.");
    }

    private void validateInformedId(Integer id) {
        if (isEmpty(id)) {
            throw new ValidationException("The product ID was not informed.");
        }
    }

    @Transactional
    public void updateProductStock(ProductStockDTO product) {
        try {
            validateStockUpdateDate(product);
            ArrayList<Product> productsForUpdate = new ArrayList<>();
            product .getProducts()
                    .forEach(salesProduct -> {
                        Product existingProduct = productRepository.findById(salesProduct.getProductId()).get();
                        if (salesProduct.getQuantity() > existingProduct.getQuantityAvailable()) {
                            throw new ValidationException(
                                    String.format("The product %s is out of stock.", existingProduct.getId()));
                        }
                        existingProduct.updateStock(salesProduct.getQuantity());
                        productsForUpdate.add(existingProduct);
                    });
            if (!isEmpty(productsForUpdate)) {
                productRepository.saveAll(productsForUpdate);
                SalesConfirmationDTO approvedMessage = new SalesConfirmationDTO(product.getSalesId(), SalesStatus.APPROVED);
                salesConfirmationSender.sendSalesConfirmationMessage(approvedMessage);
            }
        } catch (Exception ex) {
            log.error("Error while trying to update stock for message with error: {}", ex.getMessage(),ex);
            salesConfirmationSender.sendSalesConfirmationMessage(
                    new SalesConfirmationDTO(product.getSalesId(), SalesStatus.REJECTED));
        }
    }

    private void validateStockUpdateDate(ProductStockDTO product) {

        if (isEmpty(product)
                || isEmpty(product.getSalesId())) {
            throw new ValidationException("The product data and the sales ID must be informed.");
        }

        if (isEmpty(product.getProducts())) {
            throw new ValidationException("The sales' products must be informed.");
        }

        product.getProducts()
                .forEach(salesProduct -> {
                    if (isEmpty(salesProduct.getQuantity())
                        || isEmpty(salesProduct.getProductId())) {
                        throw new ValidationException("The productsID and the quantity must be informed.");
                    }
                });
    }

    public ProductSalesResponse findProductSales(Integer id) {
        Product product = productRepository.findById(id).get();
        try {
            SalesProductResponse sales = salesClient
                    .findSalesByProductId(product.getId())
                    .orElseThrow(() -> new ValidationException("The sales was not found by this product."));
            return ProductSalesResponse.of(product, sales.getSalesIds());
        } catch (Exception ex) {
            throw new ValidationException("There was an error trying to get the product's sales.");
        }
    }

    public SuccessResponse checkProductsStock(ProductCheckStockRequest request) {
        if (isEmpty(request) || isEmpty(request.getProducts())) {
            throw new ValidationException("The request data and products must be informed.");
        }
        request.getProducts()
                .forEach(this::validateStock);
        return SuccessResponse.create("The stock is ok!");
    }

    private void validateStock(ProductQuantityDTO productQuantity) {
        if (isEmpty(productQuantity.getProductId()) || isEmpty(productQuantity.getQuantity())) {
            throw new ValidationException("Product ID and quantity must be informed.");
        }
        Product product = productRepository.findById(productQuantity.getProductId()).get();
        if (productQuantity.getQuantity() > product.getQuantityAvailable()) {
            throw new ValidationException(String.format("The product %s is out of stock", product.getId()));
        }
    }
}
