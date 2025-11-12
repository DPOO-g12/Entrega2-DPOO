package marketplace;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cliente.Administrador;
import cliente.UsuarioComprador;

public class Marketplace {

	private Map<String, OfertaMarketplace> ofertas = new HashMap<>();
	private ArrayList<LogRegistro> logs = new ArrayList<>();

	public void crearOferta(OfertaMarketplace oferta) {
		ofertas.put(oferta.getId(), oferta);
		registrarLog("Oferta creada por " + oferta.getVendedor().getLogIn());
	}

	public void eliminarOferta(String id, UsuarioComprador cliente) {
		OfertaMarketplace oferta = ofertas.get(id);
		if (oferta != null && oferta.getVendedor().equals(cliente)) {
			oferta.setActiva(false);
			registrarLog("Oferta eliminada por " + cliente.getLogIn());
		}
	}

	public void contraofertar(String idOferta, ContraOferta contraoferta) {
		registrarLog("Contraoferta de " + contraoferta.getComprador().getLogIn() + " sobre oferta " + idOferta
				+ " por $" + contraoferta.getNuevoPrecio());
	}

	public void aceptarContraoferta(ContraOferta contraoferta) {
		contraoferta.setEstado("ACEPTADA");
		registrarLog("Contraoferta aceptada para la oferta " + contraoferta.getOfertaOriginal().getId());

	}

	private void registrarLog(String descripcion) {
		logs.add(new LogRegistro(LocalDateTime.now(), descripcion, "Sistema"));
	}

	public ArrayList<LogRegistro> getLogs() {
		return logs;
	}

	public void eliminarOfertaPorAdmin(String id, Administrador admin) {
		OfertaMarketplace oferta = ofertas.get(id);
		if (oferta != null) {
			oferta.setActiva(false);
			registrarLog("Oferta eliminada por el administrador " + admin.getLogIn());
		}
	}

	public void concretarVenta(OfertaMarketplace oferta, UsuarioComprador comprador, Double precioFinal) {
		if (!oferta.isActiva()) {
			registrarLog("Intento de compra fallido: la oferta " + oferta.getId() + " ya no está activa.");
			return;
		}

		UsuarioComprador vendedor = oferta.getVendedor();

		if (comprador.getSaldo() < precioFinal) {
			registrarLog("Compra fallida: saldo insuficiente de " + comprador.getLogIn());
			return;
		}

		comprador.setSaldo(comprador.getSaldo() - precioFinal);
		vendedor.setSaldo(vendedor.getSaldo() + precioFinal);

		oferta.setActiva(false);

		registrarLog("Venta concretada: " + comprador.getLogIn() + " compró la boleta de " + vendedor.getLogIn()
				+ " por $" + precioFinal);
	}

	public void concretarVentaContraOferta(ContraOferta contraoferta) {
		concretarVenta(contraoferta.getOfertaOriginal(), contraoferta.getComprador(), contraoferta.getNuevoPrecio());
		contraoferta.setEstado("ACEPTADA");
		registrarLog(
				"Contraoferta aceptada y venta concretada sobre oferta " + contraoferta.getOfertaOriginal().getId());
	}

}
