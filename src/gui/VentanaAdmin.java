package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import app.TiqueteraApp;
import cliente.Administrador;
import cliente.Usuario; 
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
        setSize(550, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // --- HEADER ---
        JPanel panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setBackground(new Color(230, 230, 250)); 
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel lblTitulo = new JLabel("Panel de Control");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblSub = new JLabel("Administrador: " + admin.getLogIn());
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        
        panelHeader.add(lblTitulo);
        panelHeader.add(lblSub);
        add(panelHeader, BorderLayout.NORTH);

        // --- BOTONES ---
        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 15, 15));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

        JButton btnGestionarVenues = new JButton("1. Aprobar/Rechazar Venues");
        JButton btnCrearVenue = new JButton("2. Crear Venue Oficial (Directo)"); 
        JButton btnVerUsuarios = new JButton("3. Ver Usuarios Registrados");
        JButton btnVerLog = new JButton("4. Ver Log Marketplace"); 
        JButton btnEstadisticas = new JButton("5. Estadísticas Financieras"); // <--- AHORA SÍ FUNCIONARÁ

        btnCrearVenue.setForeground(new Color(0, 0, 150)); 

        btnGestionarVenues.addActionListener(e -> accionGestionarVenues());
        btnCrearVenue.addActionListener(e -> accionCrearVenueDirecto()); 
        btnVerUsuarios.addActionListener(e -> accionVerUsuarios());
        btnVerLog.addActionListener(e -> accionVerLogMarketplace()); 
        btnEstadisticas.addActionListener(e -> accionReporteGlobal()); // <--- Llama al método real

        panelBotones.add(btnGestionarVenues);
        panelBotones.add(btnCrearVenue);
        panelBotones.add(btnVerUsuarios);
        panelBotones.add(btnVerLog);
        panelBotones.add(btnEstadisticas);

        add(panelBotones, BorderLayout.CENTER);

        // --- FOOTER ---
        JPanel panelFooter = new JPanel();
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setForeground(Color.RED);
        btnLogout.addActionListener(e -> {
            this.dispose();
            new VentanaLogin(nucleo).setVisible(true);
        });
        panelFooter.add(btnLogout);
        add(panelFooter, BorderLayout.SOUTH);
    }

    // =======================================================
    //                 LÓGICA DE LOS BOTONES
    // =======================================================

    // ... (Mantén tus métodos de Venues, Usuarios y Log igual que antes) ...
    // ... Copia aquí accionGestionarVenues, accionCrearVenueDirecto, etc. ...
    
    // --- PÉGALOS AQUÍ SI TE FALTAN, SI NO, SOLO AGREGA EL DE ABAJO ---
    
    private void accionGestionarVenues() {
        List<Venue> pendientes = new ArrayList<>();
        for (Venue v : nucleo.getVenues()) {
            if ("PENDIENTE".equalsIgnoreCase(v.getEstado())) pendientes.add(v);
        }
        if (pendientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay solicitudes pendientes.");
            return;
        }
        String[] opciones = new String[pendientes.size()];
        for (int i = 0; i < pendientes.size(); i++) opciones[i] = pendientes.get(i).getUbicacion();
        String sel = (String) JOptionPane.showInputDialog(this, "Evaluar Venue:", "Venues", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (sel != null) {
            Venue vSel = null;
            for(Venue v : pendientes) if(v.getUbicacion().equals(sel)) vSel = v;
            int opt = JOptionPane.showOptionDialog(this, "¿Aprobar " + sel + "?", "Evaluar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"APROBAR", "RECHAZAR"}, "APROBAR");
            try {
                if (opt == 0) { vSel.setEstado("APROBADO"); nucleo.getVenueDAO().actualizarEstadoVenue(vSel); }
                else if (opt == 1) { vSel.setEstado("RECHAZADO"); nucleo.getVenueDAO().actualizarEstadoVenue(vSel); }
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Error BD: " + e.getMessage()); }
        }
    }
    
    private void accionCrearVenueDirecto() {
         // (Tu código de crear venue directo que ya tenías)
         // ...
         // Si necesitas que te lo pase de nuevo avísame, pero ya lo tenías bien.
    }
    
    private void accionVerUsuarios() {
        StringBuilder sb = new StringBuilder("=== USUARIOS ===\n");
        for(Usuario u : nucleo.getUsuarios()) sb.append(u.getLogIn()).append(" (").append(u.getClass().getSimpleName()).append(")\n");
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(sb.toString())));
    }
    
    private void accionVerLogMarketplace() {
        // (Tu código de ver log)
        try {
            List<String> logs = nucleo.getMarketplace().getLogDeRegistros();
            JOptionPane.showMessageDialog(this, new JScrollPane(new JList<>(logs.toArray())));
        } catch(Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    /**
     * ¡AQUÍ ESTÁ LA NUEVA LÓGICA FINANCIERA!
     */
    private void accionReporteGlobal() {
        List<Tiquete> todosLosTiquetes = nucleo.getTiquetesVendidos();
        
        if (todosLosTiquetes == null || todosLosTiquetes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aún no hay ventas registradas en el sistema.");
            return;
        }

        double totalVentas = 0.0;
        int cantidadTiquetes = 0;
        Map<String, Double> ventasPorEvento = new HashMap<>();

        for (Tiquete t : todosLosTiquetes) {
            // Ignorar cortesías para la suma financiera real (opcional)
            if ("CORTESIA".equals(t.getEstado())) continue;
            if (!"ACTIVO".equals(t.getEstado()) && !"VENCIDO".equals(t.getEstado())) continue;

            double precio = t.getPrecioFinal();
            totalVentas += precio;
            cantidadTiquetes++;

            // Agrupar por evento
            String nombreEvento = (t.getEvento() != null) ? t.getEvento().getNombre() : "Paquete Múltiple";
            ventasPorEvento.put(nombreEvento, ventasPorEvento.getOrDefault(nombreEvento, 0.0) + precio);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE FINANCIERO GLOBAL ===\n\n");
        sb.append("Total Tiquetes Vendidos: ").append(cantidadTiquetes).append("\n");
        sb.append("DINERO TOTAL RECAUDADO: $").append(String.format("%,.2f", totalVentas)).append("\n");
        sb.append("---------------------------------\n");
        sb.append("Desglose por Evento:\n");
        
        for (Map.Entry<String, Double> entry : ventasPorEvento.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(": $").append(String.format("%,.2f", entry.getValue())).append("\n");
        }

        JTextArea area = new JTextArea(20, 40);
        area.setText(sb.toString());
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Estadísticas TICKETGOD", JOptionPane.INFORMATION_MESSAGE);
    }
}