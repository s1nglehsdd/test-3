package com.example.shop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text; // Текст комментария

    private String author;

    private String externalResourceUrl;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public Comment() {
    }

    public Comment(String text, String author, String externalResourceUrl, Product product) {
        this.text = text;
        this.author = author;
        this.externalResourceUrl = externalResourceUrl;
        this.product = product;
    }

    public Comment(String text, String author, Product product) {
        this.text = text;
        this.author = author;
        this.product = product;
        this.externalResourceUrl = null;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    // Геттеры и сеттеры для нового поля
    public String getExternalResourceUrl() {
        return externalResourceUrl;
    }

    public void setExternalResourceUrl(String externalResourceUrl) {
        this.externalResourceUrl = externalResourceUrl;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", author='" + author + '\'' +
                ", externalResourceUrl='" + externalResourceUrl + '\'' +
                ", product=" + (product != null ? product.getId() : "null") +
                '}';
    }
}