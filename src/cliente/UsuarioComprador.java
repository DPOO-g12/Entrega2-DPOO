package cliente;

import eventos.Evento;
import localidades.Localidades;
import tiquetes.Basico;
import tiquetes.Tiquete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Importante para el cambio

import excepciones.AutenticacionFallidaException;
import excepciones.CapacidadExcedidaLocalidad;
import excepciones.FondosInsuficientesException;
import excepciones.TiqueteNoTransferibleException;

public class UsuarioComprador extends Usuario {
    
    private List<Tiquete> tiquetesComprados;

    public UsuarioComprador(String logIn, String contrasena, double saldo) {
        super(logIn, contrasena, saldo);
        this.tiquetesComprados = new ArrayList<>();
    }

    public List<Tiquete> getTiquetesComprados() {
        return tiquetesComprados;
    }
    
    public void agregarTiquete(Tiquete t) {
        tiquetesComprados.add(t);
    }

    @Override
    public List<Tiquete> comprarTiquete(Localidades localidad, int cantidad, double porcentajeServicio, double cobroEmision) throws CapacidadExcedidaLocalidad, FondosInsuficientesException {
        double precioBaseUnitario = localidad.getPrecioFinal();
        double costoServicioUnitario = precioBaseUnitario * porcentajeServicio;
        double precioFinalunitario = precioBaseUnitario + costoServicioUnitario + cobroEmision;
        
        double costoTotalTransaccion = precioFinalunitario * cantidad;
        
        Evento evento = localidad.getEvento();
        
        if (!localidad.verificarDisponibilidad(cantidad)) {
            throw new CapacidadExcedidaLocalidad(
                "No hay disponibilidad para " + cantidad + 
                " tiquetes en esta localidad: " + localidad.getNombreLocalidad());
        }
        
        if (this.saldo < costoTotalTransaccion) {
            throw new FondosInsuficientesException("No cuenta con los fondos suficientes para realizar la compra");
        }

        this.saldo = saldo - costoTotalTransaccion;
        
        List<String> asientosAsignados = localidad.venderTiquetes(cantidad);
        
        List<Tiquete> tiquetesCreados = new ArrayList<>();
        
        for (String asiento : asientosAsignados) {
            Tiquete nuevoTiquete = new Basico(precioBaseUnitario, porcentajeServicio, cobroEmision, java.time.LocalDate.now().toString(), this, localidad, evento, "ACTIVO", asiento);  
            this.tiquetesComprados.add(nuevoTiquete);
            tiquetesCreados.add(nuevoTiquete);
        }
        
        return tiquetesCreados;
    }

    @Override
    public void pedirRembolso(Tiquete tiquete) throws TiqueteNoTransferibleException {
        // 1. Validar que el tiquete le pertenece
        if (!this.tiquetesComprados.contains(tiquete)) {
            throw new TiqueteNoTransferibleException("No puedes pedir reembolso de un tiquete que no te pertenece.");
        }
        
        String estado = tiquete.getEstado();
        if (!estado.equals("ACTIVO")) {
            throw new TiqueteNoTransferibleException("Solo puedes pedir reembolso de tiquetes 'ACTIVO'. Estado actual: " + estado);
        }
    
        tiquete.setEstado("PENDIENTE_REEMBOLSO");
    }
    
    /**
     * MÉTODO CORREGIDO:
     * 1. Usa Map<String, Usuario> para búsqueda rápida.
     * 2. Elimina el tiquete de 'this.tiquetesComprados' (IMPORTANTE).
     * 3. Lo agrega a la lista del destinatario.
     */
    @Override
    public void transferirTiquete(Tiquete tiquete, String passwordConfirmacion, String loginDestinatario, Map<String, Usuario> mapaUsuarios) throws AutenticacionFallidaException, TiqueteNoTransferibleException, Exception {
        
        // 1. Validaciones
        if (!this.tiquetesComprados.contains(tiquete)) {
            throw new TiqueteNoTransferibleException("El tiquete " + tiquete.getIdTiquete() + " no te pertenece.");
        }
        
        if (!this.contrasena.equals(passwordConfirmacion)) {
            throw new AutenticacionFallidaException("Contraseña incorrecta. Transferencia cancelada.");
        }
        
        // 2. Buscar Destinatario (Usando MAPA, más eficiente)
        Usuario destinatario = mapaUsuarios.get(loginDestinatario);
        
        if (destinatario == null) {
            throw new Exception("Usuario destinatario '" + loginDestinatario + "' no encontrado.");
        }
        
        // Evitar auto-transferencia
        if (destinatario.getLogIn().equals(this.getLogIn())) {
            throw new Exception("No puedes transferirte a ti mismo.");
        }

        if (destinatario instanceof cliente.Administrador) {
            throw new TiqueteNoTransferibleException("No se pueden transferir tiquetes a un Administrador.");
        }
        
        // 3. MOVIMIENTO EN MEMORIA (La clave para que desaparezca de tu lista)
        
        // A. Cambiar dueño interno del tiquete
        tiquete.transferirTiquete(destinatario);
        
        // B. Quitar de MI lista
        this.tiquetesComprados.remove(tiquete);
        
        // C. Agregar a la lista del DESTINATARIO
        if (destinatario instanceof UsuarioComprador) {
            ((UsuarioComprador) destinatario).agregarTiquete(tiquete);
        } else if (destinatario instanceof OrganizadorEventos) {
            ((OrganizadorEventos) destinatario).getTiquetesComprados().add(tiquete);
        }
        
        System.out.println("Transferencia en memoria completada: " + this.getLogIn() + " -> " + loginDestinatario);
    }
}
