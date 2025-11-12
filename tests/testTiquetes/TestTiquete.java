package testTiquetes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Venue;
import excepciones.VenueOcupado;
import localidades.Localidades;
import localidades.NoNumerada;
import tiquetes.Basico;

public class TestTiquete {
	
	private final double  PRECIO_BASE = 50.0;
	private final double PORCENTAJE_SERVICIO = 0.10;
	private final double COBRO_EMISION = 2.0;
	private final String FECHA = "2026-12-10";
	
	private OrganizadorEventos clienteOriginal;
	private OrganizadorEventos clienteDestinatario;
	private Localidades localidad;
	private Evento evento;
	
	@BeforeEach 
	
	void setUp () throws VenueOcupado {
		
		clienteOriginal = new OrganizadorEventos("user1", "pass", 100.0);
        clienteDestinatario = new OrganizadorEventos("user2", "pass", 50.0);
        
        Venue venue = new Venue("Lugar", "Ubicacion", 100, null, "APROBADO");
        evento = new Evento("E01", "Concierto", FECHA, venue, clienteOriginal);
        
        localidad = new NoNumerada(PRECIO_BASE, 50, "General", evento);

	}
	
	@Test
    void testConstructorEInicializacion() {
        // 1. Crear el tiquete (usamos Basico para instanciar Tiquete)
        Basico tiquete = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                                     clienteOriginal, localidad, evento, "ACTIVO", "A-5");

        // Verificaciones de inicialización
        assertEquals(PRECIO_BASE, tiquete.getPrecioBase(),"El precio base debe ser correcto.");
        assertEquals(FECHA, tiquete.getFecha());
        assertSame(clienteOriginal, tiquete.getCliente());
        assertEquals("ACTIVO", tiquete.getEstado());
        assertTrue(tiquete.isTransferible(), "El tiquete debe ser transferible por defecto.");
        assertNotNull(tiquete.getIdTiquete(), "El ID del tiquete no debe ser nulo.");
        assertTrue(tiquete.getTipoTiquete().equals("BASICO"), "Debe devolver el tipo de tiquete correcto.");
        
        // Verificación de Basico
        assertEquals("A-5", tiquete.getNumeroAsiento(), "El número de asiento debe ser correcto.");
    }
	
	@Test 
	
	void testCalculoDeCostosInicales () {
		
		Basico tiquete = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                clienteOriginal, localidad, evento, "ACTIVO", null);

		double CostoServicioEsperado = PRECIO_BASE * PORCENTAJE_SERVICIO;
		assertEquals (CostoServicioEsperado, tiquete.getCostoServicio(), "El costo de servicio no es el esperado");
		
		assertEquals (COBRO_EMISION, tiquete.getCostoEmision(), "el costo de emision no es el esperado");
		
		double precioFinalEsperado = CostoServicioEsperado + COBRO_EMISION + PRECIO_BASE;
		
		assertEquals (precioFinalEsperado, tiquete.getPrecioFinal(), "El precio final no es el esperado");

	}
	
	@Test 
	
	void testGenerarIdUnico () {
		
		//Verificar que la lógica de generación de ID crea IDs diferentes (aunque sean muy cercanos).
        Basico tiquete1 = new Basico(50, 0.1, 2, FECHA, clienteOriginal, localidad, evento, "ACTIVO", null);
        Basico tiquete2 = new Basico(50, 0.1, 2, FECHA, clienteOriginal, localidad, evento, "ACTIVO", null);
		
		assertNotEquals (tiquete1.getIdTiquete(), tiquete2.getIdTiquete(), "Dos tiquetes no pueden tener el mismo ID");
		
		
        
        
	}
	
	
	

}
