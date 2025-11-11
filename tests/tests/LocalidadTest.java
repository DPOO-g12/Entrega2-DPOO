package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Oferta;
import eventos.Venue;
import excepciones.CapacidadExcedidaLocalidad;
import excepciones.VenueOcupado;
import localidades.Localidades;
import localidades.NoNumerada;
import localidades.Numerada;


public class LocalidadTest {

	private Localidades localidad1;
	private Localidades localidad2;
	
	private Evento evento1;
	private Evento evento2;
	
	private Venue venue1;
	private Venue venue2;
	
	private OrganizadorEventos organizador1;
	private OrganizadorEventos organizador2;
	
	private Oferta oferta1;
	private Oferta oferta2;
	
	@BeforeEach
	void setup() throws VenueOcupado {
		
		organizador1 = new OrganizadorEventos("org1", "123", 200);
		
		venue1 = new Venue("x", "Calle 33-23", 500, null, "APROBADO");
				
		evento1 = new Evento("1", "Concierto 1", "12/12/25", venue1, organizador1);
		
		localidad1 = new NoNumerada(120000, 500, "Campin", evento1);
		
		oferta1 = new Oferta(true, 0.5, LocalDateTime.of(2025, 12, 12, 20, 30), organizador1);
		
		localidad1.setOferta(oferta1);
		
		
		
		organizador2 = new OrganizadorEventos("org2", "123", 200);
		
		venue2 = new Venue("x", "Calle 55-23", 1000, null, "APROBADO");
				
		evento2 = new Evento("2", "Concierto 2", "12/12/25", venue2, organizador2);
		
		localidad2 = new Numerada(300000, 2000, "Campin", evento2, null);
		
		oferta2 = new Oferta(true, 0.5, LocalDateTime.of(2025, 12, 12, 20, 30), organizador2);
		
		localidad2.setOferta(oferta2);
	}
	
	@AfterEach
	void teardown() {
		
	}
	
	@Test
	void testGenerarLocalidadNoNumerada() {
		
		assertEquals(120000, localidad1.getPrecio(), "El precio es incorrecto");
		assertEquals(500, localidad1.getCapacidadMax(), "La capacidad es incorrecta");
		assertEquals("Campin", localidad1.getNombreLocalidad(), "El nombre es incorrecto");
		assertEquals("12/12/25", localidad1.getEvento().getFecha(), "La fecha es incorrecta");
		
	}
	
	@Test
	void testGetPrecioFinal() {
		assertEquals(60000, localidad1.getPrecioFinal());
	}
	
	@Test
	void testVenderTiquetesNoNumerados(){
		
		ArrayList<String> array1 = new ArrayList<>(); 
		array1.add(null);
		array1.add(null);
		
		assertEquals(array1 , localidad1.venderTiquetes(2));
		
		CapacidadExcedidaLocalidad exception = assertThrows(
		        CapacidadExcedidaLocalidad.class,
		        () -> localidad1.venderTiquetes(2000)
		    );

		    assertEquals("Excediste la capacidad de esta localidad: " 
		                 + localidad1.getNombreLocalidad() 
		                 + " crack", 
		                 exception.getMessage());
	}
	
	@Test
	void testGenerarLocalidadNumerada() {
		
		assertEquals(2000,localidad2.getCapacidadMax(), "La capacidad es incorrecta");
	}
	
	@Test
	void testVenderTiquetesNumerados() {
		
		ArrayList<String> array1 = new ArrayList<>(); 
		array1.add("Asiento 1299");
		array1.add("Asiento 1297");
		
		assertEquals(array1, localidad2.venderTiquetes(2));


	}
}
