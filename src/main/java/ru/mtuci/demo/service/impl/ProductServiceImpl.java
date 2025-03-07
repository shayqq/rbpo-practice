package ru.mtuci.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationProduct;
import ru.mtuci.demo.repository.ProductRepository;
import ru.mtuci.demo.service.ProductService;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService  {

    private final ProductRepository productRepository;

    @Override
    public Optional<ApplicationProduct> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public String updateProduct(Long id, String name, Boolean isBlocked) {
        Optional<ApplicationProduct> product = getProductById(id);

        if (product.isEmpty()) return "Product Not Found";

        ApplicationProduct newProduct = product.get();
        newProduct.setName(name);
        newProduct.setBlocked(isBlocked);
        productRepository.save(newProduct);
        return "OK";
    }

    @Override
    public Long createProduct(String name, Boolean isBlocked) {
        ApplicationProduct product = new ApplicationProduct();
        product.setBlocked(isBlocked);
        product.setName(name);
        productRepository.save(product);
        return productRepository.findTopByOrderByIdDesc().get().getId();
    }

}