package cliente;
import java.util.HashMap;

import eventos.Evento;
import eventos.Venue;
import tiquetes.Tiquete;

import java.util.*;


public class OrganizadorEventos extends Usuario {

	private List<Evento> eventosOrganizados;
	
	public OrganizadorEventos(String logIn, String contrasena, double saldo) {
		super(logIn, contrasena, saldo);
		this.eventosOrganizados = new ArrayList<>();
	}

	

	
	public List<Evento> getEventosOrganizados(){
		return eventosOrganizados;
	}

	public void agregarEvento(Evento evento) {
		this.eventosOrganizados.add(evento);
	}


	
	
	public Map<String, Double> calcularEstadoFinanciero(){
		Map<String, Double> reporte = new HashMap<>();
		return reporte;
	}
	
	
	@Override
	public void comprarTiquete(Evento evento, int Cantidad) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void pedirRembolso(Tiquete tiquete) {
		// TODO Auto-generated method stub
		
	}










}