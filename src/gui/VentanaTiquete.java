package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import tiquetes.Tiquete;
import app.TiqueteraApp;

public class VentanaTiquete extends JFrame {

    private TiqueteraApp nucleo;
    private Tiquete tiquete;

    public VentanaTiquete(TiqueteraApp nucleo, Tiquete tiquete) {
        this.nucleo = nucleo;
        this.tiquete = tiquete;
        
        // 1. Lógica de Negocio: Validar impresión
        if (!tiquete.isImpreso()) {
            marcarComoImpreso();
        } else {
            JOptionPane.showMessageDialog(this, "NOTA: Este tiquete ya fue impreso anteriormente.\nYa no es transferible.");
        }
        
        configurarVentana();
        iniciarComponentes();
    }
    
    private void marcarComoImpreso() {
        try {
            // A. Actualizar en Memoria
            tiquete.setImpreso(true);
            
            // B. Actualizar en Base de Datos
            // Necesitamos acceder al DAO desde el nucleo
            nucleo.getTiqueteDAO().actualizarImpresoTiquete(tiquete);
            
            JOptionPane.showMessageDialog(this, "¡Tiquete Generado Exitosamente!\nSe ha marcado como IMPRESO y se ha bloqueado para transferencias.");
        } catch (Exception e) {
            System.err.println("Error guardando estado impreso: " + e.getMessage());
        }
    }

    private void configurarVentana() {
        setTitle("Boleto Digital - ID: " + tiquete.getIdTiquete());
        setSize(750, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Solo cerrar esta ventana
        
        // Color de Fondo Azul Oscuro (Requisito PDF)
        getContentPane().setBackground(new Color(20, 30, 60));
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // Panel contenedor transparente
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2));
        panelPrincipal.setOpaque(false); 

        // --- COLUMNA IZQUIERDA: DATOS ---
        JPanel panelDatos = new JPanel();
        panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.Y_AXIS));
        panelDatos.setOpaque(false);
        panelDatos.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Construir textos
        String evento = (tiquete.getEvento() != null) ? tiquete.getEvento().getNombre() : "Paquete";
        String venue = (tiquete.getEvento() != null) ? tiquete.getEvento().getVenue().getUbicacion() : "N/A";
        String localidad = (tiquete.getLocalidad() != null) ? tiquete.getLocalidad().getNombreLocalidad() : "N/A";
        String fecha = (tiquete.getEvento() != null) ? tiquete.getEvento().getFecha() : "N/A";

        agregarTexto(panelDatos, "No. " + tiquete.getIdTiquete(), 24, true);
        panelDatos.add(Box.createVerticalStrut(20)); // Espacio
        
        agregarTexto(panelDatos, evento, 18, true);
        agregarTexto(panelDatos, "Venue: " + venue, 14, false);
        agregarTexto(panelDatos, "Localidad: " + localidad, 14, false);
        agregarTexto(panelDatos, "Fecha: " + fecha, 14, false);
        
        panelDatos.add(Box.createVerticalStrut(20));
        agregarTexto(panelDatos, "VALOR: $" + String.format("%,.0f", tiquete.getPrecioFinal()), 20, true);

        // --- COLUMNA DERECHA: QR ---
        JPanel panelQR = new JPanel(new GridBagLayout());
        panelQR.setOpaque(false);
        
        // Texto plano para el QR (Requisito PDF)
        String dataQR = "TICKETGOD VALID\n" +
                        "ID:" + tiquete.getIdTiquete() + "\n" +
                        "Evento:" + evento + "\n" +
                        "Fecha:" + fecha + "\n" +
                        "Cliente:" + tiquete.getCliente().getLogIn();

        try {
            // Generar imagen
            BufferedImage imagenQR = GeneradorQR.generarQR(dataQR, 220, 220);
            
            if (imagenQR != null) {
                JLabel lblQR = new JLabel(new ImageIcon(imagenQR));
                // Borde blanco bonito
                lblQR.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
                panelQR.add(lblQR);
            }
        } catch (Error | Exception e) {
            // Si no hay librería, mostramos un cuadro vacío
            JLabel lblError = new JLabel("[Librería QR no encontrada]");
            lblError.setForeground(Color.WHITE);
            panelQR.add(lblError);
        }

        panelPrincipal.add(panelDatos);
        panelPrincipal.add(panelQR);

        add(panelPrincipal, BorderLayout.CENTER);
        
        // Footer con marca de agua
        JLabel lblFooter = new JLabel("BoletaMaster - TicketGod System  ");
        lblFooter.setForeground(Color.GRAY);
        lblFooter.setHorizontalAlignment(SwingConstants.RIGHT);
        lblFooter.setBorder(BorderFactory.createEmptyBorder(0,0,5,10));
        add(lblFooter, BorderLayout.SOUTH);
    }

    private void agregarTexto(JPanel panel, String texto, int tamano, boolean negrita) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(Color.WHITE); // Texto blanco sobre fondo azul
        lbl.setFont(new Font("SansSerif", negrita ? Font.BOLD : Font.PLAIN, tamano));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(5));
    }
}