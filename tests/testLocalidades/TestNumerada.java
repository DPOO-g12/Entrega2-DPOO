package testLocalidades;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import localidades.Numerada;
import excepciones.CapacidadExcedidaLocalidad;

public class TestNumerada {

    private Numerada localidad;
    private Map<String, Boolean> mapaAsientos;
    private final double PRECIO = 150000.0;
    private final String NOMBRE = "VIP";

    @BeforeEach
    void setUp() {
        // Configuración del escenario:
        // Creamos un mapa con 5 asientos en total.
        // 3 Disponibles (false) y 2 Ocupados (true).
        mapaAsientos = new HashMap<>();
        mapaAsientos.put("A1", false); // Libre
        mapaAsientos.put("A2", true);  // Ocupado
        mapaAsientos.put("B1", false); // Libre
        mapaAsientos.put("B2", true);  // Ocupado
        mapaAsientos.put("C1", false); // Libre

        // Pasamos null en Evento para aislar la prueba de dependencias externas
        localidad = new Numerada(PRECIO, 5, NOMBRE, null, mapaAsientos);
    }

    // --- Tests de Inicialización ---

    @Test
    void testConstructorConMapa() {
        // Verificar que el mapa se asignó correctamente
        assertEquals(5, localidad.getAsientos().size());
        assertEquals(PRECIO, localidad.getPrecio());
        // Verificar un asiento específico
        assertTrue(localidad.getAsientos().get("A2"), "El asiento A2 debería estar ocupado.");
        assertFalse(localidad.getAsientos().get("A1"), "El asiento A1 debería estar libre.");
    }

    @Test
    void testConstructorConMapaNulo() {
        // Caso: Pasar null en el mapa (Tu código tiene un if para esto)
        Numerada localidadVacia = new Numerada(PRECIO, 10, "Vacia", null, null);
        
        assertNotNull(localidadVacia.getAsientos(), "El mapa no debe ser null aunque se pase null al constructor.");
        assertTrue(localidadVacia.getAsientos().isEmpty(), "El mapa debe estar vacío.");
    }

    // --- Tests de Disponibilidad ---

    @Test
    void testVerificarDisponibilidad() {
        // Tenemos 3 asientos libres (A1, B1, C1)
        
        // 1. Pedir menos de lo disponible
        assertTrue(localidad.verificarDisponibilidad(2), "Debe haber disponibilidad para 2.");
        
        // 2. Pedir exactamente lo disponible
        assertTrue(localidad.verificarDisponibilidad(3), "Debe haber disponibilidad para 3.");
        
        // 3. Pedir más de lo disponible
        assertFalse(localidad.verificarDisponibilidad(4), "No debe haber disponibilidad para 4.");
    }

    // --- Tests de Venta (Lógica Compleja) ---

    @Test
    void testVenderTiquetesExitoso() throws CapacidadExcedidaLocalidad {
        // Escenario: Comprar 2 boletas. Hay 3 libres.
        
        List<String> asientosVendidos = localidad.venderTiquetes(2);
        
        // 1. Verificar tamaño de la lista devuelta
        assertEquals(2, asientosVendidos.size(), "Debe retornar 2 asientos.");
        
        // 2. Verificar que los asientos devueltos AHORA están ocupados en el mapa
        for (String asiento : asientosVendidos) {
            assertTrue(localidad.getAsientos().get(asiento), 
                    "El asiento " + asiento + " ahora debería marcarse como ocupado (true).");
        }
        
        // 3. Verificar que la disponibilidad se redujo
        // Antes habían 3 libres, vendimos 2, debe quedar 1 libre.
        assertTrue(localidad.verificarDisponibilidad(1));
        assertFalse(localidad.verificarDisponibilidad(2));
    }
    
    @Test
    void testVenderTodosLosTiquetes() throws CapacidadExcedidaLocalidad {
        // Vender exactamente los 3 libres
        List<String> vendidos = localidad.venderTiquetes(3);
        
        assertEquals(3, vendidos.size());
        assertFalse(localidad.verificarDisponibilidad(1), "Ya no deben quedar asientos.");
        
        // Verificar que todos en el mapa están en true
        for (Boolean estado : localidad.getAsientos().values()) {
            assertTrue(estado, "Todos los asientos deberían estar ocupados.");
        }
    }

    @Test
    void testVenderTiquetesFallaSinCambiarEstado() {
        // Escenario: Intentar comprar 4 (solo hay 3 libres).
        // Objetivo: Verificar que lanza excepción Y que NO ocupa asientos parcialmente.
        
        assertThrows(CapacidadExcedidaLocalidad.class, () -> {
            localidad.venderTiquetes(4);
        }, "Debe lanzar excepción por capacidad.");
        
        // VALIDACIÓN DE INTEGRIDAD:
        // El mapa debe seguir teniendo 3 asientos libres (false).
        // Si tu código no validara antes del for, esto fallaría porque habría ocupado algunos antes de explotar.
        long libres = localidad.getAsientos().values().stream().filter(v -> !v).count();
        assertEquals(3, libres, "El número de asientos libres no debe cambiar si la venta falla.");
    }
}