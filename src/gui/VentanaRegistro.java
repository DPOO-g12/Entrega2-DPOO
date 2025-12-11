package gui;

import javax.swing.*;
import java.awt.*;
import app.TiqueteraApp;

public class VentanaRegistro extends JFrame {

    private TiqueteraApp nucleo;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JComboBox<String> cbTipoUsuario;

    public VentanaRegistro(TiqueteraApp nucleo) {
        this.nucleo = nucleo;
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Crear Cuenta - TICKETGOD");
        setSize(400, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
    }

    private void iniciarComponentes() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Registro de Nuevo Usuario");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitulo, gbc);

        // Usuario
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        add(new JLabel("Usuario:"), gbc);
        txtUsuario = new JTextField(15);
        gbc.gridx = 1;
        add(txtUsuario, gbc);

        // Contraseña
        gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Contraseña:"), gbc);
        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        add(txtPassword, gbc);

        // Selector de Rol (AHORA CON ADMIN)
        gbc.gridy = 3; gbc.gridx = 0;
        add(new JLabel("Tipo de Cuenta:"), gbc);
        
        // --- CAMBIO AQUÍ: Agregamos "Administrador" ---
        String[] roles = {"Comprador (Cliente)", "Organizador de Eventos", "Administrador del Sistema"};
        cbTipoUsuario = new JComboBox<>(roles);
        gbc.gridx = 1;
        add(cbTipoUsuario, gbc);

        // Botón
        JButton btnGuardar = new JButton("¡Crear Cuenta!");
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        add(btnGuardar, gbc);

        // Acción
        btnGuardar.addActionListener(e -> accionRegistrar());
    }

    private void accionRegistrar() {
        String user = txtUsuario.getText();
        String pass = new String(txtPassword.getPassword());
        String rolSeleccionado = (String) cbTipoUsuario.getSelectedItem();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Llena todos los campos.");
            return;
        }

        boolean exito = false;

        // --- CAMBIO AQUÍ: Lógica para los 3 roles ---
        if (rolSeleccionado.contains("Organizador")) {
            exito = nucleo.registrarOrganizador(user, pass);
        } else if (rolSeleccionado.contains("Administrador")) {
            // Pedimos una clave maestra por seguridad (opcional, aquí simple)
            exito = nucleo.registrarAdministrador(user, pass);
        } else {
            exito = nucleo.registrarNuevoComprador(user, pass);
        }

        if (exito) {
            JOptionPane.showMessageDialog(this, "¡Cuenta de " + rolSeleccionado + " creada!\nYa puedes iniciar sesión.");
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "El usuario ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}