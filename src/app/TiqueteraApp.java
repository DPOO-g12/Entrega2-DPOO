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
    private Administrador admin;
   
    
    
    // --- DAOs (Los "Traductores" a la BD) ---
    // Instancias de todos nuestros traductores
    private final UsuarioDAO usuarioDAO;
    private final VenueDAO venueDAO;
    private final EventoDAO eventoDAO;
    private final LocalidadDAO localidadDAO;
    private final TiqueteDAO tiqueteDAO;
    
  
   

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
        this.usuarioActual = null;
        this.admin = null;
    }
    



    
  
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
    public void cargarDatosDesdeBD() {
        System.out.println("Cargando datos desde la Base de Datos...");
        try {
            // ¡EL ORDEN ES MUY IMPORTANTE OJOOOO!
            
            // 1. Cargar Usuarios 
            this.usuarios = this.usuarioDAO.cargarTodosLosUsuarios();
            System.out.println("Cargados " + this.usuarios.size() + " usuarios.");

            // 2. Cargar Venues
            this.venues = this.venueDAO.cargarTodosLosVenues();
            System.out.println("Cargados " + this.venues.size() + " venues.");
            
            // 3. Cargar Eventos 
            List<OrganizadorEventos> organizadores = new ArrayList<>();
            for (Usuario u : this.usuarios) {
                if (u instanceof OrganizadorEventos) {
                    organizadores.add((OrganizadorEventos) u);
                }
            }
            this.eventos = this.eventoDAO.cargarTodosLosEventos(this.venues, organizadores);
            System.out.println("Cargados " + this.eventos.size() + " eventos.");
            
            // 4. Cargar Localidades (Dependen de Eventos)
            for (Evento e : this.eventos) {
                List<Oferta> ofertasDelEvento = new ArrayList<>(); 
                List<Localidades> localidades = this.localidadDAO.cargarLocalidadesParaEvento(e, ofertasDelEvento);
                for (Localidades loc : localidades) {
                    e.getLocalidades().put(loc.getNombreLocalidad(), loc);
                }
            }
            System.out.println("Localidades cargadas en sus eventos.");
            Map<String, Usuario> mapaUsuarios = new HashMap<>();
            for (Usuario u : this.usuarios) mapaUsuarios.put(u.getLogIn(), u);
            
            Map<String, Evento> mapaEventos = new HashMap<>();
            for (Evento e : this.eventos) mapaEventos.put(e.getId(), e);

            Map<Integer, Localidades> mapaLocalidades = new HashMap<>();
            for (Evento e : this.eventos) {
                for (Localidades loc : e.getLocalidades().values()) {
                    mapaLocalidades.put(loc.getIdLocalidad(), loc);
                }
            }
            
         // 5. Cargar Tiquetes 
            System.out.println("Cargando tiquetes vendidos...");
            this.tiquetesVendidos = this.tiqueteDAO.cargarTodosLosTiquetes(mapaUsuarios, mapaLocalidades, mapaEventos);
            System.out.println("Cargados " + this.tiquetesVendidos.size() + " tiquetes."); 
            
            for (Evento evento : this.eventos) {
                // Obtenemos el promotor del evento
                OrganizadorEventos promotorDelEvento = evento.getPromotor();
                if (promotorDelEvento != null && !promotorDelEvento.getEventosOrganizados().contains(evento)) {
                     promotorDelEvento.getEventosOrganizados().add(evento);
                }
            }
            for (Tiquete t : this.tiquetesVendidos) {
                Usuario dueno = t.getCliente();
                if (dueno instanceof UsuarioComprador) {
                    ((UsuarioComprador) dueno).getTiquetesComprados().add(t);
                } else if (dueno instanceof OrganizadorEventos) {
                    ((OrganizadorEventos) dueno).getTiquetesComprados().add(t);
                }
            }
            
            System.out.println("Tiquetes conectados a sus dueños.");
            System.out.println("Eventos conectados a sus organizadores.");
            for (Usuario u : this.usuarios) {
                if (u instanceof Administrador) {
                    this.admin = (Administrador) u;
                    System.out.println("Administrador '" + this.admin.getLogIn() + "' cargado.");
                    break; // Asumimos que solo hay un Admin
                }
            }
            if (this.admin == null) {
                 System.err.println("¡ADVERTENCIA! No se encontró ningún Administrador en la BD. Los cargos no se aplicarán.");
            }
            
            System.out.println("¡Carga de datos completada!");

        } catch (SQLException e) {
            System.err.println("¡¡ERROR CRÍTICO AL CARGAR LA BD!!: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); 
        }
    }
    
    
    public void mostrarMenuNoAutenticado(Scanner scanner) {
        System.out.println("\n--- Menú Principal ---");
        System.out.println("1. Iniciar Sesión");
        System.out.println("2. Registrarse como Comprador"); // <-- ¡CAMBIADO!
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
                logicaIniciarSesion(scanner);
                break;
            case 2:
                logicaPublicaRegistrarComprador(scanner); // <-- ¡YA NO ESTÁ EN CONSTRUCCIÓN!
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
    public void logicaIniciarSesion(Scanner scanner) {
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

    public Usuario buscarUsuarioPorLogin(String login) {
        for (Usuario u : this.usuarios) {
            if (u.getLogIn().equals(login)) {
                return u;
            }
        }
        return null; // No se encontró
    }
    public void logicaPublicaRegistrarComprador(Scanner scanner) {
        System.out.println("\n--- 2. Registro de Nuevo Comprador ---");
        System.out.println("¡Bienvenido a TICKETGODDDD ! Vamos a crear tu cuenta.");
        try {
            // 1. Pedir datos (Consola)
            System.out.print("Crea tu login: ");
            String login = scanner.nextLine();
            System.out.print("Crea tu contraseña: ");
            String password = scanner.nextLine();
            
            // 2. Validar que el login no exista ya
            if (buscarUsuarioPorLogin(login) != null) {
                System.err.println("¡Error! Ese login '" + login + "' ya existe. Prueba con otro, crack.");
                return;
            }

            // 3. Llamar a la Lógica de Negocio (Paso 2)
            // El saldo inicial siempre es 0 al registrarse
            UsuarioComprador nuevoComprador = new UsuarioComprador(login, password, 0.0);
            
            // 4. Llamar a la Persistencia (Paso 3)
            System.out.println("Guardando tu cuenta en la Base de Datos...");
            this.usuarioDAO.guardarUsuario(nuevoComprador);
            
            // 5. Actualizar la "Memoria" de la App
            this.usuarios.add(nuevoComprador);
            
            System.out.println("¡LISTO, " + login + "! Tu cuenta ha sido creada.");
            System.out.println("Ahora puedes Iniciar Sesión (Opción 1).");
            
        } catch (SQLException e) {
            System.err.println("¡Error de Base de Datos! No se pudo guardar: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("¡Error inesperado! " + e.getMessage());
        }
    }
    
    public void mostrarMenuAutenticado(Scanner scanner) {
        if (this.usuarioActual instanceof Administrador) {
            mostrarMenuAdmin(scanner);
        } else if (this.usuarioActual instanceof OrganizadorEventos) {
            mostrarMenuOrganizador(scanner);
        } else if (this.usuarioActual instanceof UsuarioComprador) {
            mostrarMenuComprador(scanner);
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void cerrarSesion() {
        System.out.println("Cerrando sesión de " + this.usuarioActual.getLogIn() + "...");
        this.usuarioActual = null;
    }
    public Usuario getUsuarioActual() {
        return this.usuarioActual;
    }

    // --- Esqueletos de los Menús de Rol (¡Aquí va la magia!) ---

    public void mostrarMenuAdmin(Scanner scanner) {
        // ¡Tu estilo se mantiene!
        System.out.println("\n---que bueno verte en ticketgod️ ADMINISTRADOR [" + this.usuarioActual.getLogIn() + "] ---");
        System.out.println("Que quieres hacer?");
        System.out.println("1. Crear Nuevo Venue (Aprobado)");
        System.out.println("2. Aprobar Venue Pendiente");
        System.out.println("3. Crear Nuevo Organizador"); 
        System.out.println("4. Ver Reporte de Ganancias"); 
        System.out.println("5. Cancelar Evento (Próximamente)"); 
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
                logicaAdminCrearVenue(scanner);
                break;
            case 2:
                logicaAdminAprobarVenue(scanner);
                break;
            case 3:
                logicaAdminCrearOrganizador(scanner); 
                break;
            case 4: 
            	logicaAdminVerReporte(scanner);
                break;
            case 5: // 
                System.out.println("Opción en construcción.");
                break;
            case 0:
                cerrarSesion();
                break;
            default:
                System.err.println("Opción no válida.");
        }
    }
    public void logicaAdminVerReporte(Scanner scanner) {
        System.out.println("\n--- 4. Reporte de Ganancias (TICKETGOD) ---");
        
        try {
            Administrador admin = (Administrador) this.usuarioActual;
            
            Map<String, Double> reporte = admin.calcularGanancias(
                this.tiquetesVendidos, 
                this.eventos
            );
            
            // 2. Mostrar el reporte (Consola)
            System.out.println("Este es tu reporte de ganancias de la plataforma administrador:");
            System.out.println("--------------------------------------");
            
            // Itera sobre el Map y lo imprimimos de forma bonita
            for (Map.Entry<String, Double> entry : reporte.entrySet()) {
                String clave = entry.getKey();
                Double valor = entry.getValue();
                
                // Imprime todo como ganancias
                System.out.printf("%-30s: $%,.2f\n", clave, valor);
            }
            System.out.println("--------------------------------------");

        } catch (Exception e) {
            System.err.println("¡Error inesperado al generar el reporte! " + e.getMessage());
        }
    }
    
    
    public void logicaAdminCrearVenue(Scanner scanner) {
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
    
    public void logicaAdminAprobarVenue(Scanner scanner) {
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

    public void mostrarMenuOrganizador(Scanner scanner) {
        System.out.println("\n--- ¡Bienvenido a TICKETGOD, Organizador [" + this.usuarioActual.getLogIn() + "]! ---");
        System.out.println("¿Qué quieres crear hoy?");
        System.out.println("1. Crear Nuevo Evento"); 
        System.out.println("2. Sugerir Nuevo Venue");
        System.out.println("3. Añadir Localidad a un Evento"); 
        System.out.println("4. Ver Reporte Financiero"); 
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
                logicaOrganizadorCrearEvento(scanner);
                break;
            case 2:
                logicaOrganizadorSugerirVenue(scanner);
                break;
            case 3:
               
            	logicaOrganizadorAnadirLocalidad(scanner);
                break;
            case 4:
            	logicaOrganizadorVerReporte(scanner);
                break;
            case 0:
           
                break;
            default:
                System.err.println("Opción no válida.");
        }
        
        // Si el usuario elige 0, cerramos sesión
        if (opcion == 0) {
            cerrarSesion();
        }
    }

    public void mostrarMenuComprador(Scanner scanner) {
        // ¡Mostramos el saldo actualizado cada vez!
        System.out.println("\n--- ¡Bienvenido a TICKETGOD, Comprador [" + this.usuarioActual.getLogIn() + "]! ---");
        System.out.println("Saldo actual: $" + this.usuarioActual.getSaldo());
        System.out.println("---------------------------------");
        System.out.println("1. Comprar Tiquetes");
        System.out.println("2. Ver mis Tiquetes");
        System.out.println("3. Transferir Tiquete");
        System.out.println("4. Recargar Saldo"); 
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
                logicaCompradorComprarTiquete(scanner);
                break;
            case 2:
            	logicaCompradorVerMisTiquetes(scanner);
                break;
            case 3:
            	logicaCompradorTransferirTiquete(scanner);
                break;
            case 4:
                logicaCompradorAnadirSaldo(scanner); 
                break;
            case 0:
                // No llamamos a cerrarSesion() aquí
                break;
            default:
                System.err.println("Opción no válida.");
        }
        
        if (opcion == 0) {
            cerrarSesion();
        }
    }
    public void logicaCompradorTransferirTiquete(Scanner scanner) {
        System.out.println("\n--- 3. Transferir Tiquete ---");
        
        try {
            // 1. Mostrarle al usuario los tiquetes que PUEDE transferir
            System.out.println("Tus tiquetes transferibles:");
            List<Tiquete> misTiquetes;
            List<Tiquete> tiquetesTransferibles = new ArrayList<>();
            
            // Obtenemos los tiquetes del comprador
            if (this.usuarioActual instanceof UsuarioComprador) {
                misTiquetes = ((UsuarioComprador) this.usuarioActual).getTiquetesComprados();
            } else { return; } 

            // Filtramos solo los que se pueden transferir
            for (Tiquete t : misTiquetes) {
                if (t.isTransferible() && t.getEstado().equals("ACTIVO")) {
                    tiquetesTransferibles.add(t);
                }
            }

            if (tiquetesTransferibles.isEmpty()) {
                System.out.println("No tienes tiquetes 'ACTIVOS' que sean transferibles en este momento.");
                return;
            }

            // 2. Imprimir la lista filtrada
            for (int i = 0; i < tiquetesTransferibles.size(); i++) {
                Tiquete t = tiquetesTransferibles.get(i);
                System.out.println("  " + (i + 1) + ". [" + t.getIdTiquete() + "] " + 
                                   (t.getEvento() != null ? t.getEvento().getNombre() : "Paquete"));
            }

            // 3. Pedir datos (Consola)
            System.out.print("Elige el número del tiquetes que quieres transferir (0 para cancelar): ");
            int opcion = Integer.parseInt(scanner.nextLine());
            if (opcion == 0) return;
            if (opcion < 1 || opcion > tiquetesTransferibles.size()) {
                System.err.println("Opción no válida.");
                return;
            }
            
            Tiquete tiqueteATransferir = tiquetesTransferibles.get(opcion - 1);

            System.out.print("Escribe el login del usuario que recibirá el tiquete: ");
            String loginDestinatario = scanner.nextLine();
            
            System.out.print("Para confirmar, escribe TU contraseña: ");
            String passwordConfirmacion = scanner.nextLine();

            // 4. Llamar a la Lógica  (Paso 2)
            System.out.println("Validando y transfiriendo...");
            this.usuarioActual.transferirTiquete(
                tiqueteATransferir, 
                passwordConfirmacion, 
                loginDestinatario, 
                this.usuarios // la lista  para buscar
            );
            
            // 5. Llamar a la Persistencia (Paso 3)
            // ¡Guardamos los cambios en la Base De datos!
            this.tiqueteDAO.actualizarClienteTiquete(tiqueteATransferir);
            this.tiqueteDAO.actualizarEstadoTiquete(tiqueteATransferir); // (El estado cambió a "TRANSFERIDO")
            
            System.out.println("PERFECTO, tu tiquwte ahora ha sido transferido a '" + loginDestinatario + "'.");
            System.out.println("Ya no está en tu lista de 'Mis Tiquetes'.");

        } catch (NumberFormatException e) {
            System.err.println("Error: Debes introducir un número válido.");
        } catch (TiqueteNoTransferibleException | AutenticacionFallidaException e) {
            // Capturamos los errores de negocio que definimos
            System.err.println("¡Transferencia Fallida! " + e.getMessage());
        } catch (Exception e) {
            // Capturamos otros errores (ej. "Usuario no encontrado")
            System.err.println("¡Error! " + e.getMessage());
        }
    }
    public void logicaCompradorVerMisTiquetes(Scanner scanner) {
        System.out.println("\n--- 2. Mis Tiquetes ---");
        
        try {
            // 1. Obtener la lista de tiquetes del usuario
            List<Tiquete> misTiquetes;
            if (this.usuarioActual instanceof UsuarioComprador) {
                misTiquetes = ((UsuarioComprador) this.usuarioActual).getTiquetesComprados();
            } else {
                
                System.err.println("Error: No se pudo identificar al comprador.");
                return;
            }

            // 2. Comprobar si tiene tiquetes
            if (misTiquetes.isEmpty()) {
                System.out.println("No tienes ningún tiquete en tu cuenta. ¡Compra uno!");
                return;
            }

            // 3. Imprimir la lista
            System.out.println("Estos son tus tiquetes  " + this.usuarioActual.getLogIn() + " en TICKETGOD, ESTAS LISTOOO !!!!️:");
            int i = 1;
            for (Tiquete t : misTiquetes) {
                System.out.println("---------------------------------");
                System.out.println("  Tiquete #" + i + " (ID: " + t.getIdTiquete() + ")");
                System.out.println("  Evento: " + (t.getEvento() != null ? t.getEvento().getNombre() : "Paquete Múltiple"));
                System.out.println("  Localidad: " + (t.getLocalidad() != null ? t.getLocalidad().getNombreLocalidad() : "N/A"));
                System.out.println("  Precio Pagado: $" + t.getPrecioFinal());
                System.out.println("  Estado: " + t.getEstado());
                System.out.println("  Transferible: " + (t.isTransferible() ? "Sí" : "No"));

                // Imprimir detalles específicos del tipo de tiquete
                if (t instanceof Basico) {
                    Basico b = (Basico) t;
                    if (b.getNumeroAsiento() != null) {
                        System.out.println("  Asiento: " + b.getNumeroAsiento());
                    } else {
                        System.out.println("  Asiento: General (No numerado)");
                    }
                } else if (t instanceof Deluxe) {
                    System.out.println("  Beneficios: " + ((Deluxe) t).getBeneficiosAdicionales());
                } else if (t instanceof Multiple) {
                    System.out.println("  Tiquetes Incluidos: " + ((Multiple) t).getTiquetesIncluidos().size());
                }
                
                i++;
            }
            System.out.println("---------------------------------");

        } catch (Exception e) {
            System.err.println("¡Error inesperado al mostrar tus tiquetes! " + e.getMessage());
        }
    }
    public void logicaCompradorAnadirSaldo(Scanner scanner) {
        System.out.println("\n--- 4. Recargar Saldo ---");
        try {
            System.out.print("¿Cuánto saldo quieres recargar?  ");
            double monto = Double.parseDouble(scanner.nextLine());

            if (monto <= 0) {
                System.err.println("Error: Debes recargar un monto positivo.");
                return;
            }

            // 1. Lógica de Negocio (Paso 2)
            // Actualizamos el objeto en "memoria"
            double saldoAnterior = this.usuarioActual.getSaldo();
            this.usuarioActual.setSaldo(saldoAnterior + monto);
            
            // 2. Lógica de Persistencia (Paso 3)
            // Actualizamos la BD
            System.out.println("Actualizando tu saldo en la BD...");
            this.usuarioDAO.actualizarSaldo(this.usuarioActual);
            
            System.out.println("¡ÉXITO! Tu saldo ha sido actualizado.");
            System.out.println("Saldo anterior: $" + saldoAnterior);
            System.out.println("Nuevo saldo: $" + this.usuarioActual.getSaldo());

        } catch (NumberFormatException e) {
            System.err.println("Error: Debes introducir un número válido.");
        } catch (SQLException e) {
            System.err.println("¡Error de Base de Datos! No se pudo actualizar tu saldo: " + e.getMessage());
        }
    }
    public void logicaCompradorComprarTiquete(Scanner scanner) {
        System.out.println("\n--- 1. ¡Comprar Tiquetes! ---");
        
        try {
            // 1. Validar que haya un Admin
            if (this.admin == null) {
                System.err.println("Error del sistema: No hay un Administrador configurado. No se pueden procesar ventas.");
                return;
            }
            
            // 2. Mostrar Eventos Activos
            List<Evento> eventosActivos = new ArrayList<>();
            for (Evento e : this.eventos) {
            	if (e.getEstado() != null && e.getEstado().trim().equalsIgnoreCase("ACTIVO")) {
                    eventosActivos.add(e);
                }
            }
            if (eventosActivos.isEmpty()) {
                System.out.println("¡Qué lástima! No hay eventos activos a la venta en este momento.");
                return;
            }
            
            System.out.println("Eventos disponibles:");
            for (int i = 0; i < eventosActivos.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + eventosActivos.get(i).getNombre());
            }
            System.out.print("Elige el número del evento (0 para cancelar): ");
            int opcionEvento = Integer.parseInt(scanner.nextLine());
            if (opcionEvento == 0) return;
            Evento eventoSel = eventosActivos.get(opcionEvento - 1);

            // 3. Mostrar Localidades del Evento
            List<Localidades> localidades = new ArrayList<>(eventoSel.getLocalidades().values());
            if (localidades.isEmpty()) {
                System.err.println("Error: Este evento no tiene localidades configuradas.");
                return;
            }

            System.out.println("Localidades para '" + eventoSel.getNombre() + "':");
            for (int i = 0; i < localidades.size(); i++) {
                Localidades loc = localidades.get(i);
                System.out.println("  " + (i + 1) + ". " + loc.getNombreLocalidad() + 
                                   " ($" + loc.getPrecioFinal() + ") - " + 
                                   (loc.verificarDisponibilidad(1) ? "¡Disponible!" : "Agotado"));
            }
            System.out.print("Elige el número de la localidad (0 para cancelar): ");
            int opcionLoc = Integer.parseInt(scanner.nextLine());
            if (opcionLoc == 0) return;
            Localidades locSel = localidades.get(opcionLoc - 1);

            // 4. Pedir Cantidad
            System.out.print("¿Cuántos tiquetes quieres para '" + locSel.getNombreLocalidad() + "'? ");
            int cantidad = Integer.parseInt(scanner.nextLine());

            // 5. ¡Llamar a la Lógica de Negocio (Paso 2)!
            System.out.println("Procesando tu compra...");
            
            // Obtenemos los cargos del Admin
            double cobroEmision = this.admin.getCobroPorEmision();
            double porcServicio = this.admin.getPorcentajesServiciosTipoEvento().getOrDefault("default", 0.15); // Asumimos 15%
            
            List<Tiquete> tiquetesComprados = this.usuarioActual.comprarTiquete(
                locSel, 
                cantidad, 
                porcServicio, 
                cobroEmision
            );
            
            // 6. ¡Llamar a la Persistencia (Paso 3)!
            System.out.println("Guardando tus tiquetes en la BD...");
            
            for (Tiquete t : tiquetesComprados) {
                // 6.1. Guardar el tiquete
                this.tiqueteDAO.guardarTiquete(t);
                this.tiquetesVendidos.add(t); // Añadir a la memoria
                
                // 6.2. Actualizar el inventario en la BD
                if (locSel instanceof NoNumerada) {
                    this.localidadDAO.actualizarVentaNoNumerada((NoNumerada) locSel);
                } else if (locSel instanceof Numerada) {
                    // Hacemos el "cast"
                    // Primero, nos aseguramos de que el tiquete sea Basico
                    if (t instanceof Basico) {
                        // Creamos una variable temporal del tipo correcto
                        Basico tiqueteBasico = (Basico) t;
                        
                        // Ahora sí podemos llamar al método
                        this.localidadDAO.actualizarAsientoOcupado(
                            locSel.getIdLocalidad(), 
                            tiqueteBasico.getNumeroAsiento()
                        );
                    }
                }
                }
            
            
            // 6.3. Actualizar el saldo del usuario en la BD
            this.usuarioDAO.actualizarSaldo(this.usuarioActual);
            
            System.out.println("Perfectooo Has comprado " + cantidad + " tiquete(s) para '" + eventoSel.getNombre() + "'.");
            System.out.println("Tu nuevo saldo es: $" + this.usuarioActual.getSaldo());

        } catch (NumberFormatException e) {
            System.err.println("Error: Debes introducir un número válido.");
        } catch (Exception e) {
            // ¡Capturamos SaldoInsuficienteException, CapacidadExcedidaLocalidad, etc.!
            System.err.println("¡Error al procesar tu compra! " + e.getMessage());
        }
    }
    public void logicaAdminCrearOrganizador(Scanner scanner) {
        System.out.println("\n--- 3. Registrar un nuevo Organizador ---");
        System.out.println("bueno Vamos a crear un nuevo promotor para TICKETGODDD");
        try {
            // 1. Pedir datos (Consola)
            System.out.print("Crea un Login para el nuevo organizador: ");
            String login = scanner.nextLine();
            System.out.print("Contraseña para '" + login + "': ");
            String password = scanner.nextLine();
            
            // 2. Validar que el login no exista ya
            if (buscarUsuarioPorLogin(login) != null) {
                System.err.println("¡Error! Ese login '" + login + "' ya existe. Prueba con otro, crack.");
                return;
            }

            // 3. Llamar a la Lógica de Negocio (Paso 2)
            // Creamos el objeto. El saldo inicial es 0.0
            OrganizadorEventos nuevoOrganizador = new OrganizadorEventos(login, password, 0.0);
            
            // 4. Llamar a la Persistencia (Paso 3)
            // El UsuarioDAO ya sabe cómo guardar un Organizador (en 2 tablas)
            System.out.println("Guardando a '" + login + "' en la BD...");
            this.usuarioDAO.guardarUsuario(nuevoOrganizador);
            
            // 5. Actualizar la "Memoria" de la App
            // Añadimos el nuevo usuario a la lista maestra
            this.usuarios.add(nuevoOrganizador);
            
            System.out.println("LISTO! El organizador '" + login + "' ha sido creado. ¡Ya puedes iniciar sesión!");
            
        } catch (SQLException e) {
            System.err.println("Error de Base de Datos! No se pudo guardar: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado! " + e.getMessage());
        }
    }
    
    public void logicaOrganizadorSugerirVenue(Scanner scanner) {
        System.out.println("\n--- 2. Sugerir Nuevo Venue ---");
        System.out.println("El Venue que sugieras debe ser aprobado por un Admin antes de poder usarlo.");
        try {
            // 1. Pedir datos (Consola)
            System.out.print("Tipo de venue: ");
            String tipo = scanner.nextLine();
            System.out.print("Seleccionar ubicacion (La ubicación debe ser única): ");
            String ubicacion = scanner.nextLine();
            System.out.print("Capacidad Máxima: ");
            int capacidad = Integer.parseInt(scanner.nextLine());

            // 2. Llamar a la Lógica de Negocio (Paso 2)
            // Obtenemos el usuario actual (sabemos que es Organizador)
            OrganizadorEventos organizador = (OrganizadorEventos) this.usuarioActual;
            Venue nuevoVenue = organizador.sugerirVenue(tipo, ubicacion, capacidad, null);
            
            // 3. Llamar a la Persistencia (Paso 3)
            System.out.println("Enviando sugerencia al admin...");
            this.venueDAO.guardarVenue(nuevoVenue);
            
            // 4. Actualizar la "Memoria" de la App
            this.venues.add(nuevoVenue);
            
            System.out.println("Perfectooo Se ha sugerido el venue '" + ubicacion + "'.");
            System.out.println("Espera a que un Administrador lo apruebe.");
            
        } catch (NumberFormatException e) {
            System.err.println("Error: La capacidad debe ser un número.");
        } catch (SQLException e) {
            System.err.println("Error de Base de Datos: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }
    }
    
    public void logicaOrganizadorCrearEvento(Scanner scanner) {
        System.out.println("\n--- 1. Crear Nuevo Evento ---");
        
        try {
            // 1. Filtrar los venues APROBADOS
            List<Venue> venuesAprobados = new ArrayList<>();
            for (Venue v : this.venues) {
                // Solo mostramos venues aprobados
                if (v.getEstado().equals("APROBADO")) { 
                    venuesAprobados.add(v);
                }
            }
            
            // 2. Validar que existan venues para usar
            if (venuesAprobados.isEmpty()) {
                System.err.println("¡Error! No hay Venues aprobados para crear un evento.");
                System.err.println("Sugiere un Venue (Opción 2) y espera a que un Admin lo apruebe.");
                return;
            }

            // 3. Mostrar la lista de venues disponibles (Consola)
            System.out.println("Selecciona el Venue para tu evento:");
            for (int i = 0; i < venuesAprobados.size(); i++) {
                Venue v = venuesAprobados.get(i);
                System.out.println("  " + (i + 1) + ". [" + v.getTipo() + "] " + v.getUbicacion() + " (Cap: " + v.getCapacidadMaxima() + ")");
            }

            System.out.print("Elige el número del venue (0 para cancelar): ");
            int opcionVenue = Integer.parseInt(scanner.nextLine());
            
            if (opcionVenue == 0) return;
            if (opcionVenue < 1 || opcionVenue > venuesAprobados.size()) {
                System.err.println("Opción no válida.");
                return;
            }
            
            // 4. Obtener el Venue seleccionado
            Venue venueSeleccionado = venuesAprobados.get(opcionVenue - 1);

            // 5. Pedir datos del Evento (Consola)
            System.out.print("ID único para tu evento, por ejemplo 'KendrickLamar2025': ");
            String idEvento = scanner.nextLine();
            System.out.print("Nombre del evento: ");
            String nombreEvento = scanner.nextLine();
            System.out.print("Fecha del evento (ej. '2025-12-25'): ");
            String fechaEvento = scanner.nextLine();
            
            // 6. Llamar a la Lógica de Negocio (Paso 2)
            OrganizadorEventos organizador = (OrganizadorEventos) this.usuarioActual;
            System.out.println("Creando evento y verificando disponibilidad del venue...");
            
            Evento nuevoEvento = organizador.agregarEvento(idEvento, nombreEvento, fechaEvento, venueSeleccionado);
            
            // 7. Llamar a la Persistencia (Paso 3)
            System.out.println("Guardando evento en la Base de Datos...");
            this.eventoDAO.guardarEvento(nuevoEvento);
            
            // 8. Actualizar la "Memoria" de la App
            this.eventos.add(nuevoEvento);
            
            System.out.println("Listooo, el evento '" + nombreEvento + "' ha sido creado.");
            System.out.println("El siguiente paso es añadirle localidades (Opción 3 del menú).");

        } catch (NumberFormatException e) {
            System.err.println("Error: Debes introducir un número.");
        } catch (SQLException e) {
            // Capturamos los errores de la BD (ej. ID de evento duplicado, FKs)
            System.err.println("¡Error de Base de Datos! " + e.getMessage());
        } catch (Exception e) {
            // Capturamos los errores de la Lógica (ej. VenueOcupado)
            System.err.println("¡Error de Lógica! " + e.getMessage());
        }
    }
    public void logicaOrganizadorVerReporte(Scanner scanner) {
        System.out.println("\n--- 3. Reporte Financiero, lo que has ganado ---");
        
        try {
            // 1. Lógica de Negocio (Paso 2)
            // ¡Llamamos al método que ya habíamos hecho!
            OrganizadorEventos organizador = (OrganizadorEventos) this.usuarioActual;
            
            // ¡Le pasamos la lista maestra de tiquetes!
            Map<String, Double> reporte = organizador.calcularEstadoFinanciero(this.tiquetesVendidos);
            
            // 2. Mostrar el reporte (Consola)
            System.out.println("Aquí está tu reporte de ventas TICKETGOD :");
            System.out.println("--------------------------------------");
            
            // Itera sobre el Map y lo imprime de forma bonita
            for (Map.Entry<String, Double> entry : reporte.entrySet()) {
                String clave = entry.getKey();
                Double valor = entry.getValue();
                
                if (clave.startsWith("GANANCIA_")) {
                    System.out.printf("%-30s: $%,.2f\n", clave, valor);
                } else if (clave.startsWith("PORCENTAJE_")) {
                    System.out.printf("%-30s: %.2f%%\n", clave, valor);
                }
            }
            System.out.println("--------------------------------------");

        } catch (Exception e) {
            System.err.println("¡Error inesperado al generar el reporte! " + e.getMessage());
        }
    }
    public void logicaOrganizadorAnadirLocalidad(Scanner scanner) {
        System.out.println("\n--- 3. Añadir Localidad a un Evento ---");
        
        try {
            // 1. Obtener y mostrar los eventos de ESTE organizador
            OrganizadorEventos organizador = (OrganizadorEventos) this.usuarioActual;
            List<Evento> misEventos = organizador.getEventosOrganizados();
            
            if (misEventos.isEmpty()) {
                System.err.println("¡Error! Aún no has creado ningún evento (Opción 1).");
                return;
            }

            System.out.println("Selecciona el evento al que quieres añadir la localidad:");
            for (int i = 0; i < misEventos.size(); i++) {
                Evento e = misEventos.get(i);
                System.out.println("  " + (i + 1) + ". " + e.getNombre() + " (ID: " + e.getId() + ")");
            }
            
            System.out.print("Elige el número del evento (0 para cancelar): ");
            int opcionEvento = Integer.parseInt(scanner.nextLine());
            
            if (opcionEvento == 0) return;
            if (opcionEvento < 1 || opcionEvento > misEventos.size()) {
                System.err.println("Opción no válida.");
                return;
            }
            
            Evento eventoSeleccionado = misEventos.get(opcionEvento - 1);

            // 2. Pedir tipo de localidad
            System.out.println("¿Qué tipo de localidad quieres añadir a '" + eventoSeleccionado.getNombre() + "'?");
            System.out.println("  1. No Numerada (Ej: Gramilla, General)");
            System.out.println("  2. Numerada (Ej: Platea, VIP con asientos fijos)");
            System.out.print("Elige el tipo: ");
            int tipoLocalidad = Integer.parseInt(scanner.nextLine());

            // 3. Pedir datos de la localidad
            System.out.print("Nombre de la localidad (ej. 'VIP', 'Platea Baja'): ");
            String nombreLoc = scanner.nextLine();
            System.out.print("Precio base (ej. 150000): ");
            double precio = Double.parseDouble(scanner.nextLine());
            System.out.print("Capacidad Máxima para esta localidad: ");
            int capacidad = Integer.parseInt(scanner.nextLine());

            // 4. Lógica (Paso 2) y Persistencia (Paso 3)
            if (tipoLocalidad == 1) {
                // --- NO NUMERADA  ---
                System.out.println("Creando localidad No Numerada...");
                
                // Lógica (Paso 2):
                eventoSeleccionado.agregarLocalidadNoNumerada(nombreLoc, precio, capacidad);
                
                // Persistencia (Paso 3):
                // obtener el nuevo objeto que acabamos de crear
                Localidades nuevaLoc = eventoSeleccionado.getLocalidades().get(nombreLoc); 
                this.localidadDAO.guardarLocalidad(nuevaLoc);

                System.out.println("Perfectoo, La Localidad '" + nombreLoc + "' fue añadida a '" + eventoSeleccionado.getNombre() + "'.");

            } else if (tipoLocalidad == 2) {
                // --- NUMERADA (Generamos los asientos) ---
                System.out.println("Listo, ahora Vamos a generar los asientos.");
                System.out.print("Escoje un Prefijo (forma de dsitinguir los asientos)(ej. 'A-', 'B-', 'VIP-'): ");
                String prefijo = scanner.nextLine();
                
                System.out.println("Generando " + capacidad + " asientos (ej. " + prefijo + "1 ... " + prefijo + capacidad + ")...");
                Map<String, Boolean> asientos = new HashMap<>();
                for (int i = 1; i <= capacidad; i++) {
                    asientos.put(prefijo + i, false); // false = disponible
                }
                
                // Lógica (Paso 2):
                eventoSeleccionado.agregarLocalidadNumerada(nombreLoc, precio, capacidad, asientos);
                
                // Persistencia (Paso 3):
                Localidades nuevaLoc = eventoSeleccionado.getLocalidades().get(nombreLoc);
                //  LocalidadDAO ya sabe cómo guardar los asientos
                this.localidadDAO.guardarLocalidad(nuevaLoc); 
                
                System.out.println("Perfectoo la Localidad Numerada '" + nombreLoc + "' con " + capacidad + " asientos fue añadida.");

            } else {
                System.err.println("Tipo no válido.");
            }
            
        } catch (NumberFormatException e) {
            System.err.println("Error: introduce un número válido.");
        } catch (SQLException e) {
            System.err.println("¡Error de Base de Datos! No se pudo guardar la localidad: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("¡Error inesperado! " + e.getMessage());
        }
        
    }

}