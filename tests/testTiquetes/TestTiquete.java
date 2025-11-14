package testTiquetes;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Venue;
import excepciones.TiqueteNoTransferibleException;
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
		
		assertTrue (tiquete1.getIdTiquete().startsWith("GEN-"), "El id debe empezar con el prefijo de la localidad");

	}
	
	// TEST PARA LOGICA DE TRASNFERENCIA DE TIQUETE 
	
	@Test 
	
	void testTransferenciaExitosa () throws TiqueteNoTransferibleException {
		
		Basico tiquete = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, clienteOriginal, localidad, evento, "ACTIVO", null);
		
		tiquete.transferirTiquete(clienteDestinatario);
		
		assertEquals ("TRANSFERIDO", tiquete.getEstado(), "El estado del tiquete debe cambiar");
		
		assertSame(clienteDestinatario, tiquete.getCliente(), "El dueño del cliente debe cambiar");
		
	}
	
	@Test 
	
	void testTransferenciaFallaSiNoTransferible () {
		
		Basico tiquete = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                clienteOriginal, localidad, evento, "ACTIVO", null);
		
		tiquete.setTransferible(false);
		
		assertThrows (TiqueteNoTransferibleException.class, () -> tiquete.transferirTiquete(clienteDestinatario),
				"Debe fallas si IsTransferable es false");
		
		
		assertEquals ("ACTIVO", tiquete.getEstado(), "El estado no debe cambiar");
		
		// assertSame: Se usa cuando necesitas asegurar que se está trabajando con la misma instancia de un objeto,
		// no una copia o un objeto equivalente. Por ejemplo, si un método debe devolver el mismo objeto que se le pasó.
				
		assertSame (clienteOriginal, tiquete.getCliente(), "El cliente no debio cambiar");

	}
	
	@Test 
	
	void testTransferenciaFallaSiVencido () {
		
		Basico tiquete = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                clienteOriginal, localidad, evento, "VENCIDO", null);
		
		assertThrows (TiqueteNoTransferibleException.class, ()-> tiquete.transferirTiquete(clienteDestinatario),
				"El tiquete no se debe poder transferir si ya se vencio");
		
		
		assertEquals("VENCIDO", tiquete.getEstado(), "El estado debe permanecer como VENCIDO");
		
		assertSame (clienteOriginal, tiquete.getCliente(), "EL tiquete no se debio transferir");
		
		
	}
	
	// basicos getters y setters 
	
	@Test
    void testSetEstadoYTransferible() {
        //Verificar el correcto funcionamiento de los setters básicos.
        Basico tiquete = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                                     clienteOriginal, localidad, evento, "ACTIVO", null);

        tiquete.setEstado("PENDIENTE_REEMBOLSO");
        assertEquals("PENDIENTE_REEMBOLSO", tiquete.getEstado(), "El estado debe ser el asignado por setter");
        
        tiquete.setTransferible(false);
        assertFalse(tiquete.isTransferible(), "El estado debe ser el asigando por setter");
    }
	
	

}
