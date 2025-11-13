package testCliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cliente.Administrador;
import cliente.OrganizadorEventos;
import cliente.Usuario;
import cliente.UsuarioComprador;
import eventos.Evento;
import eventos.Venue;
import excepciones.AutenticacionFallidaException;
import excepciones.CapacidadExcedidaLocalidad;
import excepciones.FondosInsuficientesException;
import excepciones.TiqueteNoTransferibleException;
import excepciones.VenueOcupado;
import localidades.Localidades;
import localidades.NoNumerada;
import localidades.Numerada;
import tiquetes.Basico;
import tiquetes.Tiquete;

public class TestUsuarioComprador {
	
	private final String PASSWORD = "AMOACR7123";
	private final double SALDO_INICIAL = 200.0;
	private final double PRECIO_BASE_LOC = 50.0;
	private final double PORCENTAJE_SERVICIO = 0.10; // 10%
    private final double COBRO_EMISION = 2.0;
    
    private final double COSTO_UNITARIO_TOTAL = 57.0;
    
    private UsuarioComprador comprador;
    private UsuarioComprador destinatario;
    private OrganizadorEventos organizador;
    private Administrador administrador;
    private List<Usuario> todosLosUsuarios;
    
    private Localidades localidadNoNumerada;
    private Localidades localidadNumerada;
    private Evento evento;
    
    @BeforeEach
    
    void setUp() throws VenueOcupado {
    	
    	comprador = new UsuarioComprador("testuser", PASSWORD, SALDO_INICIAL);
        destinatario = new UsuarioComprador("dest", "destpass", 10.0);
        organizador = new OrganizadorEventos("org", "orgpass", 0.0);
        administrador = new Administrador("admin", "adminpass"); 
        
        todosLosUsuarios = new ArrayList <>();
        todosLosUsuarios.add(comprador);
        todosLosUsuarios.add(destinatario);
        todosLosUsuarios.add(organizador);
        todosLosUsuarios.add(administrador);
        
        Venue venue = new Venue("Lugar", "Ubicacion", 100, null, "APROBADO");
        evento = new Evento("E01", "Show", "2026-11-20", venue, organizador);
        
        Map<String, Boolean> asientos = new HashMap <> ();
        
        asientos.put("A1", false);
        asientos.put("A2", false);
        asientos.put("A3", true);
        
        localidadNumerada = new Numerada (PRECIO_BASE_LOC, 3, "Platino",evento, asientos);
        
        localidadNoNumerada = new NoNumerada (PRECIO_BASE_LOC, 10, "General", evento);

    }
    
    @Test 
    
    void testConstructor () {
    	
    	//Verificar la inicialización de saldo y listas
    	assertEquals(SALDO_INICIAL, comprador.getSaldo(),"El saldo no es el correspondiente");
        assertTrue(comprador.getTiquetesComprados().isEmpty(), "La lista de tiquetes comprados se debe inicializar vacia");
        assertEquals("testuser", comprador.getLogIn(), "El login no corresponde con el asignado");
    }
    
    // Test para compra de tiquete
    
    @Test
    
    void testComprarTiqueteExitosoNumerada() throws CapacidadExcedidaLocalidad, FondosInsuficientesException {
    	
    	int cantidad =2;
    	
    	double costoTotal = COSTO_UNITARIO_TOTAL * cantidad;
    	
    	List<Tiquete> tiquetesComprados = comprador.comprarTiquete(localidadNumerada, cantidad,PORCENTAJE_SERVICIO , COBRO_EMISION);
    	
    	assertEquals (SALDO_INICIAL - costoTotal, comprador.getSaldo(), "El saldo debio haber disminuido el costo de la compra" );
    	
    	assertEquals(2,tiquetesComprados.size(), "Se debieron habber creado 2 tiquetes" );
    	
    	assertEquals (2, comprador.getTiquetesComprados().size(), "Debe haber registrado los tiquetes comprados");
    	
    	assertFalse (localidadNumerada.verificarDisponibilidad(1), "La localidad ya no debe tener asientos libres");
    	
    	assertTrue(tiquetesComprados.get(0) instanceof Basico , "El tiquete creado debe ser basico");
    	
    }
    
    @Test 
    
    void testComprarTiqueteFallaPorCapacidadExcedida () {
    	
    	int cantidad = 3;
    	
    	double saldoAntes = comprador.getSaldo();
    	
    	assertThrows (CapacidadExcedidaLocalidad.class, () ->
    	comprador.comprarTiquete(localidadNumerada, cantidad, PORCENTAJE_SERVICIO, COBRO_EMISION), "Debe fallar por capacidad excedida");
    	
    	assertEquals(saldoAntes, comprador.getSaldo(), "El saldo no debio haber disminuido");
    }
    
    @Test 
    
    void testComprarTiqueteFallaPorSaldoInsuficiente() {
    	
    	int cantidad = 4;
    	
    	double saldoAntes = comprador.getSaldo();
    	
    	assertThrows (FondosInsuficientesException.class, () ->
    	comprador.comprarTiquete(localidadNoNumerada, cantidad, PORCENTAJE_SERVICIO, COBRO_EMISION),
    	"Debe fallar con FondosInsuficientesException si el costo total excede el saldo");
    	
    	assertEquals(saldoAntes, comprador.getSaldo(), "El saldo no debio ser alterado");
    	
    	assertTrue (comprador.getTiquetesComprados().isEmpty(), "No debio haberse comprado ningun tiquete");
    	
    	assertTrue(localidadNoNumerada.verificarDisponibilidad(10), "La localidad debio mantener su capacidad");
    	
    }
    
    // TEST DE REEMBOLSO
    
    @Test
    
    void testPedirRembolsoExitoso () throws CapacidadExcedidaLocalidad, FondosInsuficientesException, TiqueteNoTransferibleException {
    	
    	List<Tiquete> tiquetes = comprador.comprarTiquete(localidadNoNumerada, 1, PORCENTAJE_SERVICIO, COBRO_EMISION);
        Tiquete tiquete = tiquetes.get(0);
        
        comprador.pedirRembolso(tiquete);
        
        assertEquals("PENDIENTE_REEMBOLSO", tiquete.getEstado(), "El estado debe cambiar a PENDIENTE_REEMBOLSO.");

    }
    
    @Test
    
    void testPedirRembolsoFallaSiTiqueteNoLePertenece () throws CapacidadExcedidaLocalidad, FondosInsuficientesException {
    	
    	UsuarioComprador compradorOG = new UsuarioComprador ("BICHOSIU", "123445", 100000);
    	
    	List<Tiquete> tiquetesAjenos = compradorOG.comprarTiquete(localidadNoNumerada, 1, PORCENTAJE_SERVICIO, COBRO_EMISION);
        Tiquete tiqueteAjeno = tiquetesAjenos.get(0);
        
        assertThrows (TiqueteNoTransferibleException.class, () ->
        comprador.pedirRembolso(tiqueteAjeno), "Debe fallar si el tiquete no está en la lista de tiquetes comprados del comprador");

    }
    
    @Test
    void testPedirRembolsoFallaSiEstadoNoEsActivo() throws CapacidadExcedidaLocalidad, FondosInsuficientesException {
        // El tiquete está en estado TRANSFERIDO.
    	
        List<Tiquete> tiquetes = comprador.comprarTiquete(localidadNoNumerada, 1, PORCENTAJE_SERVICIO, COBRO_EMISION);
        Tiquete tiquete = tiquetes.get(0);
        tiquete.setEstado("TRANSFERIDO");

        assertThrows(
            TiqueteNoTransferibleException.class,() -> comprador.pedirRembolso(tiquete),
            "Debe fallar si el estado del tiquete no es ACTIVO.");
    }
    
    // TESTS PARA TRASNFERIR TIQUETE
    
    @Test
    void testTransferenciaExitosa() throws AutenticacionFallidaException, TiqueteNoTransferibleException, Exception {
    	
    	Tiquete tiquete = comprador.comprarTiquete(localidadNoNumerada, 1, PORCENTAJE_SERVICIO, COBRO_EMISION).get(0);
        
        comprador.transferirTiquete(tiquete, PASSWORD, destinatario.getLogIn(), todosLosUsuarios);
        
        assertFalse (comprador.getTiquetesComprados().contains(tiquete),"El tiquete debe ser removido del comprador");
        
        assertTrue (destinatario.getTiquetesComprados().contains(tiquete), "El tiquete debe ser agregado al destinatario");
        
        assertEquals ("TRANSFERIDO", tiquete.getEstado(), "El estado del tiquete debe cambiar");

    }
    
    @Test
    void testTransferenciaFallaPorContrasena() throws AutenticacionFallidaException, TiqueteNoTransferibleException, Exception {
    	
    	Tiquete tiquete = comprador.comprarTiquete(localidadNoNumerada, 1, PORCENTAJE_SERVICIO, COBRO_EMISION).get(0);
        
        assertThrows (AutenticacionFallidaException.class, ()->
        comprador.transferirTiquete(tiquete, "INCORRECT PASS", destinatario.getLogIn(), todosLosUsuarios), 
        "No se puede transferir un tiquete si la autenticacion de la contraseña falla");
        
        assertTrue (comprador.getTiquetesComprados().contains(tiquete), "El tiquete debe seguir siendo del comprador");
    }
    
    @Test
    void testTransferenciaFallaSiDestinatarioEsAdmin() throws CapacidadExcedidaLocalidad, FondosInsuficientesException {
        // 10. Transferir a un administrador (debe estar prohibido)
        Tiquete tiquete = comprador.comprarTiquete(localidadNoNumerada, 1, PORCENTAJE_SERVICIO, COBRO_EMISION).get(0);

        assertThrows(
            TiqueteNoTransferibleException.class,() -> 
            comprador.transferirTiquete(tiquete, PASSWORD, administrador.getLogIn(), todosLosUsuarios),
            "Debe fallar si el destinatario es un Administrador.");
    }
	

}
