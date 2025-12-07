package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import app.TiqueteraApp;
import cliente.*;

public class VentanaLogin extends JFrame{
	
	private TiqueteraApp nucleo;
	private JTextField txtUsuario;
	private JPasswordField txtPassword;
	private JButton btnIngresar;
	
	public VentanaLogin (TiqueteraApp nucleo) {
		
		this.nucleo = nucleo;
		configurarVentana();
		iniciarComponentes();

	}
	
	public void configurarVentana () {
		
		setTitle("Login - TICKETGOD");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        setLayout(new GridBagLayout()); 
        setResizable(false);
	}
	
	public void iniciarComponentes() {
		
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Bienvenido a TICKETGOD");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitulo, gbc);

        // Usuario
        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
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

        // Botón
        btnIngresar = new JButton("Iniciar Sesión");
        //btnIngresar.setBackground(new Color(50, 150, 250)); // Azulito
        //btnIngresar.setForeground(Color.WHITE);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        add(btnIngresar, gbc);

        // Acción del botón
        btnIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarLogin();
            }
        });
    }
	
	private void procesarLogin () {
		
		String login = txtUsuario.getText();
		String password = new String (txtPassword.getPassword());
		
		Usuario usuario = nucleo.AutenticarUsuario(login, password);
		
		if (usuario != null) {
			// Login Exitoso
            this.dispose(); // Cierra esta ventana
            abrirDashboard(usuario); // Abre la siguiente
        } else {
            // Login Fallido
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
	
	private void abrirDashboard(Usuario u) {
        // 1. Cerrar la ventana de Login actual
        this.dispose(); 

        // 2. Verificar el rol y abrir la ventana correspondiente
        if (u instanceof UsuarioComprador) {
            // Hacemos el "cast" (convertir Usuario genérico a UsuarioComprador)
            UsuarioComprador comprador = (UsuarioComprador) u;
            
            // Creamos y mostramos la nueva ventana
            VentanaComprador dashboard = new VentanaComprador(nucleo, comprador);
            dashboard.setVisible(true);
            
        } else if (u instanceof Administrador) {
        	
        	new VentanaAdmin(nucleo, (Administrador) u).setVisible(true);
            
            
        } else if (u instanceof OrganizadorEventos) {
            JOptionPane.showMessageDialog(null, "Panel de ORGANIZADOR en construcción");
            new VentanaOrganizador(nucleo, (OrganizadorEventos) u).setVisible(true);
    }
	
	
	
	}
	
}


