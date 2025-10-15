package tiquetes;

public abstract class Tiquete {
	
	private double precio;
	private int idTiquete;
	private String Fecha;
	private String hora;
	private double cargoServicio;
	public static final double EMISION = 0;
	private boolean usado;
	
	
	
	public double getPrecio() {
		return precio;
	}
	public int getIdTiquete() {
		return idTiquete;
	}
	public String getFecha() {
		return Fecha;
	}
	public String getHora() {
		return hora;
	}
	public double getCargoServicio() {
		return cargoServicio;
	}
	public static double getEmision() {
		return EMISION;
	}
	public boolean isUsado() {
		return usado;
	}
	
	
	
	
	
}
