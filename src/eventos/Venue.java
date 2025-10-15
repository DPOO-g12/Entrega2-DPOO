package eventos;

import java.util.ArrayList;

import java.util.List;

import excepciones.VenueOcupado;

public class Venue {
	
	private String venueName;
	private String ubicacion;
	private int capacidadMax;
	private String tipo;
	private List <String> restriciones = new ArrayList <>();
	private List <Evento> eventosEnVenue = new ArrayList <>();
	
	public Venue(String venueName, String ubicacion, int capacidadMax, String tipo, List<String> restriciones,
			List<Evento> eventosEnVenue) {
		super();
		this.venueName = venueName;
		this.ubicacion = ubicacion;
		this.capacidadMax = capacidadMax;
		this.tipo = tipo;
		this.restriciones = restriciones;
		this.eventosEnVenue = eventosEnVenue;
	}

	public String getVenueName() {
		return venueName;
	}

	public String getUbicacion() {
		return ubicacion;
	}

	public int getCapacidadMax() {
		return capacidadMax;
	}

	public String getTipo() {
		return tipo;
	}

	public List<String> getRestriciones() {
		return restriciones;
	}

	public List<Evento> getEventosEnVenue() {
		return eventosEnVenue;
	}

	
	
	public void agregarEventoAlVenue ( Evento e ) {
		
	
		eventosEnVenue.add(e);
		
	}
	
	public String verificarDisponibilidadVenue (Evento e, ArrayList<Evento> n ) throws VenueOcupado{
		
		for (Evento x: n) {
			
			if (x.fecha.equals(e.fecha)) {
			
			throw new VenueOcupado(e.Venue.getVenueName);
			}
			
			else 
			
		}
		
		
		
		
		
		
	}
	
	
	
	

}
