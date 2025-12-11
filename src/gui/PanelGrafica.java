package gui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;
import tiquetes.Tiquete;

public class PanelGrafica extends JPanel {

    private Map<String, Integer> ventasPorFecha;
    private List<String> fechasOrdenadas;

    public PanelGrafica(List<Tiquete> tiquetes) {
        this.ventasPorFecha = new HashMap<>();
        
        // 1. Procesar los datos: Contar ventas por fecha
        for (Tiquete t : tiquetes) {
            // Asumimos que la fecha viene formato "YYYY-MM-DD" o similar
            String fecha = t.getFecha(); 
            ventasPorFecha.put(fecha, ventasPorFecha.getOrDefault(fecha, 0) + 1);
        }
        
        // 2. Ordenar las fechas cronológicamente
        this.fechasOrdenadas = new ArrayList<>(ventasPorFecha.keySet());
        Collections.sort(fechasOrdenadas);
        
        // Configuración visual básica
        this.setPreferredSize(new Dimension(600, 400));
        this.setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Convertimos a Graphics2D para mejores herramientas
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- DIMENSIONES DEL GRÁFICO ---
        int padding = 50;
        int ancho = getWidth() - (2 * padding);
        int alto = getHeight() - (2 * padding);
        
        // Dibujar Ejes X e Y
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(padding, getHeight() - padding, padding, padding); // Eje Y
        g2.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding); // Eje X

        if (fechasOrdenadas.isEmpty()) {
            g2.drawString("No hay datos para graficar", getWidth()/2 - 50, getHeight()/2);
            return;
        }

        // --- CÁLCULOS MATEMÁTICOS ---
        int numBarras = fechasOrdenadas.size();
        int anchoBarra = ancho / numBarras;
        // Dejamos un espacio entre barras
        int espacioEntreBarras = Math.max(2, anchoBarra / 4); 
        int anchoRealBarra = anchoBarra - espacioEntreBarras;

        // Encontrar el valor máximo para escalar la altura
        int maxVentas = 0;
        for (int v : ventasPorFecha.values()) if (v > maxVentas) maxVentas = v;
        // Evitar división por cero
        if (maxVentas == 0) maxVentas = 1;

        // --- DIBUJAR BARRAS ---
        for (int i = 0; i < numBarras; i++) {
            String fecha = fechasOrdenadas.get(i);
            int cantidad = ventasPorFecha.get(fecha);

            // Regla de tres para la altura: (cantidad / max) * altura_disponible
            int alturaBarra = (int) (((double) cantidad / maxVentas) * alto);

            int x = padding + (i * anchoBarra) + (espacioEntreBarras / 2);
            int y = (getHeight() - padding) - alturaBarra;

            // Dibujar Barra
            g2.setColor(new Color(100, 149, 237)); // Azul bonito (Cornflower Blue)
            g2.fillRect(x, y, anchoRealBarra, alturaBarra);
            
            // Dibujar Borde
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(x, y, anchoRealBarra, alturaBarra);

            // Dibujar Cantidad (Arriba de la barra)
            g2.setColor(Color.BLACK);
            String labelCant = String.valueOf(cantidad);
            int labelWidth = g2.getFontMetrics().stringWidth(labelCant);
            g2.drawString(labelCant, x + (anchoRealBarra / 2) - (labelWidth / 2), y - 5);

            // Dibujar Fecha (Debajo de la barra - Eje X)
            // Si hay muchas fechas, solo pintamos la corta o rotamos (aquí simplificado)
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            String fechaCorta = fecha.length() > 5 ? fecha.substring(5) : fecha; // MM-DD
            g2.drawString(fechaCorta, x, getHeight() - padding + 15);
        }
        
        // Título del Eje Y
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.rotate(-Math.PI / 2);
        g2.drawString("Ventas", -getHeight() / 2, padding - 30);
        g2.rotate(Math.PI / 2); // Restaurar rotación
    }
}