package cliente;
import java.util.HashMap;

import eventos.Evento;
import eventos.Venue;
import localidades.Localidades;
import tiquetes.Basico;
import tiquetes.Tiquete;
import excepciones.CapacidadExcedidaLocalidad;
import java.util.*;


public class OrganizadorEventos extends Usuario {

	private List<Evento> eventosOrganizados;
	private List<Tiquete> tiquetesComprados;
	
	public OrganizadorEventos(String logIn, String contrasena, double saldo) {
		super(logIn, contrasena, saldo);
		this.eventosOrganizados = new ArrayList<>();
		this.tiquetesComprados = new ArrayList<>();
	}

	

	
	public List<Evento> getEventosOrganizados(){
		return eventosOrganizados;
	}
	
	public List<Tiquete> getTiquetesComprados() { 
	    return tiquetesComprados;
	}

	public void agregarEvento(Evento evento) {
		this.eventosOrganizados.add(evento);
	}


	
	
	public Map<String, Double> calcularEstadoFinanciero(){
		Map<String, Double> reporte = new HashMap<>();
		return reporte;
	}
	
	
	@Override
	public void comprarTiquete(Localidades localidad, int cantidad, double porcentajeServicio, double cobroEmision ) throws CapacidadExcedidaLocalidad {
		Evento evento = localidad.getEvento();
		
		// si este organizador es es mismo q organiza el evento
		
		boolean esCortesia = evento.getPromotor().getLogIn().equals(this.getLogIn());
		
		
		if (esCortesia == true) {
	        if (!localidad.verificarDisponibilidad(cantidad)) {
	             throw new CapacidadExcedidaLocalidad(
	                "No hay disponibilidad para " + cantidad + 
	                " tiquetes (cortesía) en " + localidad.getNombreLocalidad()
	            );
	        }
	        
	        List<String> asientosAsignados = localidad.venderTiquetes(cantidad);
	        
	        for (String asiento : asientosAsignados) {
	            
	            Tiquete nuevoTiquete = new Basico(localidad.getPrecioFinal(),0.0, 0.0, evento.getFecha(),this, localidad,evento,"CORTESIA", asiento );          
	            this.tiquetesComprados.add(nuevoTiquete);
	        }
	    
	    
		} 
			
	}
	
	
	
	public Venue sugerirVenue(String tipo, String ubicacion, int capacidadMaxima, List<String> restricciones) {
	    

	    Venue nuevoVenue = new Venue(tipo,ubicacion,capacidadMaxima,restricciones,"PENDIENTE"); 
	    return nuevoVenue;
	}
	
	
	@Override
	public void pedirRembolso(Tiquete tiquete) {
		// TODO Auto-generated method stub
		
	}










}