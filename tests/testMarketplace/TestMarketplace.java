package testMarketplace;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

// Clases del Sistema
import marketplace.Marketplace;
import marketplace.OfertaReventa;
import cliente.Administrador;
import cliente.OrganizadorEventos;
import cliente.UsuarioComprador;
import eventos.Evento;
import eventos.Venue;
import localidades.Localidades;
import localidades.NoNumerada;
import tiquetes.Basico;
import tiquetes.Deluxe;
import tiquetes.Tiquete;

// Excepciones
import excepciones.FondosInsuficientesException;
import excepciones.VenueOcupado;

public class TestMarketplace {

    private Marketplace marketplace;
    
    // Actores
    private UsuarioComprador vendedor;
    private UsuarioComprador comprador;
    private Administrador admin;
    private OrganizadorEventos organizador;
    
    // Objetos del sistema
    private Evento evento;
    private Localidades localidad;
    private Tiquete tiqueteNormal; // Tiquete apto para venta
    private Tiquete tiqueteDeluxe; // Tiquete NO apto (Deluxe)
    private Tiquete tiqueteAjeno;  // Tiquete que no me pertenece

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Inicializar Marketplace (esto crea las tablas en BD también)
        marketplace = new Marketplace();
        
        // 2. Inicializar Usuarios con saldos iniciales
        vendedor = new UsuarioComprador("vendedor", "pass", 100.0);
        comprador = new UsuarioComprador("comprador", "pass", 500.0); // Tiene plata
        admin = new Administrador("admin", "pass");
        organizador = new OrganizadorEventos("org", "pass", 1000.0);
        
        // 3. Crear entorno (Venue, Evento, Localidad)
        Venue venue = new Venue("Arena", "Calle 1", 1000, null, "APROBADO");
        evento = organizador.agregarEvento("E1", "Concierto", "2026-01-01", venue);
        evento.agregarLocalidadNoNumerada("General", 50.0, 100);
        localidad = evento.getLocalidades().get("General");
        
        // 4. Crear Tiquetes y asignarlos
        
        // Tiquete Normal (Dueño: Vendedor)
        tiqueteNormal = new Basico(50.0, 0.0, 0.0, "2026-01-01", vendedor, localidad, evento, "ACTIVO", null);
        vendedor.getTiquetesComprados().add(tiqueteNormal); // Importante: Añadir a su lista
        
        // Tiquete Deluxe (Dueño: Vendedor) - REGLA: No se puede revender
        tiqueteDeluxe = new Deluxe(100.0, 0.0, 0.0, "2026-01-01", vendedor, localidad, evento, "ACTIVO", null);
        vendedor.getTiquetesComprados().add(tiqueteDeluxe);
        
        // Tiquete Ajeno (Dueño: Comprador) - Para probar que vendedor no puede vender lo que no es suyo
        tiqueteAjeno = new Basico(50.0, 0.0, 0.0, "2026-01-01", comprador, localidad, evento, "ACTIVO", null);
        comprador.getTiquetesComprados().add(tiqueteAjeno);
    }

    // --- TESTS DE PUBLICACIÓN ---

    @Test
    public void testPublicarOfertaExitosa() throws Exception {
        // El vendedor publica su tiquete normal por $80.0
        marketplace.publicarOferta(vendedor, tiqueteNormal, 80.0);
        
        assertEquals(1, marketplace.getOfertasActivas().size(), "Debe haber 1 oferta activa.");
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        assertEquals(80.0, oferta.getPrecio(), 0.001);
        assertSame(tiqueteNormal, oferta.getTiquete());
        assertSame(vendedor, oferta.getVendedor());
    }

    @Test
    public void testPublicarFallaSiTiqueteNoEsMio() {
        // Vendedor intenta vender el tiquete del Comprador
        assertThrows(Exception.class, () -> 
            marketplace.publicarOferta(vendedor, tiqueteAjeno, 100.0),
            "Debe fallar si el usuario no es el dueño del tiquete."
        );
    }

    @Test
    public void testPublicarFallaSiEsDeluxe() {
        // Requerimiento: Los Deluxe no se pueden revender
        assertThrows(Exception.class, () -> 
            marketplace.publicarOferta(vendedor, tiqueteDeluxe, 200.0),
            "Debe fallar si el tiquete es Deluxe."
        );
    }
    
    @Test
    public void testPublicarFallaSiTiqueteNoEsActivo() {
        tiqueteNormal.setEstado("USADO");
        assertThrows(Exception.class, () -> 
            marketplace.publicarOferta(vendedor, tiqueteNormal, 50.0),
            "Solo se pueden revender tiquetes ACTIVOS o TRANSFERIDOS."
        );
    }

    // --- TESTS DE COMPRA (TRANSACCIÓN) ---

    @Test
    public void testComprarOfertaExitosa() throws Exception {
        // 1. Publicar oferta (Vendedor vende a 80.0)
        marketplace.publicarOferta(vendedor, tiqueteNormal, 80.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // Saldos iniciales: Vendedor=100, Comprador=500
        
        // 2. Comprador compra la oferta
        marketplace.comprarOferta(comprador, oferta);
        
        // VERIFICACIONES FINANCIERAS
        assertEquals(500.0 - 80.0, comprador.getSaldo(), 0.001, "Al comprador se le debió descontar el precio.");
        assertEquals(100.0 + 80.0, vendedor.getSaldo(), 0.001, "El vendedor debió recibir el dinero.");
        
        // VERIFICACIONES DE PROPIEDAD
        assertSame(comprador, tiqueteNormal.getCliente(), "El dueño del tiquete debió cambiar.");
        
        // VERIFICACIONES DE INVENTARIO (Listas)
        assertFalse(vendedor.getTiquetesComprados().contains(tiqueteNormal), "El tiquete debió salir de la lista del vendedor.");
        assertTrue(comprador.getTiquetesComprados().contains(tiqueteNormal), "El tiquete debió entrar a la lista del comprador.");
        
        // VERIFICACIÓN DE MARKETPLACE
        assertTrue(marketplace.getOfertasActivas().isEmpty(), "La oferta debió desaparecer de las activas.");
    }

    @Test
    public void testComprarFallaPorFondosInsuficientes() throws Exception {
        // Vendedor publica oferta muy cara (1000.0)
        marketplace.publicarOferta(vendedor, tiqueteNormal, 1000.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // Comprador solo tiene 500.0
        assertThrows(FondosInsuficientesException.class, () -> 
            marketplace.comprarOferta(comprador, oferta),
            "Debe lanzar excepción si no hay saldo suficiente."
        );
        
        // Verificar que la oferta sigue ahí
        assertEquals(1, marketplace.getOfertasActivas().size());
    }

    // --- TESTS DE GESTIÓN (ELIMINAR) ---

    @Test
    public void testEliminarOfertaPorElDueno() throws Exception {
        marketplace.publicarOferta(vendedor, tiqueteNormal, 80.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // El dueño se arrepiente y la borra
        marketplace.eliminarOferta(vendedor, oferta);
        
        assertTrue(marketplace.getOfertasActivas().isEmpty(), "La oferta debió ser eliminada.");
        assertFalse(oferta.isActiva());
    }
    
    @Test
    public void testEliminarOfertaPorAdmin() throws Exception {
        marketplace.publicarOferta(vendedor, tiqueteNormal, 80.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // El admin decide borrarla (moderación)
        marketplace.eliminarOferta(admin, oferta);
        
        assertTrue(marketplace.getOfertasActivas().isEmpty(), "El admin debe poder borrar cualquier oferta.");
    }
    
    @Test
    public void testEliminarOfertaFallaPorIntruso() throws Exception {
        marketplace.publicarOferta(vendedor, tiqueteNormal, 80.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // El comprador intenta borrar la oferta del vendedor (sin comprarla)
        assertThrows(Exception.class, () -> 
            marketplace.eliminarOferta(comprador, oferta),
            "Un usuario ajeno no puede borrar ofertas de otros."
        );
    }

    // --- TESTS DE LOGS ---

    @Test
    public void testConsultarLogPermisos() throws Exception {
        // Generar una acción para que haya log
        marketplace.publicarOferta(vendedor, tiqueteNormal, 50.0);
        
        // 1. Admin consulta (Debe funcionar)
        List<String> logs = marketplace.consultarLog(admin);
        assertNotNull(logs);
        assertFalse(logs.isEmpty(), "El log debe tener el registro de la publicación.");
        
        // 2. Mortal consulta (Debe fallar)
        assertThrows(Exception.class, () -> 
            marketplace.consultarLog(vendedor),
            "Solo el admin puede ver el log."
        );
    }
    
    // MAYOR COBERTURA 
    
    @Test
    void testEliminarOfertaFallaSiNoEsDuenoNiAdmin() throws Exception {
        marketplace.publicarOferta(vendedor, tiqueteNormal, 100.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // Intentar borrar con un usuario ajeno (comprador)
        assertThrows(Exception.class, () -> 
            marketplace.eliminarOferta(comprador, oferta),
            "Debe lanzar excepción: 'No tienes permiso para borrar esta oferta.'"
        );
    }
    
    @Test
    void testComprarOfertaFallaSiInactiva() throws Exception {
        marketplace.publicarOferta(vendedor, tiqueteNormal, 100.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // Desactivamos manualmente (simulando que ya se vendió o borró)
        oferta.setActiva(false);
        
        assertThrows(Exception.class, () -> 
            marketplace.comprarOferta(comprador, oferta),
            "Debe lanzar excepción: 'La oferta ya no está disponible.'"
        );
    }
    
    @Test
    void testAceptarContraofertaExitosa() throws Exception {
        marketplace.publicarOferta(vendedor, tiqueteNormal, 100.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // Vendedor acepta una contraoferta de 90.0
        marketplace.aceptarContraoferta(vendedor, comprador, oferta, 90.0);
        
        // Verificar transacción
        assertEquals(500.0 - 90.0, comprador.getSaldo(), 0.001);
        assertEquals(100.0 + 90.0, vendedor.getSaldo(), 0.001);
        assertFalse(oferta.isActiva());
    }

    @Test
    void testAceptarContraofertaFallaSiNoEsVendedor() throws Exception {
        marketplace.publicarOferta(vendedor, tiqueteNormal, 100.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // Comprador intenta "auto-aceptarse" la oferta
        assertThrows(Exception.class, () -> 
            marketplace.aceptarContraoferta(comprador, comprador, oferta, 50.0),
            "Solo el vendedor puede aceptar contraofertas."
        );
    }

    @Test
    void testAceptarContraofertaFallaPorSaldo() throws Exception {
        marketplace.publicarOferta(vendedor, tiqueteNormal, 1000.0); // Muy caro
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // Vendedor acepta, pero comprador solo tiene 500.0
        assertThrows(FondosInsuficientesException.class, () -> 
            marketplace.aceptarContraoferta(vendedor, comprador, oferta, 1000.0)
        );
    }
    
    @Test
    void testTransaccionConOrganizadorComoActor() throws Exception {
        // Escenario: Organizador VENDE, Comprador COMPRA
        // Necesitamos un tiquete que sea del organizador (ej. una cortesía o compra propia)
        Tiquete tiqueteOrg = new Basico(50.0, 0.0, 0.0, "2026", organizador, localidad, evento, "ACTIVO", null);
        organizador.getTiquetesComprados().add(tiqueteOrg);
        
        marketplace.publicarOferta(organizador, tiqueteOrg, 200.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        // Comprador compra
        marketplace.comprarOferta(comprador, oferta);
        
        // Verificar que se actualizó la lista del ORGANIZADOR
        assertFalse(organizador.getTiquetesComprados().contains(tiqueteOrg), "El tiquete debió salir de la lista del organizador.");
        assertEquals(1000.0 + 200.0, organizador.getSaldo(), 0.001);
    }
    
    @Test
    void testTransaccionConOrganizadorComoComprador() throws Exception {
        // Escenario: Vendedor VENDE, Organizador COMPRA
        marketplace.publicarOferta(vendedor, tiqueteNormal, 50.0);
        OfertaReventa oferta = marketplace.getOfertasActivas().get(0);
        
        marketplace.comprarOferta(organizador, oferta);
        
        // Verificar que entró a la lista del ORGANIZADOR
        assertTrue(organizador.getTiquetesComprados().contains(tiqueteNormal), "El tiquete debió entrar a la lista del organizador.");
        assertEquals(1000.0 - 50.0, organizador.getSaldo(), 0.001);
    }
}