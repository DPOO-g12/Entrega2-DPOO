package app; // (Asegúrate que diga package app;)

import gui.VentanaLogin;
import persistencia.InicializadorBD; // <--- IMPORTAR ESTO

public class MainGUI {

    public static void main(String[] args) {
        System.out.println("Iniciando interfaz gráfica...");
        
        // ---------------------------------------------------------
        // 1. ¡¡CREAR TABLAS SI NO EXISTEN!! (SALVAVIDAS)
        // ---------------------------------------------------------
        InicializadorBD.crearTablas(); 

        // 2. Inicializar el núcleo (Carga BD, etc.)
        TiqueteraApp nucleo = new TiqueteraApp();
        nucleo.cargarDatosDesdeBD(); 

        // 3. Arrancar la ventana
        java.awt.EventQueue.invokeLater(() -> {
            VentanaLogin login = new VentanaLogin(nucleo);
            login.setVisible(true);
        });
    }
}