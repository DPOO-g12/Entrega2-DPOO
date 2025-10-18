package tiquetes;
import java.util.List;
import java.util.ArrayList;
import cliente.UsuarioComprador;
import eventos.Evento;
import localidades.Localidades;

public class Deluxe extends Tiquete {
	
	private List<String> beneficiosAdicionales;

	public Deluxe(double precioBase, double porcentajeServicio, double cobroFijoEmision, String fecha,
			UsuarioComprador cliente, Localidades localidad, Evento evento, String estado,
			List<String> beneficiosAdicionales) {
		super(precioBase, porcentajeServicio, cobroFijoEmision, fecha, cliente, localidad, evento, estado);
		
		
		if (beneficiosAdicionales == null) {
			this.beneficiosAdicionales = new ArrayList<>();
		} else { 
			this.beneficiosAdicionales = beneficiosAdicionales;
		}
		
	
		this.transferible = false;

	}

	@Override
	public String getTipoTiquete() {
		return "DELUXE";
	} 
	
	
	public List<String> getBeneficiosAdicionales(){
		return beneficiosAdicionales;
	}

}
