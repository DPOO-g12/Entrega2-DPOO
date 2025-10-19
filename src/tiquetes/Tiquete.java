package tiquetes;
import java.util.Random;

import cliente.Usuario;
import localidades.Localidades;
import eventos.Evento;
import excepciones.TiqueteNoTransferibleException;

public abstract class Tiquete {
	
	private int id_tiquete_db;
	protected String idTiquete; 
	protected double precioBase;
	protected double costoServicio;
	protected double costoEmision;
	protected double precioFinal;
	protected String fecha;
	protected Usuario cliente;
	protected Localidades localidad;
	protected Evento evento;
	protected boolean transferible;
	protected String estado;
	
	
	// Metodo para generar un String unico, la logica la saque usando gemini.
	
	private String generarIdTiquete(Localidades localidad, Evento evento) {
		// 1. Obtiene un prefijo de la localidad o evento
        String prefijo = localidad.getNombreLocalidad().substring(0, Math.min(3, localidad.getNombreLocalidad().length())).toUpperCase();
        
        // 2. Obtiene un timestamp (milisegundos) y un número aleatorio
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000); // Número aleatorio de 0 a 999
        
        // 3. Concatena los elementos para formar el ID
        // Ejemplo: GRA-1634567890123-456
        return String.format("%s-%d-%03d", prefijo, timestamp, random);
		
		
		
	}
	
	
	
	public Tiquete(double precioBase, double porcentajeServicio, double cobroFijoEmision, String fecha, Usuario cliente,
			Localidades localidad, Evento evento, String estado) {
		
		this.id_tiquete_db = -1;
		this.idTiquete = generarIdTiquete(localidad, evento);
		
		this.precioBase = precioBase;
		this.fecha = fecha;
		this.cliente = cliente;
		this.localidad = localidad;
		this.evento = evento;
		this.estado = estado;
		this.transferible = true;
		
		// calcular los costos que va a tener el tiquete al final de una vez en el constructor para que
		//cuando se cree el tiquete ya tenga el costo de una vez 
		
		this.costoServicio = precioBase * porcentajeServicio;
		this.costoEmision = cobroFijoEmision;
		this.precioFinal = precioBase + this.costoServicio + this.costoEmision;
		
		
	}

	
	public abstract String getTipoTiquete();

	public void transferirTiquete(Usuario nuevoCliente) throws TiqueteNoTransferibleException {
	    
	    if (!this.transferible) {
	        throw new TiqueteNoTransferibleException("El tiquete ID " + this.idTiquete + " no se puede transferir pillin es Deluxe o su tipo no lo permite. ");
	    }
	    
	    if (this.estado.equals("VENCIDO")) {
	        throw new TiqueteNoTransferibleException("El tiquete ID " + this.idTiquete + " no se puede transferir pillin porque está VENCIDO, A nosotros no nos vz la cara crack :).");
	    }


	    this.cliente = nuevoCliente;
	    this.estado = "TRANSFERIDO";
	    

	}


	public double getPrecioBase() {
		return precioBase;
	}

	public String getFecha() {
		return fecha;
	}

	public Usuario getCliente() {
		return cliente;
	}

	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
	}

	public Localidades getLocalidad() {
		return localidad;
	}


	public Evento getEvento() {
		return evento;
	}


	public boolean isTransferible() {
		return transferible;
	}


	public void setTransferible(boolean transferible) {
		this.transferible = transferible;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getIdTiquete() {
		return idTiquete;
	}
	
	public double getCostoServicio() {
		return costoServicio;
	}

	public double getCostoEmision() {
		return costoEmision;
	}
	
	public double getPrecioFinal() {
		return precioFinal;
	}
	
	public int getIdTiqueteDb() {
        return id_tiquete_db;
    }

    public void setIdTiqueteDb(int id_tiquete_db) {
        this.id_tiquete_db = id_tiquete_db;
    }

	
	
	
	
	
	
	
	

	
	
	
	

	
	
	
	
}
