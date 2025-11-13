package tiquetes;
import java.util.List;
import cliente.Usuario;
import eventos.Evento;
import java.util.ArrayList;
import excepciones.TiqueteNoTransferibleException;

public class Multiple extends Tiquete  {

	private List<Tiquete> tiquetesIncluidos;

	private List<Evento> eventosAsociados;

	public Multiple(double precioTotalPaquete, String fecha,
			Usuario cliente, String estado,
			List<Tiquete> tiquetesIncluidos, List<Evento> eventosAsociados ) {


		super(precioTotalPaquete, 0.0, 0.0, fecha, cliente, null, null,estado);

		if (tiquetesIncluidos == null) {
			this.tiquetesIncluidos = new ArrayList<>();
		} else {
			this.tiquetesIncluidos = tiquetesIncluidos;
		}

		if (eventosAsociados == null) {
			this.eventosAsociados = new ArrayList<>();
		} else {
			this.eventosAsociados = eventosAsociados;
		}


	}

	public static Multiple cargarDesdeDB(
			int id_db, String id_java, double pBase, double pServicio, double pEmision, 
			double pFinal, String fecha, String estado, boolean transferible, 
			Usuario cliente, List<Tiquete> tiquetesInc, List<Evento> eventosAso) {

		Multiple t = new Multiple(pBase, fecha, cliente, estado, tiquetesInc, eventosAso);

		t.id_tiquete_db = id_db;
		t.idTiquete = id_java;
		t.precioFinal = pFinal;
		t.transferible = transferible;
		t.costoServicio = pServicio;
		t.costoEmision = pEmision;

		return t;
	}



	@Override
	public String getTipoTiquete() {
		return "MULTIPLE";
	}


	@Override
	public void transferirTiquete(Usuario nuevoCliente) throws TiqueteNoTransferibleException {


		for (Tiquete t : this.tiquetesIncluidos) {
			String estadoHijo = t.getEstado();
			if (estadoHijo.equals("VENCIDO") || estadoHijo.equals("TRANSFERIDO")) {


				throw new TiqueteNoTransferibleException("No se puede transferir el paquete: " 
						+ "El tiquete interno " + t.getIdTiquete() + " ya fue vencido o transferido.");
			}
		}

		super.transferirTiquete(nuevoCliente);

		for (Tiquete t : tiquetesIncluidos) {
			try {
				t.transferirTiquete(nuevoCliente); 
			} catch (TiqueteNoTransferibleException e) {

				System.err.println("Error inesperado al transferir tiquete crack: " + e.getMessage());
			}
		}
	}



	public List<Tiquete> getTiquetesIncluidos() {
		return tiquetesIncluidos;
	}

	public List<Evento> getEventosAsociados() {
		return eventosAsociados;
	}

	public void transferirUnoDeMisTiquetes(String idTiqueteHijo, Usuario nuevoCliente) 
			throws TiqueteNoTransferibleException {

		//Validar que el PAQUETE no ha sido transferido.
		if (this.getEstado().equals("TRANSFERIDO")) {
			throw new TiqueteNoTransferibleException(
					"El paquete completo ya ha sido transferido. No puede modificar sus componentes."
					);
		}

		//Encontrar el tiquete hijo por ID (usando su ID único)
		Tiquete tiqueteHijo = null;
		for (Tiquete t : tiquetesIncluidos) {
			if (t.getIdTiquete().equals(idTiqueteHijo)) {
				tiqueteHijo = t;
				break;
			}
		}

		if (tiqueteHijo == null) {
			throw new TiqueteNoTransferibleException("Tiquete hijo con ID " + idTiqueteHijo + " no encontrado en el paquete.");
		}

		//Ejecutar la transferencia del hijo
		//El método transferirTiquete del hijo se encarga de cambiar su estado y cliente.
		tiqueteHijo.transferirTiquete(nuevoCliente);

		// 4. Regla de Negocio: Opcional. Si quieres que el paquete padre cambie de estado 
		//    después de la primera transferencia individual, lo harías aquí. 
		//    Por ahora, el padre permanece 'ACTIVO' como discutimos.
	}

}
