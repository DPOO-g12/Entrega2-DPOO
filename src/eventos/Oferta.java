package eventos;
import cliente.OrganizadorEventos;

public class Oferta {
	private boolean activo;
	private double descuento;
	private String fechaFinDescuento;
	private OrganizadorEventos promotor;
	
	
	public Oferta(boolean activo, double descuento, String fechaFinDescuento, OrganizadorEventos promotor) {
		super();
		this.activo = activo;
		this.descuento = descuento;
		this.fechaFinDescuento = fechaFinDescuento;
		this.promotor = promotor;
	}
	
	
	public boolean getActivo() {
		return activo;
	}
	
	public boolean isOfertaValida() {
		return activo;
	}
	
	public String getFechaFinDescuento() {
		return fechaFinDescuento;
	}
	public double getDescuento() {
		return descuento;
	}
	
	



}
