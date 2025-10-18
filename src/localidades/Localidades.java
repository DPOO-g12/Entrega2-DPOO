package localidades;
import eventos.Oferta;
import java.util.List;


public abstract class Localidades {
	private double precio;
	private int capacidadMax; 
	private String nombreLocalidad;
	private Oferta oferta;
	
	public Localidades(double precio, int capacidadMax, String nombreLocalidad, Oferta oferta) {
		this.precio = precio;
		this.capacidadMax = capacidadMax;
		this.nombreLocalidad = nombreLocalidad;
		this.oferta = oferta;
	}
	
	public abstract boolean verificarDisponibilidad(int cantidad);
	
	public abstract List<String> venderTiquetes(int cantidad);
	
	
	public double getPrecioFinal() {
		double finaal = 0.0;
		if (this.oferta != null && this.oferta.isOfertaValida()) {
			finaal = this.oferta.getDescuento();	
		}
			
		
		return Math.max(0.0, this.precio - finaal);
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
	
	
	

	
}
