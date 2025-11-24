package testCliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import excepciones.OperacionNoAutorizadaException;
import excepciones.TiqueteNoTransferibleException;
import localidades.Localidades;
import localidades.NoNumerada;
import tiquetes.Basico;
import tiquetes.Multiple;
import tiquetes.Tiquete;

public class TestOrganizadorEventos {
	
	private final String LOGIN_ORG = "promotor1";
    private final String PASS_ORG = "pass123";
    private final String FECHA = "2026-12-01";
    private final double PRECIO_BASE = 100.0;
    private final double SALDO_INICIAL = 500.0;
    
    private OrganizadorEventos organizador;
    private OrganizadorEventos otroOrganizador;
    private UsuarioComprador comprador;
    private Administrador administrador; 
    private Venue venuePropio;
    private Evento eventoPropio;
    private Evento eventoAjeno;
    private Localidades localidadGeneral;
    private Localidades localidadVIP;
    private List<Usuario> todosLosUsuarios;
    
    @BeforeEach 
    
    void setUp () throws Exception {
    	
    	organizador = new OrganizadorEventos(LOGIN_ORG, PASS_ORG, SALDO_INICIAL);
        otroOrganizador = new OrganizadorEventos("rival", "pass456", 0.0);
        comprador = new UsuarioComprador("client", "clientpass", 100050.0);
        administrador = new Administrador("admin", "adminpass"); 
        
        todosLosUsuarios = new ArrayList<>(Arrays.asList(organizador, otroOrganizador, comprador, administrador));
        
        venuePropio = new Venue("Estadio", "Centro", 200, null, "APROBADO");
 
        Venue venueAjeno = new Venue("Bar", "Sur", 50, null, "APROBADO"); 
        
        
        //ARREGLO POR YO SOLITOO QUE ORGULLO
        // PARA QUE EL TEST DE testCalcularEstadoFinancieroExhaustivo PUEDA SERVIR SE NECESITA QUE EL EVENTO QUEDE ASOCIADO A LISTA DEL ORG
        // POR TANTO SE DEBE CREAR EL EVENTO A TRAVES DE EL METODO DE agregarEvento PARA QUE SE AÑADA A LA LISTA DEL ORG
        // Y PARA QUE eventoPropio APUNTE AL MISMO OBJETO EN MEMORIA SE USA .get

        organizador.agregarEvento("E1", "Mi Show", FECHA, venuePropio);
        eventoPropio = organizador.getEventosOrganizados().get(0);
        
      
        
        eventoAjeno = new Evento("E2", "Show Rival", "2026-12-02", venueAjeno, otroOrganizador);
        
        eventoPropio.agregarLocalidadNoNumerada("General", PRECIO_BASE, 10);
        eventoPropio.agregarLocalidadNoNumerada("VIP", PRECIO_BASE * 2, 5);
        
        // cambios en localidad en vez de ser una instancia se llama a la localidad por un get
        // para que en memoria apunte al mismo objeto 
        
        localidadGeneral = eventoPropio.getLocalidades().get("General");
        localidadVIP = eventoPropio.getLocalidades().get("VIP");
        
        
        
    	
    }
    
    @Test 
    
    void testCrearOfertaAsignaYPromotorEsCorrecto () {
    	//Verificar que la Oferta se crea con el promotor correcto y se asigna a la localidad.
    	
    	LocalDateTime fechaFin = LocalDateTime.now().plusHours(1);
    	organizador.crearOferta(localidadGeneral, 0.5, fechaFin);
    	
    	assertNotNull (localidadGeneral.getOferta(), "La oferrta debe ser asignada");
    	
    	assertSame (organizador, localidadGeneral.getOferta().getPromotor(), "El promotor de la oferta debe ser el org");

    }
    
    @Test 
    
    void testSugerirVenueCreaPendiente() {
    	
    	Venue sugerido = organizador.sugerirVenue("Club", "Norte", 500, null);
    	
    	assertEquals ("PENDIENTE", sugerido.getEstado(), "Un venue sugerido debe ser PENDIENTE");

    }
    
    //TESTS PARA GENERAR CORTESIAS
    
    @Test 
    
    void testComprarTiqueteGeneraCortesiaExitoso () throws CapacidadExcedidaLocalidad, OperacionNoAutorizadaException {
    	
    	List<Tiquete> tiquetes = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0);
    	
    	Tiquete cortesia = tiquetes.get(0);
    	
    	assertEquals ("CORTESIA", cortesia.getEstado(), "Un tiquete comprado por un Org debe ser cortesia");
    	
    	assertEquals (0.0, cortesia.getCostoServicio(), "El costo de servicio de una cortesia debe ser 0");
    	
    	assertEquals (SALDO_INICIAL, organizador.getSaldo(), "El saldo no debe cambiar por una cortesia");
    	
    	assertTrue (organizador.getTiquetesComprados().contains(cortesia), "El tiquete debe ser agregado a organizador");

    }
    
    @Test 
    
    void testComprarTiqueteFallaEventoAjeno () {
    	//Intentar comprar tiquetes (cortesías) para un evento que no organizó
    	
    	Localidades locAjena = new NoNumerada(50.0, 10, "RivalLoc", eventoAjeno);
    	
    	assertThrows (OperacionNoAutorizadaException.class, ()->
    	organizador.comprarTiquete(locAjena, 1, 0.1, 5.0), "Un org no puede comprar un tiquete de un eveto que no organiza");

    }
    
    //TEST PARA CREAR PASE DE TEMPORADA
    
    @Test 
    
    void testCrearPaquetePaseDeTemporadaExitoso () throws CapacidadExcedidaLocalidad, OperacionNoAutorizadaException {
    	
    	//Crear un paquete con 2 localidades de su propio evento
    	
    	List<Localidades> localidadesPaquete = Arrays.asList(localidadGeneral, localidadVIP);
        double precioPaquete = 350.0;
        
        Multiple paquete = organizador.crearPaquetePaseDeTemporada(localidadesPaquete, precioPaquete);
        
        assertEquals(350.0, paquete.getPrecioFinal(),"El precio del paquete no coincide");

        assertEquals(2, paquete.getTiquetesIncluidos().size(), "Debe incluir dos tiquetes internos.");
        
        // (1 tiquete vendido en cada localidad para el paquete)
        assertEquals(9, localidadGeneral.getCapacidadMax() - ((NoNumerada)localidadGeneral).getTiquetesVendidos(), "Quedan 9 en General.");
        assertTrue(organizador.getTiquetesComprados().contains(paquete), "El paquete se le debe añadir al org");
        
    }
    
   @Test 
   
   void testCrearPaqueteFallaEventoAjeno () {
	//Intentar crear paquete usando una localidad de evento ajeno.
	   
	   Localidades locAjena = new NoNumerada(50.0, 10, "RivalLoc", eventoAjeno);
	   
	   List <Localidades> LocalidadesDPaquete = new ArrayList <> ();
	   
	   LocalidadesDPaquete.add(locAjena);
	   LocalidadesDPaquete.add(localidadGeneral);
	   
	   assertThrows (OperacionNoAutorizadaException.class, ()->
	   organizador.crearPaquetePaseDeTemporada(LocalidadesDPaquete, 1000), "Un org no puede crear un paquete con un evento que no le pertenece");

   }
   
   @Test 
   
   void testCrearPaquetePaseDeTemporadaFallaSiLocalidadNoTieneCapacidad () {
	   
	   localidadVIP.venderTiquetes(5);
	   
	   List<Localidades> LocDPaquete = new ArrayList <> ();
	   
	   LocDPaquete.add(localidadGeneral);
	   LocDPaquete.add(localidadVIP);
	   
	   assertThrows (CapacidadExcedidaLocalidad.class, () ->
	   organizador.crearPaquetePaseDeTemporada(LocDPaquete, 350.0), 
	   "Debe fallar si una de las localidades no tiene el único asiento libre requerido para el paquete");
	   
	   assertEquals (10, localidadGeneral.getCapacidadMax() - ((NoNumerada)localidadGeneral).getTiquetesVendidos(), 
			   			"Debe permanecer la capacidad al no haberse efectuado ningun paquete");
   }
   
   //TESTS PARA PEDIR REEMBOLSO
   
   @Test 
    
   void testPedirRembolsoFallaPorCortesia () throws CapacidadExcedidaLocalidad, OperacionNoAutorizadaException {
	//Tiquetes CORTESIA no se pueden reembolsar.
	   
	 Tiquete cortesia = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
	 
	 assertThrows (TiqueteNoTransferibleException.class, () ->
	 organizador.pedirRembolso(cortesia), "No se puede pedir reembolso en una cortesia");
	   
   }
   
   @Test
   public void testPedirRembolsoFallaSiTiqueteNoLePertenece() throws OperacionNoAutorizadaException, CapacidadExcedidaLocalidad, FondosInsuficientesException {
       // Intenta pedir rembolso de un tiquete que le pertenece al comprador
       Tiquete tiqueteAjeno = comprador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
       
       assertThrows(TiqueteNoTransferibleException.class,() -> organizador.pedirRembolso(tiqueteAjeno),
           "Debe fallar si el tiquete no está en la lista de comprados del organizador");
   }

   @Test
   public void testPedirRembolsoFallaSiEstadoNoEsActivo() throws OperacionNoAutorizadaException, CapacidadExcedidaLocalidad {
       //Verifica que no puede pedir rembolso si el tiquete fue transferido
       Tiquete cortesia = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
       cortesia.setEstado("TRANSFERIDO");
       
       assertThrows(TiqueteNoTransferibleException.class,() -> organizador.pedirRembolso(cortesia),
           "Debe fallar si el estado no es ACTIVO.");
   }
   
   // TEST PARA TRANSFERIR TIQUETE
   
   @Test
   void testTransferenciaExitosa() throws AutenticacionFallidaException, TiqueteNoTransferibleException, Exception {
   	
	   Tiquete cortesia = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
       
	   organizador.transferirTiquete(cortesia, PASS_ORG, comprador.getLogIn(), todosLosUsuarios);
       
       assertFalse (organizador.getTiquetesComprados().contains(cortesia),"El tiquete debe ser removido del comprador");
       
       assertTrue (comprador.getTiquetesComprados().contains(cortesia), "El tiquete debe ser agregado al destinatario");
       
       assertEquals ("TRANSFERIDO", cortesia.getEstado(), "El estado del tiquete debe cambiar");

   }
   
   @Test
   void testTransferenciaFallaPorContrasena() throws AutenticacionFallidaException, TiqueteNoTransferibleException, Exception {
   	
	   Tiquete cortesia = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
       
       assertThrows (AutenticacionFallidaException.class, ()->
       organizador.transferirTiquete(cortesia, "INCORRECT PASS", comprador.getLogIn(), todosLosUsuarios), 
       "No se puede transferir un tiquete si la autenticacion de la contraseña falla");
       
       assertTrue (organizador.getTiquetesComprados().contains(cortesia), "El tiquete debe seguir siendo del comprador");
   }
   
   
   @Test 
   
   void testTransferenciaFallaSiDestinatarioEsAdmin () throws CapacidadExcedidaLocalidad, OperacionNoAutorizadaException {
	   
	   Tiquete cortesia = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
	   
	   assertThrows (TiqueteNoTransferibleException.class, ()->
	   organizador.transferirTiquete(cortesia, PASS_ORG, administrador.getLogIn(), todosLosUsuarios), "No se puede trasnferir un tiquete a un admin");

   }
   
   // TEST PARA REPORTES FINANCIEROS 
   
   @Test
   
   void testCalcularEstadoFinancieroExhaustivo () throws CapacidadExcedidaLocalidad, OperacionNoAutorizadaException {
	   
	   // T1: ACTIVO (Debe contar 100.0) - Localidad General
       Tiquete t1_Activo = new Basico(100.0, 0.1, 5.0, FECHA, comprador, localidadGeneral, eventoPropio, "ACTIVO", null); 
       
       // T2: CORTESIA (Debe contar 0.0 en ganancias) - Localidad VIP (Base 200)
       Tiquete t2_Cortesia = organizador.comprarTiquete(localidadVIP, 1, 0.1, 5.0).get(0);
       
       // T3: REEMBOLSADO (Debe contar 0.0 en ganancias - NUEVA LÓGICA) - Localidad General
       Tiquete t3_Reembolsado = new Basico(100.0, 0.1, 5.0, FECHA, comprador, localidadGeneral, eventoPropio, "REEMBOLSADO", null); 
       
       // T4: TRANSFERIDO (Debe contar 200.0 en ganancias, es una venta pagada) - Localidad VIP
       Tiquete t4_Transferido = new Basico(200.0, 0.1, 5.0, FECHA, comprador, localidadVIP, eventoPropio, "TRANSFERIDO", null);
	   
	   
	   List<Tiquete> todosLosTiquetes = new ArrayList <> () ;
	   
	   todosLosTiquetes.add(t1_Activo);
	   todosLosTiquetes.add(t2_Cortesia);
	   todosLosTiquetes.add(t3_Reembolsado);
	   todosLosTiquetes.add(t4_Transferido);
	   
	   Map <String, Double> reporte = organizador.calcularEstadoFinanciero(todosLosTiquetes);
	   
	   //Ganancia Localidad General: T1(100) + T3(0) = 100.0
	   assertEquals (100, reporte.get("GANANCIA_LOC_General"), "Ganancia General debe ser 100.0 (solo Tiquete ACTIVO)");
	   
	   //Ganancia Localidad VIP: T2(0) + T4(200) = 200.0
	   assertEquals (200.0, reporte.get("GANANCIA_LOC_VIP"),"Ganancia VIP debe ser 200.0 (solo Tiquete TRANSFERIDO).");
	   
	   //Ganancia Global: 100.0 (General) + 200.0 (VIP) = 300.0
	   assertEquals (300.0, reporte.get("GANANCIA_GLOBAL"), "La ganancia global debe ser 300");
	   
	   // Tiquetes vendidos (contados): 4
       // Capacidad Total: 15 (10 General + 5 VIP)
       // Porcentaje Global: (4 / 15) * 100 = 26.666...%
	   
	   double expectedPorc = (4.0 / 15.0) * 100.0;
	   
	   assertEquals (expectedPorc, reporte.get("PORCENTAJE_VENTA_GLOBAL"),0.001, "Porcentaje global debe ser 26.66% basado en 4/15 ventas");

   }
   
   @Test 
   
   void testCalcularEstadoFinancieroConLocalidadesVacias () {
	//Test para asegurar que el cálculo maneja correctamente la división por cero (0/0)
	   
	   OrganizadorEventos orgVacio = new OrganizadorEventos ("empty", "pass", 0.0);
	   
	// Se ejecuta el reporte sobre sus eventos (que está vacío) y una lista de tiquetes vacía.
	   
	   Map<String, Double> reporte = orgVacio.calcularEstadoFinanciero(new ArrayList <> ());
	   
	   assertEquals (0.0, reporte.get("GANANCIA_GLOBAL"), "La ganancia debe ser 0");
	   
	   assertEquals (0.0, reporte.get("PORCENTAJE_VENTA_GLOBAL"), "El porcentaje de venta debe ser 0");
	   
	   
   }
   
// -------------------------------------------------------------------
   //  NUEVOS TESTS PARA SUBIR COBERTURA AL 100%
   // -------------------------------------------------------------------

   @Test
   void testTransferenciaExitosaAOtroOrganizador() throws Exception {
       // Cubre la línea: } else if (destinatario instanceof OrganizadorEventos) {
       
       // 1. Organizador compra una cortesía
       Tiquete cortesia = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
       
       // 2. Transfiere esa cortesía a 'otroOrganizador' (definido en setUp)
       organizador.transferirTiquete(cortesia, PASS_ORG, otroOrganizador.getLogIn(), todosLosUsuarios);
       
       // 3. Verificaciones
       assertFalse(organizador.getTiquetesComprados().contains(cortesia), "El tiquete debe salir del origen");
       assertTrue(otroOrganizador.getTiquetesComprados().contains(cortesia), "El tiquete debe llegar al otro organizador");
       assertEquals("TRANSFERIDO", cortesia.getEstado());
       assertSame(otroOrganizador, cortesia.getCliente());
   }

   @Test
   void testTransferenciaFallaUsuarioNoEncontrado() {
       // Cubre la línea: if (destinatario == null) { throw new Exception(...) }
       
       // 1. Organizador tiene un tiquete
       Tiquete cortesia = null;
       try {
            cortesia = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
       } catch (Exception e) { fail("No debió fallar la compra inicial"); }

       Tiquete tiqueteFinal = cortesia; // Variable final o efectivamente final para la lambda
       
       // 2. Intentar transferir a un usuario que NO está en la lista 'todosLosUsuarios'
       assertThrows(Exception.class, () -> 
           organizador.transferirTiquete(tiqueteFinal, PASS_ORG, "usuario_fantasma", todosLosUsuarios),
           "Debe lanzar Exception genérica si el usuario no se encuentra en la lista"
       );
   }

   @Test
   void testComprarTiqueteCortesiaFallaPorCapacidad() throws CapacidadExcedidaLocalidad {
       // Cubre la línea: if (!localidad.verificarDisponibilidad(cantidad)) dentro del bloque de cortesía
       
       // 1. Llenar la localidad VIP (Capacidad 5)
       // Simulamos que se vendieron todos los puestos
       localidadVIP.venderTiquetes(5); 
       
       // 2. Intentar generar una cortesía en esa localidad llena
       assertThrows(CapacidadExcedidaLocalidad.class, () ->
           organizador.comprarTiquete(localidadVIP, 1, 0.1, 5.0),
           "No se pueden generar cortesías si la localidad está llena"
       );
   }

   @Test
   void testCalcularEstadoFinancieroIgnoraTiquetesAjenos() throws Exception {
       // Cubre la lógica implícita del bucle donde el tiquete no coincide con la localidad del organizador
       
       // 1. Tiquete propio (Cortesía)
       Tiquete tiquetePropio = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
       
       // 2. Tiquete AJENO (De otro evento, creado manualmente para simular la lista global)
       // Usamos 'eventoAjeno' que pertenece a 'otroOrganizador'
       Localidades locAjena = new NoNumerada(500.0, 100, "Ajena", eventoAjeno);
       Tiquete tiqueteAjeno = new Basico(500.0, 0.1, 5.0, FECHA, comprador, locAjena, eventoAjeno, "ACTIVO", null);
       
       List<Tiquete> listaMixta = new ArrayList<>();
       listaMixta.add(tiquetePropio);
       listaMixta.add(tiqueteAjeno);
       
       // 3. Calcular reporte
       Map<String, Double> reporte = organizador.calcularEstadoFinanciero(listaMixta);
       
       // 4. Verificar que la ganancia global sea 0.0 (Propio es cortesía=0, Ajeno=Ignorado)
       // Si sumara el ajeno, daría 500.0. Si funciona bien, da 0.0.
       assertEquals(0.0, reporte.get("GANANCIA_GLOBAL"), 0.001, "No debe sumar ganancias de tiquetes que no son de sus eventos");
   }
   

   @Test 
   
   void testPedirRembolsoCambiaEstado() throws CapacidadExcedidaLocalidad, OperacionNoAutorizadaException, TiqueteNoTransferibleException {
	   
	   Tiquete cortesia = organizador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
	   
	  cortesia.setEstado("ACTIVO");
	  
	  organizador.pedirRembolso(cortesia);
	  
	  
	  assertEquals("PENDIENTE_REEMBOLSO", cortesia.getEstado(), "Si un org tiene un tiquete activo (que no deberia pasar),"
	  		+ "y pide la cortesia esta debe cambiar de estado");
	  
	   
	   
	   
   }
   
   @Test 
   
   void testTransferirTiqueteFallaSiNoContieneTiquete () throws AutenticacionFallidaException, TiqueteNoTransferibleException, Exception {
	   
	   Tiquete tiqueteAjeno = comprador.comprarTiquete(localidadGeneral, 1, 0.1, 5.0).get(0);
	   
	   
	   
	   assertThrows (TiqueteNoTransferibleException.class, ()->
	   organizador.transferirTiquete(tiqueteAjeno, PASS_ORG, "client", todosLosUsuarios), 
	   "No se debe transferir un tiquete que no te perteneces");
	   
   }
    	
    	
    
    
}
