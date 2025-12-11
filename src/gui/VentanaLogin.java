package gui;

import javax.swing.*;
import java.awt.*;
import app.TiqueteraApp;
import cliente.*;

public class VentanaLogin extends JFrame {

    private TiqueteraApp nucleo;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    public VentanaLogin(TiqueteraApp nucleo) {
        this.nucleo = nucleo;
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Login - TICKETGOD");
        setSize(400, 320); // Un poco más alto para que quepa el botón nuevo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
    }

    private void iniciarComponentes() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Bienvenido a TICKETGOD");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitulo, gbc);

        // Usuario
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        add(new JLabel("Usuario:"), gbc);
        txtUsuario = new JTextField(15);
        gbc.gridx = 1;
        add(txtUsuario, gbc);

        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Contraseña:"), gbc);
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        add(txtPassword, gbc);

        // Botón INGRESAR
        JButton btnIngresar = new JButton("Iniciar Sesión");
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        add(btnIngresar, gbc);

        // --- AQUÍ ESTÁ EL BOTÓN NUEVO ---
        JButton btnRegistro = new JButton("¿No tienes cuenta? Regístrate aquí");
        btnRegistro.setForeground(Color.BLUE);
        btnRegistro.setBorderPainted(false);
        btnRegistro.setContentAreaFilled(false);
        gbc.gridy = 4; // Fila siguiente
        add(btnRegistro, gbc);

        // ACCIONES
        btnIngresar.addActionListener(e -> procesarLogin());
        
        btnRegistro.addActionListener(e -> {
            // Abrir la ventana de registro
            new VentanaRegistro(nucleo).setVisible(true);
        });
    }

    private void procesarLogin() {
        String login = txtUsuario.getText();
        String password = new String(txtPassword.getPassword());

        Usuario usuario = nucleo.autenticarUsuario(login, password);

        if (usuario != null) {
            this.dispose(); 
            abrirDashboard(usuario);
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirDashboard(Usuario u) {
        if (u instanceof UsuarioComprador) {
            new VentanaComprador(nucleo, (UsuarioComprador) u).setVisible(true);
        } else if (u instanceof Administrador) {
            new VentanaAdmin(nucleo, (Administrador) u).setVisible(true);
        } else if (u instanceof OrganizadorEventos) {
            new VentanaOrganizador(nucleo, (OrganizadorEventos) u).setVisible(true);
        }
    }
}
