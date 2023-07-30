package com.electronicstore.springboot.model;

import jakarta.persistence.*;

@Embeddable
public class TestItem {

    @Column(name="id", insertable = false)
    private Long id;

    @ManyToOne
    private Product product;

    private String text;

    public TestItem() {
    }

    public TestItem(String t, Product product) {
        text = t;
        this.product = product;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }


    @Override
    public String toString() {
        return "TestItem{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }
}
