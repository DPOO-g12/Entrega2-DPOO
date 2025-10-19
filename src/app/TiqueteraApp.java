package app;


import cliente.*;
import eventos.*;
import localidades.*;
import persistencia.*;
import tiquetes.*;
import excepciones.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.List;
import java.util.Map;
import java.util.Scanner; // Para leer desde la consola


public class TiqueteraApp {

    // --- Listas Maestras (La "Memoria" de la App) ---
    // Guardan el estado de la aplicación mientras se ejecuta.
    private List<Usuario> usuarios;
    private List<Venue> venues;
    private List<Evento> eventos;
    private List<Tiquete> tiquetesVendidos; // ¡La lista maestra que necesitábamos!
    private Usuario usuarioActual;
   
    
    
    // --- DAOs (Los "Traductores" a la BD) ---
    // Instancias de todos nuestros traductores
    private final UsuarioDAO usuarioDAO;
    private final VenueDAO venueDAO;
    private final EventoDAO eventoDAO;
    private final LocalidadDAO localidadDAO;
    private final TiqueteDAO tiqueteDAO;
    
    // --- Utilidades ---
    private final Scanner scanner; // Para leer la entrada del usuario

    // --- Constructor ---
    public TiqueteraApp() {
        // Inicializar las listas maestras
        this.usuarios = new ArrayList<>();
        this.venues = new ArrayList<>();
        this.eventos = new ArrayList<>();
        this.tiquetesVendidos = new ArrayList<>();

        // Inicializar todos los DAOs
        this.usuarioDAO = new UsuarioDAO();
        this.venueDAO = new VenueDAO();
        this.eventoDAO = new EventoDAO();
        this.localidadDAO = new LocalidadDAO();
        this.tiqueteDAO = new TiqueteDAO();
        
        // Inicializar el scanner
        this.scanner = new Scanner(System.in);
        this.usuarioActual = null;
    }
    
    // --- Método Main
    public static void main(String[] args) {
        TiqueteraApp app = new TiqueteraApp();
        app.iniciar(); 
    }

    
     // Método principal que carga datos y muestra el menú.
    public void iniciar() {
        // ¡PASO 1! Cargar todo lo que ya existe en la BD
        cargarDatosDesdeBD(); 
        
        System.out.println("\n¡Bienvenido a TIQUETGOD!!!!!");
        
        // ¡PASO 2! Iniciar el bucle del menú principal
        mostrarMenuPrincipal();
    }

    /**
     * Bucle principal de la aplicación.
     */
    public void mostrarMenuPrincipal() {
        while (true) {
            try {
                // Si no hay nadie logueado, muestra el menú de "invitado"
                if (this.usuarioActual == null) {
                    mostrarMenuNoAutenticado();
                } else {
                    // Si hay alguien logueado, muestra el menú de su rol
                    mostrarMenuAutenticado();
                }
            } catch (Exception e) {
                // Captura cualquier error inesperado para que la app no se rompa
                System.err.println("\n¡Ups! Ocurrió un error inesperado: " + e.getMessage());
                e.printStackTrace(); // (Ayuda a depurar)
            }
        }
    }
    
    /**
     * Función de prueba para ver el flujo completo:
     * Lógica (Paso 2) + Persistencia (Paso 3)
     */
    public void probarRegistroAdmin() {
        System.out.println("\n--- Probando Registro de Administrador ---");
        
        try {
            // 1. Datos de entrada (simulados)
            String login = "admin_jefe";
            String pass = "12345";
            
            // 2. Lógica de Negocio (Paso 2)
            // No hay mucha lógica aquí, solo crear el objeto
            Administrador admin = new Administrador(login, pass);
            admin.fijarCobroPorEmision(5000.0); // Lógica
            
            // 3. Lógica de Persistencia (Paso 3)
            // Le pasamos el objeto al DAO para que lo guarde
            System.out.println("Guardando en BD...");
            this.usuarioDAO.guardarUsuario(admin);
            
            // 4. Actualizar nuestra "memoria" local
            this.usuarios.add(admin);
            
            System.out.println("¡Administrador '" + login + "' registrado y guardado en la BD exitosamente!");
            
            // 5. Verificación
            System.out.println("Usuarios en memoria: " + this.usuarios.size());
            
        } catch (Exception e) {
            System.err.println("ERROR AL GUARDAR ADMIN: " + e.getMessage());
            e.printStackTrace(); // Muestra el error completo
        }
    }
    private void cargarDatosDesdeBD() {
        System.out.println("Cargando datos desde la Base de Datos...");
        try {
            // ¡EL ORDEN ES CRÍTICO!
            
            // 1. Cargar Usuarios (no dependen de nada)
            this.usuarios = this.usuarioDAO.cargarTodosLosUsuarios();
            System.out.println("Cargados " + this.usuarios.size() + " usuarios.");

            // 2. Cargar Venues (no dependen de nada)
            this.venues = this.venueDAO.cargarTodosLosVenues();
            System.out.println("Cargados " + this.venues.size() + " venues.");
            
            // 3. Cargar Eventos (Dependen de Usuarios y Venues)
            //    Necesitamos pasarle la lista de organizadores (que ya cargamos)
            List<OrganizadorEventos> organizadores = new ArrayList<>();
            for (Usuario u : this.usuarios) {
                if (u instanceof OrganizadorEventos) {
                    organizadores.add((OrganizadorEventos) u);
                }
            }
            this.eventos = this.eventoDAO.cargarTodosLosEventos(this.venues, organizadores);
            System.out.println("Cargados " + this.eventos.size() + " eventos.");
            
            // 4. Cargar Localidades (Dependen de Eventos)
            //    Recorremos los eventos y "poblamos" sus mapas de localidades
            for (Evento e : this.eventos) {
                // TODO: Cargar ofertas de la BD
                List<Oferta> ofertasDelEvento = new ArrayList<>(); 
                
                List<Localidades> localidades = this.localidadDAO.cargarLocalidadesParaEvento(e, ofertasDelEvento);
                // "Hidratamos" (llenamos) el mapa de localidades del evento
                for (Localidades loc : localidades) {
                    e.getLocalidades().put(loc.getNombreLocalidad(), loc);
                }
            }
            System.out.println("Localidades cargadas en sus eventos.");
            
            // 5. Cargar Tiquetes (Dependen de todo lo anterior)
            // TODO: Implementar TiqueteDAO.cargarTodosLosTiquetes
            // this.tiquetesVendidos = this.tiqueteDAO.cargarTodosLosTiquetes(...);
            System.out.println("Carga de tiquetes (PENDIENTE).");
            
            System.out.println("¡Carga de datos completada!");

        } catch (SQLException e) {
            System.err.println("¡¡ERROR CRÍTICO AL CARGAR LA BD!!: " + e.getMessage());
            e.printStackTrace();
            // Si la BD no carga, la app no puede continuar.
            System.exit(1); 
        }
    }
    
    
    private void mostrarMenuNoAutenticado() {
        System.out.println("\n--- Menú Principal ---");
        System.out.println("1. Iniciar Sesión");
        System.out.println("2. Registrarse (Próximamente)");
        System.out.println("0. Salir de la Aplicación");
        System.out.print("Elige una opción: ");

        int opcion = -1;
        try {
            opcion = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Por favor, introduce solo un número.");
            return;
        }

        switch (opcion) {
            case 1:
                logicaIniciarSesion();
                break;
            case 2:
                // TODO: logicaRegistrarUsuario();
                System.out.println("Función de registro aún no implementada.");
                break;
            case 0:
                System.out.println("¡Gracias por usar TICKETGOD!!!! Nos vemos crack.");
                System.exit(0); // Cierra la aplicación
                break;
            default:
                System.err.println("Opción no válida. Inténtalo de nuevo.");
        }
    }

    /**
     * Lógica para el inicio de sesión.
     */
    private void logicaIniciarSesion() {
        System.out.println("\n--- Iniciar Sesión ---");
        System.out.print("Usuario (login): ");
        String login = scanner.nextLine();
        System.out.print("Contraseña: ");
        String password = scanner.nextLine();

        // Buscamos al usuario en nuestra lista maestra (cargada de la BD)
        Usuario usuario = buscarUsuarioPorLogin(login);

        // Verificación
        if (usuario != null && usuario.getContrasena().equals(password)) {
            // ¡Éxito!
            this.usuarioActual = usuario; // ¡Guardamos la sesión!
            System.out.println("\n¡Bienvenido de nuevo, " + this.usuarioActual.getLogIn() + "!");
        } else {
            // Fracaso
            System.err.println("Error: Login o contraseña incorrectos.");
        }
    }

    /**
     * Método de ayuda para buscar un usuario en la lista maestra.
     * @param login El login a buscar.
     * @return El objeto Usuario o null si no se encuentra.
     */
    private Usuario buscarUsuarioPorLogin(String login) {
        for (Usuario u : this.usuarios) {
            if (u.getLogIn().equals(login)) {
                return u;
            }
        }
        return null; // No se encontró
    }
    
    private void mostrarMenuAutenticado() {
        if (this.usuarioActual instanceof Administrador) {
            mostrarMenuAdmin();
        } else if (this.usuarioActual instanceof OrganizadorEventos) {
            mostrarMenuOrganizador();
        } else if (this.usuarioActual instanceof UsuarioComprador) {
            mostrarMenuComprador();
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    private void cerrarSesion() {
        System.out.println("Cerrando sesión de " + this.usuarioActual.getLogIn() + "...");
        this.usuarioActual = null;
    }

    // --- Esqueletos de los Menús de Rol (¡Aquí va la magia!) ---

    private void mostrarMenuAdmin() {
        System.out.println("\n---que bueno verte en ticketgod️ ADMINISTRADOR [" + this.usuarioActual.getLogIn() + "] ---");
        System.out.println("Que quieres hacer?");
        System.out.println("1. Crear Nuevo Venue (Aprobado)");
        System.out.println("2. Aprobar Venue Pendiente");
        System.out.println("3. Ver Reporte de Ganancias (Próximamente)");
        System.out.println("4. Cancelar Evento (Próximamente)");
        System.out.println("0. Cerrar Sesión");
        System.out.print("Elige una opción: ");

        int opcion = -1;
        try {
            opcion = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Por favor, introduce solo un número.");
            return;
        }

        switch (opcion) {
            case 1:
                logicaAdminCrearVenue(); // ¡Nueva lógica!
                break;
            case 2:
                logicaAdminAprobarVenue(); // ¡Nueva lógica!
                break;
            case 3:
                System.out.println("Opción en construcción.");
                break;
            case 4:
                System.out.println("Opción en construcción.");
                break;
            case 0:
                cerrarSesion();
                break;
            default:
                System.err.println("Opción no válida.");
        }
    }
    private void logicaAdminCrearVenue() {
        System.out.println("\n--- 1. Crear Nuevo Venue (Aprobado) ---");
        try {
            // 1. Pedir datos (Consola)
            System.out.print("Tipo (ej. Estadio, Teatro): ");
            String tipo = scanner.nextLine();
            System.out.print("Ubicación (¡debe ser única!): ");
            String ubicacion = scanner.nextLine();
            System.out.print("Capacidad Máxima: ");
            int capacidad = Integer.parseInt(scanner.nextLine());
            // (Omitimos restricciones por simplicidad)

            // 2. Llamar a la Lógica de Negocio (Paso 2)
            // Obtenemos el usuario actual (sabemos que es Admin)
            Administrador admin = (Administrador) this.usuarioActual;
            Venue nuevoVenue = admin.crearVenue(tipo, ubicacion, capacidad, null);
            
            // 3. Llamar a la Persistencia (Paso 3)
            this.venueDAO.guardarVenue(nuevoVenue);
            
            // 4. Actualizar la "Memoria" de la App
            this.venues.add(nuevoVenue);
            
            System.out.println("¡ÉXITO! Se ha creado y aprobado el venue '" + ubicacion + "' con ID de BD: " + nuevoVenue.getIdVenue());
            
        } catch (NumberFormatException e) {
            System.err.println("Error: La capacidad debe ser un número.");
        } catch (SQLException e) {
            System.err.println("Error de Base de Datos: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }
    }
    
    private void logicaAdminAprobarVenue() {
        System.out.println("\n--- 2. Aprobar Venue Pendiente ---");
        
        // 1. Filtrar los venues PENDIENTES de la lista maestra
        List<Venue> venuesPendientes = new ArrayList<>();
        for (Venue v : this.venues) {
            if (v.getEstado().equals("PENDIENTE")) {
                venuesPendientes.add(v);
            }
        }
        
        // 2. Comprobar si hay algo que aprobar
        if (venuesPendientes.isEmpty()) {
            System.out.println("No hay venues pendientes de aprobación.");
            return;
        }
        
        // 3. Mostrar la lista de pendientes (Consola)
        System.out.println("Venues pendientes de aprobación:");
        for (int i = 0; i < venuesPendientes.size(); i++) {
            Venue v = venuesPendientes.get(i);
            System.out.println("  " + (i + 1) + ". [" + v.getTipo() + "] " + v.getUbicacion());
        }
        
        try {
            System.out.print("Elige el número del venue a aprobar (0 para cancelar): ");
            int opcion = Integer.parseInt(scanner.nextLine());
            
            if (opcion == 0) return;
            if (opcion < 1 || opcion > venuesPendientes.size()) {
                System.err.println("Opción no válida.");
                return;
            }
            
            // 4. Obtener el objeto Venue seleccionado
            Venue venueAprobar = venuesPendientes.get(opcion - 1);
            
            // 5. Llamar a la Lógica de Negocio (Paso 2)
            Administrador admin = (Administrador) this.usuarioActual;
            admin.aprobarVenue(venueAprobar); // Esto cambia el estado del objeto en "memoria"
            
            // 6. Llamar a la Persistencia (Paso 3)
            this.venueDAO.actualizarEstadoVenue(venueAprobar); // Esto guarda el cambio en la BD
            
            System.out.println("¡ÉXITO! El venue '" + venueAprobar.getUbicacion() + "' ha sido APROBADO.");

        } catch (NumberFormatException e) {
            System.err.println("Error: Debes introducir un número.");
        } catch (SQLException e) {
            System.err.println("Error de Base de Datos al actualizar el venue: " + e.getMessage());
        }
    }

    private void mostrarMenuOrganizador() {
        System.out.println("\n--- MENÚ DE ORGANIZADOR ---");
        System.out.println("1. Crear Evento (Próximamente)");
        System.out.println("2. Sugerir Venue (Próximamente)");
        System.out.println("3. Ver Reporte Financiero (Próximamente)");
        System.out.println("0. Cerrar Sesión");
        System.out.print("Elige una opción: ");
        
        int opcion = Integer.parseInt(scanner.nextLine());
        if (opcion == 0) {
            cerrarSesion();
        } else {
            System.out.println("Opción en construcción.");
        }
    }

    private void mostrarMenuComprador() {
        System.out.println("\n--- MENÚ DE COMPRADOR ---");
        System.out.println("1. Comprar Tiquete (Próximamente)");
        System.out.println("2. Ver mis Tiquetes (Próximamente)");
        System.out.println("3. Transferir Tiquete (Próximamente)");
        System.out.println("0. Cerrar Sesión");
        System.out.print("Elige una opción: ");
        
        int opcion = Integer.parseInt(scanner.nextLine());
        if (opcion == 0) {
            cerrarSesion();
        } else {
            System.out.println("Opción en construcción.");
        }
    }
    
    // TODO:
    // private void cargarDatosDesdeBD() { ... }
    // private void mostrarMenuPrincipal() { ... }
    // private void logicaRegistrarOrganizador() { ... }
    // private void logicaCrearEvento() { ... }
    // private void logicaComprarTiquete() { ... }
    // ... etc.
}