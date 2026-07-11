package com.ansh.smart_commerce.entity;

import jakarta.validation.constraints.*;
import jakarta.persistence.*;
@Entity
@Table(name="product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotBlank
    private String name;
    @Positive
    private double cost;
    @Positive
    private int stock;
    private String category;
    private String imageUrl;
    public Product(){}
    public Product(String name, double cost, int stock, String category, String imageUrl) {
        this.name = name;
        this.cost = cost;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }
    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getCost() {
        return cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
