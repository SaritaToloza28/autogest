package dev.saritacarolina.autogest.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "spare_part")
public class SparePart {

    //Llave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "ean_code", nullable = false, unique = true, length = 13)
    private String eanCode;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    //Ruta relativa de la imagen (ej. /uploads/abc.jpg)
    @Column(name = "image_path", length = 255)
    private String imagePath;

    //Tabla de rompimiento spare_part_car_model (RF-19), sin entidad propia
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "spare_part_car_model",
            joinColumns = @JoinColumn(name = "spare_part_id"),
            inverseJoinColumns = @JoinColumn(name = "car_model_id")
    )
    private Set<CarModel> compatibleModels = new HashSet<>();


    public Long getId() {
        return id;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getEanCode() {
        return eanCode;
    }

    public void setEanCode(String eanCode) {
        this.eanCode = eanCode;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Set<CarModel> getCompatibleModels() {
        return compatibleModels;
    }

    public void setCompatibleModels(Set<CarModel> compatibleModels) {
        this.compatibleModels = compatibleModels;
    }

    //Le dice a Java que dos repuestos son el mismo si tienen exactamente el mismo id en la base de datos.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SparePart other)) return false;
        return id != null && id.equals(other.getId());
    }

    //Manda todos los repuestos a la misma sección del estante para que Java no los pierda de vista antes o después de guardarlos.
    //Usa SparePart.class y no getClass() para que el proxy de Hibernate dé el mismo valor que el objeto real.
    @Override
    public int hashCode() {
        return SparePart.class.hashCode();
    }
}