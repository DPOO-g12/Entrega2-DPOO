package cliente;

import eventos.Evento;
import java.util.ArrayList;
import java.util.List;
import tiquetes.Tiquete;

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
	public void comprarTiquete(Evento evento, int Cantidad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pedirRembolso(Tiquete tiquete) {
		// TODO Auto-generated method stub
		
	}
	
	public void comprarTiqueteMultiple(int numeroEventos, int cantidadTiquetes) {
		
	}
	

}
