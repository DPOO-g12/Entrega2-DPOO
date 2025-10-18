package eventos;

import java.util.ArrayList;

import java.util.List;
import eventos.Evento;

import excepciones.VenueOcupado;

public class Venue {
	
	private String tipo;
	private String ubicacion;
	private int capacidadMaxima;
	private List<String> restricciones;
	private List<Evento> eventosAsociados;
	
	
	public Venue(String tipo, String ubicacion, int capacidadMaxima, List<String> restricciones) {
		this.tipo = tipo;
		this.ubicacion = ubicacion;
		this.capacidadMaxima = capacidadMaxima;
		if (restricciones == null) {
			this.restricciones = new ArrayList<>();
		} else {
			this.restricciones = restricciones;
		}
		
		this.eventosAsociados = new ArrayList<>();
	}
	
	public void programarEvento(Evento nuevoEvento, String fechaEvento) throws VenueOcupado {
		
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
	
	
	
	
	
	
	
	
	
		
		

		
		
		
	}
	
	
	
	


