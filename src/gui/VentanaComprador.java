package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import app.TiqueteraApp;
import cliente.UsuarioComprador;
import tiquetes.Tiquete;

public class VentanaComprador extends JFrame {

    private TiqueteraApp nucleo;
    private UsuarioComprador usuario;
    private JLabel lblSaldo; // Para poder actualizarlo visualmente

    public VentanaComprador(TiqueteraApp nucleo, UsuarioComprador usuario) {
        this.nucleo = nucleo;
        this.usuario = usuario;
        
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Panel Comprador - TICKETGOD");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar
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

        lblSaldo = new JLabel("Saldo Disponible: $" + usuario.getSaldo());
        lblSaldo.setFont(new Font("Arial", Font.BOLD, 18));
        lblSaldo.setForeground(new Color(0, 100, 0)); // Verde oscuro
        lblSaldo.setHorizontalAlignment(SwingConstants.CENTER);

        panelHeader.add(lblTitulo);
        panelHeader.add(lblSaldo);
        add(panelHeader, BorderLayout.NORTH);

        // --- 2. BOTONES PRINCIPALES (Grid central) ---
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 20, 20));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnComprar = crearBoton("Comprar Tiquetes", "Busca eventos y compra entradas");
        JButton btnMisTiquetes = crearBoton("Mis Tiquetes (Imprimir)", "Ve tus compras y genera el QR");
        JButton btnMarketplace = crearBoton("Marketplace", "Compra y vende tiquetes de otros");
        JButton btnRecargar = crearBoton("Recargar Saldo", "Añade dinero a tu cuenta");

        // --- ACCIONES ---
        
        // A. Recargar Saldo
        btnRecargar.addActionListener(e -> accionRecargarSaldo());

        // B. Ver Mis Tiquetes (Aquí conectaremos con la impresión)
        btnMisTiquetes.addActionListener(e -> accionVerMisTiquetes());

        // C. Acción Real: Abrir la ventana de compra
        btnComprar.addActionListener(e -> {
            // Verificamos si hay eventos antes de abrir
            if (nucleo.getEventos().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay eventos activos en el sistema.");
            } else {
                // Abrimos la ventana nueva pasándole el control
                new VentanaCompra(nucleo, usuario).setVisible(true);
            }
        });
        
        btnMarketplace.addActionListener(e -> {
            new VentanaMarketplace(nucleo, usuario).setVisible(true);
        });

        panelBotones.add(btnComprar);
        panelBotones.add(btnMisTiquetes);
        panelBotones.add(btnMarketplace);
        panelBotones.add(btnRecargar);

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

    // Método auxiliar para crear botones bonitos
    private JButton crearBoton(String titulo, String tooltip) {
        JButton btn = new JButton(titulo);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.setToolTipText(tooltip);
        return btn;
    }

    // --- LÓGICA DE LOS BOTONES ---

    private void accionRecargarSaldo() {
        String input = JOptionPane.showInputDialog(this, "¿Cuánto quieres recargar?");
        if (input != null && !input.isEmpty()) {
            try {
                double monto = Double.parseDouble(input);
                if (monto > 0) {
                    // Actualizar memoria
                    double nuevoSaldo = usuario.getSaldo() + monto;
                    usuario.setSaldo(nuevoSaldo);
                    
                    // Actualizar BD
                    try {
                        // Usamos el DAO que ya tienes en TiqueteraApp (necesitas un getter si es private)
                        // O creamos una instancia rápida aquí si no quieres modificar TiqueteraApp:
                        new persistencia.UsuarioDAO().actualizarSaldo(usuario);
                        
                        // Actualizar Vista
                        lblSaldo.setText("Saldo Disponible: $" + nuevoSaldo);
                        JOptionPane.showMessageDialog(this, "¡Recarga exitosa!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error guardando en BD: " + ex.getMessage());
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Monto inválido.");
            }
        }
    }

    private void accionVerMisTiquetes() {
        List<Tiquete> misTiquetes = usuario.getTiquetesComprados();

        if (misTiquetes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes tiquetes comprados.");
            return;
        }

        // Crear lista de opciones para el usuario
        String[] opciones = new String[misTiquetes.size()];
        for (int i = 0; i < misTiquetes.size(); i++) {
            Tiquete t = misTiquetes.get(i);
            String estado = t.isImpreso() ? "[YA IMPRESO]" : "[DISPONIBLE]";
            // Asumimos que getEvento() no es nulo (si es paquete, ajusta el texto)
            String nombreEvento = (t.getEvento() != null) ? t.getEvento().getNombre() : "Paquete Múltiple";
            opciones[i] = t.getIdTiquete() + " - " + nombreEvento + " " + estado;
        }

        // Mostrar selector
        String seleccion = (String) JOptionPane.showInputDialog(
            this,
            "Selecciona el tiquete que quieres ver o imprimir:",
            "Mis Tiquetes",
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );

        if (seleccion != null) {
            // Buscar el objeto real basado en la selección
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) {
                    Tiquete tiqueteSeleccionado = misTiquetes.get(i);
                    
                    // ABRIR LA VENTANA DE IMPRESIÓN (La crearemos en el siguiente paso)
                    new VentanaTiquete(nucleo, tiqueteSeleccionado).setVisible(true);
                    break;
                }
            }
        }
    }
}