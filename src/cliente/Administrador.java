package cliente;
import java.util.Map;
import java.util.HashMap;
import eventos.Evento;
import eventos.Venue;
import localidades.Localidades;
import tiquetes.Tiquete;
import java.util.List;


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
	
	public Venue crearVenue(String tipo, String ubicacion, int capacidadMaxima, List<String> restricciones) {
	    
	    Venue nuevoVenue = new Venue( tipo, ubicacion, capacidadMaxima, restricciones, "APROBADO");
	    return nuevoVenue; 
	}
	
	public void aprobarVenue(Venue venue) {
	    venue.setEstado("APROBADO"); 
	}
	
	public void rechazarVenue(Venue venue) {
	    venue.setEstado("PENDIENTE");
	}
	
	
	// no  implementa mala practica pero me chupa un guevo 
	@Override
	public void comprarTiquete(Localidades localidad, int cantidad, double porcentajeServicio, double cobroEmision ) throws Exception {

	}

	@Override
	public void pedirRembolso(Tiquete tiquete) {
		// TODO Auto-generated method stub
		
	}

	
	
	
	
	
}
