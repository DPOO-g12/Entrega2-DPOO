package localidades;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import eventos.Oferta;
import excepciones.CapacidadExcedidaLocalidad;
import eventos.Evento;

public class NoNumerada extends Localidades {
	
	private int tiquetesVendidos;

	public NoNumerada(double precio, int capacidadMax, String nombreLocalidad, Evento evento) {
		super(precio, capacidadMax, nombreLocalidad, evento);
		this.tiquetesVendidos = 0;
	}

	@Override
	public boolean verificarDisponibilidad(int cantidad) {
		boolean disponibilidad = false;
		if (tiquetesVendidos + cantidad <= this.getCapacidadMax()) {
			disponibilidad = true;
		}
		
		return disponibilidad;
	}

	@Override
	public List<String> venderTiquetes(int cantidad) throws CapacidadExcedidaLocalidad {
	    if (!verificarDisponibilidad(cantidad)) { 
	        throw new CapacidadExcedidaLocalidad("Excediste la capacidad de esta localidad: "+ this.getNombreLocalidad() + " crack");
	    }
	    this.tiquetesVendidos += cantidad;

	    List<String> asientosAsignados = new ArrayList<>();


	    for (int i = 0; i < cantidad; i++) {
	        asientosAsignados.add(null); 
	    }
	    return asientosAsignados;
	}
	
	

	public int getTiquetesVendidos() {
		return tiquetesVendidos;
	}
	

	

}
