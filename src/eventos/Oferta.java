package eventos;
import cliente.OrganizadorEventos;
import java.time.LocalDateTime;

public class Oferta {
	private boolean activo;
	private double descuento;
	private LocalDateTime fechaFinDescuento;
	private OrganizadorEventos promotor;
	
	
	public Oferta(boolean activo, double descuento, LocalDateTime fechaFinDescuento, OrganizadorEventos promotor) {
		super();
		this.activo = activo;
		this.descuento = descuento;
		this.fechaFinDescuento = fechaFinDescuento;
		this.promotor = promotor;
	}
	
	
	public boolean getActivo() {
		return activo;
	}
	
	
	// Sacado de Gemini parea que utilizara la fecha de ahorita porque ni idea como se hacia.
	
	public boolean isOfertaValida() {
	    // 1. Obtenemos la fecha y hora actual
	    LocalDateTime ahora = LocalDateTime.now();

	    // 2. Comparamos
	    // La oferta es válida SI está activa Y la hora actual es ANTES de la hora de fin.
	    return this.activo && ahora.isBefore(this.fechaFinDescuento);
	}
	
	
	
	public LocalDateTime getFechaFinDescuento() {
		return fechaFinDescuento;
	}
	public double getDescuento() {
		return descuento;
	}
	
	



}
