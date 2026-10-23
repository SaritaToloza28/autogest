package dev.saritacarolina.autogest.model.entity;

import dev.saritacarolina.autogest.model.constant.RepairStatus;
import dev.saritacarolina.autogest.model.constant.ServiceType;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

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

    //Motivo de la intervención
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RepairStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 10)
    private ServiceType serviceType;

    //Solo obligatorias si serviceType = EXTERNAL
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_license_plate", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mechanic_id", nullable = false)
    private AppUser mechanic;

    //Posible relación con repuestos usados (pendiente de aprobación del profesor)
    // @OneToMany(mappedBy = "repair", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<RepairSparePart> usedParts = new ArrayList<>();


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

    // public List<RepairSparePart> getUsedParts() {
    //     return usedParts;
    // }
    //
    // public void setUsedParts(List<RepairSparePart> usedParts) {
    //     this.usedParts = usedParts;
    // }
}