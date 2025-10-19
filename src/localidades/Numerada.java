package localidades;
import java.util.List;
import java.util.Map;
import eventos.Oferta;
import java.util.ArrayList;
import java.util.*;
import excepciones.CapacidadExcedidaLocalidad;
import eventos.Evento;


public class Numerada extends Localidades{
	
	private Map<String,Boolean> asientos;
	
	

	public Numerada(double precio, int capacidadMax, String nombreLocalidad, Oferta oferta, Evento evento,
			Map<String, Boolean> asientosIniciales) {
		super(precio, capacidadMax, nombreLocalidad, oferta, evento);
		
		if (asientosIniciales == null) {
			this.asientos = new HashMap<String, Boolean>();
		} else {
			this.asientos = asientosIniciales;
		}
		
	}

	@Override
	public boolean verificarDisponibilidad(int cantidad) {
		int asientosLibres = 0;
		for (Boolean b: asientos.values()) {
			if (b == false) {
				asientosLibres += 1;
			}
		}
		
		return asientosLibres >= cantidad;
	}

	
	@Override
	public List<String> venderTiquetes(int cantidad) throws CapacidadExcedidaLocalidad {
		
		List<String> asientosAsignados = new ArrayList<>();
		
		if (verificarDisponibilidad(cantidad) == false) {
			throw new CapacidadExcedidaLocalidad("Excedite la capacidad de la localidad: " + this.getNombreLocalidad() + " crack ");
		}
		int recorridos = 0;
		for (Map.Entry<String, Boolean> entry : asientos.entrySet()) {
			
			if((entry.getValue() == false) && (recorridos < cantidad)) {
				asientos.put(entry.getKey(), true);
				recorridos += 1;
				asientosAsignados.add(entry.getKey());
				
			}
			
			if (recorridos == cantidad) {
				break;
			}
			
		}
				
		return asientosAsignados;
	}

	
	
	public Map<String, Boolean> getAsientos() {
		return asientos;
	}
	
	
	
	
	
	

}
