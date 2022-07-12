package br.com.cursoudemy.productapi.modoles.product.rabbitmq;

import br.com.cursoudemy.productapi.modoles.product.dto.ProductStockDTO;
import br.com.cursoudemy.productapi.modoles.product.service.ProductService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductStockListener {

    @Autowired
    private ProductService productService;

    @RabbitListener(queues = "${app-config.rabbit.queue.product-stock}")
    public void recieveProductStockMessage(ProductStockDTO product) {
        productService.updateProductStock(product);
    }
}
