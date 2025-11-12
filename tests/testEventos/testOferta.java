package testEventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.OrganizadorEventos;
import eventos.Oferta;

public class testOferta {
	
	private OrganizadorEventos promotor;
	
	private final LocalDateTime AHORA = LocalDateTime.now();
	private final LocalDateTime FECHA_FUTURA = AHORA.plusDays(7);
	private final LocalDateTime  FECHA_PASADA = AHORA.minusDays(1);
	
	@BeforeEach 
	
	void setUp() {
		
		promotor = new OrganizadorEventos ("ORG3", "pasword3", 0);
	}
	
	@Test 
	
	void testConstructorYGetters () {
		
		Oferta oferta = new Oferta (true, 0.4, FECHA_FUTURA, promotor);
		
		assertTrue(oferta.getActivo(), "La oferta debe estar activa");
		assertEquals (0.4, oferta.getDescuento(), "El descuento debe ser 0.4");
		
		assertEquals (FECHA_FUTURA, oferta.getFechaFinDescuento(), "La fecha de fin del desceunto es incorrecta");

	}
	
	// TESTS PARA ISOFERTAVALIDA
	
	@Test 
	
	void testOfertaValidaYActiva () {
		
		Oferta oferta = new Oferta (true, 0.1, FECHA_FUTURA, promotor);
		assertTrue(oferta.isOfertaValida(), "La oferta debe ser valida");

	}
	
	@Test 
	
	void testOfertaInvalidaPorFechaVencida() {
		//Escenario de expiración: Activa, pero la fecha de fin ya pasó.
		
		Oferta oferta = new Oferta (true, 0.1, FECHA_PASADA, promotor);
		
		assertFalse (oferta.isOfertaValida(), "LA OFERTA YA PASO DE TIEMPO");

	}
	
	@Test 
	
	void testOfertaInvalidaPorEstadoInactivo () {
		//Escenario de desactivación: Fecha en el futuro, pero estado 'activo' en false.
		
		Oferta oferta = new Oferta (false, 0.1,FECHA_FUTURA, promotor );
		
		assertFalse(oferta.isOfertaValida(), "LA OFERTA ESTA INACTIVA");
		
	}
	
	@Test 
	
	void testOfertaInvalidaPorEstadoYFecha () {
		// 5. Escenario de doble invalidez: Inactiva y vencida.
		
		Oferta oferta = new Oferta (false, 0.1,FECHA_PASADA, promotor );
		
		assertFalse (oferta.isOfertaValida(), "LA OFERTA ES DOBLEMENTE INVALIDA");
		
		
		
		
	}
	
	
	
	
	

}
