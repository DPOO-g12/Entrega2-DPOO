package tiquetes;
import java.util.List;
import cliente.UsuarioComprador;
import eventos.Evento;
import java.util.ArrayList;

public class Multiple extends Tiquete  {
	
	private List<Tiquete> tiquetesIncluidos;
	
	private List<Evento> eventosAsociados;

	public Multiple(double precioTotalPaquete, String fecha,
			UsuarioComprador cliente, String estado,
			List<Tiquete> tiquetesIncluidos, List<Evento> eventosAsociados ) {
		
		
		super(precioTotalPaquete, 0.0, 0.0, fecha, cliente, null, null,estado);
		
		if (tiquetesIncluidos == null) {
			this.tiquetesIncluidos = new ArrayList<>();
			} else {
				this.tiquetesIncluidos = tiquetesIncluidos;
			}
				
		if (eventosAsociados == null) {
			this.eventosAsociados = new ArrayList<>();
		} else {
			this.eventosAsociados = eventosAsociados;
			}
			
			
		}
	
	

	@Override
	public String getTipoTiquete() {
		return "MULTIPLE";
	}
	
	@Override
	public void transferirTiquete(UsuarioComprador nuevoCliente) {
		
		super.transferirTiquete(nuevoCliente);
		
		for(Tiquete t: tiquetesIncluidos) {
			t.transferirTiquete(nuevoCliente);
		}
		System.out.println("El tiquete ha sido transferido al igual que sus otros " + tiquetesIncluidos.size() + " tiquetes. "
				+ "Gracias");
	
	}



	public List<Tiquete> getTiquetesIncluidos() {
		return tiquetesIncluidos;
	}

	public List<Evento> getEventosAsociados() {
		return eventosAsociados;
	}
	
	
	
	
	
	

}
