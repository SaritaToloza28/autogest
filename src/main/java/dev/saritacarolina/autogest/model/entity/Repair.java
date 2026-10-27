package dev.saritacarolina.autogest.model.entity;

import dev.saritacarolina.autogest.model.constant.RepairStatus;
import dev.saritacarolina.autogest.model.constant.ServiceType;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "repair")
public class Repair {

    //Llave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "repair_date", nullable = false)
    private LocalDateTime repairDate;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RepairStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 20)
    private ServiceType serviceType;

    //Solo obligatorias si serviceType = EXTERNAL
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mechanic_id", nullable = false)
    private AppUser mechanic;

    //Tabla de rompimiento repair_spare_part: repuesto -> cantidad usada
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "repair_spare_part", joinColumns = @JoinColumn(name = "repair_id"))
    @MapKeyJoinColumn(name = "spare_part_id")
    @Column(name = "quantity", nullable = false)
    private Map<SparePart, Integer> usedParts = new HashMap<>();


    //Asigna una fecha o un estado si repairDate y status quedaron vacios
    @PrePersist
    private void prePersist() {
        if (repairDate == null) {
            repairDate = LocalDateTime.now();
        }
        if (status == null) {
            status = RepairStatus.PENDING;
        }
    }


    public Long getId() {
        return id;
    }

    public LocalDateTime getRepairDate() {
        return repairDate;
    }

    public void setRepairDate(LocalDateTime repairDate) {
        this.repairDate = repairDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public AppUser getMechanic() {
        return mechanic;
    }

    public void setMechanic(AppUser mechanic) {
        this.mechanic = mechanic;
    }
    public Map<SparePart, Integer> getUsedParts() {
        return usedParts;
    }

    public void setUsedParts(Map<SparePart, Integer> usedParts) {
        this.usedParts = usedParts;
    }
}