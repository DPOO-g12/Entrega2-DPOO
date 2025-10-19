package cliente;
import java.util.Map;
import java.util.HashMap;
import eventos.Evento;
import eventos.Venue;
import localidades.Localidades;
import tiquetes.Tiquete;
import java.util.List;


public class Administrador extends Usuario {
	
	private Map<String,Double> porcentajesServiciosTipoEvento;
	private double cobroPorEmision;
	
	
	public Administrador(String logIn, String contrasena) {
		super(logIn, contrasena, 0.0);
		this.porcentajesServiciosTipoEvento = new HashMap<String, Double>();
		this.cobroPorEmision = 0.0;
	}
	
	

	public void fijarCobroPorEmision(double cobro) {
		this.cobroPorEmision = cobro;
	}
	
	
	
	public void setPorcentajesServiciosTipoEvento(String tipoEvento, double porcentaje) {
		this.porcentajesServiciosTipoEvento.put(tipoEvento, porcentaje);
	}



	public Map<String, Double> getPorcentajesServiciosTipoEvento() {
		return porcentajesServiciosTipoEvento;
	}

	public double getCobroPorEmision() {
		return cobroPorEmision;
	}

	public void cancelarEvento(Evento evento, List<Tiquete> todosLosTiquetesVendidos, boolean isCancelacionPorAdmin)  {
		
		evento.setEstado("CANCELADO");
		
		for (Tiquete tiquete :todosLosTiquetesVendidos ) {
			
			if (tiquete.getEvento() != null && tiquete.getEvento().equals(evento)) {
				
				String estadoActual = tiquete.getEstado();
				
				if (estadoActual.equals("ACTIVO") || estadoActual.equals("TRANSFERIDO")) {
					
					double montoReembolso = 0.0;
					
					if (isCancelacionPorAdmin) {
	                    // Regla Admin: precio pagado (base + servicio) menos emisión
	                    montoReembolso = tiquete.getPrecioBase() + tiquete.getCostoServicio();
	                    
					} else {
	                    // Regla Organizador: solo precio base
	                    montoReembolso = tiquete.getPrecioBase();
	                }
					
					
					if (montoReembolso > 0) {
	                    Usuario dueno = tiquete.getCliente();
	                    dueno.setSaldo(dueno.getSaldo() + montoReembolso);
	                }
					
					tiquete.setEstado("REEMBOLSADO");
					
				}	
			}
		}

	}
	
	public Venue crearVenue(String tipo, String ubicacion, int capacidadMaxima, List<String> restricciones) {
	    
	    Venue nuevoVenue = new Venue( tipo, ubicacion, capacidadMaxima, restricciones, "APROBADO");
	    return nuevoVenue; 
	}
	
	public void aprobarVenue(Venue venue) {
	    venue.setEstado("APROBADO"); 
	}
	
	public void rechazarVenue(Venue venue) {
	    venue.setEstado("PENDIENTE");
	}
	
	
	// no  implementa mala practica pero me chupa un guevo 
	@Override
	public List<Tiquete> comprarTiquete(Localidades localidad, int cantidad, double porcentajeServicio, double cobroEmision ) throws Exception {
		return null;

	}

	@Override
	public void pedirRembolso(Tiquete tiquete) {
		// TODO Auto-generated method stub
		
	}

	public void gestionarReembolso(Tiquete tiquete, boolean aprobar) {
	    
	    if (tiquete == null || !tiquete.getEstado().equals("PENDIENTE_REEMBOLSO")) {
	        return; 
	    }
	    
	    if (aprobar) {
	       
	        // Asumiremos que da el reembolso completo (base + servicio).
	        double montoReembolso = tiquete.getPrecioBase() + tiquete.getCostoServicio();
	        
	        //Aplicar el reembolso al saldo del dueño
	        if (montoReembolso > 0) {
	            Usuario dueno = tiquete.getCliente();
	            dueno.setSaldo(dueno.getSaldo() + montoReembolso);
	        }
	        
	        tiquete.setEstado("REEMBOLSADO");
	        
	      //logica de rechazo
	    } else {
	        tiquete.setEstado("ACTIVO");
	    }
	}
	
	public Map<String, Double> calcularGanancias( List<Tiquete> todosLosTiquetesVendidos, List<Evento> todosLosEventos) {
		    
		    Map<String, Double> reporte = new HashMap<>();
		    double gananciasTotales = 0.0;

		    // Mapa para guardar ganancias por organizador
		    Map<OrganizadorEventos, Double> gananciasPorOrganizador = new HashMap<>();

		    // Iteramos sobre TODOS los tiquetes vendidos
		    for (Tiquete tiquete : todosLosTiquetesVendidos) {
		        
		        String estado = tiquete.getEstado();
		        
		        if (!estado.equals("CORTESIA") && !estado.equals("REEMBOLSADO")) {
		            
		            double gananciaEsteTiquete = tiquete.getCostoServicio() + tiquete.getCostoEmision();
		            
		            //Sumar a las ganancias totales
		            gananciasTotales += gananciaEsteTiquete;
		            
		            //Sumar a las ganancias por evento
		            Evento evento = tiquete.getEvento();
		            if (evento != null) {
		                String claveEvento = "GANANCIA_EVT_" + evento.getNombre();
		                double gananciaEventoActual = reporte.getOrDefault(claveEvento, 0.0);
		                reporte.put(claveEvento, gananciaEventoActual + gananciaEsteTiquete);
		                
		                //Sumar a las ganancias por organizador
		                OrganizadorEventos promotor = evento.getPromotor();
		                double gananciaPromotorActual = gananciasPorOrganizador.getOrDefault(promotor, 0.0);
		                gananciasPorOrganizador.put(promotor, gananciaPromotorActual + gananciaEsteTiquete);
		            }
		        }
		    }

		    // Guardar las ganancias totales
		    reporte.put("GANANCIA_TOTAL_TIQUETERA", gananciasTotales);

		    // Guardar las ganancias por organizador en el reporte principal
		    for (Map.Entry<OrganizadorEventos, Double> entry : gananciasPorOrganizador.entrySet()) {
		        String clavePromotor = "GANANCIA_PROMOTOR_" + entry.getKey().getLogIn();
		        reporte.put(clavePromotor, entry.getValue());
		    }
		    return reporte;
		}
	
	@Override
	public void transferirTiquete(Tiquete tiquete,String passwordConfirmacion, String loginDestinatario, List<Usuario> todosLosUsuarios) {
	    // Vacío
	}
	
}
