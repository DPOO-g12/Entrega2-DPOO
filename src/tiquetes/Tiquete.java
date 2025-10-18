package tiquetes;
import java.util.Random;

import cliente.UsuarioComprador;
import localidades.Localidades;
import eventos.Evento;;

public abstract class Tiquete {
	
	protected String idTiquete; 
	protected double precioBase;
	protected double costoServicio;
	protected double costoEmision;
	protected double precioFinal;
	protected String fecha;
	protected UsuarioComprador cliente;
	protected Localidades localidad;
	protected Evento evento;
	protected boolean transferible;
	protected String estado;
	
	
	// Metodo para generar un String unico, la logica la saque usando gemini.
	
	private String generarIdTiquete(Localidades localidad, Evento evento) {
		// 1. Obtiene un prefijo de la localidad o evento
        String prefijo = localidad.getNombre().substring(0, Math.min(3, localidad.getNombre().length())).toUpperCase();
        
        // 2. Obtiene un timestamp (milisegundos) y un número aleatorio
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000); // Número aleatorio de 0 a 999
        
        // 3. Concatena los elementos para formar el ID
        // Ejemplo: GRA-1634567890123-456
        return String.format("%s-%d-%03d", prefijo, timestamp, random);
		
		
		
	}
	
	
	
	public Tiquete(double precioBase, double porcentajeServicio, double cobroFijoEmision, String fecha, UsuarioComprador cliente,
			Localidades localidad, Evento evento, String estado) {
		
		
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

	public void transferirTiquete(UsuarioComprador nuevoCliente) {
	    // Restricciones
		// 1. El paquete Deluxe no se puede transferir
	    if (!this.transferible) { // transferible tiene un valro en defecto de true, el unico que lo tendra en False es el tiquete Deluxe.
	    
	        throw new IllegalStateException("El tiquete ID " + this.idTiquete + " no se puede transferir (no es transferible).");
	    }
	    // 2. No se puede transferir si ya se ha vencido.
	    
	   
	    if (this.estado.equals("VENCIDO")) {
	    	throw new IllegalStateException("Se te vencio el tiquete bro");
	    	}
	

	    this.cliente = nuevoCliente;
	    
	
	    this.estado = "TRANSFERIDO";
	    
	    System.out.println("Tiquete " + this.idTiquete + " transferido exitosamente a: " + nuevoCliente.getLogIn());
	}


	public double getPrecioBase() {
		return precioBase;
	}

	public String getFecha() {
		return fecha;
	}

	public UsuarioComprador getCliente() {
		return cliente;
	}

	public void setCliente(UsuarioComprador cliente) {
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

	
	
	
	
	
	
	
	

	
	
	
	

	
	
	
	
}
