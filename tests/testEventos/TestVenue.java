package testEventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Venue;
import excepciones.VenueOcupado;

public class TestVenue {
	
	private Venue venueAprobado;
	private Venue venuePendiente;
	private OrganizadorEventos promotor;
	
	

	@BeforeEach 
	
	void setUp() {
		
		promotor = new OrganizadorEventos("ORG1", "password1", 0);
		
		List <String> restricciones = new ArrayList<> ();
		restricciones.add("no fumar");
		
		venueAprobado = new Venue("Estadio","Calle falsa",50000, restricciones, "APROBADO");
		
		venueAprobado.setIdVenue(1); // simular ID asignado por persistencia
		
		venuePendiente = new Venue ("Teatro", "CALLE 1",1000, null, "PENDIENTE");
	}
	
	@Test
    void testConstructorYGetters() {
        //Verificar la inicialización correcta de los atributos
        assertEquals(1, venueAprobado.getIdVenue(), "El ID del Venue debería ser 1.");
        assertEquals("Estadio", venueAprobado.getTipo(), "El tipo debería ser 'Estadio'.");
        assertEquals("Calle falsa", venueAprobado.getUbicacion(), "La ubicación es incorrecta.");
        assertEquals(50000, venueAprobado.getCapacidadMaxima(), "La capacidad máxima es incorrecta.");
        assertEquals("APROBADO", venueAprobado.getEstado(), "El estado inicial debería ser APROBADO.");
        assertFalse(venueAprobado.getRestricciones().isEmpty(), "Debe tener restricciones.");
        assertTrue(venueAprobado.getEventosAsociados().isEmpty(), "Debe iniciar sin eventos asociados.");
    }
	
	@Test 
	
	void testConstructorConNull () {
		
		assertNotNull (venuePendiente.getRestricciones(), "Al pasar null deberia hacer que las restricciones sean una lista vacia");
	
		assertTrue(venuePendiente.getRestricciones().isEmpty(), "no debe tener restricciones");
	}
	
	@Test 
	
	void testSetEstado () {
		
		venuePendiente.setEstado("APROBADO");
		
		assertEquals ("APROBADO", venuePendiente.getEstado(), "el cambio de estado no se ve refeljado");
	}
	
	// tests para programar evento y disponiblidad 
	
	
	@Test 
	
	void testProgramarEventoExitoso () throws VenueOcupado {
		
		Evento evento1 = new Evento ("E001", "Concierto Rock", "2026-12-01", venueAprobado, promotor);
		
		assertTrue(venueAprobado.getEventosAsociados().contains(evento1), "El venue no agrega el evento que se tiene planificado");
		assertEquals (1, venueAprobado.getEventosAsociados().size(), "El venue solo debe tener un evento asociado");
		
	}
	
	@Test 
	
	void testProgramarEventoVenueNoAprobado ()  {
		
		// assertThrows es para revisar que se lanza la excepcion esperada
		// se debe instanciar la excepcion dentro del assert porque si no ocurre nates 
		
		VenueOcupado exception = assertThrows(VenueOcupado.class, () -> 
		new Evento ("E001", "Concierto Rock", "2026-12-01", venuePendiente, promotor), "Deberia lanzar la excepcion de VenueOcupado");
		
		assertTrue(exception.getMessage().contains("no ha sido aprobado por el administrador"));

	}
	
	@Test 
	
	void testProgramarEventoMismaFecha () throws VenueOcupado {
		
		// Verificar que NO se puede programar si ya hay un evento en esa fecha
		
		String fechaComun = "2026-10-31";
		
		Evento eventoBase = new Evento("E001", "Evento Base", fechaComun, venueAprobado, promotor);
		
		VenueOcupado exception = assertThrows (VenueOcupado.class, ()->
		
		new Evento ("E002", "Evento Base2", fechaComun, venueAprobado, promotor), "Deberia lanzar VenueOcupado si ya hay un evento en esa fecha");
		
		// Verificar que solo el primer evento está asociado
		
		assertEquals (1, venueAprobado.getEventosAsociados().size(), "Solo deberia haber un evento asociado");
		
		assertTrue ( venueAprobado.getEventosAsociados().contains(eventoBase), "Deberia registrar el primer evento registrado");
		
		assertTrue(exception.getMessage().contains("Reservaron antes que tu este venue para el dia"));
				
		
	}
	
	@Test 
	
	void testProgramarEventosFechasDiferentes () throws VenueOcupado {
		
		//Verificar que SÍ se pueden programar eventos en fechas diferentes
		
		Evento evento1 = new Evento("E001", "Concierto 1", "2026-11-01", venueAprobado, promotor);
        Evento evento2 = new Evento("E002", "Concierto 2", "2026-11-02", venueAprobado, promotor);
        
        assertEquals (2, venueAprobado.getEventosAsociados().size(), "Debe registar todos los eventos si no hay problemas");
        
        assertTrue (venueAprobado.getEventosAsociados().contains(evento2) &&  venueAprobado.getEventosAsociados().contains(evento1),
        			"Debe contener ambos eventos");
	}
	
	// test para verificar disponibilidad 
	
	@Test 
	
	void testVerificarDisponibilidadVenueLibre () {
		
		assertTrue (venueAprobado.verificarDisponibilidad("2026-05-01"), "El venue deberia estar libre para una fecha sin evento");
	}
	
	@Test 
	
	void testVerificarDisponibilidadVenueOcupado () throws VenueOcupado {
		
		new Evento("E001", "Concierto 1", "2026-11-01", venueAprobado, promotor);

		String fechaOcupada = "2026-11-01";
		
		assertFalse (venueAprobado.verificarDisponibilidad(fechaOcupada), "No debe estar disponible para un fecha ya programada");
		
		assertTrue (venueAprobado.verificarDisponibilidad("2026-11-02"), " debe estar disponible para un fecha no programada");
		
		
		
	}

}
