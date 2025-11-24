package persistencia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import marketplace.Marketplace;
import marketplace.OfertaReventa;
import cliente.Usuario;
import tiquetes.Tiquete;

public class MarketplaceDAO {

    // --- INICIALIZACIÓN ---
    public void crearTablasSiNoExisten() {
        String sqlOfertas = "CREATE TABLE IF NOT EXISTS MARKETPLACE_OFERTAS (" +
                            "id_tiquete TEXT PRIMARY KEY, " +
                            "login_vendedor TEXT, " +
                            "precio REAL)";

        String sqlLogs = "CREATE TABLE IF NOT EXISTS MARKETPLACE_LOGS (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "fecha_hora TEXT, " +
                         "mensaje TEXT)";

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlOfertas);
            stmt.execute(sqlLogs);
        } catch (SQLException e) {
            System.err.println("Error creando tablas Marketplace: " + e.getMessage());
        }
    }

    // --- GESTIÓN DE OFERTAS ---

    public void guardarOferta(OfertaReventa oferta) throws SQLException {
        String sql = "INSERT OR REPLACE INTO MARKETPLACE_OFERTAS (id_tiquete, login_vendedor, precio) VALUES (?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, oferta.getTiquete().getIdTiquete());
            pstmt.setString(2, oferta.getVendedor().getLogIn());
            pstmt.setDouble(3, oferta.getPrecio());
            pstmt.executeUpdate();
        }
    }

    public void eliminarOferta(String idTiquete) throws SQLException {
        String sql = "DELETE FROM MARKETPLACE_OFERTAS WHERE id_tiquete = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, idTiquete);
            pstmt.executeUpdate();
        }
    }

    public void cargarOfertas(Marketplace marketplace, List<Usuario> usuarios, List<Tiquete> tiquetes) {
        String sql = "SELECT * FROM MARKETPLACE_OFERTAS";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String idTiquete = rs.getString("id_tiquete");
                double precio = rs.getDouble("precio");

                // Reconstruir objetos buscando en memoria
                Tiquete tiqueteReal = tiquetes.stream()
                    .filter(t -> t.getIdTiquete().equals(idTiquete)).findFirst().orElse(null);
                
                if (tiqueteReal != null) {
                    // Añadir directamente a la lista para saltar validaciones de "nueva publicación"
                    marketplace.getOfertasActivas().add(new OfertaReventa(tiqueteReal, precio));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cargando ofertas: " + e.getMessage());
        }
    }

    // --- GESTIÓN DE LOGS ---

    public void guardarLog(String registroCompleto) {
        String sql = "INSERT INTO MARKETPLACE_LOGS (fecha_hora, mensaje) VALUES (?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Separamos la fecha del mensaje si es posible, o guardamos todo en mensaje
            // Formato esperado: "[2025-11-24 13:00:00] Acción..."
            String fecha = java.time.LocalDateTime.now().toString();
            
            pstmt.setString(1, fecha);
            pstmt.setString(2, registroCompleto);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error guardando log: " + e.getMessage());
        }
    }

    public List<String> cargarLogs() {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT mensaje FROM MARKETPLACE_LOGS ORDER BY id ASC";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(rs.getString("mensaje"));
            }
        } catch (SQLException e) {
            System.err.println("Error cargando logs: " + e.getMessage());
        }
        return logs;
    }
}