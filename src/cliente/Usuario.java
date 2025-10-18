package cliente;

import eventos.Evento;
import tiquetes.Tiquete;

public abstract class Usuario {
	
	protected String logIn;
	protected String contrasena;
	protected double saldo;

	
	
	
	public Usuario(String logIn, String contrasena, double saldo) {
		super();
		this.logIn = logIn;
		this.contrasena = contrasena;
		this.saldo = saldo;
	
	}
	public abstract void comprarTiquete(Evento evento, int Cantidad);
	public abstract void pedirRembolso(Tiquete tiquete);
	public String getLogIn() {
		return logIn;
	}
	public void setLogIn(String logIn) {
		this.logIn = logIn;
	}
	public String getContrasena() {
		return contrasena;
	}
	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}
	public double getSaldo() {
		return saldo;
	}
	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}
	
	
	
	
	

}
