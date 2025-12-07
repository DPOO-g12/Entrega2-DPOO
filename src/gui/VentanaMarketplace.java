package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import app.TiqueteraApp;
import cliente.UsuarioComprador;
import marketplace.OfertaReventa;
import tiquetes.Tiquete;

public class VentanaMarketplace extends JFrame {

    private TiqueteraApp nucleo;
    private UsuarioComprador usuario;
    
    // Componentes
    private JList<String> listaOfertasVisual;
    private DefaultListModel<String> modeloLista;
    private List<OfertaReventa> ofertasReales; // Para mantener la referencia a los objetos
    
    private JButton btnComprar;
    private JButton btnVender;
    private JButton btnActualizar;

    public VentanaMarketplace(TiqueteraApp nucleo, UsuarioComprador usuario) {
        this.nucleo = nucleo;
        this.usuario = usuario;
        this.ofertasReales = new ArrayList<>();
        
        configurarVentana();
        iniciarComponentes();
        cargarOfertas();
    }

    private void configurarVentana() {
        setTitle("Marketplace de Reventa - TICKETGOD");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // --- TÍTULO ---
        JLabel lblTitulo = new JLabel("Ofertas Disponibles");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // --- LISTA CENTRAL ---
        modeloLista = new DefaultListModel<>();
        listaOfertasVisual = new JList<>(modeloLista);
        listaOfertasVisual.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaOfertasVisual.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(listaOfertasVisual);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTONES INFERIORES ---
        JPanel panelBotones = new JPanel();
        
        btnComprar = new JButton("Comprar Oferta");
        btnVender = new JButton("Vender mi Tiquete");
        btnActualizar = new JButton("Actualizar Lista");
        
        // Estilos
        btnComprar.setBackground(new Color(0, 100, 0)); // Verde
        btnComprar.setForeground(Color.BLACK);
        btnVender.setBackground(new Color(0, 0, 150)); // Azul
        btnVender.setForeground(Color.WHITE);

        // Acciones
        btnActualizar.addActionListener(e -> cargarOfertas());
        btnComprar.addActionListener(e -> accionComprar());
        btnVender.addActionListener(e -> accionVender());

        panelBotones.add(btnActualizar);
        panelBotones.add(btnVender);
        panelBotones.add(btnComprar);
        
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarOfertas() {
        modeloLista.clear();
        ofertasReales.clear();
        
        // Obtenemos las ofertas activas del Marketplace
        List<OfertaReventa> ofertas = nucleo.getMarketplace().getOfertasActivas();
        
        if (ofertas.isEmpty()) {
            modeloLista.addElement("--- No hay ofertas disponibles en este momento ---");
        } else {
            for (OfertaReventa oferta : ofertas) {
                // Filtro visual: No mostramos nuestras propias ofertas (opcional)
                // Si quieres verlas para borrarlas, quita este if
                
                ofertasReales.add(oferta);
                
                String item = String.format("Evento: %-20s | Precio: $%-10.2f | Vendedor: %s", 
                        oferta.getTiquete().getEvento().getNombre(),
                        oferta.getPrecio(),
                        oferta.getVendedor().getLogIn());
                
                if (oferta.getVendedor().equals(usuario)) {
                    item += " (TÚ)";
                }
                
                modeloLista.addElement(item);
            }
        }
    }

    private void accionComprar() {
        int index = listaOfertasVisual.getSelectedIndex();
        if (index < 0 || ofertasReales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona una oferta primero.");
            return;
        }
        
        OfertaReventa ofertaSel = ofertasReales.get(index);
        
        // Validar que no me compre a mí mismo
        if (ofertaSel.getVendedor().equals(usuario)) {
            int resp = JOptionPane.showConfirmDialog(this, "Es tu oferta. ¿Quieres eliminarla del mercado?", "Gestionar", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                try {
                    nucleo.getMarketplace().eliminarOferta(usuario, ofertaSel);
                    JOptionPane.showMessageDialog(this, "Oferta eliminada.");
                    cargarOfertas();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
            return;
        }

        // Confirmar compra
        int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Comprar tiquete por $" + ofertaSel.getPrecio() + "?", 
                "Confirmar Compra", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 1. Lógica del Marketplace (Mueve dinero y cambia dueño en memoria)
                nucleo.getMarketplace().comprarOferta(usuario, ofertaSel);
                
                // 2. Persistencia (Guardar cambios en BD)
                nucleo.getTiqueteDAO().actualizarClienteTiquete(ofertaSel.getTiquete()); // Tiquete cambió de dueño
                nucleo.getUsuarioDAO().actualizarSaldo(usuario); // Comprador pagó
                nucleo.getUsuarioDAO().actualizarSaldo(ofertaSel.getVendedor()); // Vendedor cobró
                
                JOptionPane.showMessageDialog(this, "¡Compra Exitosa! El tiquete ahora es tuyo.");
                cargarOfertas(); // Refrescar lista
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error en la compra: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void accionVender() {
        // 1. Filtrar mis tiquetes aptos (Activos, Transferibles, NO IMPRESOS)
        List<Tiquete> aptos = new ArrayList<>();
        for (Tiquete t : usuario.getTiquetesComprados()) {
            // REGLA CRÍTICA: No se pueden vender tiquetes impresos
            if (t.getEstado().equals("ACTIVO") && t.isTransferible() && !t.isImpreso()) {
                aptos.add(t);
            }
        }
        
        if (aptos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes tiquetes aptos para vender.\n(Recuerda: No puedes vender tiquetes ya impresos o Deluxe).");
            return;
        }
        
        // 2. Selector visual
        String[] opciones = new String[aptos.size()];
        for (int i = 0; i < aptos.size(); i++) {
            opciones[i] = aptos.get(i).getIdTiquete() + " - " + aptos.get(i).getEvento().getNombre();
        }
        
        String seleccion = (String) JOptionPane.showInputDialog(this, "Elige el tiquete a vender:", "Publicar Oferta", 
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        
        if (seleccion != null) {
            // Buscar objeto seleccionado
            Tiquete aVender = null;
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) {
                    aVender = aptos.get(i);
                    break;
                }
            }
            
            // 3. Pedir Precio
            String precioStr = JOptionPane.showInputDialog(this, "¿A qué precio lo quieres vender?");
            if (precioStr != null && !precioStr.isEmpty()) {
                try {
                    double precio = Double.parseDouble(precioStr);
                    
                    // 4. Publicar
                    nucleo.getMarketplace().publicarOferta(usuario, aVender, precio);
                    JOptionPane.showMessageDialog(this, "¡Oferta Publicada!");
                    cargarOfertas(); // Refrescar para ver mi propia oferta
                    
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Precio inválido.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        }
    }
}