package testTiquetes;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Venue;
import excepciones.TiqueteNoTransferibleException;
import excepciones.VenueOcupado;
import localidades.Localidades;
import localidades.NoNumerada;
import tiquetes.Deluxe;

public class TestDeluxe {
	
	private final double PRECIO_BASE = 200.0;
	private final double PORCENTAJE_SERVICIO = 0.15;
	private final double COBRO_EMISION = 5.0;
	private final String FECHA = "2026-12-10";
	
	private OrganizadorEventos clienteOriginal;
	private OrganizadorEventos clienteDestinatario;
	private Localidades localidad;
	private Evento evento;
	private List<String> beneficios;
	
	@BeforeEach 
	
	void setUp () throws VenueOcupado {
		
		clienteOriginal = new OrganizadorEventos("user1", "pass", 100.0);
        clienteDestinatario = new OrganizadorEventos("user2", "pass", 50.0);
        
        Venue venue = new Venue("Lugar", "Ubicacion", 100, null, "APROBADO");
        evento = new Evento("E01", "Concierto", FECHA, venue, clienteOriginal);
        
        localidad = new NoNumerada(PRECIO_BASE, 50, "VIP-Box", evento);
        
		beneficios = new ArrayList <>();
		
		beneficios.add("Acesso Backstage");
		beneficios.add("Merchandising Gratuito");

	}
	
	@Test 
	
	void testConstructor () {
		
        Deluxe tiquete = new Deluxe(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                                     clienteOriginal, localidad, evento, "ACTIVO", beneficios);

        assertEquals(235.0, tiquete.getPrecioFinal(), "El precio final debe ser 200 + 30 + 5 = 235.0");
        assertEquals("DELUXE", tiquete.getTipoTiquete(), "El tipo debe ser 'DELUXE'.");
        
        assertEquals(2, tiquete.getBeneficiosAdicionales().size(), "Debe tener 2 beneficios asignados.");
        

	}
	
	@Test

    void testConstructorManejoBeneficiosNulos() {
        
        Deluxe tiquete = new Deluxe(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, 
                                     clienteOriginal, localidad, evento, "ACTIVO", null);
        
        assertNotNull(tiquete.getBeneficiosAdicionales(), "La lista no debe ser null.");
        assertTrue(tiquete.getBeneficiosAdicionales().isEmpty(), "La lista debe ser vacía si se pasó null.");
    }
	
	@Test 
	
	void testRestriccionTransferibilidadFija () {
		
		Deluxe tiquete = new Deluxe(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, clienteOriginal, localidad, evento, "ACTIVO", beneficios);
	
		assertFalse (tiquete.isTransferible(), "Un tiquete deluxe no puede ser transferible");

	}
	
	@Test 
	
	void testTransferirTiqueteFallaPorNoTransferible () {
		
		Deluxe tiquete = new Deluxe(PRECIO_BASE, PORCENTAJE_SERVICIO, COBRO_EMISION, FECHA, clienteOriginal, localidad, evento, "ACTIVO", beneficios);
		
		assertThrows (TiqueteNoTransferibleException.class, () ->
		tiquete.transferirTiquete(clienteDestinatario),"La transferencia debe fallar porque el tiquete es NO transferible.");
		
		assertSame(clienteOriginal, tiquete.getCliente(), "El cliente no debio haber cambiado");
		
		
		
		
	}
	
	@Test
	void testCargarDesdeDB() {
		// Datos simulados de BD
		int idDB = 50;
		String idJava = "TICKET-DELUXE-001";
		double pBase = 300.0;
		double pServ = 0.1;
		double pEmision = 5.0;
		double pFinal = 335.0;
		String fechaDB = "2025-12-31";
		String estadoDB = "ACTIVO";
		boolean transferibleDB = false; // Deluxe siempre es false
		
		// Llamamos al método estático que queremos probar
		Deluxe tiqueteRecuperado = Deluxe.cargarDesdeDB(idDB, idJava, pBase, pServ, pEmision, pFinal, 
				fechaDB, estadoDB, transferibleDB, clienteOriginal, localidad, evento, beneficios);
		
		// Verificamos que los datos se asignaron correctamente
		assertEquals(idDB, tiqueteRecuperado.getIdTiqueteDb());
		assertEquals(idJava, tiqueteRecuperado.getIdTiquete());
		assertEquals(pFinal, tiqueteRecuperado.getPrecioFinal(), 0.001);
		assertFalse(tiqueteRecuperado.isTransferible()); // Confirmamos que recuperó el valor
		
		// Verificamos los beneficios
		assertEquals(2, tiqueteRecuperado.getBeneficiosAdicionales().size());
		assertTrue(tiqueteRecuperado.getBeneficiosAdicionales().contains("Acesso Backstage"));
	}
	
	

}
