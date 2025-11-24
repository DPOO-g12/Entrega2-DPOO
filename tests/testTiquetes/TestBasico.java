package testTiquetes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

public class TestBasico {
	
	
	private OrganizadorEventos cliente;
	private Localidades localidad;
	private Evento evento;
	
	//Datos de prueba
	
	private final double PRECIO_BASE = 50.0;
	private final double PORCENTAJE_SERVICIO = 0.10;
	private final double COBRO_EMISION = 2.0;
	private final String FECHA = "2026-12-10";
    private final String ASIENTO = "S-14";
	
	
    @BeforeEach 
    
    void setUp () throws VenueOcupado {
    	
    	cliente = new OrganizadorEventos("userCompra", "pass", 100.0);

        Venue venue = new Venue("Lugar", "Ubicacion", 100, null, "APROBADO");
        evento = new Evento("E01", "Concierto", FECHA, venue, cliente);
        
        localidad = new NoNumerada(PRECIO_BASE, 50, "General", evento);

    }
    
    @Test 
    
    void testImplementacionTipoTiquete () {
    	
    	  Basico tiquete = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
    	            cliente, localidad, evento, "ACTIVO", ASIENTO);
    	
    	  assertEquals ("BASICO", tiquete.getTipoTiquete(), "Debe ser un tiquete Basico");

    }
    
    @Test 
    
    void  testNumeroAsientoGetter () {
    	
    	Basico tiquete = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                cliente, localidad, evento, "ACTIVO", ASIENTO);
    	
    	assertEquals (ASIENTO, tiquete.getNumeroAsiento(), "El asiento debe ser el asignado");
    	
    	Basico tiqueteNoAsiento = new Basico(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                cliente, localidad, evento, "ACTIVO", null);
    	
    	assertNull (tiqueteNoAsiento.getNumeroAsiento(), "El asiento puede ser nulo en localidades no numeradas");
    }
    
    @Test
	void testCargarDesdeDB() {
		// Datos simulados de BD
		int idDB = 20;
		String idJava = "BASICO-ABC-123";
		double pBase = 100.0;
		double pServ = 0.1;
		double pEmi = 2.0;
		double pFinal = 112.0;
		String fechaDB = "2025-12-25";
		String estadoDB = "ACTIVO";
		boolean transferibleDB = true;
		String asientoDB = "A-1";
		
		// Ejecutar el método estático
		Basico tiqueteRecuperado = Basico.cargarDesdeDB(
				idDB, idJava, pBase, pServ, pEmi, pFinal, 
				fechaDB, estadoDB, transferibleDB, 
				cliente, localidad, evento, asientoDB);
		
		// Verificar reconstrucción
		assertEquals(idDB, tiqueteRecuperado.getIdTiqueteDb());
		assertEquals(idJava, tiqueteRecuperado.getIdTiquete());
		assertEquals(pFinal, tiqueteRecuperado.getPrecioFinal(), 0.001);
		assertEquals(asientoDB, tiqueteRecuperado.getNumeroAsiento());
		assertEquals("BASICO", tiqueteRecuperado.getTipoTiquete());
		assertTrue(tiqueteRecuperado.isTransferible());
	}

}
