package com.electronicstore.springboot.model;

import jakarta.annotation.Generated;
import jakarta.persistence.*;

@Embeddable
//@Entity
public class TestItem {

    //@Id
    //@Column(name="id")
    //sequence based generator
    //@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="shopping_cart_test_item_id_seq")
    //@SequenceGenerator(name = "shopping_cart_test_item_id_seq", sequenceName = "shopping_cart_test_item_id_seq", allocationSize = 1)
    //@Id
    //@GeneratedValue(strategy=GenerationType.IDENTITY)
    //@Column(name = "id", updatable = false, nullable = false)
    @Column(name="id", insertable = false)
    private Long id;

    private String text;

    public TestItem() {
    }

    public TestItem(String t) {
        text = t;
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

    @Override
    public String toString() {
        return "TestItem{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }
}
