package app;

import cliente.Administrador;
import cliente.Usuario;
import java.util.Scanner;

public class AdminApp {


    private TiqueteraApp nucleo; 
    private Scanner scanner;

    
    public AdminApp() {
        this.scanner = new Scanner(System.in);
        this.nucleo = new TiqueteraApp(); 
        
   
        nucleo.cargarDatosDesdeBD(); 
    }

    // ESTE ES EL PRIMER MAIN hptttsss
    public static void main(String[] args) {
        System.out.println("--- Lanzando Consola de Administrador ---");
        AdminApp app = new AdminApp();
        app.ejecutar();
    }


    public void ejecutar() {
        
        // Paso 1: 
        if (!autenticarComoAdmin()) {
            System.err.println("Acceso denegado. Esta consola es solo para Administradores.");
            return; // Salir de la app
        }

        // Paso 2: Bucle del Menú (Requisito 3c y 3d)
        // El 'usuarioActual' ya está guardado en el 'nucleo'
        System.out.println("\n¡Autenticación exitosa!");
        
        boolean seguir = true;
        while (seguir) {
            try {
                // Llamamos al método de menú que refactorizamos
                nucleo.mostrarMenuAdmin(scanner);
                
                // (Revisamos si el usuario cerró sesión)
                if (nucleo.getUsuarioActual() == null) {
                    seguir = false;
                }
                
            } catch (Exception e) {
                System.err.println("Error inesperado en el menú: " + e.getMessage());
            }
        }
        
        System.out.println("Ha cerrado sesión. vemossss.");
    }


    private boolean autenticarComoAdmin() {
        System.out.println("Por favor, autentíquese para continuar.");
        
        // Llamamos a la lógica de login (que ahora es pública)
        nucleo.logicaIniciarSesion(scanner);
        
        Usuario usuario = nucleo.getUsuarioActual();
        
        // Verificación de ROL
        if (usuario != null && usuario instanceof Administrador) {
            return true; // ¡Éxito!
        } else {
            // Si se logueó (ej. un Comprador) pero no es Admin, cerramos sesión
            if (usuario != null) {
                nucleo.cerrarSesion();
            }
            return false; // Fracaso
        }
    }
}