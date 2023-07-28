package com.electronicstore.springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/*import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;*/

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="product_id_seq")
    @SequenceGenerator(name = "product_id_seq", sequenceName = "product_id_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    private String name;

    private String description;

    private Double price;

    private Long categoryId;

    public Product() {}

    public Product(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Product(Long id, String name, String description) {
        this(name, description);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
