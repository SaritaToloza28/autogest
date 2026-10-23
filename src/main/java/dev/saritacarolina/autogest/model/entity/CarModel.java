package dev.saritacarolina.autogest.model.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "car_model",
        //La combinación marca + modelo no se puede repetir
        uniqueConstraints = @UniqueConstraint(columnNames = {"brand", "name"})
)
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

    //Dos modelos son el mismo si tienen el mismo id en la base de datos (igual que en SparePart).
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarModel other)) return false;
        return id != null && id.equals(other.getId());
    }

    //Valor fijo para que el objeto no cambie de posición en un Set al recibir su id, y coincida con su proxy.
    @Override
    public int hashCode() {
        return CarModel.class.hashCode();
    }
}