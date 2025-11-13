package cliente;
import eventos.Evento;
import eventos.Oferta;
import eventos.Venue;
import localidades.Localidades;
import tiquetes.Basico;
import tiquetes.Multiple;
import tiquetes.Tiquete;
import excepciones.AutenticacionFallidaException;
import excepciones.CapacidadExcedidaLocalidad;
import excepciones.OperacionNoAutorizadaException;
import excepciones.TiqueteNoTransferibleException;
import excepciones.VenueOcupado;

import java.time.LocalDateTime;
import java.util.*;


public class OrganizadorEventos extends Usuario {

	private List<Evento> eventosOrganizados;
	private List<Tiquete> tiquetesComprados;
	
	public OrganizadorEventos(String logIn, String contrasena, double saldo) {
		super(logIn, contrasena, saldo);
		this.eventosOrganizados = new ArrayList<>();
		this.tiquetesComprados = new ArrayList<>();
	}

	

	public List<Evento> getEventosOrganizados(){
		return eventosOrganizados;
	}
	
	public List<Tiquete> getTiquetesComprados() { 
	    return tiquetesComprados;
	}

	public Evento agregarEvento(String id, String nombre, String fecha, Venue venue) 
		    throws VenueOcupado, Exception {
		    Evento nuevoEvento = new Evento(
		        id, 
		        nombre, 
		        fecha, 
		        venue, 
		        this
		    );
		    
		  
		    this.eventosOrganizados.add(nuevoEvento);
		    return nuevoEvento;
		}


	
	
	public Map<String, Double> calcularEstadoFinanciero(List<Tiquete> todosLosTiquetesVendidos) {
	    
	    Map<String, Double> reporte = new HashMap<>();

	    double gananciasGlobales = 0.0;
	    int tiquetesVendidosGlobales = 0; 
	    int tiquetesDisponiblesGlobales = 0;

	    // Iteramos sobre los eventos que ESTE organizador posee
	    for (Evento evento : this.eventosOrganizados) {
	        
	        double gananciasPorEvento = 0.0;
	        int tiquetesVendidosPorEvento = 0;
	        int tiquetesDisponiblesPorEvento = 0;

	        // Iteramos sobre las localidades de CADA evento
	        for (Localidades loc : evento.getLocalidades().values()) {
	            
	            double gananciasPorLocalidad = 0.0;
	            int tiquetesVendidosPorLocalidad = 0;
	            int tiquetesDisponiblesLocalidad = loc.getCapacidadMax();

	            // Ahora, filtramos la lista maestra de tiquetes
	            for (Tiquete tiquete : todosLosTiquetesVendidos) {
	                
	                if (tiquete.getLocalidad() != null && tiquete.getLocalidad().equals(loc)) {
	                    
	                    if (!tiquete.getEstado().equals("CORTESIA")) {
	                        gananciasPorLocalidad += tiquete.getPrecioBase();
	                    }
	                   
	                    tiquetesVendidosPorLocalidad++;
	                }
	            }

	            // Calcular porcentaje de venta de la localidad
	            double porcVentaLoc = (tiquetesDisponiblesLocalidad == 0) ? 0.0 :
	                ((double) tiquetesVendidosPorLocalidad / tiquetesDisponiblesLocalidad);
	            
	            reporte.put("GANANCIA_LOC_" + loc.getNombreLocalidad(), gananciasPorLocalidad);
	            reporte.put("PORCENTAJE_LOC_" + loc.getNombreLocalidad(), porcVentaLoc * 100.0);
	            
	            gananciasPorEvento += gananciasPorLocalidad;
	            tiquetesVendidosPorEvento += tiquetesVendidosPorLocalidad;
	            tiquetesDisponiblesPorEvento += tiquetesDisponiblesLocalidad;
	        }

	        double porcVentaEvt = (tiquetesDisponiblesPorEvento == 0) ? 0.0 :
	            ((double) tiquetesVendidosPorEvento / tiquetesDisponiblesPorEvento);
	        

	        reporte.put("GANANCIA_EVT_" + evento.getNombre(), gananciasPorEvento);
	        reporte.put("PORCENTAJE_EVT_" + evento.getNombre(), porcVentaEvt * 100.0);

	        // Acumular totales globales
	        gananciasGlobales += gananciasPorEvento;
	        tiquetesVendidosGlobales += tiquetesVendidosPorEvento;
	        tiquetesDisponiblesGlobales += tiquetesDisponiblesPorEvento;
	    }

	    // Calcular porcentaje de venta global
	    double porcVentaGlobal = (tiquetesDisponiblesGlobales == 0) ? 0.0 :
	        ((double) tiquetesVendidosGlobales / tiquetesDisponiblesGlobales);

	    // Guardar reporte global
	    reporte.put("GANANCIA_GLOBAL", gananciasGlobales);
	    reporte.put("PORCENTAJE_VENTA_GLOBAL", porcVentaGlobal * 100.0);

	    return reporte;
	}
	
	
	@Override
	public List<Tiquete> comprarTiquete(Localidades localidad, int cantidad, double porcentajeServicio, double cobroEmision ) throws CapacidadExcedidaLocalidad, OperacionNoAutorizadaException {
		Evento evento = localidad.getEvento();
		
		// si este organizador es es mismo q organiza el evento
		
		boolean esCortesia = evento.getPromotor().getLogIn().equals(this.getLogIn());
		
		List<Tiquete> tiquetesCreados = new ArrayList<>();
		
		
		if (esCortesia == true) {
	        if (!localidad.verificarDisponibilidad(cantidad)) {
	             throw new CapacidadExcedidaLocalidad(
	                "No hay disponibilidad para " + cantidad + 
	                " tiquetes (cortesía) en " + localidad.getNombreLocalidad()
	            );
	        }
	        
	        List<String> asientosAsignados = localidad.venderTiquetes(cantidad);
	        
	        for (String asiento : asientosAsignados) {
	            
	            Tiquete nuevoTiquete = new Basico(localidad.getPrecioFinal(),0.0, 0.0, evento.getFecha(),this, localidad,evento,"CORTESIA", asiento );          
	            this.tiquetesComprados.add(nuevoTiquete);
	            tiquetesCreados.add(nuevoTiquete);
	        }
	    
	    
		} else {
	        throw new OperacionNoAutorizadaException(
	            "Como Organizador, solo puedes 'comprar' (generar cortesías) " +  "para tus propios eventos. Para otros eventos, usa una cuenta de Comprador.");
	    }
	    
	    return tiquetesCreados;
			
	}
	
	public void crearOferta(Localidades localidad, double descuento, LocalDateTime fechaFin) {
		Oferta nuevaOferta = new Oferta(true, descuento,fechaFin, this);
		    localidad.setOferta(nuevaOferta);	
	}
	
	
	public Venue sugerirVenue(String tipo, String ubicacion, int capacidadMaxima, List<String> restricciones) {
	    

	    Venue nuevoVenue = new Venue(tipo,ubicacion,capacidadMaxima,restricciones,"PENDIENTE"); 
	    return nuevoVenue;
	}
	
	
	public void pedirRembolso(Tiquete tiquete) throws TiqueteNoTransferibleException {
	    

	    if (!this.tiquetesComprados.contains(tiquete)) {
	        throw new TiqueteNoTransferibleException("No puedes pedir reembolso de un tiquete que no te pertenece.");
	    }

	    if (tiquete.getEstado().equals("CORTESIA")) {
	        throw new TiqueteNoTransferibleException("No se pueden reembolsar tiquetes de 'CORTESIA'.NO SEA LIZO LICHIGO");
	    }

	    String estado = tiquete.getEstado();
	    if (!estado.equals("ACTIVO")) {
	        throw new TiqueteNoTransferibleException("Solo puedes pedir reembolso de tiquetes 'ACTIVO'. Estado actual: " + estado);
	    }
	    
	    tiquete.setEstado("PENDIENTE_REEMBOLSO");
	}


	public Multiple crearPaquetePaseDeTemporada(List<Localidades> localidadesDelPaquete, double precioPaquete) throws CapacidadExcedidaLocalidad, OperacionNoAutorizadaException {

		List<Tiquete> tiquetesInternos = new ArrayList<>();
	    List<Evento> eventosAsociados = new ArrayList<>();
	    
	    for (Localidades loc : localidadesDelPaquete) {
	        Evento evento = loc.getEvento();
	        
	        //Validar que el evento le pertenece a este organizador
	        if (!evento.getPromotor().getLogIn().equals(this.getLogIn())) {
	            throw new OperacionNoAutorizadaException(
	                "No puedes crear paquetes con tiquetes del evento '" + 
	                evento.getNombre() + "', no te pertenece."
	            );
	        }
	        
	        if (!loc.verificarDisponibilidad(1)) {
	            throw new CapacidadExcedidaLocalidad(
	                "No hay disponibilidad en " + loc.getNombreLocalidad() + 
	                " para crear el paquete."
	            );
	        }
	        
	        List<String> asiento = loc.venderTiquetes(1);
	        
	        Tiquete tiqueteInterno = new Basico(loc.getPrecioFinal(), 0.0, 0.0, evento.getFecha(),this, loc,evento, "CORTESIA", asiento.get(0));
	            
	            tiquetesInternos.add(tiqueteInterno);
	            
	            if (!eventosAsociados.contains(evento)) {
	                eventosAsociados.add(evento);
	            }
	            
	            
	}
	    
	    String fechaHoy = java.time.LocalDate.now().toString(); 

        Multiple paquete = new Multiple( precioPaquete,fechaHoy,this,"ACTIVO",tiquetesInternos,eventosAsociados);
        
        this.tiquetesComprados.add(paquete);
        
        return paquete;

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
