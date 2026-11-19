package dev.saritacarolina.autogest.model.entity;

import dev.saritacarolina.autogest.model.constant.RoleName;
import jakarta.persistence.*;

//Tabla de roles sincronizada con el enum RoleName (las filas se insertan en data.sql)
@Entity
@Table(name = "app_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 20, nullable = false, unique = true)
    private RoleName name;

    //Roles inmutables: definidos en el enum RoleName, sin setters
    public Role() {}

    public Long getRoleId() {
        return roleId;
    }

    public RoleName getName() {
        return name;
    }
}
