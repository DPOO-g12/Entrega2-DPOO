package app;


import cliente.OrganizadorEventos; 
import cliente.Usuario;
import java.util.Scanner;


public class OrganizadorApp {


    private TiqueteraApp nucleo; 
    private Scanner scanner;

    // Constructor
    public OrganizadorApp() {
        this.scanner = new Scanner(System.in);
        this.nucleo = new TiqueteraApp(); 
      
        nucleo.cargarDatosDesdeBD(); 
    }

    // ¡ESTE ES EL SEGUNDO MAIN! 
    public static void main(String[] args) {
        System.out.println("--- Lanzando Consola de Organizador (TICKETGOD) ---");
        OrganizadorApp app = new OrganizadorApp();
        app.ejecutar();
    }

 
    public void ejecutar() {
        
      
        if (!autenticarComoOrganizador()) { 
            System.err.println("Acceso denegado. Esta consola es solo para Organizadores.");
            return; // Salir de la app
        }

        // Paso 2: while del Menú
        System.out.println("\n¡Autenticación exitosa!");
        
        boolean seguir = true;
        while (seguir) {
            try {
                // Llamar al método de menú del Organizador
                nucleo.mostrarMenuOrganizador(scanner);
                
                // (Revisamos si el usuario cerró sesión)
                if (nucleo.getUsuarioActual() == null) {
                    seguir = false;
                }
                
            } catch (Exception e) {
                System.err.println("Error inesperado en el menú: " + e.getMessage());
            }
        }
        
        System.out.println("Ha cerrado sesión. ¡Vuelve pronto, crack!");
    }

    /**
     * Lógica de autenticación que valida el ROL.
     */
    private boolean autenticarComoOrganizador() { 
        System.out.println("Por favor, autentíquese para continuar.");
        
        // Llamamos a la lógica de login
        nucleo.logicaIniciarSesion(scanner);
        
        Usuario usuario = nucleo.getUsuarioActual();
        
        // Verificación de ROL
        if (usuario != null && usuario instanceof OrganizadorEventos) { 
            return true; // ¡Éxito!
        } else {
            // Si se logueó (ej. un Admin) pero no es Organizador, cerramos sesión
            if (usuario != null) {
                nucleo.cerrarSesion();
            }
            return false; // Fracaso papi paila.
        }
    }
}