package testTiquetes;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import tiquetes.Basico;
import tiquetes.Multiple;
import tiquetes.Tiquete;

public class TestMultiple {

	private final double PRECIO_PAQUETE = 1000;
	private final String FECHA = "2026-12-10";
	
	private OrganizadorEventos clienteOriginal;
	private OrganizadorEventos clienteDestinatario;
	
	private List<Tiquete> tiquetesInternos;
	private List<Evento> eventosAsociados;
	
	private Tiquete tiqueteBase1;
	private Tiquete tiqueteBase2;
	private Evento evento1;
	private Evento evento2;
	
	@BeforeEach
	
	void setUp () throws VenueOcupado {
		
		clienteOriginal = new OrganizadorEventos("userOriginal", "pass", 100.0);
        clienteDestinatario = new OrganizadorEventos("userDestino", "pass", 50.0);
        
        Venue venue1 = new Venue("Lugar 1", "Ubicacion 1", 100, null, "APROBADO"); // 🛠️ NUEVO VENUE
        // Venue para Evento 2 (SEPARADO)
        Venue venue2 = new Venue("Lugar 2", "Ubicacion 2", 100, null, "APROBADO"); // 🛠️ NUEVO VENUE
        
        // Usamos Venue1 para Evento1
        evento1 = new Evento("E01", "Show 1", FECHA, venue1, clienteOriginal); 
        
        // Usamos Venue2 para Evento2
        evento2 = new Evento("E02", "Show 2", FECHA, venue2, clienteOriginal);
        
        tiqueteBase1 = new Basico(50.0, 0.0, 0.0, FECHA, clienteOriginal, null, evento1, "ACTIVO", "A-1");
        tiqueteBase2 = new Basico(75.0, 0.0, 0.0, FECHA, clienteOriginal, null, evento2, "ACTIVO", "B-2");
        
        tiquetesInternos = new ArrayList <> ();
        
        tiquetesInternos.add(tiqueteBase1);
        tiquetesInternos.add(tiqueteBase2);
        
        eventosAsociados = new ArrayList <> ();
        
        eventosAsociados.add(evento1);
        eventosAsociados.add(evento2);
	}
	
	@Test 
	
	void testConstructor() {
		
		Multiple paquete = new Multiple (PRECIO_PAQUETE, FECHA,clienteOriginal, "ACTIVO", tiquetesInternos,eventosAsociados);
		
		assertEquals ("MULTIPLE", paquete.getTipoTiquete(), "El tipo debe ser multiple");
		// 🛠️ CORRECCIÓN 1: Agregar el delta (0.001) para la comparación de 'double'.
		assertEquals (PRECIO_PAQUETE, paquete.getPrecioFinal(), 0.001, "El precio final debe ser el precio del paquete");
		
		// 🛠️ CORRECCIÓN 1: Agregar el delta (0.001) para la comparación de 'double'.
		assertEquals (0.0, paquete.getCostoServicio(), 0.001, "Un paquete no debe tener costo de servicio");
		// 🛠️ CORRECCIÓN 1: Agregar el delta (0.001) para la comparación de 'double'.
		assertEquals (0.0, paquete.getCostoEmision(), 0.001, "Un paquete no debe tener costo de emision");
		assertSame (clienteOriginal, paquete.getCliente(), "El cliente debe ser el asigando");
		
		assertEquals (2, paquete.getEventosAsociados().size(), "Debe tener la cantidad de eventos del paquete");
		assertEquals (2, paquete.getTiquetesIncluidos().size(), "Debe tener la cantidad de tiquetes del paquete");
	}
	
	@Test 
	
	void testConstructorConNulo () {
		//Verificar que las listas nulas se manejan creando ArrayLists vacías.
		
		Multiple paquete = new Multiple(PRECIO_PAQUETE, FECHA, clienteOriginal, "ACTIVO", 
                null, null);
		
		assertNotNull (paquete.getEventosAsociados(), "La lista de eventos asociados no debe ser nula");
		assertTrue (paquete.getEventosAsociados().isEmpty(), "La lista de eventos debe estar vacia");
		
		assertNotNull (paquete.getTiquetesIncluidos(), "La lista de tiquetes no debe ser nula");
		assertTrue (paquete.getTiquetesIncluidos().isEmpty(), "La lista de tiquetes debe estar vacia");
	}
	
	//TEST DE TRANSFERENCIA
	
	@Test 
	
	void testTransferenciaPaqueteCompletoExitoso () throws TiqueteNoTransferibleException {
		
		Multiple paquete = new Multiple(PRECIO_PAQUETE, FECHA, clienteOriginal, "ACTIVO", tiquetesInternos, eventosAsociados);
		
		paquete.transferirTiquete(clienteDestinatario);
		
		assertEquals ("TRANSFERIDO", paquete.getEstado(), "El estado del paquete debe ser trasnferido");
		
		assertSame(clienteDestinatario, tiqueteBase1.getCliente(), "El tiquete se debio haber trasnferido");
		assertEquals ("TRANSFERIDO", tiqueteBase2.getEstado(), "El estado del tiquete debio cambiar");

	}
	
	@Test 
	
	void testTransferenciaPaqueteFallaSiTiqueteInternoVencido () {
		
		tiqueteBase1.setEstado("VENCIDO");
		
		Multiple paquete = new Multiple(PRECIO_PAQUETE, FECHA, clienteOriginal, "ACTIVO",tiquetesInternos, eventosAsociados);
		
		assertThrows (TiqueteNoTransferibleException.class, () ->
		paquete.transferirTiquete(clienteDestinatario), "Debe fallar si cualquier tiquete interno esta vencido");
		
		assertEquals ("ACTIVO", paquete.getEstado(), "El estado del paquete no debe cambiar");
		
	}
	
	@Test 
	
	void testTransferenciaPaqueteFallaSiHijoYaTransferidoIndividualmente() throws TiqueteNoTransferibleException {
        // Fallo: Un tiquete interno ya ha sido transferido individualmente antes de la transferencia del paquete
		
		tiqueteBase1.transferirTiquete(clienteDestinatario);
		
		Multiple paquete = new Multiple(PRECIO_PAQUETE, FECHA, clienteOriginal, "ACTIVO", tiquetesInternos, eventosAsociados);
	
		OrganizadorEventos clienteTercero = new OrganizadorEventos("userTercero", "pass", 10.0);
		
		assertThrows (TiqueteNoTransferibleException.class, () -> 
		paquete.transferirTiquete(clienteTercero), "Debe fallar si cualquier tiquete ya ha sido transferido");
	}
	
	// TEST PARA LOGICA DE TRANSFERENCIA INDIVIDUAL
	
	@Test
	
	void testTransferenciaIndividualDeHijoExitosa () throws TiqueteNoTransferibleException {
		
		Multiple paquete = new Multiple(PRECIO_PAQUETE, FECHA, clienteOriginal, "ACTIVO", tiquetesInternos, eventosAsociados);
		
		String idHijo = tiqueteBase1.getIdTiquete();
		
		paquete.transferirUnoDeMisTiquetes(idHijo, clienteDestinatario);
		
		assertEquals ("TRANSFERIDO", tiqueteBase1.getEstado(), "El tiquete trasnferido debe cambiar su estado");
		
		assertSame (clienteDestinatario, tiqueteBase1.getCliente(), "El cliente del tiquete transferido debio cambiar" );
		
		assertSame (clienteOriginal, paquete.getCliente(), "El dueño del paquete no debio cambiar");
		
		// 🛠️ CORRECCIÓN 2: Ajuste el mensaje de error para ser preciso: NO debe cambiar.
		assertEquals("ACTIVO", tiqueteBase2.getEstado(), "El otro tiquete hijo NO debe cambiar su estado, debe seguir ACTIVO.");
		                
		assertEquals("ACTIVO", paquete.getEstado(), "El paquete padre NO debe cambiar su estado si un hijo es transferido individualmente.");
	}
	
	@Test 
	
	void testTransferenciaIndividualFallaSiPaquetePadreTransferido () throws TiqueteNoTransferibleException {
		//Fallo: No se puede transferir un hijo individual si el paquete padre ya se transfirió.
		
		Multiple paquete = new Multiple(PRECIO_PAQUETE, FECHA, clienteOriginal, "ACTIVO",tiquetesInternos, eventosAsociados);
		
		paquete.transferirTiquete(clienteDestinatario);
		
		assertThrows (TiqueteNoTransferibleException.class, () -> 
		paquete.transferirUnoDeMisTiquetes(tiqueteBase1.getIdTiquete(), clienteOriginal), "No se puede trasnferir un tiquete ind. si el paquete al que pertenece ya se trasnfirio");
	}

	@Test 
	
	void testTransferenciaIndividualFallaSiTiqueteNoExiste () {
		//Fallo: El ID de tiquete hijo no pertenece al paquete.
		
		
		Multiple paquete = new Multiple(PRECIO_PAQUETE, FECHA, clienteOriginal, "ACTIVO", 
                tiquetesInternos, eventosAsociados);
		
		assertThrows (TiqueteNoTransferibleException.class, () ->
		paquete.transferirUnoDeMisTiquetes("NO EXISTENTE", clienteDestinatario), "No se puede transferir un tiquete que no tengas");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
