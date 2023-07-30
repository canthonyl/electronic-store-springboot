package com.electronicstore.springboot.model;


import jakarta.persistence.*;

@Entity
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_category_id_seq")
    @SequenceGenerator(name = "product_category_id_seq", sequenceName = "product_category_id_seq", allocationSize = 1)
    private Long id;

    private String name;

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

}
