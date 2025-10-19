package cliente;

import eventos.Evento;
import localidades.Localidades;

import java.util.ArrayList;
import java.util.List;
import tiquetes.Tiquete;
import localidades.Localidades;
import excepciones.AutenticacionFallidaException;
import excepciones.CapacidadExcedidaLocalidad;
import excepciones.TiqueteNoTransferibleException;
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
	public List<Tiquete> comprarTiquete(Localidades localidad, int cantidad, double porcentajeServicio, double cobroEmision ) throws CapacidadExcedidaLocalidad{
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
		
		List<Tiquete> tiquetesCreados = new ArrayList<>();
		
		for (String asiento: asientosAsignados) {
			
			Tiquete nuevoTiquete = new Basico(precioBaseUnitario,porcentajeServicio, cobroEmision, evento.getFecha(), this, localidad, evento, "ACTIVO", asiento);  
			this.tiquetesComprados.add(nuevoTiquete);
			tiquetesCreados.add(nuevoTiquete);
			
		            
		          
		}
		

		return tiquetesCreados;
		
		
	}


	@Override
	public void pedirRembolso(Tiquete tiquete) throws TiqueteNoTransferibleException{
		// TODO Auto-generated method stub
		
		// 1. Validar que el tiquete le pertenece
	    if (!this.tiquetesComprados.contains(tiquete)) {
	        throw new TiqueteNoTransferibleException("No puedes pedir reembolso de un tiquete que no te pertenece.");
	}
	    
	    String estado = tiquete.getEstado();
	    if (!estado.equals("ACTIVO")) {
	        throw new TiqueteNoTransferibleException("Solo puedes pedir reembolso de tiquetes 'ACTIVO'. Estado actual: " + estado);
	    }
	

	    tiquete.setEstado("PENDIENTE_REEMBOLSO");
}
	
	@Override
	public void transferirTiquete(Tiquete tiquete, String passwordConfirmacion, String loginDestinatario,List<Usuario> todosLosUsuarios) throws AutenticacionFallidaException, TiqueteNoTransferibleException, Exception {
		
		if (!this.tiquetesComprados.contains(tiquete)) {
	        throw new TiqueteNoTransferibleException("El tiquete " + tiquete.getIdTiquete() + " no te pertenece.");
	    }
		
		if (!this.contrasena.equals(passwordConfirmacion)) {
	        throw new AutenticacionFallidaException("Contraseña incorrecta. Transferencia cancelada.");
	    }
		
		Usuario destinatario = null;
	    for (Usuario u : todosLosUsuarios) {
	        if (u.getLogIn().equals(loginDestinatario)) {
	            destinatario = u;
	            break;
	        }
	    }
	    
	    if (destinatario == null) {
	        throw new Exception("Usuario destinatario '" + loginDestinatario + "' no encontrado.");
	    }
	    
	    if (destinatario instanceof Administrador) {
	        throw new TiqueteNoTransferibleException("No se pueden transferir tiquetes a un Administrador.");
	    }
	    
	    tiquete.transferirTiquete(destinatario);
	    this.tiquetesComprados.remove(tiquete);
	    
	    if (destinatario instanceof UsuarioComprador) {
	    	
	        ((UsuarioComprador) destinatario).getTiquetesComprados().add(tiquete);
	        
	    } else if (destinatario instanceof OrganizadorEventos) {
	    	
	        ((OrganizadorEventos) destinatario).getTiquetesComprados().add(tiquete);
	    }
		
		
	}
	
	
}
