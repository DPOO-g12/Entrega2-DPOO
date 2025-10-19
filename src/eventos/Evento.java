package eventos;
import java.util.Map;
import java.util.HashMap;
import localidades.Localidades;
import excepciones.VenueOcupado;
import cliente.OrganizadorEventos;
import localidades.NoNumerada;
import localidades.Numerada;
public class Evento {
	
	
	private String id;
	private String nombre;
	private String fecha;
	private Venue venue;
	private Map<String, Localidades> localidades;
	private OrganizadorEventos promotor;
	private String estado;
	
	public Evento(String id, String nombre, String fecha, Venue venue, OrganizadorEventos promotor) throws VenueOcupado {
		super();
		this.id = id;
		this.nombre = nombre;
		this.fecha = fecha;
		this.venue = venue;
		this.promotor = promotor;
		this.localidades = new HashMap<String, Localidades>();
		this.estado = "Activo";
		
		venue.programarEvento(this, fecha);
	}
	
		
	
	
	public void agregarLocalidadNumerada(String nombre, double precio, int capacidad, Map<String, Boolean> asientos) {
	    Numerada nueva = new Numerada(precio, capacidad, nombre, this, asientos);
	    this.localidades.put(nombre, nueva);
	}

	public void agregarLocalidadNoNumerada(String nombre, double precio, int capacidad) {
	    NoNumerada nueva = new NoNumerada(precio, capacidad, nombre, this);
	    this.localidades.put(nombre, nueva);
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
	
	public OrganizadorEventos getPromotor() {
		return promotor;
	}
	
	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	
	
	

}
