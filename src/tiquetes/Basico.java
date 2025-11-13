package tiquetes;

import cliente.UsuarioComprador;
import eventos.Evento;
import localidades.Localidades;
import cliente.Usuario;

public class Basico extends Tiquete {
	
	private String numeroAsiento;

	public Basico(double precioBase, double porcentajeServicio, double cobroFijoEmision, String fecha,
			Usuario cliente, Localidades localidad, Evento evento, String estado, String numeroAsiento) {
		super(precioBase, porcentajeServicio, cobroFijoEmision, fecha, cliente, localidad, evento, estado);
		
		this.numeroAsiento = numeroAsiento;
	}
	
	public static Basico cargarDesdeDB(
            int id_db, String id_java, double pBase, double pServicio, double pEmision, 
            double pFinal, String fecha, String estado, boolean transferible, 
            Usuario cliente, Localidades loc, Evento evt, String numeroAsiento) {
        
        // 1. Llama al constructor "tonto" de Tiquete
        Basico t = new Basico(pBase, pServicio, pEmision, fecha, cliente, loc, evt, estado, numeroAsiento);
        
        // 2. Seteamos los valores que el constructor normal no inicializa
        t.id_tiquete_db = id_db;
        t.idTiquete = id_java;
        t.precioFinal = pFinal;
        t.transferible = transferible;
        
        return t;
    }

	@Override
	public String getTipoTiquete() {
		return "BASICO";
	}
	
	
	public String getNumeroAsiento(){
		return numeroAsiento;
	}
	
	

}
