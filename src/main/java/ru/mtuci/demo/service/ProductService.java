package ru.mtuci.demo.service;

import ru.mtuci.demo.model.ApplicationProduct;
import java.util.Optional;

public interface ProductService {

    Optional<ApplicationProduct> getProductById(Long id);
    String updateProduct(Long id, String name, Boolean isBlocked);
    Long createProduct(String name, Boolean isBlocked);

}