package com.guaumiau.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guaumiau.model.PetEntity;
import com.guaumiau.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para PetController
 * 
 * Estrategia de testing:
 * - Uso de @WebMvcTest para cargar solo el contexto del controlador (pruebas rápidas)
 * - MockBean para simular el repositorio sin conectar a base de datos real
 * - Cobertura de casos exitosos, validaciones y manejo de errores
 * - Verificación de códigos HTTP, estructura JSON y comportamiento del repositorio
 * 
 * IMPORTANTE: Estas pruebas NO afectan la base de datos real (Supabase) ni ninguna 
 * configuración de producción. El repositorio es completamente simulado (mock).
 */
@WebMvcTest(PetController.class)
@DisplayName("Pruebas unitarias del controlador PetController")
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PetRepository petRepository;

    private PetEntity testPet;
    private List<PetEntity> testPets;

    /**
     * Configuración inicial antes de cada prueba
     * Se ejecuta automáticamente antes de cada método @Test
     */
    @BeforeEach
    void setUp() {
        // Crear una mascota de prueba con datos válidos
        testPet = new PetEntity();
        testPet.setId(1);
        testPet.setName("Firulais");
        testPet.setType("Perro");
        testPet.setUserEmail("usuario@example.com");

        // Crear lista de mascotas para pruebas de consulta múltiple
        PetEntity pet2 = new PetEntity();
        pet2.setId(2);
        pet2.setName("Michi");
        pet2.setType("Gato");
        pet2.setUserEmail("usuario@example.com");

        testPets = Arrays.asList(testPet, pet2);
    }

    // ========== PRUEBAS POST /api/pets (Crear mascota) ==========

    /**
     * Caso exitoso: Crear una mascota con todos los datos válidos
     * Verifica que se retorne HTTP 201 CREATED y la mascota guardada
     */
    @Test
    @DisplayName("POST /api/pets - Debe crear una mascota exitosamente con datos válidos")
    void createPet_WithValidData_ShouldReturnCreated() throws Exception {
        // Given: Configurar el mock para retornar la mascota guardada
        when(petRepository.save(any(PetEntity.class))).thenReturn(testPet);

        // When & Then: Ejecutar la petición POST y verificar la respuesta
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPet)))
                .andExpect(status().isCreated()) // Verifica código HTTP 201
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Firulais"))
                .andExpect(jsonPath("$.type").value("Perro"))
                .andExpect(jsonPath("$.userEmail").value("usuario@example.com"));

        // Verificar que se llamó al repositorio una vez
        verify(petRepository, times(1)).save(any(PetEntity.class));
    }

    /**
     * Caso extremo: Intentar crear mascota sin nombre (campo requerido)
     * Verifica que se retorne HTTP 400 BAD REQUEST
     */
    @Test
    @DisplayName("POST /api/pets - Debe retornar BAD REQUEST cuando falta el nombre")
    void createPet_WithoutName_ShouldReturnBadRequest() throws Exception {
        // Given: Crear mascota sin nombre
        PetEntity invalidPet = new PetEntity();
        invalidPet.setType("Perro");
        invalidPet.setUserEmail("usuario@example.com");

        // When & Then: Verificar que retorna 400
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPet)))
                .andExpect(status().isBadRequest());

        // Verificar que NO se llamó al repositorio
        verify(petRepository, never()).save(any(PetEntity.class));
    }

    /**
     * Caso extremo: Intentar crear mascota sin tipo (campo requerido)
     * Verifica validación de campos obligatorios
     */
    @Test
    @DisplayName("POST /api/pets - Debe retornar BAD REQUEST cuando falta el tipo")
    void createPet_WithoutType_ShouldReturnBadRequest() throws Exception {
        // Given: Crear mascota sin tipo
        PetEntity invalidPet = new PetEntity();
        invalidPet.setName("Firulais");
        invalidPet.setUserEmail("usuario@example.com");

        // When & Then
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPet)))
                .andExpect(status().isBadRequest());

        verify(petRepository, never()).save(any(PetEntity.class));
    }

    /**
     * Caso extremo: Intentar crear mascota sin email de usuario (campo requerido)
     * Verifica que se valide la asociación con el usuario
     */
    @Test
    @DisplayName("POST /api/pets - Debe retornar BAD REQUEST cuando falta userEmail")
    void createPet_WithoutUserEmail_ShouldReturnBadRequest() throws Exception {
        // Given: Crear mascota sin userEmail
        PetEntity invalidPet = new PetEntity();
        invalidPet.setName("Firulais");
        invalidPet.setType("Perro");

        // When & Then
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPet)))
                .andExpect(status().isBadRequest());

        verify(petRepository, never()).save(any(PetEntity.class));
    }

    // ========== PRUEBAS GET /api/pets?userEmail={email} (Obtener mascotas por email) ==========

    /**
     * Caso exitoso: Obtener mascotas de un usuario existente
     * Verifica que se retorne la lista completa de mascotas
     */
    @Test
    @DisplayName("GET /api/pets?userEmail={email} - Debe retornar lista de mascotas del usuario")
    void getPetsByUserEmail_WithValidEmail_ShouldReturnPetsList() throws Exception {
        // Given: Configurar mock para retornar lista de mascotas
        when(petRepository.findByUserEmail(anyString())).thenReturn(testPets);

        // When & Then
        mockMvc.perform(get("/api/pets")
                .param("userEmail", "usuario@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Verifica que la lista tiene 2 elementos
                .andExpect(jsonPath("$[0].name").value("Firulais"))
                .andExpect(jsonPath("$[1].name").value("Michi"));

        verify(petRepository, times(1)).findByUserEmail("usuario@example.com");
    }

    /**
     * Caso extremo: Obtener mascotas con email vacío
     * Verifica que se rechace la petición con parámetro inválido
     */
    @Test
    @DisplayName("GET /api/pets?userEmail= - Debe retornar BAD REQUEST con email vacío")
    void getPetsByUserEmail_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/pets")
                .param("userEmail", ""))
                .andExpect(status().isBadRequest());

        verify(petRepository, never()).findByUserEmail(anyString());
    }

    /**
     * Caso extremo: Usuario sin mascotas registradas
     * Verifica que se retorne una lista vacía (no error)
     */
    @Test
    @DisplayName("GET /api/pets?userEmail={email} - Debe retornar lista vacía si no hay mascotas")
    void getPetsByUserEmail_WithNoResults_ShouldReturnEmptyList() throws Exception {
        // Given: Usuario sin mascotas
        when(petRepository.findByUserEmail(anyString())).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/pets")
                .param("userEmail", "nuevo@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Lista vacía

        verify(petRepository, times(1)).findByUserEmail("nuevo@example.com");
    }

    // ========== PRUEBAS GET /api/pets/{id} (Obtener mascota por ID) ==========

    /**
     * Caso exitoso: Obtener mascota por ID existente
     * Verifica que se retorne la mascota correcta
     */
    @Test
    @DisplayName("GET /api/pets/{id} - Debe retornar mascota existente por ID")
    void getPetById_WithExistingId_ShouldReturnPet() throws Exception {
        // Given
        when(petRepository.findById(1)).thenReturn(Optional.of(testPet));

        // When & Then
        mockMvc.perform(get("/api/pets/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Firulais"))
                .andExpect(jsonPath("$.type").value("Perro"));

        verify(petRepository, times(1)).findById(1);
    }

    /**
     * Caso extremo: Intentar obtener mascota con ID inexistente
     * Verifica que se retorne HTTP 404 NOT FOUND
     */
    @Test
    @DisplayName("GET /api/pets/{id} - Debe retornar NOT FOUND con ID inexistente")
    void getPetById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given: ID que no existe en la base de datos
        when(petRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/pets/999"))
                .andExpect(status().isNotFound());

        verify(petRepository, times(1)).findById(999);
    }

    // ========== PRUEBAS DELETE /api/pets/{id} (Eliminar mascota) ==========

    /**
     * Caso exitoso: Eliminar mascota existente
     * Verifica que se retorne HTTP 204 NO CONTENT
     */
    @Test
    @DisplayName("DELETE /api/pets/{id} - Debe eliminar mascota existente exitosamente")
    void deletePet_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Given: Mascota existe en la base de datos
        when(petRepository.existsById(1)).thenReturn(true);
        doNothing().when(petRepository).deleteById(1);

        // When & Then
        mockMvc.perform(delete("/api/pets/1"))
                .andExpect(status().isNoContent()); // Código 204

        // Verificar que se llamó a existsById y deleteById
        verify(petRepository, times(1)).existsById(1);
        verify(petRepository, times(1)).deleteById(1);
    }

    /**
     * Caso extremo: Intentar eliminar mascota que no existe
     * Verifica que se retorne HTTP 404 NOT FOUND
     */
    @Test
    @DisplayName("DELETE /api/pets/{id} - Debe retornar NOT FOUND al intentar eliminar ID inexistente")
    void deletePet_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given: ID no existe
        when(petRepository.existsById(999)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/pets/999"))
                .andExpect(status().isNotFound());

        // Verificar que NO se llamó a deleteById
        verify(petRepository, times(1)).existsById(999);
        verify(petRepository, never()).deleteById(999);
    }

    // ========== PRUEBAS DE RENDIMIENTO Y CASOS LÍMITE ==========

    /**
     * Caso límite: Crear mascota con nombres muy largos
     * Verifica que el sistema maneje strings largos correctamente
     */
    @Test
    @DisplayName("POST /api/pets - Debe manejar nombres muy largos correctamente")
    void createPet_WithLongName_ShouldHandleCorrectly() throws Exception {
        // Given: Nombre de 200 caracteres
        String longName = "A".repeat(200);
        PetEntity petWithLongName = new PetEntity();
        petWithLongName.setName(longName);
        petWithLongName.setType("Perro");
        petWithLongName.setUserEmail("usuario@example.com");
        
        PetEntity savedPet = new PetEntity();
        savedPet.setId(1);
        savedPet.setName(longName);
        savedPet.setType("Perro");
        savedPet.setUserEmail("usuario@example.com");

        when(petRepository.save(any(PetEntity.class))).thenReturn(savedPet);

        // When & Then
        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(petWithLongName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(longName));

        verify(petRepository, times(1)).save(any(PetEntity.class));
    }

    /**
     * Caso límite: Crear múltiples mascotas para el mismo usuario
     * Verifica que el sistema permita múltiples mascotas por usuario
     */
    @Test
    @DisplayName("POST /api/pets - Debe permitir crear múltiples mascotas para el mismo usuario")
    void createPet_MultiplePetsForSameUser_ShouldSucceed() throws Exception {
        // Given: Primera mascota
        when(petRepository.save(any(PetEntity.class))).thenReturn(testPet);

        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPet)))
                .andExpect(status().isCreated());

        // Segunda mascota para el mismo usuario
        PetEntity secondPet = new PetEntity();
        secondPet.setId(2);
        secondPet.setName("Michi");
        secondPet.setType("Gato");
        secondPet.setUserEmail("usuario@example.com");

        when(petRepository.save(any(PetEntity.class))).thenReturn(secondPet);

        mockMvc.perform(post("/api/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondPet)))
                .andExpect(status().isCreated());

        // Verificar que se llamó save dos veces
        verify(petRepository, times(2)).save(any(PetEntity.class));
    }

    /**
     * Caso límite: Email con formato especial pero válido
     * Verifica que se acepten emails con diferentes formatos válidos
     */
    @Test
    @DisplayName("GET /api/pets?userEmail={email} - Debe aceptar emails con formato especial")
    void getPetsByUserEmail_WithSpecialFormatEmail_ShouldSucceed() throws Exception {
        // Given: Email con formato especial pero válido
        String specialEmail = "usuario+test@example.co.uk";
        when(petRepository.findByUserEmail(specialEmail)).thenReturn(testPets);

        // When & Then
        mockMvc.perform(get("/api/pets")
                .param("userEmail", specialEmail))
                .andExpect(status().isOk());

        verify(petRepository, times(1)).findByUserEmail(specialEmail);
    }
}
