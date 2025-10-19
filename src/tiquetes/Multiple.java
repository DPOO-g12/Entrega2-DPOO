package tiquetes;
import java.util.List;
import cliente.Usuario;
import eventos.Evento;
import java.util.ArrayList;
import excepciones.TiqueteNoTransferibleException;

public class Multiple extends Tiquete  {
	
	private List<Tiquete> tiquetesIncluidos;
	
	private List<Evento> eventosAsociados;

	public Multiple(double precioTotalPaquete, String fecha,
			Usuario cliente, String estado,
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
	public void transferirTiquete(Usuario nuevoCliente) throws TiqueteNoTransferibleException {
	    
	   
	    for (Tiquete t : this.tiquetesIncluidos) {
	        String estadoHijo = t.getEstado();
	        if (estadoHijo.equals("VENCIDO") || estadoHijo.equals("TRANSFERIDO")) {
	            
	           
	            throw new TiqueteNoTransferibleException("No se puede transferir el paquete: " 
	                + "El tiquete interno " + t.getIdTiquete() + " ya fue vencido o transferido.");
	        }
	    }

	    super.transferirTiquete(nuevoCliente);
	    
	    for (Tiquete t : tiquetesIncluidos) {
	        try {
	            t.transferirTiquete(nuevoCliente); 
	        } catch (TiqueteNoTransferibleException e) {
	     
	            System.err.println("Error inesperado al transferir tiquete crack: " + e.getMessage());
	        }
	    }
	}



	public List<Tiquete> getTiquetesIncluidos() {
		return tiquetesIncluidos;
	}

	public List<Evento> getEventosAsociados() {
		return eventosAsociados;
	}
	
	
	
	
	
	
	

}
