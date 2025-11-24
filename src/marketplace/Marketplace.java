package marketplace;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cliente.Administrador;
import cliente.Usuario;
import excepciones.FondosInsuficientesException;
import persistencia.MarketplaceDAO; // 1. IMPORTAR EL DAO
import tiquetes.Deluxe;
import tiquetes.Tiquete;

public class Marketplace {

    private List<OfertaReventa> ofertasActivas;
    private List<String> logDeRegistros;
    
    // 2. ATRIBUTO PARA EL DAO
    private MarketplaceDAO dao;

    public Marketplace() {
        this.ofertasActivas = new ArrayList<>();
        this.logDeRegistros = new ArrayList<>();
        
        // 3. INICIALIZAR DAO Y CREAR TABLAS
        this.dao = new MarketplaceDAO();
        this.dao.crearTablasSiNoExisten(); 
    }
    
    // Setter para cargar el historial desde la BD al iniciar la App
    public void setLogDeRegistros(List<String> logsGuardados) {
        this.logDeRegistros = logsGuardados;
    }

    // --- MÉTODOS DE LOG ---

    private void registrarEnLog(String accion) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String registro = String.format("[%s] %s", ahora.format(formatter), accion);
        
        this.logDeRegistros.add(registro);
        System.out.println("LOG MARKETPLACE: " + registro);
        
        // 4. GUARDAR LOG EN BD
        this.dao.guardarLog(registro);
    }

    // Solo el administrador puede ver el log
    public List<String> consultarLog(Usuario solicitante) throws Exception {
        if (!(solicitante instanceof Administrador)) {
            throw new Exception("Acceso denegado: Solo el administrador puede ver el log.");
        }
        return logDeRegistros;
    }

    // --- MÉTODOS PRINCIPALES ---

    public void publicarOferta(Usuario vendedor, Tiquete tiquete, double precio) throws Exception {
        if (!tiquete.getCliente().equals(vendedor)) {
            throw new Exception("No puedes vender un tiquete que no te pertenece.");
        }

        if (tiquete instanceof Deluxe) {
            throw new Exception("Los tiquetes Deluxe no se pueden revender.");
        }
        
        if (!tiquete.getEstado().equals("ACTIVO") && !tiquete.getEstado().equals("TRANSFERIDO")) {
             throw new Exception("El tiquete no es válido para reventa (Estado: " + tiquete.getEstado() + ")");
        }

        OfertaReventa nuevaOferta = new OfertaReventa(tiquete, precio);
        
        // 5. GUARDAR OFERTA EN BD (Antes de añadirla a la lista)
        try {
            this.dao.guardarOferta(nuevaOferta);
            this.ofertasActivas.add(nuevaOferta);
        } catch (Exception e) {
            throw new Exception("Error guardando oferta en Base de Datos: " + e.getMessage());
        }
        
        registrarEnLog("OFERTA CREADA: Usuario " + vendedor.getLogIn() + " publicó tiquete " + tiquete.getIdTiquete() + " por $" + precio);
    }

    public void eliminarOferta(Usuario solicitante, OfertaReventa oferta) throws Exception {
        boolean esDueno = oferta.getVendedor().equals(solicitante);
        boolean esAdmin = solicitante instanceof Administrador;

        if (!esDueno && !esAdmin) {
            throw new Exception("No tienes permiso para borrar esta oferta.");
        }

        // 6. ELIMINAR DE BD
        try {
            this.dao.eliminarOferta(oferta.getTiquete().getIdTiquete());
            
            oferta.setActiva(false);
            this.ofertasActivas.remove(oferta);
            
            String quien = esAdmin ? "ADMINISTRADOR" : "Usuario " + solicitante.getLogIn();
            registrarEnLog("OFERTA ELIMINADA: Por " + quien + ". Tiquete: " + oferta.getTiquete().getIdTiquete());
            
        } catch (Exception e) {
            throw new Exception("Error eliminando oferta de BD: " + e.getMessage());
        }
    }

    public void comprarOferta(Usuario comprador, OfertaReventa oferta) throws FondosInsuficientesException, Exception {
        if (!oferta.isActiva()) {
            throw new Exception("La oferta ya no está disponible.");
        }
        
        if (comprador.getSaldo() < oferta.getPrecio()) {
            throw new FondosInsuficientesException("Saldo insuficiente para comprar esta oferta.");
        }

        procesarTransaccion(comprador, oferta, oferta.getPrecio());
        
        registrarEnLog("VENTA DIRECTA: Comprador " + comprador.getLogIn() + " compró tiquete " + oferta.getTiquete().getIdTiquete() + " por $" + oferta.getPrecio());
    }

    public void realizarContraoferta(Usuario comprador, OfertaReventa oferta, double montoOfrecido) {
        registrarEnLog("CONTRAOFERTA: Usuario " + comprador.getLogIn() + " ofreció $" + montoOfrecido + " por tiquete " + oferta.getTiquete().getIdTiquete() + " (Precio original: " + oferta.getPrecio() + ")");
    }

    public void aceptarContraoferta(Usuario vendedor, Usuario comprador, OfertaReventa oferta, double montoAcordado) throws Exception {
        if (!oferta.getVendedor().equals(vendedor)) {
            throw new Exception("Solo el vendedor puede aceptar contraofertas.");
        }
        
        if (comprador.getSaldo() < montoAcordado) {
            registrarEnLog("VENTA FALLIDA: Aceptación de contraoferta falló por saldo insuficiente del comprador " + comprador.getLogIn());
            throw new FondosInsuficientesException("El comprador ya no tiene saldo suficiente.");
        }

        procesarTransaccion(comprador, oferta, montoAcordado);
        
        registrarEnLog("CONTRAOFERTA ACEPTADA: Vendedor aceptó $" + montoAcordado + " de " + comprador.getLogIn() + " por tiquete " + oferta.getTiquete().getIdTiquete());
    }

    // Método auxiliar privado para mover el dinero y el tiquete
    private void procesarTransaccion(Usuario comprador, OfertaReventa oferta, double monto) {
        Usuario vendedor = oferta.getVendedor();
        Tiquete tiquete = oferta.getTiquete();

        // A. Mover dinero
        comprador.setSaldo(comprador.getSaldo() - monto);
        vendedor.setSaldo(vendedor.getSaldo() + monto);

        // B. Actualizar Listas de Inventario (Sacar del vendedor)
        if (vendedor instanceof cliente.UsuarioComprador) {
            ((cliente.UsuarioComprador) vendedor).getTiquetesComprados().remove(tiquete);
        } else if (vendedor instanceof cliente.OrganizadorEventos) {
            ((cliente.OrganizadorEventos) vendedor).getTiquetesComprados().remove(tiquete);
        }

        // C. Cambiar dueño en el tiquete
        tiquete.setCliente(comprador);
        
        // D. Agregar a la lista del Comprador
        if (comprador instanceof cliente.UsuarioComprador) {
            ((cliente.UsuarioComprador) comprador).getTiquetesComprados().add(tiquete);
        } else if (comprador instanceof cliente.OrganizadorEventos) {
            ((cliente.OrganizadorEventos) comprador).getTiquetesComprados().add(tiquete);
        }

        // 7. ELIMINAR DE BD (Porque ya se vendió, sale del marketplace)
        try {
            this.dao.eliminarOferta(tiquete.getIdTiquete());
        } catch (Exception e) {
            System.err.println("Error crítico: No se pudo borrar oferta vendida de BD.");
        }

        // E. Cerrar oferta en memoria
        oferta.setActiva(false);
        this.ofertasActivas.remove(oferta);
    }
    
    public List<OfertaReventa> getOfertasActivas() {
        return ofertasActivas;
    }
    
	
	
}