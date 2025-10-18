package eventos;
import java.util.Map;
import java.util.HashMap;
import localidades.Localidades;
import excepciones.VenueOcupado;

public class Evento {
	
	
	private String id;
	private String nombre;
	private String fecha;
	private Venue venue;
	private Map<String, Localidades> localidades;
	
	public Evento(String id, String nombre, String fecha, Venue venue) throws VenueOcupado {
		super();
		this.id = id;
		this.nombre = nombre;
		this.fecha = fecha;
		this.venue = venue;
		this.localidades = new HashMap<String, Localidades>();
		
		venue.programarEvento(this, fecha);
	}
	
		
	
	
	public void agregarLocalidades(String nombre, Localidades localidad) {
		localidades.put(nombre, localidad);

	}

	public String getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getFecha() {
		return fecha;
	}

	public Venue getVenue() {
		return venue;
	}

	public Map<String, Localidades> getLocalidades() {
		return localidades;
	}
	
	
	
	
	

}
