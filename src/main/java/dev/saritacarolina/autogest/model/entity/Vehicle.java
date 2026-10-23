package dev.saritacarolina.autogest.model.entity;

import dev.saritacarolina.autogest.model.constant.PlateColor;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //Placa normalizada (ej. ABC-123), única
    @Column(name = "license_plate", nullable = false, unique = true, length = 7)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "plate_color", nullable = false, length = 30)
    private PlateColor plateColor;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_model_id", nullable = false)
    private CarModel carModel;


    public Long getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public PlateColor getPlateColor() {
        return plateColor;
    }

    public void setPlateColor(PlateColor plateColor) {
        this.plateColor = plateColor;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public CarModel getCarModel() {
        return carModel;
    }

    public void setCarModel(CarModel carModel) {
        this.carModel = carModel;
    }
}