package com.guaumiau.repository;

import com.guaumiau.model.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para PetEntity - operaciones CRUD compatibles con Android
 * Métodos: findByUserEmail, findById, deleteById
 */
@Repository
public interface PetRepository extends JpaRepository<PetEntity, Integer> {
    
    /**
     * Obtener todas las mascotas de un usuario por email
     * Equivalente a getPetsByUserEmail en Android
     */
    List<PetEntity> findByUserEmail(String userEmail);
}
