package com.guaumiau.controller;

import com.guaumiau.model.PetEntity;
import com.guaumiau.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para PetEntity
 * Endpoints minimalistas compatibles con app Android
 */
@RestController
@RequestMapping("/api/pets")
public class PetController {
    
    private final PetRepository petRepository;
    
    public PetController(PetRepository petRepository) {
        this.petRepository = petRepository;
    }
    
    /**
     * POST /api/pets - Crear nueva mascota
     */
    @PostMapping
    public ResponseEntity<PetEntity> createPet(@RequestBody PetEntity pet) {
        if (pet.getName() == null || pet.getType() == null || pet.getUserEmail() == null) {
            return ResponseEntity.badRequest().build();
        }
        PetEntity savedPet = petRepository.save(pet);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPet);
    }
    
    /**
     * GET /api/pets?userEmail={email} - Obtener mascotas por email de usuario
     */
    @GetMapping
    public ResponseEntity<List<PetEntity>> getPetsByUserEmail(@RequestParam String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<PetEntity> pets = petRepository.findByUserEmail(userEmail);
        return ResponseEntity.ok(pets);
    }
    
    /**
     * GET /api/pets/{id} - Obtener mascota por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PetEntity> getPetById(@PathVariable Integer id) {
        return petRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * DELETE /api/pets/{id} - Eliminar mascota por ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Integer id) {
        if (!petRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        petRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
