package eventos;

import java.util.ArrayList;

import java.util.List;
import eventos.Evento;

import excepciones.VenueOcupado;

public class Venue {
	
	private int id_venue;
	private String tipo;
	private String ubicacion;
	private int capacidadMaxima;
	private List<String> restricciones;
	private List<Evento> eventosAsociados;
	private String estado;
	
	
	public Venue(String tipo, String ubicacion, int capacidadMaxima, List<String> restricciones, String estadoInicial) {
		this.id_venue = -1;
		this.tipo = tipo;
		this.ubicacion = ubicacion;
		this.capacidadMaxima = capacidadMaxima;
		if (restricciones == null) {
			this.restricciones = new ArrayList<>();
		} else {
			this.restricciones = restricciones;
		}
		
		this.estado = estadoInicial;
		this.eventosAsociados = new ArrayList<>();
	}
	
	public void programarEvento(Evento nuevoEvento, String fechaEvento) throws VenueOcupado {
		
		if (!this.estado.equals("APROBADO")) {
			throw new VenueOcupado("EL venue no ha sido aprobado por el administrador, contactate con el crack.");
		}
		
		if (verificarDisponibilidad(fechaEvento) == false) {
			throw new VenueOcupado("Reservaron antes que tu este venue para el dia: "+ fechaEvento + ", metele ganas viejo");
		}
		
		this.eventosAsociados.add(nuevoEvento);
		
		
	}
	
	
	
	
	public boolean verificarDisponibilidad(String fechaEvento) {
		boolean cumple = true;
		for (Evento e: eventosAsociados) {
			if(e.getFecha().equals(fechaEvento)) {
				cumple = false;
			}
		}
		
		return cumple;
		
	}

	public String getTipo() {
		return tipo;
	}

	public String getUbicacion() {
		return ubicacion;
	}

	public int getCapacidadMaxima() {
		return capacidadMaxima;
	}

	public List<String> getRestricciones() {
		return restricciones;
	}

	public List<Evento> getEventosAsociados() {
		return eventosAsociados;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	public int getIdVenue() {
        return id_venue;
    }

    public void setIdVenue(int id_venue) {
        this.id_venue = id_venue;
    }
	
	
	
	
	
	
	
	
	
		
		

		
		
		
	}
	
	
	
	


