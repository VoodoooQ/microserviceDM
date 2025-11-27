package com.guaumiau.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para la entidad PetEntity
 * 
 * Estrategia de testing:
 * - Validación de constructores (vacío y con parámetros)
 * - Verificación de getters y setters
 * - Pruebas de igualdad y comportamiento de objetos
 * - Casos límite con valores especiales
 * 
 * IMPORTANTE: Estas son pruebas POJO puras que solo verifican la lógica
 * de la entidad, sin tocar base de datos ni configuraciones externas.
 */
@DisplayName("Pruebas unitarias de la entidad PetEntity")
class PetEntityTest {

    private PetEntity pet;

    /**
     * Configuración inicial antes de cada prueba
     */
    @BeforeEach
    void setUp() {
        pet = new PetEntity();
    }

    // ========== PRUEBAS DE CONSTRUCTORES ==========

    /**
     * Caso exitoso: Constructor vacío debe crear objeto con campos null
     * Requerido por JPA para instanciar entidades
     */
    @Test
    @DisplayName("Constructor vacío - Debe crear entidad con campos null")
    void emptyConstructor_ShouldCreateEntityWithNullFields() {
        // When: Crear con constructor vacío
        PetEntity emptyPet = new PetEntity();

        // Then: Verificar que todos los campos son null
        assertThat(emptyPet.getId()).isNull();
        assertThat(emptyPet.getName()).isNull();
        assertThat(emptyPet.getType()).isNull();
        assertThat(emptyPet.getUserEmail()).isNull();
    }

    /**
     * Caso exitoso: Constructor con parámetros debe inicializar campos
     * Constructor de conveniencia para crear instancias rápidamente
     */
    @Test
    @DisplayName("Constructor con parámetros - Debe inicializar todos los campos excepto ID")
    void parameterizedConstructor_ShouldInitializeFields() {
        // When: Crear con constructor parametrizado
        PetEntity petWithParams = new PetEntity("Firulais", "Perro", "usuario@example.com");

        // Then: Verificar que los campos se inicializaron correctamente
        assertThat(petWithParams.getName()).isEqualTo("Firulais");
        assertThat(petWithParams.getType()).isEqualTo("Perro");
        assertThat(petWithParams.getUserEmail()).isEqualTo("usuario@example.com");
        assertThat(petWithParams.getId()).isNull(); // ID se asigna al guardar en BD
    }

    // ========== PRUEBAS DE GETTERS Y SETTERS ==========

    /**
     * Caso exitoso: Setter y getter de ID deben funcionar correctamente
     */
    @Test
    @DisplayName("setId/getId - Debe establecer y obtener el ID correctamente")
    void setGetId_ShouldWorkCorrectly() {
        // When: Establecer ID
        pet.setId(42);

        // Then: Verificar que se obtuvo el ID correcto
        assertThat(pet.getId()).isEqualTo(42);
    }

    /**
     * Caso exitoso: Setter y getter de name deben funcionar correctamente
     */
    @Test
    @DisplayName("setName/getName - Debe establecer y obtener el nombre correctamente")
    void setGetName_ShouldWorkCorrectly() {
        // When: Establecer nombre
        pet.setName("Michi");

        // Then: Verificar que se obtuvo el nombre correcto
        assertThat(pet.getName()).isEqualTo("Michi");
    }

    /**
     * Caso exitoso: Setter y getter de type deben funcionar correctamente
     */
    @Test
    @DisplayName("setType/getType - Debe establecer y obtener el tipo correctamente")
    void setGetType_ShouldWorkCorrectly() {
        // When: Establecer tipo
        pet.setType("Gato");

        // Then: Verificar que se obtuvo el tipo correcto
        assertThat(pet.getType()).isEqualTo("Gato");
    }

    /**
     * Caso exitoso: Setter y getter de userEmail deben funcionar correctamente
     */
    @Test
    @DisplayName("setUserEmail/getUserEmail - Debe establecer y obtener el email correctamente")
    void setGetUserEmail_ShouldWorkCorrectly() {
        // When: Establecer email
        pet.setUserEmail("test@example.com");

        // Then: Verificar que se obtuvo el email correcto
        assertThat(pet.getUserEmail()).isEqualTo("test@example.com");
    }

    /**
     * Caso exitoso: Configurar todos los campos mediante setters
     */
    @Test
    @DisplayName("Setters completos - Debe poder establecer todos los campos")
    void setAllFields_ShouldWorkCorrectly() {
        // When: Establecer todos los campos
        pet.setId(1);
        pet.setName("Rex");
        pet.setType("Perro");
        pet.setUserEmail("owner@example.com");

        // Then: Verificar todos los campos
        assertThat(pet.getId()).isEqualTo(1);
        assertThat(pet.getName()).isEqualTo("Rex");
        assertThat(pet.getType()).isEqualTo("Perro");
        assertThat(pet.getUserEmail()).isEqualTo("owner@example.com");
    }

    // ========== PRUEBAS DE CASOS LÍMITE ==========

    /**
     * Caso límite: Establecer valores null debe ser permitido
     * Importante para validar flexibilidad antes de persistir
     */
    @Test
    @DisplayName("Setters con null - Debe permitir establecer valores null")
    void setNullValues_ShouldBeAllowed() {
        // Given: Entidad con valores iniciales
        pet.setName("Firulais");
        pet.setType("Perro");
        pet.setUserEmail("test@example.com");

        // When: Establecer valores null
        pet.setName(null);
        pet.setType(null);
        pet.setUserEmail(null);

        // Then: Verificar que los valores son null
        assertThat(pet.getName()).isNull();
        assertThat(pet.getType()).isNull();
        assertThat(pet.getUserEmail()).isNull();
    }

    /**
     * Caso límite: Establecer strings vacíos debe ser permitido
     */
    @Test
    @DisplayName("Setters con strings vacíos - Debe permitir strings vacíos")
    void setEmptyStrings_ShouldBeAllowed() {
        // When: Establecer strings vacíos
        pet.setName("");
        pet.setType("");
        pet.setUserEmail("");

        // Then: Verificar que se guardaron strings vacíos
        assertThat(pet.getName()).isEmpty();
        assertThat(pet.getType()).isEmpty();
        assertThat(pet.getUserEmail()).isEmpty();
    }

    /**
     * Caso límite: Establecer strings muy largos debe funcionar
     */
    @Test
    @DisplayName("Setters con strings largos - Debe manejar strings de gran tamaño")
    void setLongStrings_ShouldWork() {
        // Given: Strings muy largos
        String longString = "A".repeat(500);

        // When: Establecer strings largos
        pet.setName(longString);
        pet.setType(longString);
        pet.setUserEmail(longString);

        // Then: Verificar que se guardaron correctamente
        assertThat(pet.getName()).hasSize(500);
        assertThat(pet.getType()).hasSize(500);
        assertThat(pet.getUserEmail()).hasSize(500);
    }

    /**
     * Caso límite: Establecer ID con valores extremos
     */
    @Test
    @DisplayName("setId con valores extremos - Debe manejar Integer.MAX_VALUE y negativos")
    void setId_WithExtremeValues_ShouldWork() {
        // When & Then: Probar con valor máximo
        pet.setId(Integer.MAX_VALUE);
        assertThat(pet.getId()).isEqualTo(Integer.MAX_VALUE);

        // Probar con valor mínimo (aunque no tenga sentido en producción)
        pet.setId(Integer.MIN_VALUE);
        assertThat(pet.getId()).isEqualTo(Integer.MIN_VALUE);

        // Probar con valor 0
        pet.setId(0);
        assertThat(pet.getId()).isZero();
    }

    // ========== PRUEBAS DE COMPORTAMIENTO DE OBJETOS ==========

    /**
     * Caso de validación: Dos objetos con mismos valores NO son iguales
     * (PetEntity no implementa equals/hashCode, usa identidad de referencia)
     */
    @Test
    @DisplayName("Igualdad de objetos - Dos instancias con mismos valores son diferentes objetos")
    void equality_TwoInstancesWithSameValues_AreNotEqual() {
        // Given: Dos entidades con mismos valores
        PetEntity pet1 = new PetEntity("Firulais", "Perro", "user@example.com");
        pet1.setId(1);
        
        PetEntity pet2 = new PetEntity("Firulais", "Perro", "user@example.com");
        pet2.setId(1);

        // Then: No son iguales (diferentes referencias)
        assertThat(pet1).isNotEqualTo(pet2);
        assertThat(pet1).isNotSameAs(pet2);
    }

    /**
     * Caso de validación: Misma referencia debe ser igual a sí misma
     */
    @Test
    @DisplayName("Igualdad de objetos - Una instancia es igual a sí misma")
    void equality_SameReference_IsEqual() {
        // Given: Una entidad
        PetEntity samePet = new PetEntity("Firulais", "Perro", "user@example.com");

        // Then: Es igual a sí misma
        assertThat(samePet).isEqualTo(samePet);
        assertThat(samePet).isSameAs(samePet);
    }

    /**
     * Caso de validación: Actualización de campos debe reflejarse inmediatamente
     */
    @Test
    @DisplayName("Mutabilidad - Cambios en campos deben reflejarse inmediatamente")
    void mutability_ChangesInFields_ShouldBeReflectedImmediately() {
        // Given: Entidad con valores iniciales
        pet.setName("Original");
        assertThat(pet.getName()).isEqualTo("Original");

        // When: Cambiar el nombre
        pet.setName("Modificado");

        // Then: El cambio se refleja inmediatamente
        assertThat(pet.getName()).isEqualTo("Modificado");
    }

    /**
     * Caso de validación: Crear múltiples instancias debe ser independiente
     */
    @Test
    @DisplayName("Independencia - Múltiples instancias deben ser independientes")
    void independence_MultipleInstances_ShouldBeIndependent() {
        // Given: Crear dos instancias
        PetEntity pet1 = new PetEntity("Firulais", "Perro", "user1@example.com");
        PetEntity pet2 = new PetEntity("Michi", "Gato", "user2@example.com");

        // When: Modificar pet1
        pet1.setName("Firulais Modificado");

        // Then: pet2 no debe verse afectado
        assertThat(pet1.getName()).isEqualTo("Firulais Modificado");
        assertThat(pet2.getName()).isEqualTo("Michi");
    }

    // ========== PRUEBAS DE ESCENARIOS REALISTAS ==========

    /**
     * Escenario realista: Simular el ciclo de vida completo de una entidad
     */
    @Test
    @DisplayName("Ciclo de vida completo - Crear, modificar y verificar entidad")
    void lifecycleCycle_CreateModifyVerify_ShouldWorkCorrectly() {
        // 1. Crear entidad (como lo haría el controlador)
        PetEntity newPet = new PetEntity("Luna", "Gato", "maria@example.com");
        assertThat(newPet.getId()).isNull(); // Aún no tiene ID

        // 2. Simular que la base de datos asignó un ID
        newPet.setId(123);
        assertThat(newPet.getId()).isEqualTo(123);

        // 3. Usuario actualiza el nombre
        newPet.setName("Luna la Traviesa");
        assertThat(newPet.getName()).isEqualTo("Luna la Traviesa");

        // 4. Verificar que todos los campos están correctos
        assertThat(newPet.getId()).isEqualTo(123);
        assertThat(newPet.getName()).isEqualTo("Luna la Traviesa");
        assertThat(newPet.getType()).isEqualTo("Gato");
        assertThat(newPet.getUserEmail()).isEqualTo("maria@example.com");
    }

    /**
     * Escenario realista: Entidad recibida desde JSON (deserialización)
     */
    @Test
    @DisplayName("Deserialización simulada - Constructor vacío + setters como JSON parsing")
    void deserializationSimulation_EmptyConstructorPlusSetters_ShouldWork() {
        // Simular cómo Jackson deserializaría JSON
        // 1. Jackson crea instancia con constructor vacío
        PetEntity deserializedPet = new PetEntity();
        
        // 2. Jackson llama a los setters con valores del JSON
        deserializedPet.setName("Bobby");
        deserializedPet.setType("Perro");
        deserializedPet.setUserEmail("carlos@example.com");

        // 3. Verificar que la entidad quedó correctamente formada
        assertThat(deserializedPet.getName()).isEqualTo("Bobby");
        assertThat(deserializedPet.getType()).isEqualTo("Perro");
        assertThat(deserializedPet.getUserEmail()).isEqualTo("carlos@example.com");
    }
}
