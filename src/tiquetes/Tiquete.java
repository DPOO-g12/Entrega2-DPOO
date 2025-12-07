package tiquetes;
import java.util.Random;

import cliente.Usuario;
import localidades.Localidades;
import eventos.Evento;
import excepciones.TiqueteNoTransferibleException;

public abstract class Tiquete {

	protected int id_tiquete_db;
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
	protected boolean impreso;
	


	// Metodo para generar un String unico, la logica la saque usando gemini.

	// CLASE: Tiquete.java

	private String generarIdTiquete(Localidades localidad, Evento evento) {
		String prefijo;

		// 🛠️ CORRECCIÓN: Manejar el caso de Tiquete Múltiple donde ambos son NULL
		if (localidad != null) {
			// Opción 1: Usa el prefijo de la localidad
			prefijo = localidad.getNombreLocalidad().substring(0, Math.min(3, localidad.getNombreLocalidad().length())).toUpperCase();
		} 
		else if (evento != null) { 
			// Opción 2: Si localidad es nula, usa el prefijo del evento
			prefijo = evento.getId().substring(0, Math.min(3, evento.getId().length())).toUpperCase();
		} 
		else {
			// Opción 3: Si ambos son nulos (solo ocurre en Multiple), usa un prefijo genérico
			prefijo = "MUL"; // o "PKG" de paquete. Usaremos "MUL" de Múltiple.
		}

		// 2. Obtiene un timestamp (milisegundos) y un número aleatorio
		long timestamp = System.currentTimeMillis();
		int random = new Random().nextInt(1000); 

		// 3. Concatena los elementos para formar el ID
		return String.format("%s-%d-%03d", prefijo, timestamp, random);
	}

	protected Tiquete(int id_db, String id_java, double pBase, double pServicio, 
			double pEmision, double pFinal, String fecha, String estado, 
			boolean transferible, Usuario cliente, Localidades loc, Evento evt) {
		this.id_tiquete_db = id_db;
		this.idTiquete = id_java;
		this.precioBase = pBase;
		this.costoServicio = pServicio;
		this.costoEmision = pEmision;
		this.precioFinal = pFinal;
		this.fecha = fecha;
		this.estado = estado;
		this.transferible = transferible;
		this.cliente = cliente;
		this.localidad = loc;
		this.evento = evt;
		this.impreso = false;
		
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
		
		if (this.impreso) {
			
			return false;
		}
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
	
	public boolean isImpreso() {
		return impreso;
	}
	
	public void setImpreso (boolean estadoNuevo) {
		this.impreso = estadoNuevo;

	}



















}
