package tiquetes;
import java.util.List;
import java.util.ArrayList;
import cliente.Usuario;
import eventos.Evento;
import localidades.Localidades;


public class Deluxe extends Tiquete {
	
	private List<String> beneficiosAdicionales;

	public Deluxe(double precioBase, double porcentajeServicio, double cobroFijoEmision, String fecha,
			Usuario cliente, Localidades localidad, Evento evento, String estado,
			List<String> beneficiosAdicionales) {
		super(precioBase, porcentajeServicio, cobroFijoEmision, fecha, cliente, localidad, evento, estado);
		
		
		if (beneficiosAdicionales == null) {
			this.beneficiosAdicionales = new ArrayList<>();
		} else { 
			this.beneficiosAdicionales = beneficiosAdicionales;
		}
		
	
		this.transferible = false;

	}
	
	public static Deluxe cargarDesdeDB(
            int id_db, String id_java, double pBase, double pServicio, double pEmision, 
            double pFinal, String fecha, String estado, boolean transferible, 
            Usuario cliente, Localidades loc, Evento evt, List<String> beneficios) {
        
        Deluxe t = new Deluxe(pBase, pServicio, pEmision, fecha, cliente, loc, evt, estado, beneficios);
        
        t.id_tiquete_db = id_db;
        t.idTiquete = id_java;
        t.precioFinal = pFinal;
        t.transferible = transferible; 
        return t;
    }

	@Override
	public String getTipoTiquete() {
		return "DELUXE";
	} 
	
	
	public List<String> getBeneficiosAdicionales(){
		return beneficiosAdicionales;
	}

}
