package localidades;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import eventos.Oferta;
import excepciones.CapacidadExcedidaLocalidad;

public class NoNumerada extends Localidades {
	
	private int tiquetesVendidos;

	public NoNumerada(double precio, int capacidadMax, String nombreLocalidad, Oferta oferta) {
		super(precio, capacidadMax, nombreLocalidad, oferta);
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
		if (verificarDisponibilidad(cantidad) == false) {
			throw new CapacidadExcedidaLocalidad("Excediste la capacidad de esta localidad: "+ this.getNombreLocalidad() + " crack");
			
		}
		
		List<String> vendidos = new ArrayList<>();
		Random random = new Random();
		// id utilizando gemini que me ayudo con la forma en que se formateaban los ids de los asientos.
		String prefijo = this.getNombreLocalidad().substring(0, Math.min(3, this.getNombreLocalidad().length())).toUpperCase();
		for (int i = 0; i < cantidad; i++) {
            // Genera un número aleatorio largo y lo formatea a 10 dígitos (ej: GRA-0123456789)
            long randomNum = random.nextLong() & Long.MAX_VALUE;
            String token = String.format("%s-%010d", prefijo, randomNum % 10000000000L);
            vendidos.add(token);
        }
		
		// listo ya no mas gemini
		
		this.tiquetesVendidos += cantidad;
		
		
		return vendidos;
	}
	

	public int getTiquetesVendidos() {
		return tiquetesVendidos;
	}
	
	

}
