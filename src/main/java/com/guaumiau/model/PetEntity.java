package com.guaumiau.model;

import jakarta.persistence.*;

/**
 * Entidad PetEntity - compatible con modelo Android
 * Campos: id, name, type, userEmail
 */
@Entity
@Table(name = "pets")
public class PetEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String type;
    
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    
    // Constructor vacío requerido por JPA
    public PetEntity() {
    }
    
    // Constructor con parámetros
    public PetEntity(String name, String type, String userEmail) {
        this.name = name;
        this.type = type;
        this.userEmail = userEmail;
    }
    
    // Getters y Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
