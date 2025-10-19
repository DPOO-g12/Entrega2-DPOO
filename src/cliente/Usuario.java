package cliente;


import tiquetes.Tiquete;

import java.util.List;

import excepciones.AutenticacionFallidaException;
import excepciones.TiqueteNoTransferibleException;
import localidades.Localidades;

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
	public abstract List<Tiquete> comprarTiquete(Localidades localidad, int cantidad, double porcentajeServicio, double cobroEmision ) throws Exception;

	public abstract void pedirRembolso(Tiquete tiquete) throws TiqueteNoTransferibleException;
	
	public abstract void transferirTiquete(Tiquete tiquete,String passwordConfirmacion, String loginDestinatario, List<Usuario> todosLosUsuarios) throws AutenticacionFallidaException, TiqueteNoTransferibleException, Exception;
	
	
	
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
