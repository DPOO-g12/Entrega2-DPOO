package marketplace;

import java.time.LocalDateTime;

public class LogRegistro {

	private LocalDateTime fechaHora;
	private String descripcion;
	private String usuario;

	public LogRegistro(LocalDateTime fechaHora, String descripcion, String usuario) {

		this.fechaHora = fechaHora;
		this.descripcion = descripcion;
		this.usuario = usuario;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
}
