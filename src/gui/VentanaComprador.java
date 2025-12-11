package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import app.TiqueteraApp;
import cliente.UsuarioComprador;
import eventos.Evento;
import localidades.Localidades;
import tiquetes.Basico;
import tiquetes.Tiquete;

public class VentanaComprador extends JFrame {

    private TiqueteraApp nucleo;
    private UsuarioComprador usuario;
    private JLabel lblSaldo; 

    public VentanaComprador(TiqueteraApp nucleo, UsuarioComprador usuario) {
        this.nucleo = nucleo;
        this.usuario = usuario;
        
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Panel Comprador - TICKETGOD");
        setSize(700, 550); // Un poco más alto para el nuevo botón
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // --- 1. ENCABEZADO (Bienvenida y Saldo) ---
        JPanel panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelHeader.setBackground(new Color(240, 248, 255)); // Azul muy clarito

        JLabel lblTitulo = new JLabel("Hola, " + usuario.getLogIn());
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        lblSaldo = new JLabel("Saldo Disponible: $" + String.format("%,.0f", usuario.getSaldo()));
        lblSaldo.setFont(new Font("Arial", Font.BOLD, 18));
        lblSaldo.setForeground(new Color(0, 100, 0)); // Verde oscuro
        lblSaldo.setHorizontalAlignment(SwingConstants.CENTER);

        panelHeader.add(lblTitulo);
        panelHeader.add(lblSaldo);
        add(panelHeader, BorderLayout.NORTH);

        // --- 2. BOTONES PRINCIPALES ---
        // Cambiamos a 3 filas, 2 columnas para acomodar el 5to botón
        JPanel panelBotones = new JPanel(new GridLayout(3, 2, 20, 20));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnComprar = crearBoton("1. Comprar Tiquetes", "Busca eventos y compra entradas");
        JButton btnMisTiquetes = crearBoton("2. Mis Tiquetes (Imprimir)", "Ve tus compras y genera el QR");
        JButton btnMarketplace = crearBoton("3. Marketplace", "Compra y vende tiquetes de otros");
        JButton btnRecargar = crearBoton("4. Recargar Saldo", "Añade dinero a tu cuenta");
        JButton btnTransferir = crearBoton("5. Transferir a Amigo", "Envía un tiquete a otro usuario"); // <--- NUEVO
        JButton btnAbono = crearBoton("6. Comprar Abono / Paquete", "Arma un combo de varios eventos");
        
        
        
        // Botón de salir (lo ponemos en el grid para rellenar el hueco 6, o en el footer)
        // Por diseño, dejaremos el hueco 6 vacío o ponemos un label vacío.

        // --- ACCIONES ---
        
        // 1. Comprar
        btnComprar.addActionListener(e -> {
            if (nucleo.getEventos().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay eventos disponibles en el sistema.");
            } else {
                new VentanaCompra(nucleo, usuario).setVisible(true);
            }
        });

        // 2. Ver / Imprimir
        btnMisTiquetes.addActionListener(e -> accionVerMisTiquetes());

        // 3. Marketplace
        btnMarketplace.addActionListener(e -> {
            new VentanaMarketplace(nucleo, usuario).setVisible(true);
        });

        // 4. Recargar
        btnRecargar.addActionListener(e -> accionRecargarSaldo());

        // 5. Transferir (NUEVO)
        btnTransferir.addActionListener(e -> accionTransferirTiquete());

        btnAbono.addActionListener(e -> accionComprarAbono());

        panelBotones.add(btnComprar);
        panelBotones.add(btnMisTiquetes);
        panelBotones.add(btnMarketplace);
        panelBotones.add(btnRecargar);
        panelBotones.add(btnTransferir); // <--- AGREGADO
        panelBotones.add(btnAbono);
        

        add(panelBotones, BorderLayout.CENTER);

        // --- 3. PIE DE PÁGINA (Logout) ---
        JPanel panelFooter = new JPanel();
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setForeground(Color.RED);
        
        btnLogout.addActionListener(e -> {
            this.dispose(); 
            new VentanaLogin(nucleo).setVisible(true);
        });
        
        panelFooter.add(btnLogout);
        add(panelFooter, BorderLayout.SOUTH);
    }

    private JButton crearBoton(String titulo, String tooltip) {
        JButton btn = new JButton(titulo);
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.setToolTipText(tooltip);
        return btn;
    }

    // =======================================================
    //                 LÓGICA DE LOS BOTONES
    // =======================================================

    private void accionRecargarSaldo() {
        String input = JOptionPane.showInputDialog(this, "¿Cuánto quieres recargar?");
        if (input != null && !input.isEmpty()) {
            try {
                double monto = Double.parseDouble(input);
                if (monto > 0) {
                    double nuevoSaldo = usuario.getSaldo() + monto;
                    usuario.setSaldo(nuevoSaldo);
                    
                    // Actualizar BD
                    nucleo.getUsuarioDAO().actualizarSaldo(usuario);
                    
                    // Actualizar Vista
                    lblSaldo.setText("Saldo Disponible: $" + String.format("%,.0f", nuevoSaldo));
                    JOptionPane.showMessageDialog(this, "¡Recarga exitosa!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void accionVerMisTiquetes() {
        List<Tiquete> misTiquetesOriginales = usuario.getTiquetesComprados();

        if (misTiquetesOriginales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes tiquetes comprados.");
            return;
        }

        // Listas auxiliares para el mapeo visual
        List<String> lineasVisuales = new ArrayList<>();
        List<Tiquete> referenciasTiquetes = new ArrayList<>(); // Para saber cuál abrir al hacer click

        // --- LÓGICA DE "DESEMPAQUETADO" ---
        for (Tiquete t : misTiquetesOriginales) {
            
            if (t instanceof tiquetes.Multiple) {
                // SI ES UN PAQUETE: Mostramos sus hijos individuales
                tiquetes.Multiple pack = (tiquetes.Multiple) t;
                
                if (pack.getTiquetesIncluidos() != null) {
                    for (Tiquete hijo : pack.getTiquetesIncluidos()) {
                        String estado = obtenerEstadoVisual(hijo);
                        String nombreEvt = (hijo.getEvento() != null) ? hijo.getEvento().getNombre() : "Evento";
                        
                        // Formato: "ID - Evento [📦 PACK ABONO] - ESTADO"
                        String linea = hijo.getIdTiquete() + " - " + nombreEvt + " [📦 PACK ABONO] " + estado;
                        
                        lineasVisuales.add(linea);
                        referenciasTiquetes.add(hijo); // Guardamos ref al hijo para poder abrirlo individualmente
                    }
                }
            } 
            else {
                // SI ES UN TIQUETE NORMAL (Basico o Deluxe)
                String estado = obtenerEstadoVisual(t);
                String tipo = (t instanceof tiquetes.Deluxe) ? " [⭐ DELUXE]" : "";
                String nombreEvt = (t.getEvento() != null) ? t.getEvento().getNombre() : "Evento";
                
                String linea = t.getIdTiquete() + " - " + nombreEvt + tipo + " " + estado;
                
                lineasVisuales.add(linea);
                referenciasTiquetes.add(t);
            }
        }

        // Convertir a Array para el JOptionPane
        String[] opciones = lineasVisuales.toArray(new String[0]);

        if (opciones.length == 0) {
            JOptionPane.showMessageDialog(this, "Error visualizando tiquetes.");
            return;
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, "Tus Entradas:", "Mis Tiquetes",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            // Buscamos cuál seleccionó en nuestra lista de referencias
            int index = -1;
            for(int i=0; i<opciones.length; i++) {
                if(opciones[i].equals(seleccion)) {
                    index = i;
                    break;
                }
            }
            
            if (index != -1) {
                Tiquete tiqueteSeleccionado = referenciasTiquetes.get(index);
                
                // Validar estado antes de abrir
                if ("REEMBOLSADO".equalsIgnoreCase(tiqueteSeleccionado.getEstado())) {
                    JOptionPane.showMessageDialog(this, "Este tiquete fue cancelado y reembolsado.");
                } else {
                    // Abrimos la boleta individual (incluso si viene de un paquete)
                    new VentanaTiquete(nucleo, tiqueteSeleccionado).setVisible(true);
                }
            }
        }
    }

    // Helper pequeño para no repetir código de estados
    private String obtenerEstadoVisual(Tiquete t) {
        if ("REEMBOLSADO".equalsIgnoreCase(t.getEstado()) || "CANCELADO".equalsIgnoreCase(t.getEstado())) return "- 🚫 CANCELADO";
        if (t.isImpreso()) return "- 🖨️ IMPRESO";
        if ("VENCIDO".equalsIgnoreCase(t.getEstado())) return "- ⌛ VENCIDO";
        return "- ✅ DISPONIBLE";
    }

    // --- NUEVO MÉTODO PARA TRANSFERIR ---
    private void accionTransferirTiquete() {
        List<Tiquete> misTiquetes = usuario.getTiquetesComprados();
        
        // 1. Filtrar solo los transferibles (No impresos, no vencidos)
        List<Tiquete> aptos = new ArrayList<>();
        for (Tiquete t : misTiquetes) {
            if (!t.isImpreso() && t.isTransferible() && "ACTIVO".equalsIgnoreCase(t.getEstado())) {
                aptos.add(t);
            }
        }

        if (aptos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes tiquetes aptos para transferir.\n(Recuerda: No puedes transferir si ya lo imprimiste).");
            return;
        }

        // 2. Selector visual
        String[] opciones = new String[aptos.size()];
        for (int i = 0; i < aptos.size(); i++) {
            String nombreEvt = (aptos.get(i).getEvento() != null) ? aptos.get(i).getEvento().getNombre() : "Paquete";
            opciones[i] = aptos.get(i).getIdTiquete() + " - " + nombreEvt;
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, 
                "Elige el tiquete a regalar:", "Transferir Tiquete", 
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            Tiquete aTransferir = null;
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) aTransferir = aptos.get(i);
            }

            // 3. Pedir Destinatario y Password
            String destino = JOptionPane.showInputDialog("Ingresa el usuario (Login) de tu amigo:");
            if (destino == null || destino.isEmpty()) return;

            String pass = JOptionPane.showInputDialog("Confirma con TU contraseña por seguridad:");
            if (pass == null) return;

            try {
                // 4. Ejecutar Lógica
                // Usamos el método transferirTiquete del usuario (asumiendo que sigue la misma lógica que Organizador)
                usuario.transferirTiquete(aTransferir, pass, destino, nucleo.getUsuarios());

                // 5. Actualizar BD (Cambiar dueño)
                nucleo.getTiqueteDAO().actualizarClienteTiquete(aTransferir);
                
                JOptionPane.showMessageDialog(this, "¡Tiquete enviado exitosamente a " + destino + "!");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error en transferencia: " + e.getMessage(), "Fallo", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Permite al usuario seleccionar varios eventos y comprarlos como un solo PAQUETE MULTIPLE.
     */
    /**
     * CATÁLOGO DE ABONOS:
     * Muestra solo los paquetes creados previamente por los Organizadores.
     * El comprador elige uno y el sistema genera una COPIA nueva para él.
     */
    /**
     * CATÁLOGO DE ABONOS MEJORADO (CON TABLA VISUAL)
     */
    private void accionComprarAbono() {
        // 1. Buscar Abonos en el sistema
        List<tiquetes.Multiple> abonosDisponibles = new ArrayList<>();
        for (Tiquete t : nucleo.getTiquetesVendidos()) {
            // Es un abono si es de tipo Multiple Y lo vende un Organizador
            if (t instanceof tiquetes.Multiple && t.getCliente() instanceof cliente.OrganizadorEventos) {
                abonosDisponibles.add((tiquetes.Multiple) t);
            }
        }

        if (abonosDisponibles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay Abonos disponibles en el catálogo.");
            return;
        }

        // 2. Construir lista visual BONITA
        String[] opciones = new String[abonosDisponibles.size()];
        
        for (int i = 0; i < abonosDisponibles.size(); i++) {
            tiquetes.Multiple abono = abonosDisponibles.get(i);
            
            // --- RECUPERAR NOMBRES DE EVENTOS ---
            StringBuilder nombres = new StringBuilder();
            
            // Intento A: Leer de los tiquetes hijos
            if (abono.getTiquetesIncluidos() != null && !abono.getTiquetesIncluidos().isEmpty()) {
                for(Tiquete hijo : abono.getTiquetesIncluidos()) {
                    if(hijo.getEvento() != null) nombres.append(hijo.getEvento().getNombre()).append(", ");
                }
            } 
            // Intento B: Leer de la lista de eventos asociados
            else if (abono.getEventosAsociados() != null && !abono.getEventosAsociados().isEmpty()) {
                for(Evento e : abono.getEventosAsociados()) {
                    nombres.append(e.getNombre()).append(", ");
                }
            } else {
                nombres.append("Sin eventos detallados");
            }
            
            // Limpieza visual
            String listaEventos = nombres.toString();
            if (listaEventos.endsWith(", ")) listaEventos = listaEventos.substring(0, listaEventos.length()-2);
            
            opciones[i] = "Abono #" + abono.getIdTiquete() + " | $" + String.format("%,.0f", abono.getPrecioFinal()) + " | Incluye: [" + listaEventos + "]";
        }

        // 3. Mostrar Selector
        String seleccion = (String) JOptionPane.showInputDialog(this, 
                "Selecciona un Abono de Temporada:", "Catálogo de Abonos", 
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            // Encontrar el objeto seleccionado
            int index = -1;
            for(int i=0; i<opciones.length; i++) if(opciones[i].equals(seleccion)) index = i;
            tiquetes.Multiple plantilla = abonosDisponibles.get(index);

            // --- VALIDACIÓN CRÍTICA: NO COMPRAR AIRE ---
            if (plantilla.getTiquetesIncluidos() == null || plantilla.getTiquetesIncluidos().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Este abono parece estar vacío (Error de carga de BD).\nIntenta reiniciar el programa o contactar al admin.");
                return;
            }

            // 4. Validar Saldo
            if (usuario.getSaldo() < plantilla.getPrecioFinal()) {
                JOptionPane.showMessageDialog(this, "Saldo insuficiente. Costo: $" + String.format("%,.0f", plantilla.getPrecioFinal()));
                return;
            }

            // 5. PROCESAR COMPRA (CLONAR)
            try {
                List<Tiquete> nuevosHijos = new ArrayList<>();
                List<Evento> eventosDelPack = new ArrayList<>();

                for (Tiquete tHijo : plantilla.getTiquetesIncluidos()) {
                    Localidades loc = tHijo.getLocalidad();
                    
                    // Verificar cupo
                    if (!loc.verificarDisponibilidad(1)) {
                        JOptionPane.showMessageDialog(this, "El evento '" + loc.getEvento().getNombre() + "' se agotó.");
                        return; 
                    }

                    // Vender (Ocupar asiento nuevo)
                    List<String> asientos = loc.venderTiquetes(1);

                    // Crear mi copia del tiquete
                    Tiquete hijoPropio = new Basico(
                        loc.getPrecioFinal(), 0, 0, 
                        java.time.LocalDate.now().toString(), 
                        usuario, loc, loc.getEvento(), 
                        "ACTIVO", asientos.get(0)
                    );
                    nuevosHijos.add(hijoPropio);
                    eventosDelPack.add(loc.getEvento());
                }

                // Crear mi copia del Paquete
                tiquetes.Multiple nuevoPaquete = new tiquetes.Multiple(
                    plantilla.getPrecioFinal(),
                    java.time.LocalDate.now().toString(),
                    usuario,
                    "ACTIVO",
                    nuevosHijos,
                    eventosDelPack
                );

                // 6. GUARDAR
                nucleo.registrarPaquete(nuevoPaquete); 
                usuario.getTiquetesComprados().add(nuevoPaquete); // Actualizar mi lista local

                // Descontar
                usuario.setSaldo(usuario.getSaldo() - plantilla.getPrecioFinal());
                nucleo.getUsuarioDAO().actualizarSaldo(usuario);
                lblSaldo.setText("Saldo Disponible: $" + String.format("%,.0f", usuario.getSaldo()));

                JOptionPane.showMessageDialog(this, "¡Abono comprado!\nHas adquirido entradas para " + nuevosHijos.size() + " eventos.\nRevisa 'Mis Tiquetes'.");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error en la compra: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}