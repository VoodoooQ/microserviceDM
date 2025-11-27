package com.guaumiau.repository;

import com.guaumiau.model.PetEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración para PetRepository
 * 
 * Estrategia de testing:
 * - Uso de @DataJpaTest para cargar solo el contexto JPA (rápido y enfocado)
 * - Base de datos H2 en memoria (NO afecta Supabase/PostgreSQL de producción)
 * - TestEntityManager para gestionar el estado de las entidades en las pruebas
 * - Verificación de operaciones CRUD y consultas personalizadas
 * 
 * IMPORTANTE: Estas pruebas usan H2 en memoria, completamente aisladas de la
 * base de datos de producción (Supabase). La configuración de Railway y Supabase
 * permanece intacta y no se ve afectada por estas pruebas.
 */
@DataJpaTest
@DisplayName("Pruebas de integración del repositorio PetRepository")
class PetRepositoryTest {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TestEntityManager entityManager;

    private PetEntity testPet1;
    private PetEntity testPet2;
    private PetEntity testPet3;

    /**
     * Configuración inicial antes de cada prueba
     * Prepara datos de prueba en la base de datos H2 en memoria
     */
    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada prueba
        petRepository.deleteAll();

        // Crear mascotas de prueba con diferentes usuarios
        testPet1 = new PetEntity("Firulais", "Perro", "usuario1@example.com");
        testPet2 = new PetEntity("Michi", "Gato", "usuario1@example.com");
        testPet3 = new PetEntity("Rex", "Perro", "usuario2@example.com");
    }

    // ========== PRUEBAS DE OPERACIÓN SAVE (Guardar) ==========

    /**
     * Caso exitoso: Guardar una nueva mascota
     * Verifica que la entidad se persista correctamente con ID autogenerado
     */
    @Test
    @DisplayName("save() - Debe guardar una mascota nueva con ID autogenerado")
    void save_NewPet_ShouldPersistWithGeneratedId() {
        // When: Guardar la mascota
        PetEntity savedPet = petRepository.save(testPet1);

        // Then: Verificar que se guardó correctamente
        assertThat(savedPet.getId()).isNotNull(); // ID fue autogenerado
        assertThat(savedPet.getName()).isEqualTo("Firulais");
        assertThat(savedPet.getType()).isEqualTo("Perro");
        assertThat(savedPet.getUserEmail()).isEqualTo("usuario1@example.com");

        // Verificar que está en la base de datos
        PetEntity foundPet = entityManager.find(PetEntity.class, savedPet.getId());
        assertThat(foundPet).isNotNull();
        assertThat(foundPet.getName()).isEqualTo("Firulais");
    }

    /**
     * Caso exitoso: Actualizar una mascota existente
     * Verifica que la operación save actualice entidades existentes
     */
    @Test
    @DisplayName("save() - Debe actualizar una mascota existente")
    void save_ExistingPet_ShouldUpdate() {
        // Given: Guardar mascota inicial
        PetEntity savedPet = entityManager.persistAndFlush(testPet1);
        Integer petId = savedPet.getId();

        // When: Actualizar el nombre
        savedPet.setName("Firulais Actualizado");
        PetEntity updatedPet = petRepository.saveAndFlush(savedPet);
        entityManager.clear(); // Limpiar caché para forzar lectura desde BD

        // Then: Verificar que se actualizó
        assertThat(updatedPet.getId()).isEqualTo(petId); // Mismo ID
        assertThat(updatedPet.getName()).isEqualTo("Firulais Actualizado");

        // Verificar en la base de datos
        PetEntity foundPet = entityManager.find(PetEntity.class, petId);
        assertThat(foundPet.getName()).isEqualTo("Firulais Actualizado");
    }

    // ========== PRUEBAS DE OPERACIÓN FIND (Buscar) ==========

    /**
     * Caso exitoso: Buscar mascota por ID existente
     * Verifica que se recupere la entidad correcta
     */
    @Test
    @DisplayName("findById() - Debe encontrar mascota por ID existente")
    void findById_WithExistingId_ShouldReturnPet() {
        // Given: Guardar mascota
        PetEntity savedPet = entityManager.persistAndFlush(testPet1);
        Integer petId = savedPet.getId();

        // When: Buscar por ID
        Optional<PetEntity> foundPet = petRepository.findById(petId);

        // Then: Verificar que se encontró
        assertThat(foundPet).isPresent();
        assertThat(foundPet.get().getName()).isEqualTo("Firulais");
        assertThat(foundPet.get().getType()).isEqualTo("Perro");
    }

    /**
     * Caso extremo: Buscar mascota con ID inexistente
     * Verifica que retorne Optional vacío (no lanza excepción)
     */
    @Test
    @DisplayName("findById() - Debe retornar Optional vacío con ID inexistente")
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When: Buscar ID que no existe
        Optional<PetEntity> foundPet = petRepository.findById(999);

        // Then: Verificar que está vacío
        assertThat(foundPet).isEmpty();
    }

    // ========== PRUEBAS DE CONSULTA PERSONALIZADA findByUserEmail ==========

    /**
     * Caso exitoso: Buscar mascotas por email de usuario
     * Verifica que se retornen todas las mascotas del usuario
     */
    @Test
    @DisplayName("findByUserEmail() - Debe retornar todas las mascotas de un usuario")
    void findByUserEmail_WithExistingUser_ShouldReturnAllUserPets() {
        // Given: Guardar dos mascotas para usuario1 y una para usuario2
        entityManager.persistAndFlush(testPet1);
        entityManager.persistAndFlush(testPet2);
        entityManager.persistAndFlush(testPet3);

        // When: Buscar mascotas de usuario1
        List<PetEntity> usuario1Pets = petRepository.findByUserEmail("usuario1@example.com");

        // Then: Verificar que se encontraron 2 mascotas
        assertThat(usuario1Pets).hasSize(2);
        assertThat(usuario1Pets).extracting(PetEntity::getName)
                .containsExactlyInAnyOrder("Firulais", "Michi");
        assertThat(usuario1Pets).allMatch(pet -> 
                pet.getUserEmail().equals("usuario1@example.com"));
    }

    /**
     * Caso extremo: Buscar mascotas de usuario sin mascotas registradas
     * Verifica que retorne lista vacía (no error ni null)
     */
    @Test
    @DisplayName("findByUserEmail() - Debe retornar lista vacía si usuario no tiene mascotas")
    void findByUserEmail_WithUserWithoutPets_ShouldReturnEmptyList() {
        // Given: Base de datos vacía o usuario sin mascotas
        
        // When: Buscar mascotas de usuario sin registros
        List<PetEntity> pets = petRepository.findByUserEmail("usuario_sin_mascotas@example.com");

        // Then: Verificar que la lista está vacía
        assertThat(pets).isEmpty();
        assertThat(pets).isNotNull(); // No es null, es lista vacía
    }

    /**
     * Caso límite: Verificar que diferentes usuarios tengan mascotas separadas
     * Prueba de aislamiento de datos entre usuarios
     */
    @Test
    @DisplayName("findByUserEmail() - Debe mantener mascotas separadas por usuario")
    void findByUserEmail_DifferentUsers_ShouldReturnSeparatePets() {
        // Given: Guardar mascotas para diferentes usuarios
        entityManager.persistAndFlush(testPet1); // usuario1
        entityManager.persistAndFlush(testPet2); // usuario1
        entityManager.persistAndFlush(testPet3); // usuario2

        // When: Buscar mascotas de cada usuario
        List<PetEntity> usuario1Pets = petRepository.findByUserEmail("usuario1@example.com");
        List<PetEntity> usuario2Pets = petRepository.findByUserEmail("usuario2@example.com");

        // Then: Verificar aislamiento de datos
        assertThat(usuario1Pets).hasSize(2);
        assertThat(usuario2Pets).hasSize(1);
        assertThat(usuario2Pets.get(0).getName()).isEqualTo("Rex");
    }

    // ========== PRUEBAS DE OPERACIÓN DELETE (Eliminar) ==========

    /**
     * Caso exitoso: Eliminar mascota por ID
     * Verifica que la entidad se elimine correctamente
     */
    @Test
    @DisplayName("deleteById() - Debe eliminar mascota correctamente")
    void deleteById_WithExistingId_ShouldRemovePet() {
        // Given: Guardar mascota
        PetEntity savedPet = entityManager.persistAndFlush(testPet1);
        Integer petId = savedPet.getId();

        // When: Eliminar la mascota
        petRepository.deleteById(petId);
        petRepository.flush(); // Forzar ejecución del DELETE
        entityManager.clear(); // Limpiar caché

        // Then: Verificar que ya no existe
        PetEntity foundPet = entityManager.find(PetEntity.class, petId);
        assertThat(foundPet).isNull();
    }

    /**
     * Caso extremo: Verificar que eliminar una mascota no afecte otras
     * Prueba de integridad de datos
     */
    @Test
    @DisplayName("deleteById() - Debe eliminar solo la mascota especificada")
    void deleteById_ShouldNotAffectOtherPets() {
        // Given: Guardar múltiples mascotas
        PetEntity saved1 = entityManager.persistAndFlush(testPet1);
        PetEntity saved2 = entityManager.persistAndFlush(testPet2);
        Integer id1 = saved1.getId();
        Integer id2 = saved2.getId();

        // When: Eliminar solo la primera
        petRepository.deleteById(id1);
        petRepository.flush(); // Forzar ejecución del DELETE
        entityManager.clear();

        // Then: Verificar que la segunda sigue existiendo
        assertThat(entityManager.find(PetEntity.class, id1)).isNull();
        assertThat(entityManager.find(PetEntity.class, id2)).isNotNull();
    }

    /**
     * Caso exitoso: Verificar existencia de mascota
     * Prueba del método existsById
     */
    @Test
    @DisplayName("existsById() - Debe verificar correctamente la existencia de mascota")
    void existsById_ShouldReturnCorrectResult() {
        // Given: Guardar mascota
        PetEntity savedPet = entityManager.persistAndFlush(testPet1);
        Integer petId = savedPet.getId();

        // When & Then: Verificar existencia
        assertThat(petRepository.existsById(petId)).isTrue();
        assertThat(petRepository.existsById(999)).isFalse();
    }

    // ========== PRUEBAS DE OPERACIONES MASIVAS ==========

    /**
     * Caso de rendimiento: Guardar múltiples mascotas
     * Verifica que el repositorio maneje lotes de datos correctamente
     */
    @Test
    @DisplayName("saveAll() - Debe guardar múltiples mascotas en lote")
    void saveAll_MultiplePets_ShouldPersistAll() {
        // Given: Lista de mascotas
        List<PetEntity> pets = List.of(testPet1, testPet2, testPet3);

        // When: Guardar todas
        List<PetEntity> savedPets = petRepository.saveAll(pets);

        // Then: Verificar que todas se guardaron
        assertThat(savedPets).hasSize(3);
        assertThat(savedPets).allMatch(pet -> pet.getId() != null);

        // Verificar en la base de datos
        long count = petRepository.count();
        assertThat(count).isEqualTo(3);
    }

    /**
     * Caso exitoso: Obtener todas las mascotas
     * Verifica la operación findAll
     */
    @Test
    @DisplayName("findAll() - Debe retornar todas las mascotas de la base de datos")
    void findAll_ShouldReturnAllPets() {
        // Given: Guardar múltiples mascotas
        entityManager.persistAndFlush(testPet1);
        entityManager.persistAndFlush(testPet2);
        entityManager.persistAndFlush(testPet3);

        // When: Obtener todas
        List<PetEntity> allPets = petRepository.findAll();

        // Then: Verificar que se encontraron todas
        assertThat(allPets).hasSize(3);
        assertThat(allPets).extracting(PetEntity::getName)
                .containsExactlyInAnyOrder("Firulais", "Michi", "Rex");
    }

    /**
     * Caso extremo: Contar mascotas en base de datos vacía
     * Verifica que count() retorne 0 correctamente
     */
    @Test
    @DisplayName("count() - Debe retornar 0 en base de datos vacía")
    void count_EmptyDatabase_ShouldReturnZero() {
        // Given: Base de datos vacía (setUp ya limpió)
        
        // When: Contar mascotas
        long count = petRepository.count();

        // Then: Verificar que es 0
        assertThat(count).isZero();
    }

    // ========== PRUEBAS DE CASOS LÍMITE Y VALIDACIÓN DE DATOS ==========

    /**
     * Caso límite: Guardar mascota con nombre muy largo
     * Verifica el manejo de strings largos
     */
    @Test
    @DisplayName("save() - Debe manejar nombres largos correctamente")
    void save_WithLongName_ShouldHandleCorrectly() {
        // Given: Mascota con nombre largo (200 caracteres)
        String longName = "A".repeat(200);
        PetEntity petWithLongName = new PetEntity(longName, "Perro", "usuario@example.com");

        // When: Guardar
        PetEntity savedPet = petRepository.save(petWithLongName);

        // Then: Verificar que se guardó correctamente
        assertThat(savedPet.getId()).isNotNull();
        assertThat(savedPet.getName()).hasSize(200);
        assertThat(savedPet.getName()).isEqualTo(longName);
    }

    /**
     * Caso límite: Múltiples mascotas con el mismo nombre para diferentes usuarios
     * Verifica que no hay restricción de nombre único
     */
    @Test
    @DisplayName("save() - Debe permitir mascotas con mismo nombre para diferentes usuarios")
    void save_SameNameDifferentUsers_ShouldSucceed() {
        // Given: Dos mascotas con el mismo nombre pero diferentes usuarios
        PetEntity pet1 = new PetEntity("Max", "Perro", "usuario1@example.com");
        PetEntity pet2 = new PetEntity("Max", "Gato", "usuario2@example.com");

        // When: Guardar ambas
        PetEntity saved1 = petRepository.save(pet1);
        PetEntity saved2 = petRepository.save(pet2);

        // Then: Verificar que ambas se guardaron
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
        assertThat(saved1.getName()).isEqualTo(saved2.getName());
        assertThat(petRepository.count()).isEqualTo(2);
    }

    /**
     * Caso límite: Email con formato especial
     * Verifica que se acepten diferentes formatos de email
     */
    @Test
    @DisplayName("findByUserEmail() - Debe aceptar emails con formato especial")
    void findByUserEmail_WithSpecialEmailFormat_ShouldWork() {
        // Given: Mascota con email especial
        String specialEmail = "usuario+test123@subdomain.example.co.uk";
        PetEntity petWithSpecialEmail = new PetEntity("Toby", "Perro", specialEmail);
        entityManager.persistAndFlush(petWithSpecialEmail);

        // When: Buscar por ese email
        List<PetEntity> pets = petRepository.findByUserEmail(specialEmail);

        // Then: Verificar que se encontró
        assertThat(pets).hasSize(1);
        assertThat(pets.get(0).getUserEmail()).isEqualTo(specialEmail);
    }
}
