package testMarketplace;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

// Clases Reales
import marketplace.OfertaReventa;
import cliente.UsuarioComprador;
import cliente.OrganizadorEventos;
import tiquetes.Basico;
import tiquetes.Tiquete;
import eventos.Evento;
import eventos.Venue;
import localidades.NoNumerada;
import excepciones.VenueOcupado;

public class TestOfertaReventa {

    private UsuarioComprador vendedor;
    private Tiquete tiquete;
    
    // Dependencias para crear un tiquete válido
    private OrganizadorEventos organizador;
    private Venue venue;
    private Evento evento;
    private NoNumerada localidad;

    @BeforeEach
    public void setUp() throws VenueOcupado, Exception {
        // 1. Crear Actores
        vendedor = new UsuarioComprador("vendedor_juan", "pass123", 100.0);
        organizador = new OrganizadorEventos("org_fest", "passOrg", 1000.0);
        
        // 2. Crear entorno del evento
        venue = new Venue("Arena Test", "Calle 100", 500, null, "APROBADO");
        
        // Usamos agregarEvento para mantener la consistencia que arreglamos antes
        evento = organizador.agregarEvento("E001", "Concierto Test", "2026-01-01", venue);
        
        // 3. Crear localidad y obtener referencia real
        evento.agregarLocalidadNoNumerada("General", 50.0, 100);
        localidad = (NoNumerada) evento.getLocalidades().get("General");
        
        // 4. Crear el tiquete que se va a revender
        // Importante: El dueño inicial es el 'vendedor'
        tiquete = new Basico(50.0, 0.1, 5.0, "2026-01-01", vendedor, localidad, evento, "ACTIVO", null);
    }

    @Test
    public void testConstructorEInicializacion() {
        double precioReventa = 80.0;
        
        // Crear la oferta
        OfertaReventa oferta = new OfertaReventa(tiquete, precioReventa);
        
        // Verificaciones
        assertSame(tiquete, oferta.getTiquete(), "El tiquete debe ser el mismo.");
        assertEquals(precioReventa, oferta.getPrecio(), 0.001, "El precio de reventa debe ser el asignado.");
        
        // Lógica clave: El vendedor de la oferta debe ser el dueño actual del tiquete
        assertSame(vendedor, oferta.getVendedor(), "El vendedor debe ser tomado automáticamente del dueño del tiquete.");
        
        // Estado inicial
        assertTrue(oferta.isActiva(), "La oferta debe nacer activa.");
    }

    @Test
    public void testSetters() {
        OfertaReventa oferta = new OfertaReventa(tiquete, 80.0);
        
        // 1. Cambiar precio
        oferta.setPrecio(75.0);
        assertEquals(75.0, oferta.getPrecio(), 0.001, "El precio debe actualizarse.");
        
        // 2. Cambiar estado (ej. si se cancela o vende)
        oferta.setActiva(false);
        assertFalse(oferta.isActiva(), "El estado debe cambiar a inactivo.");
    }
}