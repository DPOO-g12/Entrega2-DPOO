package localidades;

import eventos.Oferta;
import java.util.List;
import java.util.ArrayList; 
import eventos.Evento;

public abstract class Localidades { // O public class si ya le quitamos el abstract
    private int id_localidad;
    private double precio;
    private int capacidadMax; 
    private String nombreLocalidad;
    private Oferta oferta;
    private Evento evento;
    private int ticketsVendidos = 0; 

    // --- NUEVOS ATRIBUTOS PARA CONFIGURACIÓN DELUXE (ESTO ES LO QUE TE FALTA) ---
    private boolean deluxeHabilitado = false;
    private int deluxeLimite = 0;
    private int deluxeVendidos = 0;
    private double deluxePrecioExtra = 0.0;
    private String deluxeBeneficios = "";
    // --------------------------------------------------------------------------

    public Localidades(double precio, int capacidadMax, String nombreLocalidad, Evento evento) {
        this.id_localidad = -1;
        this.precio = precio;
        this.capacidadMax = capacidadMax;
        this.nombreLocalidad = nombreLocalidad;
        this.oferta = null;
        this.evento = evento;
        this.ticketsVendidos = 0;
    }

    // Métodos abstractos (o concretos si ya los cambiaste)
    public abstract boolean verificarDisponibilidad(int cantidad);
    public abstract List<String> venderTiquetes(int cantidad) throws excepciones.CapacidadExcedidaLocalidad;

    // --- GETTERS Y SETTERS BÁSICOS ---
    public double getPrecioFinal() {
        if (this.oferta == null || !this.oferta.isOfertaValida()) {
            return this.precio;
        }
        double porcentajeDescuento = this.oferta.getDescuento(); 
        double precioConDescuento = this.precio * (1.0 - porcentajeDescuento);
        return Math.max(0.0, precioConDescuento); 
    }

    public double getPrecio() { return precio; }
    public int getCapacidadMax() { return capacidadMax; }
    public String getNombreLocalidad() { return nombreLocalidad; }
    public Oferta getOferta() { return oferta; }
    public Evento getEvento() { return evento; }
    public int getIdLocalidad() { return id_localidad; }
    public int getTicketsVendidos() { return ticketsVendidos; }

    public void setOferta(Oferta oferta) { this.oferta = oferta; }
    public void setIdLocalidad(int id_localidad) { this.id_localidad = id_localidad; }
    public void setTicketsVendidos(int vend) { this.ticketsVendidos = vend; }
    public void setEvento(Evento e) { this.evento = e; } // Setter útil

    // ========================================================================
    //   ¡AQUÍ ESTÁN LOS MÉTODOS QUE TE FALTAN! (Cópialos)
    // ========================================================================

    public boolean isDeluxeHabilitado() { return deluxeHabilitado; }
    public void setDeluxeHabilitado(boolean b) { this.deluxeHabilitado = b; }

    public int getDeluxeLimite() { return deluxeLimite; }
    public void setDeluxeLimite(int i) { this.deluxeLimite = i; }

    public int getDeluxeVendidos() { return deluxeVendidos; }
    public void setDeluxeVendidos(int i) { this.deluxeVendidos = i; }

    public double getDeluxePrecioExtra() { return deluxePrecioExtra; }
    public void setDeluxePrecioExtra(double d) { this.deluxePrecioExtra = d; }

    public String getDeluxeBeneficios() { return deluxeBeneficios; }
    public void setDeluxeBeneficios(String s) { this.deluxeBeneficios = s; }
    
    // Método auxiliar para saber si queda cupo Deluxe
    public boolean hayCupoDeluxe(int cantidad) {
        return deluxeHabilitado && (deluxeVendidos + cantidad <= deluxeLimite);
    }
}