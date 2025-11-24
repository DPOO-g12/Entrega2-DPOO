package testEventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Venue;
import excepciones.VenueOcupado;
import localidades.NoNumerada;
import localidades.Numerada;

public class TestEvento {
	
	private final String ID_EVENTO = "E001";
    private final String NOMBRE_EVENTO = "Festival Jazz";
    private final String FECHA_EVENTO = "2026-03-15";
	private OrganizadorEventos promotor;
	private Venue venueDisponible;
	
	
	
	@BeforeEach 
	
	void setUp () {
		
		promotor = new OrganizadorEventos("jazzpromotor", "pass123", 1000.0);

	    venueDisponible = new Venue("Estadio", "Centro", 10000, null, "APROBADO");
	}
	
	//TESTS DE CONSTRUCTOR 
	
	@Test 
	
	void testCreacionEventoExitoso () {
		
	try {
		Evento evento = new Evento(ID_EVENTO, NOMBRE_EVENTO, FECHA_EVENTO, venueDisponible, promotor);
		
		assertEquals(ID_EVENTO, evento.getId(), "El ID del evento nno coincide");
        assertEquals("Activo", evento.getEstado(), "El estado del evento debe ser ACTIVO");
		
        assertTrue(venueDisponible.getEventosAsociados().contains(evento), "El venue debe asociarse con el evento");
		
	} catch (VenueOcupado e) {
		// TODO Auto-generated catch block
		fail("La creación del evento no debería fallar si el Venue está APROBADO y DISPONIBLE: " + e.getMessage());
	}	
		
	}
	
	@Test
	
	void testCreacionEventoFallaPorVenueNoAprobado () {
		
		Venue venuePendiente = new Venue("Teatro", "Sur", 500, null, "PENDIENTE");
		
		assertThrows (VenueOcupado.class, () -> 
		new Evento(ID_EVENTO, NOMBRE_EVENTO, FECHA_EVENTO, venuePendiente, promotor), "Deberia Lanzar VenueOcuapdo si es estado PENDIENTE");

	}
	
	@Test 
	
	void testCreacionEventoFallaPorVenueOcupado () throws VenueOcupado {
		
		//Verificar que el constructor falla si ya hay un evento en la misma fecha.
		
		new Evento("E001", "Evento Base", FECHA_EVENTO, venueDisponible, promotor);
		
		assertThrows (VenueOcupado.class, () ->
		new Evento("E002", "Evento Conflicto", FECHA_EVENTO, venueDisponible, promotor), "Deberia Lanzar VenueOcupado si ya hay un evento en la misma fecha");

	}
	
	// TEST PARA METODOS DE LOCALIDADES
	
	@Test 
	
	void testAgregarLocalidadNumerada () throws VenueOcupado {
		
		//Verificar que se puede agregar una Localidad Numerada
		
		Evento evento = new Evento(ID_EVENTO, NOMBRE_EVENTO, FECHA_EVENTO, venueDisponible, promotor);
		String nombreLocalidad = "Palcos VIP";
		
		Map <String,Boolean> asientos = new HashMap <>();
		
		asientos.put("P1", false);
		
		// Llamamos al método que instancia la clase Numerada REAL
		
		evento.agregarLocalidadNumerada(nombreLocalidad, 1500, 10, asientos);
		
		assertEquals (1, evento.getLocalidades().size(), "Debe haber una localidad en el mapa");

		// Verificamos que se agregó la instancia correcta (Numerada real)
		assertTrue(evento.getLocalidades().get(nombreLocalidad) instanceof Numerada, "Debe ser una instancia de Numerada.");

	}
	
	@Test 
	
	void testAgregarLocalidadNoNumerada () throws VenueOcupado {
		
		Evento evento = new Evento(ID_EVENTO, NOMBRE_EVENTO, FECHA_EVENTO, venueDisponible, promotor);
		
		String nombreLocalidad = "General";
		
		evento.agregarLocalidadNoNumerada(nombreLocalidad, 50, 5000);
		
		assertEquals (1, evento.getLocalidades().size(), "Solo debe tener una localidad");
		
		assertTrue (evento.getLocalidades().get(nombreLocalidad) instanceof NoNumerada, "Debe ser una instancia de no Numerada");
		
		// .get(nombreLocalidad) --> no te devuelve la clave, te devuelve el valor asociado a esa clave.
	}
	
	@Test 
	
	void testAgregarMultiplesLocalidadesConMismoNombre () throws VenueOcupado {
		//Verificar que la adición de una segunda localidad con el mismo nombre reemplaza a la primera
		
		Evento evento = new Evento(ID_EVENTO, NOMBRE_EVENTO, FECHA_EVENTO, venueDisponible, promotor);
		
		String nombreLocalidad = "Central";
		
		evento.agregarLocalidadNoNumerada(nombreLocalidad, 50, 1000);
		
		evento.agregarLocalidadNumerada(nombreLocalidad, 150, 200, null);
		
		assertEquals (1,evento.getLocalidades().size(), "El tamaño del mapa debe ser 1");
		
		assertTrue(evento.getLocalidades().get(nombreLocalidad) instanceof Numerada, "La localidad final debe ser la que  se agrego de ultimo");

	}
	
	@Test
    void testSetEstado() throws VenueOcupado {
        Evento evento = new Evento(ID_EVENTO, NOMBRE_EVENTO, FECHA_EVENTO, venueDisponible, promotor);
        evento.setEstado("CANCELADO");
        assertEquals("CANCELADO", evento.getEstado(), "El estado debería cambiar a CANCELADO.");
    }
	
	@Test
	void testGetters() throws VenueOcupado {
		Evento evento = new Evento(ID_EVENTO, NOMBRE_EVENTO, FECHA_EVENTO, venueDisponible, promotor);
		
		assertEquals(NOMBRE_EVENTO, evento.getNombre(), "El nombre debe coincidir");
		assertEquals(FECHA_EVENTO, evento.getFecha(), "La fecha debe coincidir");
		assertEquals(venueDisponible, evento.getVenue(), "El venue debe ser el mismo objeto");
		assertEquals(promotor, evento.getPromotor(), "El promotor debe ser el mismo objeto");
		
	}
	
	@Test
	void testCargarDesdeDB() {
		// Datos simulados de una Base de Datos
		String idDB = "DB-999";
		String nombreDB = "Evento Pasado";
		String fechaDB = "2020-01-01";
		String estadoDB = "FINALIZADO";
		
		// Llamamos al método estático
		Evento eventoRecuperado = Evento.cargarDesdeDB(idDB, nombreDB, fechaDB, venueDisponible, promotor, estadoDB);
		
		// Verificamos que el objeto se creó correctamente con los datos "viejos"
		assertEquals(idDB, eventoRecuperado.getId());
		assertEquals(nombreDB, eventoRecuperado.getNombre());
		assertEquals(fechaDB, eventoRecuperado.getFecha());
		assertEquals(estadoDB, eventoRecuperado.getEstado());
        // Verificar que el mapa de localidades se inicializó (no es nulo)
        assertTrue(eventoRecuperado.getLocalidades() != null); 
	}

}
