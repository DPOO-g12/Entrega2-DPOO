package marketplace;

import java.time.LocalDateTime;

import cliente.UsuarioComprador;
import tiquetes.Tiquete;

public class OfertaMarketplace {

	private String id;
    private UsuarioComprador vendedor;
    private Tiquete tiquete;
    private double precio;
    private LocalDateTime fechaCreacion;
    private boolean activa; 
    
    
	public OfertaMarketplace(String id, UsuarioComprador vendedor, Tiquete tiquete, double precio, LocalDateTime fechaCreacion,
			boolean activa) {

		this.id = id;
		this.vendedor = vendedor;
		this.tiquete = tiquete;
		this.precio = precio;
		this.fechaCreacion = fechaCreacion;
		this.activa = activa;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public UsuarioComprador getVendedor() {
		return vendedor;
	}


	public void setVendedor(UsuarioComprador vendedor) {
		this.vendedor = vendedor;
	}


	public Tiquete getTiquete() {
		return tiquete;
	}


	public void setTiquete(Tiquete tiquete) {
		this.tiquete = tiquete;
	}


	public double getPrecio() {
		return precio;
	}


	public void setPrecio(double precio) {
		this.precio = precio;
	}


	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}


	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}


	public boolean isActiva() {
		return activa;
	}


	public void setActiva(boolean activa) {
		this.activa = activa;
	}
}
