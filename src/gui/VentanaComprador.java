package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import app.TiqueteraApp;
import cliente.UsuarioComprador;
import tiquetes.Tiquete;

public class VentanaComprador extends JFrame {

    private TiqueteraApp nucleo;
    private UsuarioComprador usuario;
    private JLabel lblSaldo; 

    public VentanaComprador(TiqueteraApp nucleo, UsuarioComprador usuario) {
        this.nucleo = nucleo;
        this.usuario = usuario;
        
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Panel Comprador - TICKETGOD");
        setSize(700, 550); // Un poco más alto para el nuevo botón
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // --- 1. ENCABEZADO (Bienvenida y Saldo) ---
        JPanel panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelHeader.setBackground(new Color(240, 248, 255)); // Azul muy clarito

        JLabel lblTitulo = new JLabel("Hola, " + usuario.getLogIn());
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        lblSaldo = new JLabel("Saldo Disponible: $" + String.format("%,.0f", usuario.getSaldo()));
        lblSaldo.setFont(new Font("Arial", Font.BOLD, 18));
        lblSaldo.setForeground(new Color(0, 100, 0)); // Verde oscuro
        lblSaldo.setHorizontalAlignment(SwingConstants.CENTER);

        panelHeader.add(lblTitulo);
        panelHeader.add(lblSaldo);
        add(panelHeader, BorderLayout.NORTH);

        // --- 2. BOTONES PRINCIPALES ---
        // Cambiamos a 3 filas, 2 columnas para acomodar el 5to botón
        JPanel panelBotones = new JPanel(new GridLayout(3, 2, 20, 20));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnComprar = crearBoton("1. Comprar Tiquetes", "Busca eventos y compra entradas");
        JButton btnMisTiquetes = crearBoton("2. Mis Tiquetes (Imprimir)", "Ve tus compras y genera el QR");
        JButton btnMarketplace = crearBoton("3. Marketplace", "Compra y vende tiquetes de otros");
        JButton btnRecargar = crearBoton("4. Recargar Saldo", "Añade dinero a tu cuenta");
        JButton btnTransferir = crearBoton("5. Transferir a Amigo", "Envía un tiquete a otro usuario"); // <--- NUEVO
        
        // Botón de salir (lo ponemos en el grid para rellenar el hueco 6, o en el footer)
        // Por diseño, dejaremos el hueco 6 vacío o ponemos un label vacío.

        // --- ACCIONES ---
        
        // 1. Comprar
        btnComprar.addActionListener(e -> {
            if (nucleo.getEventos().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay eventos disponibles en el sistema.");
            } else {
                new VentanaCompra(nucleo, usuario).setVisible(true);
            }
        });

        // 2. Ver / Imprimir
        btnMisTiquetes.addActionListener(e -> accionVerMisTiquetes());

        // 3. Marketplace
        btnMarketplace.addActionListener(e -> {
            new VentanaMarketplace(nucleo, usuario).setVisible(true);
        });

        // 4. Recargar
        btnRecargar.addActionListener(e -> accionRecargarSaldo());

        // 5. Transferir (NUEVO)
        btnTransferir.addActionListener(e -> accionTransferirTiquete());


        panelBotones.add(btnComprar);
        panelBotones.add(btnMisTiquetes);
        panelBotones.add(btnMarketplace);
        panelBotones.add(btnRecargar);
        panelBotones.add(btnTransferir); // <--- AGREGADO

        add(panelBotones, BorderLayout.CENTER);

        // --- 3. PIE DE PÁGINA (Logout) ---
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

    private JButton crearBoton(String titulo, String tooltip) {
        JButton btn = new JButton(titulo);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.setToolTipText(tooltip);
        return btn;
    }

    // =======================================================
    //                 LÓGICA DE LOS BOTONES
    // =======================================================

    private void accionRecargarSaldo() {
        String input = JOptionPane.showInputDialog(this, "¿Cuánto quieres recargar?");
        if (input != null && !input.isEmpty()) {
            try {
                double monto = Double.parseDouble(input);
                if (monto > 0) {
                    double nuevoSaldo = usuario.getSaldo() + monto;
                    usuario.setSaldo(nuevoSaldo);
                    
                    // Actualizar BD
                    nucleo.getUsuarioDAO().actualizarSaldo(usuario);
                    
                    // Actualizar Vista
                    lblSaldo.setText("Saldo Disponible: $" + String.format("%,.0f", nuevoSaldo));
                    JOptionPane.showMessageDialog(this, "¡Recarga exitosa!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void accionVerMisTiquetes() {
        List<Tiquete> misTiquetes = usuario.getTiquetesComprados();

        if (misTiquetes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes tiquetes comprados.");
            return;
        }

        String[] opciones = new String[misTiquetes.size()];
        for (int i = 0; i < misTiquetes.size(); i++) {
            Tiquete t = misTiquetes.get(i);
            String estado = t.isImpreso() ? "[IMPRESO - VER QR]" : "[DISPONIBLE]";
            String nombreEvt = (t.getEvento() != null) ? t.getEvento().getNombre() : "Paquete";
            opciones[i] = t.getIdTiquete() + " - " + nombreEvt + " " + estado;
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, "Selecciona tiquete:", "Mis Tiquetes",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) {
                    Tiquete tSel = misTiquetes.get(i);
                    new VentanaTiquete(nucleo, tSel).setVisible(true);
                    break;
                }
            }
        }
    }

    // --- NUEVO MÉTODO PARA TRANSFERIR ---
    private void accionTransferirTiquete() {
        List<Tiquete> misTiquetes = usuario.getTiquetesComprados();
        
        // 1. Filtrar solo los transferibles (No impresos, no vencidos)
        List<Tiquete> aptos = new ArrayList<>();
        for (Tiquete t : misTiquetes) {
            if (!t.isImpreso() && t.isTransferible() && "ACTIVO".equalsIgnoreCase(t.getEstado())) {
                aptos.add(t);
            }
        }

        if (aptos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes tiquetes aptos para transferir.\n(Recuerda: No puedes transferir si ya lo imprimiste).");
            return;
        }

        // 2. Selector visual
        String[] opciones = new String[aptos.size()];
        for (int i = 0; i < aptos.size(); i++) {
            String nombreEvt = (aptos.get(i).getEvento() != null) ? aptos.get(i).getEvento().getNombre() : "Paquete";
            opciones[i] = aptos.get(i).getIdTiquete() + " - " + nombreEvt;
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, 
                "Elige el tiquete a regalar:", "Transferir Tiquete", 
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            Tiquete aTransferir = null;
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) aTransferir = aptos.get(i);
            }

            // 3. Pedir Destinatario y Password
            String destino = JOptionPane.showInputDialog("Ingresa el usuario (Login) de tu amigo:");
            if (destino == null || destino.isEmpty()) return;

            String pass = JOptionPane.showInputDialog("Confirma con TU contraseña por seguridad:");
            if (pass == null) return;

            try {
                // 4. Ejecutar Lógica
                // Usamos el método transferirTiquete del usuario (asumiendo que sigue la misma lógica que Organizador)
                usuario.transferirTiquete(aTransferir, pass, destino, nucleo.getUsuarios());

                // 5. Actualizar BD (Cambiar dueño)
                nucleo.getTiqueteDAO().actualizarClienteTiquete(aTransferir);
                
                JOptionPane.showMessageDialog(this, "¡Tiquete enviado exitosamente a " + destino + "!");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error en transferencia: " + e.getMessage(), "Fallo", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}