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
	
	private Evento(String id, String nombre, String fecha, Venue venue, OrganizadorEventos promotor, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.venue = venue;
        this.promotor = promotor;
        this.estado = estado;
        this.localidades = new HashMap<String, Localidades>(); // Inicializar el mapa
    }
    /**
     * "Fábrica" para crear un objeto Evento desde los datos de la BD.
     * Utiliza el constructor de hidratación privado.
     */
	
    public static Evento cargarDesdeDB(String id, String nombre, String fecha, Venue venue, OrganizadorEventos promotor, String estado) {
        return new Evento(id, nombre, fecha, venue, promotor, estado);
    }
	
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
