package app;

import gui.VentanaLogin;

public class MainGUI {
	
	public static void main (String [] args ) {
		
		System.out.println("Iniciando interfaz gráfica...");
        
        // 1. Inicializar el núcleo (Carga BD, crea Marketplace, etc.)
        TiqueteraApp nucleo = new TiqueteraApp();
        nucleo.cargarDatosDesdeBD(); 

        // 2. Arrancar la ventana
        java.awt.EventQueue.invokeLater(() -> {
            VentanaLogin login = new VentanaLogin(nucleo);
            login.setVisible(true);
        });
    }
		
		
	

}
