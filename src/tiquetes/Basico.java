package tiquetes;

import cliente.UsuarioComprador;
import eventos.Evento;
import localidades.Localidades;

public class Basico extends Tiquete {
	
	private String numeroAsiento;

	public Basico(double precioBase, double porcentajeServicio, double cobroFijoEmision, String fecha,
			UsuarioComprador cliente, Localidades localidad, Evento evento, String estado, String numeroAsiento) {
		super(precioBase, porcentajeServicio, cobroFijoEmision, fecha, cliente, localidad, evento, estado);
		
		this.numeroAsiento = numeroAsiento;
	}

	@Override
	public String getTipoTiquete() {
		return "BASICO";
	}
	
	
	public String getNumeroAsiento(){
		return numeroAsiento;
	}
	
	

}
