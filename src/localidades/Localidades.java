package localidades;
import eventos.Oferta;
import java.util.List;
import eventos.Evento;


public abstract class Localidades {
	private int id_localidad;
	private double precio;
	private int capacidadMax; 
	private String nombreLocalidad;
	private Oferta oferta;
	private Evento evento;
	
	public Localidades(double precio, int capacidadMax, String nombreLocalidad, Evento evento) {
		this.id_localidad = -1;
		this.precio = precio;
		this.capacidadMax = capacidadMax;
		this.nombreLocalidad = nombreLocalidad;
		this.oferta = null;
		this.evento = evento;
	}
	
	public abstract boolean verificarDisponibilidad(int cantidad);
	
	public abstract List<String> venderTiquetes(int cantidad);
	
	
	public double getPrecioFinal() {
	    if (this.oferta == null || !this.oferta.isOfertaValida()) {
	        return this.precio;
	    }

	    double porcentajeDescuento = this.oferta.getDescuento(); 

	    double precioConDescuento = this.precio * (1.0 - porcentajeDescuento);

	    // Math.max para que el valor no sea negativo de el precioFinal por la resta.
	    return Math.max(0.0, precioConDescuento); 
	}
	

	public double getPrecio() {
		return precio;
	}

	public int getCapacidadMax() {
		return capacidadMax;
	}

	public String getNombreLocalidad() {
		return nombreLocalidad;
	}

	public Oferta getOferta() {
		return oferta;
	}
	
	public Evento getEvento() {
	    return evento;
	}
	
	public void setOferta(Oferta oferta) {
		this.oferta = oferta;
	}
	
	public int getIdLocalidad() {
        return id_localidad;
    }

    public void setIdLocalidad(int id_localidad) {
        this.id_localidad = id_localidad;
    }
	
	
	

	
}
