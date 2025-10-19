package cliente;

import eventos.Evento;
import localidades.Localidades;

import java.util.ArrayList;
import java.util.List;
import tiquetes.Tiquete;
import localidades.Localidades;
import excepciones.CapacidadExcedidaLocalidad;
import tiquetes.Basico;

public class UsuarioComprador extends Usuario {
	
	
	private List<Tiquete> tiquetesComprados;
	
	


	public UsuarioComprador(String logIn, String contrasena, double saldo) {
		super(logIn, contrasena, saldo);
		this.tiquetesComprados = new ArrayList<>();
	}

	
	
	
	public List<Tiquete> getTiquetesComprados() {
		return tiquetesComprados;
	}





	@Override
	public void comprarTiquete(Localidades localidad, int cantidad, double porcentajeServicio, double cobroEmision ) throws CapacidadExcedidaLocalidad{
		double precioBaseUnitario = localidad.getPrecioFinal();
		double costoServicioUnitario = precioBaseUnitario * porcentajeServicio;
		double precioFinalunitario = precioBaseUnitario + costoServicioUnitario + cobroEmision;
		
		double costoTotalTransaccion = precioFinalunitario * cantidad;
		
		Evento evento = localidad.getEvento();
		
		if (!localidad.verificarDisponibilidad(cantidad)) {
	         throw new CapacidadExcedidaLocalidad(
	            "No hay disponibilidad para " + cantidad + 
	            " tiquetes en esta localidad:  " + localidad.getNombreLocalidad()
	         + " Crack.! :(");
	    }
		
		this.saldo = saldo - costoTotalTransaccion;
		
		List<String> asientosAsignados = localidad.venderTiquetes(cantidad); // si es NUmerada tiene uun asiento si no es una lista de NULL. 
		
		// ahora si crear los eventos uff
		
		for (String asiento: asientosAsignados) {
			
			Tiquete nuevoTiquete = new Basico(precioBaseUnitario,porcentajeServicio, cobroEmision, evento.getFecha(), this, localidad, evento, "ACTIVO", asiento);  
			this.tiquetesComprados.add(nuevoTiquete);
		            
		          
		}
	}


	@Override
	public void pedirRembolso(Tiquete tiquete) {
		// TODO Auto-generated method stub
		
	}
	
	public void comprarTiqueteMultiple(int numeroEventos, int cantidadTiquetes) {
		
	}
	

}
