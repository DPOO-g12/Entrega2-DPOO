package testLocalidades;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import localidades.NoNumerada;
import excepciones.CapacidadExcedidaLocalidad;

public class TestNoNumerada {

    private NoNumerada localidad;
    private final double PRECIO = 50000.0;
    private final int CAPACIDAD_MAX = 100;
    private final String NOMBRE = "General";

    @BeforeEach
    void setUp() {
        // Pasamos null en Evento para aislar la prueba.
        // No necesitamos un Evento real para probar si la localidad suma bien los tiquetes.
        localidad = new NoNumerada(PRECIO, CAPACIDAD_MAX, NOMBRE, null);
    }

    // --- Test de Estado Inicial ---
    
    @Test
    void testConstructor() {
        assertEquals(0, localidad.getTiquetesVendidos(), "Al inicio no debe haber tiquetes vendidos.");
        assertEquals(CAPACIDAD_MAX, localidad.getCapacidadMax(), "La capacidad máxima debe ser la asignada.");
        assertEquals(PRECIO, localidad.getPrecio(), "El precio debe ser el asignado.");
    }

    // --- Tests de Disponibilidad ---

    @Test
    void testVerificarDisponibilidadTotal() {
        // Caso: Pedir exactamente la capacidad máxima
        assertTrue(localidad.verificarDisponibilidad(CAPACIDAD_MAX), 
                "Debe haber disponibilidad si pido justo el total.");
    }

    @Test
    void testVerificarDisponibilidadExcedida() {
        // Caso: Pedir 1 más de la capacidad
        assertFalse(localidad.verificarDisponibilidad(CAPACIDAD_MAX + 1), 
                "No debe haber disponibilidad si pido más del límite.");
    }
    
    @Test
    void testVerificarDisponibilidadParcial() {
        // Caso: Simular que ya se vendieron algunos
        localidad.setTiquetesVendidos(90); // Quedan 10
        
        assertTrue(localidad.verificarDisponibilidad(10), "Debe permitir comprar los 10 restantes.");
        assertFalse(localidad.verificarDisponibilidad(11), "No debe permitir comprar 11 si quedan 10.");
    }

    // --- Tests de Venta (Lógica Principal) ---

    @Test
    void testVenderTiquetesExitoso() throws CapacidadExcedidaLocalidad {
        int cantidadCompra = 5;
        
        List<String> tickets = localidad.venderTiquetes(cantidadCompra);
        
        // 1. Verificar que el contador aumentó
        assertEquals(cantidadCompra, localidad.getTiquetesVendidos(), "El contador de vendidos debe aumentar.");
        
        // 2. Verificar la lista de retorno
        assertEquals(cantidadCompra, tickets.size(), "Debe devolver una lista del tamaño de la compra.");
        
        // 3. Verificar que en NoNumerada los 'asientos' son null
        // (Según tu lógica: asientosAsignados.add(null))
        assertNull(tickets.get(0), "En localidad no numerada, la lista contiene nulos.");
    }

    @Test
    void testVenderTiquetesLanzaExcepcion() {
        // Intentar vender más de la capacidad (101)
        assertThrows(CapacidadExcedidaLocalidad.class, () -> {
            localidad.venderTiquetes(CAPACIDAD_MAX + 1);
        }, "Debe lanzar excepción si se excede la capacidad.");
        
        // Verificar que el contador NO cambió tras el fallo
        assertEquals(0, localidad.getTiquetesVendidos(), "Si falla la venta, el contador no debe moverse.");
    }
    
    @Test
    void testVentasAcumulativas() throws CapacidadExcedidaLocalidad {
        // Venta 1
        localidad.venderTiquetes(50);
        assertEquals(50, localidad.getTiquetesVendidos());
        
        // Venta 2
        localidad.venderTiquetes(40);
        assertEquals(90, localidad.getTiquetesVendidos());
        
        // Intento de Venta 3 (Fallido: pide 11, quedan 10)
        assertThrows(CapacidadExcedidaLocalidad.class, () -> localidad.venderTiquetes(11));
        
        // El estado debe seguir en 90
        assertEquals(90, localidad.getTiquetesVendidos());
    }
    
    // --- Test de Setter (Para cobertura) ---
    @Test
    void testSetTiquetesVendidos() {
        localidad.setTiquetesVendidos(10);
        assertEquals(10, localidad.getTiquetesVendidos());
    }
    
    

}