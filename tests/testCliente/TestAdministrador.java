package testCliente;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.Administrador;
import cliente.OrganizadorEventos;
import cliente.UsuarioComprador;
import eventos.Evento;
import eventos.Venue;
import excepciones.VenueOcupado;
import localidades.Localidades;
import tiquetes.Basico;
import tiquetes.Tiquete;

public class TestAdministrador {
	
	private Administrador administrador;
	private OrganizadorEventos organizador1;
	private OrganizadorEventos organizador2;
	private UsuarioComprador comprador;
	
	private List<Tiquete> todosLosTiquetes;
	private List<Evento> todosLosEventos;
	
	private Venue venuePendiente;
	private Venue venueAprobado;
	private Evento eventoA_Org1;
	private Evento eventoB_Org2;
	
	private Localidades localidadA;
    private Localidades localidadB;
    
    private Tiquete tiqueteActivo;
    private Tiquete tiqueteTransferido;
    private Tiquete tiquetePendiente;
    private Tiquete tiqueteCortes;
    private Tiquete tiqueteReembolsado;
	
	
	
	@BeforeEach 
	
	void setUp () throws VenueOcupado, Exception {
		
		administrador = new Administrador("admin", "adminpass");
        organizador1 = new OrganizadorEventos("org1", "pass1", 1000.0);
        organizador2 = new OrganizadorEventos("org2", "pass2", 1000.0);
        comprador = new UsuarioComprador("client1", "pass", 500.0); 
        
        // Inicializar Venues
        venuePendiente = new Venue("Pista", "Sur", 500, null, "PENDIENTE");
        venueAprobado = new Venue("Estadio", "Norte", 1000, null, "APROBADO");
        
        // Inicializar Eventos
        
        organizador1.agregarEvento("EA", "Festival A", "2027-01-01", venueAprobado);
        eventoA_Org1 = organizador1.getEventosOrganizados().get(0);
        
        organizador2.agregarEvento("EB", "Festival B", "2027-01-02", venueAprobado);
        eventoB_Org2 = organizador2.getEventosOrganizados().get(0);
        
        todosLosEventos = new ArrayList <>();
        
        todosLosEventos.add(eventoA_Org1);
        todosLosEventos.add(eventoB_Org2);
        
        // Inicializar Localidades
        
        eventoA_Org1.agregarLocalidadNoNumerada("General", 100.0, 100);
        localidadA = eventoA_Org1.getLocalidades().get("General");
        
        eventoB_Org2.agregarLocalidadNoNumerada("General", 200.0, 100);
        localidadB = eventoB_Org2.getLocalidades().get("General");
        
     // Inicializar Tiquetes
        tiqueteActivo = new Basico(100.0, 0.1, 5.0, "2027-01-01", comprador, localidadA, eventoA_Org1, "ACTIVO", null);
        tiqueteTransferido = new Basico(200.0, 0.1, 5.0, "2027-01-02", comprador, localidadB, eventoB_Org2, "TRANSFERIDO", null);
        tiquetePendiente = new Basico(100.0, 0.1, 5.0, "2027-01-01", comprador, localidadA, eventoA_Org1, "PENDIENTE_REEMBOLSO", null);
        tiqueteCortes = new Basico(100.0, 0.0, 0.0, "2027-01-01", organizador1, localidadA, eventoA_Org1, "CORTESIA", null);
        tiqueteReembolsado = new Basico(100.0, 0.1, 5.0, "2027-01-01", comprador, localidadA, eventoA_Org1, "REEMBOLSADO", null);
        
        todosLosTiquetes  = new ArrayList <> ();
        
        todosLosTiquetes.add(tiqueteActivo);
        todosLosTiquetes.add(tiqueteTransferido);
        todosLosTiquetes.add(tiquetePendiente);
        todosLosTiquetes.add(tiqueteCortes);
        todosLosTiquetes.add(tiqueteReembolsado);

	}
	
	// Tests para Gestión de Costos
	
	@Test 
	
	void testGestionDeCostos () {
		
		administrador.fijarCobroPorEmision(10.0);
		administrador.setPorcentajesServiciosTipoEvento("Concierto", 0.15);
		
		assertEquals (10.0, administrador.getCobroPorEmision(), "El cobro por emision debe ser el fijado");
		
		assertEquals (0.15, administrador.getPorcentajesServiciosTipoEvento().get("Concierto"), "");
		
	}
	
	// Tests para Gestión de Venues
	
	@Test
	
	void testGestionDeVenues () {
		
		Venue vCreado = administrador.crearVenue("Teatro", "Oeste", 300, null);
		assertEquals ("APROBADO", vCreado.getEstado(), "Un venue creado por un admin debe ser automaticamente Aprobado");
		
		
		administrador.aprobarVenue(venuePendiente);
		
		assertEquals ("APROBADO", venuePendiente.getEstado(), "Si un admin aprueba un venue debe cambiar el estado");
		
		administrador.rechazarVenue(venueAprobado);
		
		assertEquals ("PENDIENTE", venueAprobado.getEstado(), "Un venue rechazado por un admin debe ser PENDIENTE");
		
	}
	
	// Tests para gestionarReembolso
	
	@Test 
	
	void testGestionarReembolsoAprobado () {
		
		// Aprobar un tiquete PENDIENTE_REEMBOLSO
		
		double saldoAntes = comprador.getSaldo();
		double reembolsoEsperado = tiquetePendiente.getCostoServicio() + tiquetePendiente.getPrecioBase();
		
		administrador.gestionarReembolso(tiquetePendiente, true);
		
		assertEquals ("REEMBOLSADO", tiquetePendiente.getEstado(), "Un tiquete debe cambiar de estado si fue reembolsado");
		assertEquals (saldoAntes + reembolsoEsperado, comprador.getSaldo(), "El saldo del comprador debe aumentar" );

	}
	
	@Test 
	
	void testGestionarReembolsoRechazado () {
		// Rechazar un tiquete PENDIENTE_REEMBOLSO
		
		double saldoAntes = comprador.getSaldo();
		
		administrador.gestionarReembolso(tiquetePendiente, false);
		
		assertEquals ("ACTIVO", tiquetePendiente.getEstado(), "El estado del tiquete debe regresar a ACTIVO");
		
		assertEquals (saldoAntes , comprador.getSaldo(), "El saldo del comprador no debe cambiar");

	}
	
	@Test 
	
	void testGestionarReembolsoIgnoraTiquetesInvalidos () {
		//Intentar gestionar un tiquete ACTIVO (debe ser ignorado)
		
		administrador.gestionarReembolso(tiqueteActivo, true);
		assertEquals("ACTIVO", tiqueteActivo.getEstado(), "El estado no debe cambiar si no estaba PENDIENTE.");

	}
	
	// Tests para cancelarEvento (Reembolso Masivo) 
	
	@Test 
	
	void testCancelarEventoPorAdmin () {
		
		// Cancelación iniciada por Admin (Reembolsa Base + Servicio)
		
		double saldoAntes = comprador.getSaldo();
		double reembolsoEsperado = tiqueteActivo.getPrecioBase() + tiqueteActivo.getCostoServicio();
		
		administrador.cancelarEvento(eventoA_Org1, todosLosTiquetes, true); // true = admin quien cancela
		
		assertEquals("CANCELADO", eventoA_Org1.getEstado(), "El evento debe estar CANCELADO");
		assertEquals ("REEMBOLSADO", tiqueteActivo.getEstado(), "El tiquete ACTIVO debe ser REEMBOLSADO");
		
		assertEquals (reembolsoEsperado + saldoAntes, comprador.getSaldo(), "El comprador debe recibir Base + Servicio");

	}
	
	@Test 
	
	void testCancelarEventoPorOrganizador () {
		//Cancelación iniciada por Organizador (Reembolsa solo Base)
		
		double saldoAntes = comprador.getSaldo();
		double reembolsoEsperado = tiqueteActivo.getPrecioBase();
		
		administrador.cancelarEvento(eventoA_Org1, todosLosTiquetes, false); // false = org quien cancela
		
		assertEquals("CANCELADO", eventoA_Org1.getEstado(), "El evento debe estar CANCELADO");
		assertEquals ("REEMBOLSADO", tiqueteActivo.getEstado(), "El tiquete ACTIVO debe ser REEMBOLSADO");
		
		assertEquals (reembolsoEsperado + saldoAntes, comprador.getSaldo(), "El comprador debe recibir solo Base");
	}
	
	// Tests para calcularGanancias (Reporte Financiero) 
	
	@Test 
	
	void testCalcularGananciasExhaustivo () {
		
		Map <String, Double> reporte = administrador.calcularGanancias(todosLosTiquetes, todosLosEventos);
		
		// tiqueteActivo (Evento A, Org 1): Serv=10, Emi=5. GANANCIA = 15
        // tiqueteTransferido (Evento B, Org 2): Serv=20, Emi=5. GANANCIA = 25
        // tiquetePendiente (Evento A, Org 1): Ignorado (no es ACTIVO/TRANSFERIDO)
        // tiqueteCortes (Evento A, Org 1): Ignorado (CORTESIA)
        // tiqueteReembolsado (Evento A, Org 1): Ignorado (REEMBOLSADO)
		
		assertEquals(40.0, reporte.get("GANANCIA_TOTAL_TIQUETERA"), 0.001, "Ganancia total debe ser 40.0.");
        
        assertEquals(15.0, reporte.get("GANANCIA_EVT_Festival A"), 0.001, "Ganancia Evento A debe ser 15.0.");
        assertEquals(25.0, reporte.get("GANANCIA_EVT_Festival B"), 0.001, "Ganancia Evento B debe ser 25.0.");
       
        assertEquals(15.0, reporte.get("GANANCIA_PROMOTOR_org1"), 0.001, "Ganancia Promotor 1 debe ser 15.0.");
        assertEquals(25.0, reporte.get("GANANCIA_PROMOTOR_org2"), 0.001, "Ganancia Promotor 2 debe ser 25.0.");
		
        
		
	}
	
	
	
	

}
