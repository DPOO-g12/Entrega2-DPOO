package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import app.TiqueteraApp;
import cliente.UsuarioComprador;
import eventos.Evento;
import localidades.Localidades;
import tiquetes.Tiquete;

public class VentanaCompra extends JFrame {

    private TiqueteraApp nucleo;
    private UsuarioComprador usuario;
    
    // Componentes de la pantalla
    private JComboBox<String> cbEventos;
    private JComboBox<String> cbLocalidades;
    private JTextField txtCantidad;
    private JLabel lblPrecio;
    private JButton btnConfirmar;
    
    // Listas para saber qué objeto seleccionó el usuario
    private List<Evento> eventosActivos;
    private List<Localidades> localidadesActuales;

    public VentanaCompra(TiqueteraApp nucleo, UsuarioComprador usuario) {
        this.nucleo = nucleo;
        this.usuario = usuario;
        this.eventosActivos = new ArrayList<>();
        this.localidadesActuales = new ArrayList<>();
        
        configurarVentana();
        iniciarComponentes();
        cargarEventos(); // Llenar la lista apenas abre
    }

    private void configurarVentana() {
        setTitle("Comprar Tiquetes - TICKETGOD");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana
        setLayout(new GridBagLayout());
    }

    private void iniciarComponentes() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 1. Elegir Evento
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Selecciona Evento:"), gbc);
        
        cbEventos = new JComboBox<>();
        gbc.gridx = 1;
        add(cbEventos, gbc);
        
        // Cuando cambie el evento, cargamos sus localidades
        cbEventos.addActionListener(e -> cargarLocalidades());

        // 2. Elegir Localidad
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Localidad:"), gbc);
        
        cbLocalidades = new JComboBox<>();
        gbc.gridx = 1;
        add(cbLocalidades, gbc);
        
        // Cuando cambie la localidad, actualizamos el precio visual
        cbLocalidades.addActionListener(e -> actualizarPrecioVisual());

        // 3. Cantidad
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Cantidad:"), gbc);
        
        txtCantidad = new JTextField("1");
        gbc.gridx = 1;
        add(txtCantidad, gbc);

        // 4. Precio
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        lblPrecio = new JLabel("Precio Unitario: $0.0");
        lblPrecio.setFont(new Font("Arial", Font.BOLD, 14));
        lblPrecio.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblPrecio, gbc);

        // 5. Botón Confirmar
        btnConfirmar = new JButton("COMPRAR AHORA");
        btnConfirmar.setBackground(new Color(0, 150, 0));
        btnConfirmar.setForeground(Color.BLACK);
        gbc.gridy = 4;
        add(btnConfirmar, gbc);
        
        btnConfirmar.addActionListener(e -> procesarCompra());
    }

    private void cargarEventos() {
        cbEventos.removeAllItems();
        eventosActivos.clear();
        
        // Usamos el getter que añadimos a TiqueteraApp
        for (Evento e : nucleo.getEventos()) {
            if ("ACTIVO".equalsIgnoreCase(e.getEstado())) {
                eventosActivos.add(e);
                cbEventos.addItem(e.getNombre());
            }
        }
    }

    private void cargarLocalidades() {
        cbLocalidades.removeAllItems();
        localidadesActuales.clear();
        
        int indiceEvento = cbEventos.getSelectedIndex();
        if (indiceEvento >= 0 && indiceEvento < eventosActivos.size()) {
            Evento eventoSel = eventosActivos.get(indiceEvento);
            
            for (Localidades loc : eventoSel.getLocalidades().values()) {
                localidadesActuales.add(loc);
                cbLocalidades.addItem(loc.getNombreLocalidad());
            }
        }
        actualizarPrecioVisual();
    }
    
    private void actualizarPrecioVisual() {
        int indiceLoc = cbLocalidades.getSelectedIndex();
        if (indiceLoc >= 0 && indiceLoc < localidadesActuales.size()) {
            double precio = localidadesActuales.get(indiceLoc).getPrecioFinal();
            lblPrecio.setText("Precio Unitario: $" + precio);
        }
    }

    private void procesarCompra() {
        try {
            int indiceLoc = cbLocalidades.getSelectedIndex();
            if (indiceLoc < 0) return;
            
            int cantidad = Integer.parseInt(txtCantidad.getText());
            Localidades locSel = localidadesActuales.get(indiceLoc);
            
            // --- LÓGICA DEL NEGOCIO ---
            // Usamos tu método existente de comprarTiquete
            // Asumimos un porcentaje de servicio fijo por ahora (o lo traes del admin)
            double cobroEmision = 5000.0; // Valor ejemplo o nucleo.getAdmin().getCobro...
            double porcServicio = 0.15;

            List<Tiquete> tiquetesNuevos = usuario.comprarTiquete(locSel, cantidad, porcServicio, cobroEmision);
            
            // Guardar en BD cada tiquete
            for (Tiquete t : tiquetesNuevos) {
                nucleo.getTiqueteDAO().guardarTiquete(t);
            }
            // Actualizar saldo usuario en BD
            nucleo.getUsuarioDAO().actualizarSaldo(usuario);
            
            JOptionPane.showMessageDialog(this, "¡Compra Exitosa!");
            this.dispose(); // Cierra la ventana
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}