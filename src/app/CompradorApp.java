package app;

import cliente.UsuarioComprador; 
import cliente.Usuario;
import java.util.Scanner;


public class CompradorApp {

    private TiqueteraApp nucleo; 
    private Scanner scanner;

    public CompradorApp() {
        this.scanner = new Scanner(System.in);
        this.nucleo = new TiqueteraApp();
        nucleo.cargarDatosDesdeBD(); 
    }

   
    public static void main(String[] args) {
        System.out.println("--- Bienvenido a TICKETGOD ---");
        CompradorApp app = new CompradorApp();
        app.ejecutar();
    }


    public void ejecutar() {
        boolean seguir = true;
        while (seguir) {
            try {
                if (nucleo.getUsuarioActual() == null) {
                    nucleo.mostrarMenuNoAutenticado(scanner);
                    
                } else {
                    if (nucleo.getUsuarioActual() instanceof UsuarioComprador) {
                        nucleo.mostrarMenuComprador(scanner);
                    } else {
                        System.err.println("¡Error! Esta consola es solo para Compradores.");
                        nucleo.cerrarSesion();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error inesperado en el menú: " + e.getMessage());
            }
        }
    }
}