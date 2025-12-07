package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import app.TiqueteraApp;
import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Venue;

public class VentanaOrganizador extends JFrame {

    private TiqueteraApp nucleo;
    private OrganizadorEventos organizador;

    public VentanaOrganizador(TiqueteraApp nucleo, OrganizadorEventos organizador) {
        this.nucleo = nucleo;
        this.organizador = organizador;
        
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Panel ORGANIZADOR - TICKETGOD");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // --- HEADER ---
        JPanel panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setBackground(new Color(255, 250, 205)); // Amarillo clarito
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitulo = new JLabel("Bienvenido, " + organizador.getLogIn());
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblRol = new JLabel("Gestión de Eventos y Venues");
        lblRol.setHorizontalAlignment(SwingConstants.CENTER);
        
        panelHeader.add(lblTitulo);
        panelHeader.add(lblRol);
        add(panelHeader, BorderLayout.NORTH);

        // --- BOTONES ---
        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 20, 20));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JButton btnCrearEvento = new JButton("1. Crear Nuevo Evento");
        JButton btnSugerirVenue = new JButton("2. Sugerir Nuevo Venue");
        JButton btnCortesias = new JButton("3. Mis Cortesías (Transferir)");
        JButton btnReporte = new JButton("3. Ver Mis Ganancias");

        // ACCIONES
        btnCrearEvento.addActionListener(e -> accionCrearEvento());
        btnSugerirVenue.addActionListener(e -> accionSugerirVenue());
        btnCortesias.addActionListener(e -> accionGestionarCortesias());
        btnReporte.addActionListener(e -> accionVerReporte());

        panelBotones.add(btnCrearEvento);
        panelBotones.add(btnSugerirVenue);
        panelBotones.add(btnCortesias);
        panelBotones.add(btnReporte);

        add(panelBotones, BorderLayout.CENTER);

        // --- FOOTER ---
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new VentanaLogin(nucleo).setVisible(true);
        });
        add(btnLogout, BorderLayout.SOUTH);
    }

    // --- LÓGICA ---

    private void accionSugerirVenue() {
        // Formulario simple con JOptionPanes
        String tipo = JOptionPane.showInputDialog(this, "Tipo de Venue (ej. Estadio, Teatro):");
        if (tipo == null || tipo.isEmpty()) return;

        String ubicacion = JOptionPane.showInputDialog(this, "Ubicación/Nombre (ej. Arena Bogotá):");
        if (ubicacion == null || ubicacion.isEmpty()) return;

        String capStr = JOptionPane.showInputDialog(this, "Capacidad Máxima:");
        if (capStr == null) return;

        try {
            int capacidad = Integer.parseInt(capStr);
            
            // Lógica de negocio
            Venue nuevo = organizador.sugerirVenue(tipo, ubicacion, capacidad, null);
            
            // Persistencia
            // Asumimos que tienes acceso al DAO desde nucleo (agrega getter si falta en TiqueteraApp)
            // nucleo.getVenueDAO().guardarVenue(nuevo); <--- DESCOMENTAR SI TIENES EL GETTER
            
            // Si no tienes el getter del VenueDAO público, lo agregamos a la lista en memoria al menos:
            nucleo.getVenues().add(nuevo); 
            
            JOptionPane.showMessageDialog(this, "Venue sugerido. Espera aprobación del Admin.");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La capacidad debe ser un número.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void accionCrearEvento() {
        // 1. Filtrar Venues APROBADOS
        List<Venue> aprobados = new ArrayList<>();
        for (Venue v : nucleo.getVenues()) {
            if ("APROBADO".equalsIgnoreCase(v.getEstado())) {
                aprobados.add(v);
            }
        }

        if (aprobados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay Venues aprobados disponibles.");
            return;
        }

        // 2. Selector de Venue
        String[] opciones = new String[aprobados.size()];
        for (int i = 0; i < aprobados.size(); i++) opciones[i] = aprobados.get(i).getUbicacion();

        String seleccion = (String) JOptionPane.showInputDialog(this, "Selecciona Venue:", 
                "Crear Evento", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            Venue venueSel = null;
            for (Venue v : aprobados) if (v.getUbicacion().equals(seleccion)) venueSel = v;

            // 3. Pedir datos
            String id = JOptionPane.showInputDialog("ID del Evento (ej. E001):");
            String nombre = JOptionPane.showInputDialog("Nombre del Evento:");
            String fecha = JOptionPane.showInputDialog("Fecha (YYYY-MM-DD):");

            if (id != null && nombre != null && fecha != null) {
                try {
                    // Lógica
                    Evento nuevo = organizador.agregarEvento(id, nombre, fecha, venueSel);
                    
                    // Persistencia (Añadir a lista global y guardar en BD)
                    nucleo.getEventos().add(nuevo);
                    // nucleo.getEventoDAO().guardarEvento(nuevo); <--- SI TIENES EL GETTER
                    
                    JOptionPane.showMessageDialog(this, "¡Evento Creado! (Recuerda añadir localidades después)");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        }
    }

    private void accionVerReporte() {
        try {
            // Necesitamos la lista global de tiquetes para filtrar los de este organizador
            // (El método calcularEstadoFinanciero ya lo hace)
            Map<String, Double> reporte = organizador.calcularEstadoFinanciero(nucleo.getTiquetesVendidos());
            
            StringBuilder sb = new StringBuilder();
            sb.append("--- REPORTE ORGANIZADOR ---\n\n");
            for (Map.Entry<String, Double> entry : reporte.entrySet()) {
                if (entry.getKey().startsWith("PORCENTAJE")) {
                    sb.append(entry.getKey()).append(": ").append(String.format("%.2f%%", entry.getValue())).append("\n");
                } else {
                    sb.append(entry.getKey()).append(": $").append(String.format("%,.2f", entry.getValue())).append("\n");
                }
            }
            
            JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(sb.toString())), "Finanzas", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void accionGestionarCortesias() {
        // 1. Obtener los tiquetes que posee el organizador (Cortesías)
        java.util.List<tiquetes.Tiquete> misTiquetes = organizador.getTiquetesComprados();

        if (misTiquetes.isEmpty()) {
            // Opción para crear una cortesía si no tiene
            int resp = JOptionPane.showConfirmDialog(this, 
                "No tienes tiquetes de cortesía.\n¿Quieres generar uno ahora para uno de tus eventos?", 
                "Cortesías", JOptionPane.YES_NO_OPTION);
            
        }

        // 2. Mostrar lista para seleccionar
        String[] opciones = new String[misTiquetes.size()];
        for (int i = 0; i < misTiquetes.size(); i++) {
            tiquetes.Tiquete t = misTiquetes.get(i);
            opciones[i] = t.getIdTiquete() + " - " + t.getEvento().getNombre() + " (" + t.getEstado() + ")";
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, 
                "Selecciona la cortesía a transferir:", "Mis Cortesías", 
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            // Buscar el objeto real
            tiquetes.Tiquete tiqueteSel = null;
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) tiqueteSel = misTiquetes.get(i);
            }

            // 3. Flujo de Transferencia
            if (tiqueteSel != null) {
                // Validar impresión (Requisito del PDF)
                if (tiqueteSel.isImpreso()) {
                    JOptionPane.showMessageDialog(this, "Este tiquete ya fue impreso. No se puede transferir.");
                    return;
                }

                String destinatario = JOptionPane.showInputDialog("Ingrese el Login del usuario destino:");
                if (destinatario == null || destinatario.isEmpty()) return;

                String pass = JOptionPane.showInputDialog("Confirma con TU contraseña:");
                if (pass == null) return;

                try {
                    // Usamos la lógica de negocio del usuario
                    // Necesitamos la lista global de usuarios para buscar al destino
                    // (Asegúrate de tener nucleo.getUsuarios() disponible o usa el DAO)
                    
                    // Nota: Como transferirTiquete pide la lista de usuarios, necesitamos el getter en TiqueteraApp
                    // Si no lo tienes público, agrégalo: public List<Usuario> getUsuarios() { return usuarios; }
                    
                    organizador.transferirTiquete(tiqueteSel, pass, destinatario, nucleo.getUsuarios());
                    
                    // Actualizar BD
                    nucleo.getTiqueteDAO().actualizarClienteTiquete(tiqueteSel);
                    nucleo.getTiqueteDAO().actualizarEstadoTiquete(tiqueteSel);

                    JOptionPane.showMessageDialog(this, "¡Cortesía transferida exitosamente a " + destinatario + "!");
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Fallo Transferencia", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

   
    
    

}