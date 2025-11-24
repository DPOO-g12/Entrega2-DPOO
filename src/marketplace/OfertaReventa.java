package marketplace;

import java.util.ArrayList;
import java.util.List;
import cliente.Usuario;
import tiquetes.Tiquete;

public class OfertaReventa {
    
    private Tiquete tiquete;
    private Usuario vendedor;
    private double precio;
    private boolean activa;
    // Guardamos las contraofertas como un par: Usuario y Monto. 
    // Podrías crear una clase 'Contraoferta', pero para simplificar usaremos un mapa o lógica interna.
    private List<String> historialContraofertas; // Solo para visualización o log interno

    public OfertaReventa(Tiquete tiquete, double precio) {
        this.tiquete = tiquete;
        this.vendedor = tiquete.getCliente(); // El vendedor es el dueño actual
        this.precio = precio;
        this.activa = true;
        this.historialContraofertas = new ArrayList<>();
    }

    public Tiquete getTiquete() { 
    	return tiquete; 
    }
    
    public Usuario getVendedor() { 
    	return vendedor; 
    	
    }
    public double getPrecio() { 
    	return precio; 
    }
    public boolean isActiva() { 
    	return activa; 
    }
    
    public void setActiva(boolean activa) { 
    	this.activa = activa; 
    }
    public void setPrecio(double precio) { 
    	this.precio = precio; } 
    // Por si el vendedor quiere ajustar
}