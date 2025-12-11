package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import app.TiqueteraApp;
import cliente.UsuarioComprador;
import eventos.Evento;
import localidades.Localidades;
import tiquetes.Basico;
import tiquetes.Deluxe;
import tiquetes.Tiquete;

public class VentanaCompra extends JFrame {

    private TiqueteraApp nucleo;
    private UsuarioComprador comprador;
    
    private JComboBox<String> comboEventos;
    private JComboBox<String> comboLocalidades;
    private JTextField txtCantidad;
    private JCheckBox chkDeluxe; 
    private JLabel lblPrecioTotal; // Para mostrar el cálculo en tiempo real

    public VentanaCompra(TiqueteraApp nucleo, UsuarioComprador comprador) {
        this.nucleo = nucleo;
        this.comprador = comprador;
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Comprar Tiquetes - TICKETGOD");
        setSize(450, 480);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 1, 10, 10)); 
    }

    private void iniciarComponentes() {
        // 1. Selector de Evento
        JPanel pnlEvento = new JPanel(new FlowLayout());
        pnlEvento.add(new JLabel("Evento:"));
        
        List<Evento> activos = new ArrayList<>();
        for(Evento e : nucleo.getEventos()) if("ACTIVO".equalsIgnoreCase(e.getEstado())) activos.add(e);
        
        String[] nombresEvt = new String[activos.size()];
        for(int i=0; i<activos.size(); i++) nombresEvt[i] = activos.get(i).getNombre();
        comboEventos = new JComboBox<>(nombresEvt);
        comboEventos.addActionListener(e -> cargarLocalidades());
        
        pnlEvento.add(comboEventos);
        add(pnlEvento);

        // 2. Selector de Localidad
        JPanel pnlLoc = new JPanel(new FlowLayout());
        pnlLoc.add(new JLabel("Localidad:"));
        comboLocalidades = new JComboBox<>();
        comboLocalidades.addActionListener(e -> actualizarPrecioEstimado()); // Actualizar precio al cambiar
        pnlLoc.add(comboLocalidades);
        add(pnlLoc);

        // 3. Cantidad
        JPanel pnlCant = new JPanel(new FlowLayout());
        pnlCant.add(new JLabel("Cantidad:"));
        txtCantidad = new JTextField("1", 5);
        pnlCant.add(txtCantidad);
        add(pnlCant);

        // 4. OPCIÓN DELUXE (Oculta por defecto)
        JPanel pnlDeluxe = new JPanel(new FlowLayout());
        chkDeluxe = new JCheckBox("Mejorar a paquete DELUXE (+ $50.000 c/u)");
        chkDeluxe.setToolTipText("Incluye Souvenirs y Acceso Rápido. NO TRANSFERIBLE.");
        chkDeluxe.setVisible(false); // <--- INVISIBLE POR DEFECTO
        chkDeluxe.addActionListener(e -> actualizarPrecioEstimado());
        pnlDeluxe.add(chkDeluxe);
        add(pnlDeluxe);
        
        // 5. Label de Precio
        JPanel pnlPrecio = new JPanel(new FlowLayout());
        lblPrecioTotal = new JLabel("Total Estimado: $0");
        lblPrecioTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblPrecioTotal.setForeground(new Color(0, 100, 0));
        pnlPrecio.add(lblPrecioTotal);
        add(pnlPrecio);

        // 6. Botón Comprar
        JButton btnComprar = new JButton("Confirmar Compra");
        btnComprar.setBackground(new Color(50, 205, 50));
        btnComprar.setOpaque(true);
        btnComprar.addActionListener(e -> ejecutarCompra());
        add(btnComprar);

        if (activos.size() > 0) cargarLocalidades();
    }

    private void cargarLocalidades() {
        comboLocalidades.removeAllItems();
        String nomEvt = (String) comboEventos.getSelectedItem();
        if(nomEvt == null) return;

        Evento evt = null;
        for(Evento e : nucleo.getEventos()) if(e.getNombre().equals(nomEvt)) evt = e;

        if (evt.getLocalidades() != null) {
            for(Localidades l : evt.getLocalidades().values()) {
                double precio = l.getPrecioFinal();
                comboLocalidades.addItem(l.getNombreLocalidad() + " ($" + precio + ")");
            }
        }
        
        // Resetear visualización Deluxe
        chkDeluxe.setSelected(false);
        chkDeluxe.setVisible(false); 
    }

    /**
     * Lógica clave: Decide si muestra la opción Deluxe y calcula el total.
     */
    private void actualizarPrecioEstimado() {
        String itemLoc = (String) comboLocalidades.getSelectedItem();
        if (itemLoc == null) return;
        
        // Limpiar el nombre para buscar el objeto (quitamos el precio visual)
        String nombreLocReal = itemLoc.split(" \\(")[0];
        
        try {
            String nomEvt = (String) comboEventos.getSelectedItem();
            Evento evt = null;
            for(Evento e : nucleo.getEventos()) if(e.getNombre().equals(nomEvt)) evt = e;
            
            Localidades loc = evt.getLocalidades().get(nombreLocReal);
            
            // --- LÓGICA DE VISIBILIDAD DELUXE ---
            if (loc.isDeluxeHabilitado()) {
                chkDeluxe.setVisible(true);
                chkDeluxe.setText("Mejorar a DELUXE (+" + String.format("%,.0f", loc.getDeluxePrecioExtra()) + ")");
                chkDeluxe.setToolTipText("Incluye: " + loc.getDeluxeBeneficios());
                
                // Verificar si quedan cupos Deluxe disponibles
                int cantDeseada = 1;
                try { cantDeseada = Integer.parseInt(txtCantidad.getText()); } catch(Exception e){}
                
                if (!loc.hayCupoDeluxe(cantDeseada)) {
                    chkDeluxe.setEnabled(false);
                    chkDeluxe.setText("Deluxe AGOTADO");
                    chkDeluxe.setSelected(false);
                } else {
                    chkDeluxe.setEnabled(true);
                }
            } else {
                // Si esta localidad no tiene config Deluxe, ocultamos la opción
                chkDeluxe.setVisible(false);
                chkDeluxe.setSelected(false);
            }

            // --- CÁLCULO DE PRECIO ---
            int cant = Integer.parseInt(txtCantidad.getText());
            double unitario = loc.getPrecioFinal();
            
            if (chkDeluxe.isSelected()) {
                unitario += loc.getDeluxePrecioExtra(); // Sumamos el extra real configurado
            }
            
            double total = unitario * cant;
            lblPrecioTotal.setText("Total Estimado: $" + String.format("%,.0f", total));
            
        } catch (NumberFormatException e) {
            lblPrecioTotal.setText("Total Estimado: ...");
        } catch (Exception e) {
            // Ignorar errores menores durante la escritura
        }
    }

    private void ejecutarCompra() {
        try {
            // A. Obtener datos
            String nomEvt = (String) comboEventos.getSelectedItem();
            String itemLoc = (String) comboLocalidades.getSelectedItem();
            if (nomEvt == null || itemLoc == null) return;
            
            String nombreLocReal = itemLoc.split(" \\(")[0];
            int cantidad = Integer.parseInt(txtCantidad.getText());
            boolean esDeluxe = chkDeluxe.isSelected(); 

            // B. Buscar Objetos
            Evento evento = null;
            for(Evento e : nucleo.getEventos()) if(e.getNombre().equals(nomEvt)) evento = e;
            Localidades localidad = evento.getLocalidades().get(nombreLocReal);

            // C. Validaciones
            if (!localidad.verificarDisponibilidad(cantidad)) {
                JOptionPane.showMessageDialog(this, "No hay cupo suficiente.");
                return;
            }
            
            if (esDeluxe && !localidad.hayCupoDeluxe(cantidad)) {
                JOptionPane.showMessageDialog(this, "No hay suficientes cupos DELUXE.");
                return;
            }

            // D. Calcular Costos
            double precioUnitario = localidad.getPrecioFinal();
            if (esDeluxe) precioUnitario += localidad.getDeluxePrecioExtra();

            double subtotal = precioUnitario * cantidad;
            
            if (comprador.getSaldo() < subtotal) {
                JOptionPane.showMessageDialog(this, "Saldo insuficiente. Costo: $" + subtotal);
                return;
            }

            // E. Ejecutar Venta
            List<String> asientos = localidad.venderTiquetes(cantidad);
            String fecha = java.time.LocalDate.now().toString();
            List<Tiquete> nuevosTiquetes = new ArrayList<>();

            for (String asiento : asientos) {
                Tiquete t;
                if (esDeluxe) {
                    List<String> beneficios = new ArrayList<>();
                    if (localidad.getDeluxeBeneficios() != null) {
                        for(String b : localidad.getDeluxeBeneficios().split(",")) beneficios.add(b.trim());
                    }
                    t = new Deluxe(precioUnitario, 0.0, 0.0, fecha, comprador, localidad, evento, "ACTIVO", beneficios);
                    localidad.setDeluxeVendidos(localidad.getDeluxeVendidos() + 1);
                } else {
                    t = new Basico(precioUnitario, 0.0, 0.0, fecha, comprador, localidad, evento, "ACTIVO", asiento);
                }
                nuevosTiquetes.add(t);
            }

            // F. Guardar en BD
            nucleo.registrarCortesias(nuevosTiquetes); 
            
            if (esDeluxe) new persistencia.LocalidadDAO().actualizarVendidosDeluxe(localidad);
            
            // --- ¡AQUÍ ESTÁ EL ARREGLO! ---
            // Agregar los tiquetes a la lista local del usuario para verlos YA MISMO
            for (Tiquete t : nuevosTiquetes) {
                comprador.agregarTiquete(t);
            }
            // -----------------------------
            
            // Descontar Saldo
            comprador.setSaldo(comprador.getSaldo() - subtotal);
            nucleo.getUsuarioDAO().actualizarSaldo(comprador);

            JOptionPane.showMessageDialog(this, "¡Compra Exitosa!\nHas adquirido " + cantidad + " tiquetes.");
            this.dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}