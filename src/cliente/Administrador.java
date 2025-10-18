package cliente;
import java.util.Map;
import java.util.HashMap;
import eventos.Evento;
import tiquetes.Tiquete;


public class Administrador extends Usuario {
	
	private Map<String,Double> porcentajesServiciosTipoEvento;
	private double cobroPorEmision;
	
	
	public Administrador(String logIn, String contrasena) {
		super(logIn, contrasena, 0.0);
		this.porcentajesServiciosTipoEvento = new HashMap<String, Double>();
		this.cobroPorEmision = 0.0;
	}
	
	

	public void fijarCobroPorEmision(double cobro) {
		this.cobroPorEmision = cobro;
	}
	
	
	
	public void setPorcentajesServiciosTipoEvento(String tipoEvento, double porcentaje) {
		this.porcentajesServiciosTipoEvento.put(tipoEvento, porcentaje);
	}



	public Map<String, Double> getPorcentajesServiciosTipoEvento() {
		return porcentajesServiciosTipoEvento;
	}

	public double getCobroPorEmision() {
		return cobroPorEmision;
	}

	public void cancelarEvento(Evento evento) {
		System.out.println("EVENTO CANCELADO: El Administrador ha cancelado el evento,: " + evento.getNombre() + ". VETE PA LA CASA PERDISTE LA PLATA");
		System.out.println("Aunque no perdiste todo.");
		System.out.println("Iniciando proceso de reembolso a usuarios... " + "\n");
		
	}
	
	
	
	// no  implementa 
	@Override
	public void comprarTiquete(Evento evento, int Cantidad) {
		 
	
	}

	@Override
	public void pedirRembolso(Tiquete tiquete) {
		// TODO Auto-generated method stub
		
	}

		
	
	
	
}
