package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import app.TiqueteraApp;
import cliente.Administrador;
import eventos.Evento;
import eventos.Venue;
import tiquetes.Tiquete;

public class VentanaAdmin extends JFrame {

    private TiqueteraApp nucleo;
    private Administrador admin;

    public VentanaAdmin(TiqueteraApp nucleo, Administrador admin) {
        this.nucleo = nucleo;
        this.admin = admin;
        
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Panel ADMINISTRADOR - TICKETGOD");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // --- HEADER ---
        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(new Color(50, 50, 50)); // Gris oscuro
        JLabel lblTitulo = new JLabel("MODO ADMINISTRADOR");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        panelHeader.add(lblTitulo);
        add(panelHeader, BorderLayout.NORTH);

        // --- BOTONES ---
        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 20, 20));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JButton btnAprobarVenues = new JButton("1. Aprobar Venues Pendientes");
        JButton btnVerLogs = new JButton("2. Ver Log Auditoría Marketplace");
        JButton btnCancelarEvento = new JButton("3. Cancelar Evento (Emergencia)");
        JButton btnReporte = new JButton("4. Ver Reporte Financiero");

        // ACCIONES
        btnAprobarVenues.addActionListener(e -> accionAprobarVenue());
        btnVerLogs.addActionListener(e -> accionVerLogs());
        btnCancelarEvento.addActionListener(e -> accionCancelarEvento());
        btnReporte.addActionListener(e -> accionVerReporte());

        panelBotones.add(btnAprobarVenues);
        panelBotones.add(btnVerLogs);
        panelBotones.add(btnCancelarEvento);
        panelBotones.add(btnReporte);

        add(panelBotones, BorderLayout.CENTER);

        // --- FOOTER ---
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setForeground(Color.RED);
        btnLogout.addActionListener(e -> {
            this.dispose();
            new VentanaLogin(nucleo).setVisible(true);
        });
        add(btnLogout, BorderLayout.SOUTH);
    }

    // --- LÓGICA DE LOS BOTONES ---

    private void accionAprobarVenue() {
        // 1. Filtrar pendientes
        List<Venue> pendientes = new ArrayList<>();
        for (Venue v : nucleo.getVenues()) {
            if ("PENDIENTE".equalsIgnoreCase(v.getEstado())) {
                pendientes.add(v);
            }
        }

        if (pendientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay venues pendientes de aprobación.");
            return;
        }

        // 2. Mostrar selector
        String[] opciones = new String[pendientes.size()];
        for (int i = 0; i < pendientes.size(); i++) {
            opciones[i] = pendientes.get(i).getUbicacion() + " (" + pendientes.get(i).getTipo() + ")";
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, "Selecciona Venue a aprobar:", 
                "Aprobar Venue", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            // Buscar objeto
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) {
                    Venue v = pendientes.get(i);
                    try {
                        // Lógica
                        admin.aprobarVenue(v);
                        // Persistencia (Asumiendo que tienes este método en VenueDAO, o usamos guardarVenue si hace update)
                        // Si no tienes actualizar, puedes usar guardarVenue si el ID es el mismo
                        // nucleo.getVenueDAO().actualizarEstadoVenue(v); <--- SI TIENES ESTE METODO
                        // Si no lo tienes, muéstralo solo en memoria o agrégalo al DAO
                        JOptionPane.showMessageDialog(this, "¡Venue Aprobado! Ahora los organizadores pueden usarlo.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                    }
                    break;
                }
            }
        }
    }

    private void accionVerLogs() {
        try {
            List<String> logs = nucleo.getMarketplace().consultarLog(admin);
            
            JTextArea textArea = new JTextArea(15, 50);
            textArea.setEditable(false);
            for (String log : logs) {
                textArea.append(log + "\n");
            }
            
            JScrollPane scroll = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(this, scroll, "Log de Auditoría", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void accionCancelarEvento() {
        // Filtrar activos
        List<Evento> activos = new ArrayList<>();
        // Necesitamos acceso a los eventos. Si TiqueteraApp no tiene getEventos() publico, añádelo.
        for (Evento e : nucleo.getEventos()) {
            if ("ACTIVO".equalsIgnoreCase(e.getEstado())) {
                activos.add(e);
            }
        }

        if (activos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay eventos activos para cancelar.");
            return;
        }

        // Selector simple
        String[] nombres = new String[activos.size()];
        for(int i=0; i<activos.size(); i++) nombres[i] = activos.get(i).getNombre();

        String seleccion = (String) JOptionPane.showInputDialog(this, "SELECCIONA EVENTO A CANCELAR:", 
                "ZONA DE PELIGRO", JOptionPane.WARNING_MESSAGE, null, nombres, nombres[0]);

        if (seleccion != null) {
            // Confirmación extra
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "¿ESTÁS SEGURO?\nEsto reembolsará el dinero a todos los clientes.", 
                    "Confirmar Cancelación", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Buscar el evento real
                Evento eventoSel = null;
                for(Evento e : activos) if(e.getNombre().equals(seleccion)) eventoSel = e;
                
                try {
                    // Obtener lista de tiquetes vendidos (necesitas un getter en TiqueteraApp para tiquetesVendidos)
                    // Como TiqueteraApp tiene la lista tiquetesVendidos privada, vamos a asumir que creaste el getter
                    // public List<Tiquete> getTiquetesVendidos() { return tiquetesVendidos; }
                    
                    // Si no tienes el getter, avísame. Por ahora usaré una lista vacía para que no compile error, 
                    // PERO DEBES AGREGAR EL GETTER EN TIQUETERAAPP.
                    List<Tiquete> todosLosTiquetes = nucleo.getTiquetesVendidos(); 
                    
                    admin.cancelarEvento(eventoSel, todosLosTiquetes, true);
                    
                    // Guardar cambios en BD (Evento y Saldos)
                    // nucleo.getEventoDAO().guardarEvento(eventoSel); // O actualizarEstado
                    // Loop para guardar saldos de usuarios afectados... (Simplificado para el ejemplo)
                    
                    JOptionPane.showMessageDialog(this, "Evento Cancelado y Reembolsos procesados.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        }
    }

    private void accionVerReporte() {
        // Necesitamos lista de tiquetes y eventos
        Map<String, Double> reporte = admin.calcularGanancias(nucleo.getTiquetesVendidos(), nucleo.getEventos());
        
        StringBuilder sb = new StringBuilder();
        sb.append("--- REPORTE FINANCIERO ---\n\n");
        for (Map.Entry<String, Double> entry : reporte.entrySet()) {
            sb.append(entry.getKey()).append(": $").append(String.format("%,.2f", entry.getValue())).append("\n");
        }
        
        JTextArea textArea = new JTextArea(15, 40);
        textArea.setText(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Finanzas", JOptionPane.INFORMATION_MESSAGE);
    }
}