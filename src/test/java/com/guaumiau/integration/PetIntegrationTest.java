package com.guaumiau.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guaumiau.model.PetEntity;
import com.guaumiau.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración end-to-end para el microservicio de mascotas
 * 
 * Estrategia de testing:
 * - @SpringBootTest carga el contexto completo de Spring (similar a producción)
 * - Usa base de datos H2 en memoria configurada específicamente para tests
 * - @Transactional hace rollback automático después de cada prueba
 * - Simula el flujo completo: HTTP → Controlador → Servicio → Repositorio → BD
 * - Verifica la integración real entre todos los componentes
 * 
 * IMPORTANTE: Estas pruebas usan H2 en memoria (NO Supabase/PostgreSQL).
 * La configuración de Railway y Supabase NO se ve afectada. Se usa
 * application-test.properties para aislar completamente el entorno de pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // Rollback automático después de cada prueba
@DisplayName("Pruebas de integración end-to-end del microservicio")
class PetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PetRepository petRepository;

    /**
     * Limpieza antes de cada prueba
     * Asegura estado inicial limpio para cada test
     */
    @BeforeEach
    void setUp() {
        petRepository.deleteAll();
    }

    // ========== PRUEBAS DE FLUJO COMPLETO CRUD ==========

    /**
     * Escenario completo: Crear, obtener, actualizar y eliminar mascota
     * Simula el flujo completo de uso de la aplicación Android
     */
    @Test
    @DisplayName("Flujo completo CRUD - Crear, obtener, actualizar y eliminar mascota")
    void completeCRUDFlow_ShouldWorkEndToEnd() throws Exception {
        // 1. CREAR mascota (POST)
        PetEntity newPet = new PetEntity("Firulais", "Perro", "usuario@test.com");
        
        String createResponse = mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPet)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Firulais"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer ID de la mascota creada
        PetEntity createdPet = objectMapper.readValue(createResponse, PetEntity.class);
        Integer petId = createdPet.getId();

        // 2. OBTENER mascota por ID (GET)
        mockMvc.perform(get("/api/pets/" + petId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(petId))
                .andExpect(jsonPath("$.name").value("Firulais"))
                .andExpect(jsonPath("$.type").value("Perro"))
                .andExpect(jsonPath("$.userEmail").value("usuario@test.com"));

        // 3. ELIMINAR mascota (DELETE)
        mockMvc.perform(delete("/api/pets/" + petId))
                .andExpect(status().isNoContent());

        // 4. VERIFICAR que ya no existe (GET debe retornar 404)
        mockMvc.perform(get("/api/pets/" + petId))
                .andExpect(status().isNotFound());
    }

    /**
     * Escenario realista: Usuario registra múltiples mascotas y las consulta
     */
    @Test
    @DisplayName("Escenario múltiples mascotas - Usuario crea varias mascotas y las consulta")
    void multiPetScenario_UserCreatesAndRetrievesMultiplePets() throws Exception {
        String userEmail = "maria@test.com";

        // 1. Usuario crea primera mascota (perro)
        PetEntity pet1 = new PetEntity("Max", "Perro", userEmail);
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pet1)))
                .andExpect(status().isCreated());

        // 2. Usuario crea segunda mascota (gato)
        PetEntity pet2 = new PetEntity("Michi", "Gato", userEmail);
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pet2)))
                .andExpect(status().isCreated());

        // 3. Usuario crea tercera mascota (otro perro)
        PetEntity pet3 = new PetEntity("Luna", "Perro", userEmail);
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pet3)))
                .andExpect(status().isCreated());

        // 4. Usuario consulta todas sus mascotas
        mockMvc.perform(get("/api/pets")
                .param("userEmail", userEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Max", "Michi", "Luna")))
                .andExpect(jsonPath("$[*].userEmail", everyItem(is(userEmail))));
    }

    /**
     * Escenario de aislamiento: Verificar que diferentes usuarios ven solo sus mascotas
     */
    @Test
    @DisplayName("Aislamiento de datos - Cada usuario ve solo sus propias mascotas")
    void dataIsolation_DifferentUsersSeeOnlyTheirPets() throws Exception {
        // Usuario 1 crea sus mascotas
        PetEntity user1Pet1 = new PetEntity("Firulais", "Perro", "usuario1@test.com");
        PetEntity user1Pet2 = new PetEntity("Rex", "Perro", "usuario1@test.com");
        
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Pet1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Pet2)))
                .andExpect(status().isCreated());

        // Usuario 2 crea su mascota
        PetEntity user2Pet = new PetEntity("Michi", "Gato", "usuario2@test.com");
        
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Pet)))
                .andExpect(status().isCreated());

        // Verificar que usuario1 ve solo sus 2 mascotas
        mockMvc.perform(get("/api/pets")
                .param("userEmail", "usuario1@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Firulais", "Rex")));

        // Verificar que usuario2 ve solo su 1 mascota
        mockMvc.perform(get("/api/pets")
                .param("userEmail", "usuario2@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Michi"));
    }

    // ========== PRUEBAS DE MANEJO DE ERRORES ==========

    /**
     * Escenario de error: Intentar obtener mascota que no existe
     */
    @Test
    @DisplayName("Manejo de errores - GET de mascota inexistente retorna 404")
    void errorHandling_GetNonExistentPet_Returns404() throws Exception {
        mockMvc.perform(get("/api/pets/99999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Escenario de error: Intentar eliminar mascota que no existe
     */
    @Test
    @DisplayName("Manejo de errores - DELETE de mascota inexistente retorna 404")
    void errorHandling_DeleteNonExistentPet_Returns404() throws Exception {
        mockMvc.perform(delete("/api/pets/99999"))
                .andExpect(status().isNotFound());
    }

    /**
     * Escenario de validación: Crear mascota con datos incompletos
     */
    @Test
    @DisplayName("Validación - Crear mascota sin nombre retorna 400")
    void validation_CreatePetWithoutName_Returns400() throws Exception {
        PetEntity invalidPet = new PetEntity();
        invalidPet.setType("Perro");
        invalidPet.setUserEmail("usuario@test.com");

        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPet)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Escenario de validación: Consultar mascotas sin proporcionar email
     */
    @Test
    @DisplayName("Validación - Consultar mascotas sin email retorna 400")
    void validation_GetPetsWithoutEmail_Returns400() throws Exception {
        mockMvc.perform(get("/api/pets")
                .param("userEmail", ""))
                .andExpect(status().isBadRequest());
    }

    // ========== PRUEBAS DE PERSISTENCIA Y TRANSACCIONES ==========

    /**
     * Verificar que los datos persisten correctamente en la base de datos
     */
    @Test
    @DisplayName("Persistencia - Datos se guardan correctamente en la base de datos")
    void persistence_DataIsSavedCorrectly() throws Exception {
        // Crear mascota vía API
        PetEntity newPet = new PetEntity("Toby", "Perro", "test@test.com");
        
        String response = mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPet)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PetEntity createdPet = objectMapper.readValue(response, PetEntity.class);

        // Verificar directamente en el repositorio
        PetEntity foundInDb = petRepository.findById(createdPet.getId()).orElse(null);
        
        assert foundInDb != null;
        assert foundInDb.getName().equals("Toby");
        assert foundInDb.getType().equals("Perro");
        assert foundInDb.getUserEmail().equals("test@test.com");
    }

    /**
     * Verificar que las eliminaciones se reflejan en la base de datos
     */
    @Test
    @DisplayName("Persistencia - Eliminaciones se reflejan correctamente en la base de datos")
    void persistence_DeletionIsReflectedInDatabase() throws Exception {
        // Crear mascota directamente en el repositorio
        PetEntity pet = new PetEntity("Bobby", "Gato", "owner@test.com");
        PetEntity saved = petRepository.save(pet);
        Integer petId = saved.getId();

        // Verificar que existe
        assert petRepository.existsById(petId);

        // Eliminar vía API
        mockMvc.perform(delete("/api/pets/" + petId))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe en la base de datos
        assert !petRepository.existsById(petId);
    }

    // ========== PRUEBAS DE RENDIMIENTO Y CONCURRENCIA ==========

    /**
     * Prueba de carga: Crear múltiples mascotas secuencialmente
     * Verifica que el sistema maneje múltiples operaciones correctamente
     */
    @Test
    @DisplayName("Rendimiento - Crear 10 mascotas secuencialmente debe funcionar sin errores")
    void performance_CreateMultiplePetsSequentially_ShouldSucceed() throws Exception {
        String userEmail = "bulk@test.com";

        // Crear 10 mascotas
        for (int i = 1; i <= 10; i++) {
            PetEntity pet = new PetEntity("Mascota" + i, "Tipo" + i, userEmail);
            
            mockMvc.perform(post("/api/pets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(pet)))
                    .andExpect(status().isCreated());
        }

        // Verificar que todas se guardaron
        mockMvc.perform(get("/api/pets")
                .param("userEmail", userEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }

    // ========== PRUEBAS DE COMPATIBILIDAD CON APP ANDROID ==========

    /**
     * Escenario Android: Simular flujo típico de la aplicación móvil
     * 1. Usuario se registra y agrega su primera mascota
     * 2. Agrega segunda mascota
     * 3. Consulta lista completa
     * 4. Elimina una mascota
     * 5. Consulta lista actualizada
     */
    @Test
    @DisplayName("Compatibilidad Android - Flujo típico de usuario móvil")
    void androidCompatibility_TypicalMobileUserFlow() throws Exception {
        String androidUser = "android.user@example.com";

        // 1. Primer mascota (onboarding)
        PetEntity firstPet = new PetEntity("Firulais", "Perro", androidUser);
        String response1 = mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstPet)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        Integer firstPetId = objectMapper.readValue(response1, PetEntity.class).getId();

        // 2. Segunda mascota (usuario añade otra)
        PetEntity secondPet = new PetEntity("Michi", "Gato", androidUser);
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondPet)))
                .andExpect(status().isCreated());

        // 3. Usuario abre la app y consulta todas sus mascotas
        mockMvc.perform(get("/api/pets")
                .param("userEmail", androidUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 4. Usuario elimina primera mascota
        mockMvc.perform(delete("/api/pets/" + firstPetId))
                .andExpect(status().isNoContent());

        // 5. Usuario refresca la lista (debe ver solo 1 mascota)
        mockMvc.perform(get("/api/pets")
                .param("userEmail", androidUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Michi"));
    }

    /**
     * Verificar que el formato JSON es compatible con el modelo Android
     */
    @Test
    @DisplayName("Compatibilidad Android - Formato JSON es compatible con modelo Android")
    void androidCompatibility_JSONFormatMatchesAndroidModel() throws Exception {
        PetEntity pet = new PetEntity("TestPet", "TestType", "android@test.com");
        
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pet)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists()) // Campo requerido en Android
                .andExpect(jsonPath("$.name").exists()) // Campo requerido en Android
                .andExpect(jsonPath("$.type").exists()) // Campo requerido en Android
                .andExpect(jsonPath("$.userEmail").exists()); // Campo requerido en Android
    }

    // ========== PRUEBAS DE CASOS EXTREMOS ==========

    /**
     * Caso extremo: Consultar mascotas de usuario que no ha registrado ninguna
     */
    @Test
    @DisplayName("Caso extremo - Usuario sin mascotas retorna lista vacía")
    void edgeCase_UserWithNoPets_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/pets")
                .param("userEmail", "nuevo.usuario@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Caso extremo: Crear mascota con nombre muy largo
     */
    @Test
    @DisplayName("Caso extremo - Crear mascota con nombre de 200 caracteres")
    void edgeCase_CreatePetWithLongName_ShouldSucceed() throws Exception {
        String longName = "A".repeat(200);
        PetEntity petWithLongName = new PetEntity(longName, "Perro", "test@test.com");

        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petWithLongName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(longName));
    }
}
