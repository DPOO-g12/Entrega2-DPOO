package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import app.TiqueteraApp;
import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Venue;
import localidades.Localidades;
import tiquetes.Tiquete;

public class VentanaOrganizador extends JFrame {

    private TiqueteraApp nucleo;
    private OrganizadorEventos organizador;

    public VentanaOrganizador(TiqueteraApp nucleo, OrganizadorEventos organizador) {
        this.nucleo = nucleo;
        this.organizador = organizador;
        
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setTitle("Panel ORGANIZADOR - TICKETGOD");
        setSize(600, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // --- HEADER ---
        JPanel panelHeader = new JPanel(new GridLayout(2, 1));
        panelHeader.setBackground(new Color(255, 250, 205)); // Amarillo suave
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel lblTitulo = new JLabel("Bienvenido, " + organizador.getLogIn());
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblSubtitulo = new JLabel("Gestión de Eventos y Cortesías");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        panelHeader.add(lblTitulo);
        panelHeader.add(lblSubtitulo);
        add(panelHeader, BorderLayout.NORTH);

        // --- BOTONES ---
        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 15, 15));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        JButton btnCrearEvento = new JButton("1. Crear Nuevo Evento");
        JButton btnAnadirLocalidad = new JButton("2. Añadir Localidad a Evento");
        JButton btnSugerirVenue = new JButton("3. Sugerir Nuevo Venue");
        JButton btnCortesias = new JButton("4. Mis Cortesías (Transferir)");
        JButton btnReporte = new JButton("5. Ver Mis Ganancias");
        JButton btnGenerarCortesia = new JButton("6. Generar Cortesías (Pedir)"); // <--- NUEVO BOTÓN
        JButton btnCrearOferta = new JButton("7. Crear Oferta (Descuento)"); // <--- NUEVO BOTÓN
        JButton btnCrearPaquete = new JButton("8. Crear Abono de Temporada");
        btnCrearPaquete.setForeground(new Color(128, 0, 128)); // Morado
        
        JButton btnDeluxe = new JButton("9. Habilitar DELUXE");
        btnDeluxe.setForeground(new Color(184, 134, 11));
        
        // Estilo para resaltar la acción importante
        btnAnadirLocalidad.setForeground(new Color(0, 100, 0)); 
        btnAnadirLocalidad.setFont(new Font("Arial", Font.BOLD, 14));

        // ACCIONES
        btnCrearEvento.addActionListener(e -> accionCrearEvento());
        btnAnadirLocalidad.addActionListener(e -> accionAnadirLocalidad());
        btnSugerirVenue.addActionListener(e -> accionSugerirVenue());
        btnCortesias.addActionListener(e -> accionGestionarCortesias());
        btnReporte.addActionListener(e -> accionVerReporte());
        btnGenerarCortesia.addActionListener(e -> accionGenerarCortesias());
        btnCrearOferta.addActionListener(e -> accionCrearOferta());
        btnCrearPaquete.addActionListener(e -> accionCrearPaquete());
        btnDeluxe.addActionListener(e -> accionHabilitarDeluxe());

        panelBotones.add(btnCrearEvento);
        panelBotones.add(btnAnadirLocalidad);
        panelBotones.add(btnSugerirVenue);
        panelBotones.add(btnCortesias);
        panelBotones.add(btnReporte);
        panelBotones.add(btnGenerarCortesia);
        panelBotones.add(btnCrearOferta);
        panelBotones.add(btnCrearPaquete);
        panelBotones.add(btnDeluxe);
        
        add(panelBotones, BorderLayout.CENTER);

        // --- FOOTER ---
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

    // =======================================================
    //                 LÓGICA DE LOS BOTONES
    // =======================================================

    /**
     * BOTÓN 1: Crear Evento (Pide ID, Nombre, Fecha y Venue).
     */
    private void accionCrearEvento() {
        // 1. Filtrar Venues APROBADOS
        List<Venue> aprobados = new ArrayList<>();
        for (Venue v : nucleo.getVenues()) {
            if ("APROBADO".equalsIgnoreCase(v.getEstado())) {
                aprobados.add(v);
            }
        }

        if (aprobados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay Venues aprobados disponibles.");
            return;
        }

        // 2. Selector de Venue
        String[] opciones = new String[aprobados.size()];
        for (int i = 0; i < aprobados.size(); i++) {
            opciones[i] = aprobados.get(i).getUbicacion();
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, "Selecciona el Venue:", 
                "Crear Evento", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            Venue venueSel = null;
            for (Venue v : aprobados) if (v.getUbicacion().equals(seleccion)) venueSel = v;

            // 3. Pedir datos
            String id = JOptionPane.showInputDialog("ID único del Evento (ej. EVT-001):");
            String nombre = JOptionPane.showInputDialog("Nombre del Evento:");
            String fecha = JOptionPane.showInputDialog("Fecha (YYYY-MM-DD):");

            if (id != null && nombre != null && fecha != null) {
                try {
                    // A. Crear el objeto en memoria (lógica del dominio)
                    Evento nuevo = organizador.agregarEvento(id, nombre, fecha, venueSel);
                    
                    // B. ¡AQUÍ ESTÁ EL CAMBIO! -> Llamamos al puente para GUARDAR EN BD
                    nucleo.registrarNuevoEvento(nuevo);
                    
                    JOptionPane.showMessageDialog(this, "¡Evento Creado y Guardado en Base de Datos!");
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
                }
            }
        }
    }
    /**
     * BOTÓN 2: Añadir Localidad (Pregunta si es Numerada o General).
     */
    private void accionAnadirLocalidad() {
        // 1. Filtrar SOLO mis eventos propios
        List<Evento> misEventosPropios = new ArrayList<>();
        
        for (Evento e : nucleo.getEventos()) {
            // Verificamos que el evento tenga promotor y que sea YO (el usuario logueado)
            if (e.getPromotor() != null && 
                e.getPromotor().getLogIn().equals(organizador.getLogIn())) {
                misEventosPropios.add(e);
            }
        }

        if (misEventosPropios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes eventos registrados bajo tu nombre.\nCrea uno primero (Botón 1).");
            return;
        }

        // 2. Seleccionar Evento (Ahora solo muestra los míos)
        String[] nombres = new String[misEventosPropios.size()];
        for(int i=0; i<misEventosPropios.size(); i++) {
            nombres[i] = misEventosPropios.get(i).getNombre();
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, "Elige TU Evento:", 
                "Añadir Localidad", JOptionPane.QUESTION_MESSAGE, null, nombres, nombres[0]);

        if (seleccion != null) {
            Evento eventoSel = null;
            // Buscamos en la lista filtrada
            for(Evento e : misEventosPropios) {
                if(e.getNombre().equals(seleccion)) {
                    eventoSel = e;
                    break;
                }
            }

            // 3. Pedir datos de la Localidad
            String nombreLoc = JOptionPane.showInputDialog("Nombre de Localidad (ej. VIP, General):");
            if (nombreLoc == null || nombreLoc.isEmpty()) return;

            String precioStr = JOptionPane.showInputDialog("Precio del Tiquete ($):");
            String capStr = JOptionPane.showInputDialog("Capacidad Total:");

            // Pregunta de tipo (Numerada vs General)
            int tipoResp = JOptionPane.showOptionDialog(this, 
                    "¿Qué tipo de localidad es?", 
                    "Tipo de Localidad", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    new String[]{"Numerada (Sillas)", "General (No Numerada)"}, 
                    "General (No Numerada)");
            
            boolean esNumerada = (tipoResp == JOptionPane.YES_OPTION);

            try {
                double precio = Double.parseDouble(precioStr);
                int capacidad = Integer.parseInt(capStr);

                // Guardar en BD
                nucleo.agregarLocalidadAEvento(eventoSel, nombreLoc, precio, capacidad, esNumerada);
                
                JOptionPane.showMessageDialog(this, "¡Localidad '" + nombreLoc + "' añadida a TU evento exitosamente!");

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: Precio y Capacidad deben ser números.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    /**
     * BOTÓN 3: Sugerir Venue.
     */
    private void accionSugerirVenue() {
        String tipo = JOptionPane.showInputDialog(this, "Tipo (ej. Estadio, Teatro):");
        String ubicacion = JOptionPane.showInputDialog(this, "Nombre/Ubicación:");
        String capStr = JOptionPane.showInputDialog(this, "Capacidad Máxima:");
        
        if (tipo != null && ubicacion != null && capStr != null) {
            try {
                int capacidad = Integer.parseInt(capStr);
                
                // A. Crear objeto en memoria
                Venue v = organizador.sugerirVenue(tipo, ubicacion, capacidad, null);
                
                // B. ¡AQUÍ ESTÁ EL CAMBIO! -> Llamamos al puente para GUARDAR EN BD
                nucleo.registrarNuevoVenue(v);
                
                JOptionPane.showMessageDialog(this, "Venue sugerido y guardado en BD.\nEstado: PENDIENTE");
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "La capacidad debe ser un número.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
            }
        }
    }

    /**
     * BOTÓN 4: Transferir Cortesías.
     */
    private void accionGestionarCortesias() {
        java.util.List<tiquetes.Tiquete> misTiquetes = organizador.getTiquetesComprados();

        if (misTiquetes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes tiquetes de cortesía asignados.");
            return;
        }

        String[] opciones = new String[misTiquetes.size()];
        for (int i = 0; i < misTiquetes.size(); i++) {
            tiquetes.Tiquete t = misTiquetes.get(i);
            String estado = t.isImpreso() ? "[IMPRESO - BLOQUEADO]" : "[DISPONIBLE]";
            // Evitamos error si localidad es nula (puede pasar en paquetes)
            String locNombre = (t.getLocalidad() != null) ? t.getLocalidad().getNombreLocalidad() : "N/A";
            
            opciones[i] = t.getIdTiquete() + " - " + t.getEvento().getNombre() + " (" + locNombre + ") " + estado;
        }

        String seleccion = (String) JOptionPane.showInputDialog(this, 
                "Selecciona la cortesía a transferir:", "Mis Cortesías", 
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            tiquetes.Tiquete tiqueteSel = null;
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) tiqueteSel = misTiquetes.get(i);
            }

            if (tiqueteSel != null) {
                if (tiqueteSel.isImpreso()) {
                    JOptionPane.showMessageDialog(this, "ERROR: Este tiquete ya fue impreso. No se puede transferir.");
                    return;
                }
                String dest = JOptionPane.showInputDialog("Login del destinatario:");
                String pass = JOptionPane.showInputDialog("Confirma tu contraseña:");
                
                if (dest != null && pass != null) {
                    try {
                        organizador.transferirTiquete(tiqueteSel, pass, dest, nucleo.getUsuarios());
                        nucleo.getTiqueteDAO().actualizarClienteTiquete(tiqueteSel);
                        JOptionPane.showMessageDialog(this, "¡Transferencia exitosa a " + dest + "!");
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * BOTÓN 5: Reporte Financiero.
     */
    private void accionVerReporte() {
        try {
            // 1. Verificamos que existan tiquetes en el sistema
            List<tiquetes.Tiquete> todos = nucleo.getTiquetesVendidos();
            if (todos == null || todos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay tiquetes vendidos en todo el sistema aún.");
                return;
            }

            // 2. Verificamos que el organizador tenga eventos
            if (organizador.getEventosOrganizados() == null || organizador.getEventosOrganizados().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No tienes eventos registrados bajo tu autoría.");
                return;
            }

            // 3. Cálculo usando tu método
            Map<String, Double> reporte = organizador.calcularEstadoFinanciero(todos);
            
            if (reporte.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "El reporte está vacío. Posibles causas:\n- Nadie ha comprado tiquetes para tus eventos.\n- Los tiquetes son cortesías (no suman ganancia).");
                 return;
            }

            // 4. Construcción del texto
            StringBuilder sb = new StringBuilder("=== TUS GANANCIAS ===\n\n");
            
            // Ordenamos un poco la salida
            if (reporte.containsKey("GANANCIA_GLOBAL")) {
                sb.append("TOTAL GLOBAL: $").append(String.format("%,.0f", reporte.get("GANANCIA_GLOBAL"))).append("\n");
                sb.append("----------------------------\n");
            }

            for (Map.Entry<String, Double> entry : reporte.entrySet()) {
                String clave = entry.getKey();
                // Saltamos el global porque ya lo pusimos arriba
                if (clave.equals("GANANCIA_GLOBAL") || clave.equals("PORCENTAJE_VENTA_GLOBAL")) continue;

                if (clave.contains("PORCENTAJE")) {
                    sb.append(clave).append(": ").append(String.format("%.2f%%", entry.getValue())).append("\n");
                } else {
                    sb.append(clave).append(": $").append(String.format("%,.0f", entry.getValue())).append("\n");
                }
            }
            
            JTextArea textArea = new JTextArea(15, 40);
            textArea.setText(sb.toString());
            textArea.setEditable(false);
            
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Finanzas Organizador", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            e.printStackTrace(); // Imprime en consola para ver el error real
            JOptionPane.showMessageDialog(this, "Error generando reporte: " + e.getMessage() + "\n(Revisa la consola para más detalles)");
        }
    
    }
    
    private void accionGenerarCortesias() {
        // 1. Verificar eventos propios
        List<Evento> misEventos = organizador.getEventosOrganizados();
        if (misEventos == null || misEventos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes eventos registrados para generar cortesías.");
            return;
        }

        // 2. Seleccionar Evento
        String[] nombresEvt = new String[misEventos.size()];
        for(int i=0; i<misEventos.size(); i++) nombresEvt[i] = misEventos.get(i).getNombre();

        String selEvt = (String) JOptionPane.showInputDialog(this, "Elige el Evento:", 
                "Generar Cortesía", JOptionPane.QUESTION_MESSAGE, null, nombresEvt, nombresEvt[0]);

        if (selEvt == null) return;
        
        Evento eventoSel = null;
        for(Evento e : misEventos) if(e.getNombre().equals(selEvt)) eventoSel = e;

        // 3. Seleccionar Localidad
        if (eventoSel.getLocalidades() == null || eventoSel.getLocalidades().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Este evento no tiene localidades creadas.");
            return;
        }

        List<Localidades> locs = new ArrayList<>(eventoSel.getLocalidades().values());
        String[] nombresLoc = new String[locs.size()];

        // --- CORRECCIÓN DEL BUCLE ---
        for(int i=0; i<locs.size(); i++) {
            Localidades loc = locs.get(i);
            int disponibles = 0;

            if (loc instanceof localidades.NoNumerada) {
                // Si es No Numerada: Restamos Capacidad - Vendidos
                localidades.NoNumerada nn = (localidades.NoNumerada) loc;
                disponibles = nn.getCapacidadMax() - nn.getTiquetesVendidos();
            } 
            else if (loc instanceof localidades.Numerada) {
                // Si es Numerada: Contamos cuántos 'false' (libres) hay en el mapa
                localidades.Numerada num = (localidades.Numerada) loc;
                if (num.getAsientos() != null) {
                    for (Boolean ocupado : num.getAsientos().values()) {
                        if (!ocupado) disponibles++;
                    }
                }
            }

            nombresLoc[i] = loc.getNombreLocalidad() + " (Disp: " + disponibles + ")";
        }
        // -----------------------------

        String selLoc = (String) JOptionPane.showInputDialog(this, "Elige la Localidad:",
                "Generar Cortesía", JOptionPane.QUESTION_MESSAGE, null, nombresLoc, nombresLoc[0]);
        
        if (selLoc == null) return;

        Localidades locSel = locs.get(0); // Default
        // Búsqueda simple por coincidencia de string (puedes mejorarla con IDs si quieres)
        for (int i = 0; i < nombresLoc.length; i++) {
            if (nombresLoc[i].equals(selLoc)) locSel = locs.get(i);
        }

        // 4. Pedir Cantidad
        String cantStr = JOptionPane.showInputDialog("¿Cuántas cortesías necesitas?");
        if (cantStr == null) return;

        try {
            int cantidad = Integer.parseInt(cantStr);
            
            // 5. EJECUTAR LA LÓGICA DEL ORGANIZADOR
            // Llamamos a tu método comprarTiquete (que maneja la lógica de cortesía internamente)
            List<Tiquete> cortesias = organizador.comprarTiquete(locSel, cantidad, 0.0, 0.0);
            
            // 6. GUARDAR EN BD (Usando el nuevo método del núcleo)
            nucleo.registrarCortesias(cortesias);
            
            JOptionPane.showMessageDialog(this, "¡Éxito! Se han generado " + cantidad + " tiquetes de cortesía.\nAhora puedes verlos en 'Mis Cortesías' y transferirlos.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar: " + e.getMessage());
        }
    }
    
    /**
     * ACCIÓN NUEVA: Crear Descuento
     */
    private void accionCrearOferta() {
        // 1. Filtrar mis eventos
        List<Evento> misEventos = organizador.getEventosOrganizados();
        if (misEventos == null || misEventos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes eventos para crear ofertas.");
            return;
        }

        // 2. Seleccionar Evento
        String[] nombresEvt = new String[misEventos.size()];
        for(int i=0; i<misEventos.size(); i++) nombresEvt[i] = misEventos.get(i).getNombre();
        
        String selEvt = (String) JOptionPane.showInputDialog(this, "Elige el Evento:", 
                "Crear Oferta", JOptionPane.QUESTION_MESSAGE, null, nombresEvt, nombresEvt[0]);
        if (selEvt == null) return;
        
        Evento eventoSel = null;
        for(Evento e : misEventos) if(e.getNombre().equals(selEvt)) eventoSel = e;

        // 3. Seleccionar Localidad
        List<Localidades> locs = new ArrayList<>(eventoSel.getLocalidades().values());
        if (locs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Este evento no tiene localidades.");
            return;
        }
        
        String[] nombresLoc = new String[locs.size()];
        for(int i=0; i<locs.size(); i++) nombresLoc[i] = locs.get(i).getNombreLocalidad();

        String selLoc = (String) JOptionPane.showInputDialog(this, "Aplica descuento a:", 
                "Crear Oferta", JOptionPane.QUESTION_MESSAGE, null, nombresLoc, nombresLoc[0]);
        if (selLoc == null) return;

        Localidades locSel = null;
        for(Localidades l : locs) if(l.getNombreLocalidad().equals(selLoc)) locSel = l;

        // 4. Pedir Datos de la Oferta
        String descStr = JOptionPane.showInputDialog("Porcentaje de Descuento (ej. 20 para 20%):");
        String diasStr = JOptionPane.showInputDialog("¿Cuántos días durará la oferta?");

        try {
            double porc = Double.parseDouble(descStr);
            int dias = Integer.parseInt(diasStr);
            
            if (porc <= 0 || porc >= 100) {
                JOptionPane.showMessageDialog(this, "El porcentaje debe ser entre 1 y 99.");
                return;
            }

            // Convertir a decimal (20 -> 0.20)
            double descuentoDecimal = porc / 100.0;
            
            // Calcular fecha fin
            java.time.LocalDateTime fechaFin = java.time.LocalDateTime.now().plusDays(dias);
            
            // 5. Crear Objeto Oferta (Usando tu lógica)
            // Nota: Tu método crearOferta en OrganizadorEventos solo lo hace en memoria,
            // nosotros creamos el objeto manual aquí para mandarlo al nucleo y guardar en BD.
            eventos.Oferta nuevaOferta = new eventos.Oferta(true, descuentoDecimal, fechaFin, organizador);
            
            // 6. GUARDAR (Llamar al puente)
            nucleo.registrarOferta(locSel, nuevaOferta);
            
            JOptionPane.showMessageDialog(this, "¡Oferta Creada!\nAhora la localidad '" + locSel.getNombreLocalidad() + "' cuesta " + porc + "% menos.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingresa números válidos.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void accionCrearPaquete() {
        // 1. Obtener mis eventos
        List<Evento> misEventos = organizador.getEventosOrganizados();
        
        // Validación: Necesitas eventos para agrupar
        if (misEventos == null || misEventos.size() < 2) {
            JOptionPane.showMessageDialog(this, "Para crear un abono necesitas tener al menos 2 eventos registrados.\nActualmente tienes: " + (misEventos==null?0:misEventos.size()));
            return;
        }

        // --- SOLUCIÓN: USAR CHECKBOXES EN LUGAR DE LISTA ---
        // Creamos un panel vertical para poner las casillas
        JPanel panelCheckboxes = new JPanel();
        panelCheckboxes.setLayout(new BoxLayout(panelCheckboxes, BoxLayout.Y_AXIS));
        
        // Lista para guardar las referencias a las casillas y saber cuáles se marcaron después
        List<JCheckBox> listaChecks = new ArrayList<>();
        
        for (Evento e : misEventos) {
            // Creamos una casilla por cada evento
            JCheckBox chk = new JCheckBox(e.getNombre());
            listaChecks.add(chk);
            panelCheckboxes.add(chk);
        }

        // Ponemos el panel dentro de un Scroll por si tienes muchos eventos
        JScrollPane scroll = new JScrollPane(panelCheckboxes);
        scroll.setPreferredSize(new Dimension(300, 200));

        // Mostramos el diálogo
        int opt = JOptionPane.showConfirmDialog(this, scroll, 
                "Marca los eventos para el Abono:", JOptionPane.OK_CANCEL_OPTION);
        
        if (opt == JOptionPane.OK_OPTION) {
            // 2. Recolectar los nombres de los que quedaron marcados (Check = true)
            List<String> seleccionados = new ArrayList<>();
            for (JCheckBox chk : listaChecks) {
                if (chk.isSelected()) {
                    seleccionados.add(chk.getText());
                }
            }

            // Validar que seleccionó al menos 2
            if (seleccionados.size() < 2) {
                JOptionPane.showMessageDialog(this, "Debes marcar mínimo 2 casillas para crear un abono.");
                return;
            }

            // 3. Pedir localidad para cada evento seleccionado
            List<Localidades> localidadesParaPaquete = new ArrayList<>();

            for (String nombreEvt : seleccionados) {
                Evento evt = null;
                for(Evento e : misEventos) if(e.getNombre().equals(nombreEvt)) evt = e;

                if (evt.getLocalidades() == null || evt.getLocalidades().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El evento '" + nombreEvt + "' no tiene localidades. Operación cancelada.");
                    return;
                }
                
                // Selector simple de localidad para este evento
                String[] locNombres = new String[evt.getLocalidades().size()];
                int k=0;
                List<Localidades> locList = new ArrayList<>(evt.getLocalidades().values());
                for(Localidades l : locList) locNombres[k++] = l.getNombreLocalidad();
                
                String locSel = (String) JOptionPane.showInputDialog(this, 
                        "Para el evento '" + nombreEvt + "',\nelige qué localidad incluir en el abono:", 
                        "Configurando Abono", JOptionPane.QUESTION_MESSAGE, null, locNombres, locNombres[0]);
                
                if(locSel == null) return; // Canceló
                
                for(Localidades l : locList) if(l.getNombreLocalidad().equals(locSel)) localidadesParaPaquete.add(l);
            }

            // 4. Pedir precio total
            String precioStr = JOptionPane.showInputDialog("Precio TOTAL del Abono:");
            if (precioStr == null) return;

            try {
                double precio = Double.parseDouble(precioStr);
                
                // 5. Crear el Paquete (Tu lógica de negocio)
                tiquetes.Multiple paquete = organizador.crearPaquetePaseDeTemporada(localidadesParaPaquete, precio);
                
                // 6. Guardar en BD (Tu lógica de persistencia)
                nucleo.registrarPaquete(paquete);
                
                JOptionPane.showMessageDialog(this, "¡Abono Creado Exitosamente!\nIncluye entradas para " + seleccionados.size() + " eventos.");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void accionHabilitarDeluxe() {
        // 1. BUSCAR MIS EVENTOS
        List<Evento> misEventos = organizador.getEventosOrganizados();
        if (misEventos == null || misEventos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes eventos registrados.");
            return;
        }

        // 2. SELECCIONAR EVENTO
        String[] nombresEvt = new String[misEventos.size()];
        for(int i=0; i<misEventos.size(); i++) nombresEvt[i] = misEventos.get(i).getNombre();

        String selEvt = (String) JOptionPane.showInputDialog(this, "Elige el Evento:", 
                "Habilitar Deluxe", JOptionPane.QUESTION_MESSAGE, null, nombresEvt, nombresEvt[0]);

        if (selEvt == null) return; // Canceló

        Evento eventoSel = null;
        for(Evento e : misEventos) if(e.getNombre().equals(selEvt)) eventoSel = e;

        // 3. SELECCIONAR LOCALIDAD
        if (eventoSel.getLocalidades() == null || eventoSel.getLocalidades().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Este evento no tiene localidades creadas.");
            return;
        }

        List<Localidades> locs = new ArrayList<>(eventoSel.getLocalidades().values());
        String[] nombresLoc = new String[locs.size()];
        for(int i=0; i<locs.size(); i++) nombresLoc[i] = locs.get(i).getNombreLocalidad();

        String selLoc = (String) JOptionPane.showInputDialog(this, "Elige la Localidad a volver Deluxe:", 
                "Habilitar Deluxe", JOptionPane.QUESTION_MESSAGE, null, nombresLoc, nombresLoc[0]);

        if (selLoc == null) return; // Canceló

        // Definimos la variable locSel que faltaba
        Localidades locSel = null;
        for(Localidades l : locs) if(l.getNombreLocalidad().equals(selLoc)) locSel = l;

        // --- AHORA SÍ EMPIEZA TU LÓGICA ---

        if (locSel.isDeluxeHabilitado()) {
            JOptionPane.showMessageDialog(this, "Esta localidad YA tiene venta Deluxe habilitada.");
            return;
        }

        // 4. Pedir Configuración
        String cantStr = JOptionPane.showInputDialog("¿Cuántos tiquetes Deluxe quieres habilitar? (Cupo máximo):");
        String precioStr = JOptionPane.showInputDialog("¿Cuál es el PRECIO EXTRA sobre el valor base?");
        String benef = JOptionPane.showInputDialog("Beneficios (ej. Gorra, Fila Rápida):");

        // Validar que no cancelara los inputs
        if (cantStr == null || precioStr == null || benef == null) return;

        try {
            int limite = Integer.parseInt(cantStr);
            double extra = Double.parseDouble(precioStr);

            if (limite > locSel.getCapacidadMax()) {
                JOptionPane.showMessageDialog(this, "El límite Deluxe no puede ser mayor a la capacidad total (" + locSel.getCapacidadMax() + ").");
                return;
            }

            // 5. Configurar Objeto en Memoria
            locSel.setDeluxeHabilitado(true);
            locSel.setDeluxeLimite(limite);
            locSel.setDeluxePrecioExtra(extra);
            locSel.setDeluxeBeneficios(benef);

            // 6. Guardar en BD
            // Usamos una nueva instancia del DAO para guardar esta configuración específica
            new persistencia.LocalidadDAO().habilitarDeluxe(locSel);

            JOptionPane.showMessageDialog(this, "¡Venta Deluxe Habilitada!\nLos compradores ahora verán la opción.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: Cantidad y Precio deben ser números.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}