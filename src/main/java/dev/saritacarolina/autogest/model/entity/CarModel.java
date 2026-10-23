package dev.saritacarolina.autogest.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "car_model")
public class CarModel {

    //Llave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "brand", nullable = false, length = 60)
    private String brand;


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}