package marketplace;

import java.time.LocalDateTime;

import cliente.UsuarioComprador;


public class ContraOferta {

	private String id;
    private OfertaMarketplace ofertaOriginal;
    private UsuarioComprador comprador;
    private double nuevoPrecio;
    private LocalDateTime fecha;
    private String estado; //PENDIENTE, ACEPTADA O RECHAZADA
    
	public ContraOferta(String id, OfertaMarketplace ofertaOriginal, UsuarioComprador comprador, double nuevoPrecio, LocalDateTime fecha,
			String estado) {
		this.id = id;
		this.ofertaOriginal = ofertaOriginal;
		this.comprador = comprador;
		this.nuevoPrecio = nuevoPrecio;
		this.fecha = fecha;
		this.estado = estado;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public OfertaMarketplace getOfertaOriginal() {
		return ofertaOriginal;
	}

	public void setOfertaOriginal(OfertaMarketplace ofertaOriginal) {
		this.ofertaOriginal = ofertaOriginal;
	}

	public UsuarioComprador getComprador() {
		return comprador;
	}

	public void setComprador(UsuarioComprador comprador) {
		this.comprador = comprador;
	}

	public double getNuevoPrecio() {
		return nuevoPrecio;
	}

	public void setNuevoPrecio(double nuevoPrecio) {
		this.nuevoPrecio = nuevoPrecio;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
    
}
