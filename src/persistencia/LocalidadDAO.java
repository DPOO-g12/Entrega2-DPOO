package persistencia;

import eventos.Evento;
import eventos.Oferta;
import localidades.Localidades;
import localidades.NoNumerada;
import localidades.Numerada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para manejar las operaciones CRUD de la jerarquía de Localidades.
 * Maneja las tablas Localidades, Numerada, NoNumerada, Asiento y ConfigDeluxe.
 */
public class LocalidadDAO {

    public void guardarLocalidad(Localidades loc) throws SQLException {
        
        // SQL para la tabla PADRE
        String sqlLocalidad = "INSERT INTO Localidades (nombre, precio, capacidad_max, id_evento, id_oferta) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmtLoc = null;
        
        try {
            conn = ConexionSQLite.conectar();
            // --- INICIO DE LA TRANSACCIÓN ---
            conn.setAutoCommit(false);

            // --- 1. Guardar en la tabla PADRE (Localidades) ---
            pstmtLoc = conn.prepareStatement(sqlLocalidad, Statement.RETURN_GENERATED_KEYS);
            pstmtLoc.setString(1, loc.getNombreLocalidad());
            pstmtLoc.setDouble(2, loc.getPrecio());
            pstmtLoc.setInt(3, loc.getCapacidadMax());
            pstmtLoc.setString(4, loc.getEvento().getId());
            
            // Manejar la FK de oferta
            if (loc.getOferta() != null) {
                // Si tuvieras el ID de oferta en el objeto, lo pondrías aquí. 
                // Por ahora null como tenías.
                pstmtLoc.setNull(5, java.sql.Types.INTEGER); 
            } else {
                pstmtLoc.setNull(5, java.sql.Types.INTEGER);
            }
            
            pstmtLoc.executeUpdate();

            // --- 2. Obtener el ID generado ---
            int idGenerado;
            try (ResultSet generatedKeys = pstmtLoc.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idGenerado = generatedKeys.getInt(1);
                    loc.setIdLocalidad(idGenerado); 
                } else {
                    throw new SQLException("No se pudo obtener el ID para la localidad.");
                }
            }

            // --- 3. Guardar en la tabla HIJA correspondiente ---
            if (loc instanceof NoNumerada) {
                NoNumerada nn = (NoNumerada) loc;
                String sqlNN = "INSERT INTO NoNumerada (id_localidad, tiquetes_vendidos) VALUES (?, ?)";
                try (PreparedStatement pstmtNN = conn.prepareStatement(sqlNN)) {
                    pstmtNN.setInt(1, idGenerado);
                    pstmtNN.setInt(2, nn.getTiquetesVendidos());
                    pstmtNN.executeUpdate();
                }
                
            } else if (loc instanceof Numerada) {
                Numerada n = (Numerada) loc;
                String sqlN = "INSERT INTO Numerada (id_localidad) VALUES (?)";
                try (PreparedStatement pstmtN = conn.prepareStatement(sqlN)) {
                    pstmtN.setInt(1, idGenerado);
                    pstmtN.executeUpdate();
                }
                
                // Guardar Asientos
                guardarAsientos(idGenerado, n.getAsientos(), conn);
            }

            // --- FIN DE LA TRANSACCIÓN ---
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback(); 
            throw new SQLException("Error al guardar la localidad: " + e.getMessage());
        } finally {
            if (pstmtLoc != null) pstmtLoc.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Localidades> cargarLocalidadesParaEvento(Evento evento, List<Oferta> todasLasOfertas) throws SQLException {
        List<Localidades> localidades = new ArrayList<>();
        
        String sql = "SELECT l.*, " +
                     "       nn.tiquetes_vendidos, " +
                     "       n.id_localidad AS is_numerada " +
                     "FROM Localidades l " +
                     "LEFT JOIN NoNumerada nn ON l.id_localidad = nn.id_localidad " +
                     "LEFT JOIN Numerada n ON l.id_localidad = n.id_localidad " +
                     "WHERE l.id_evento = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexionSQLite.conectar();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, evento.getId());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id_localidad = rs.getInt("id_localidad");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                int capacidad = rs.getInt("capacidad_max");
                int id_oferta_fk = rs.getInt("id_oferta");
                
                Oferta oferta = null; // (Lógica de oferta pendiente si se requiere)

                Localidades loc;
                
                if (rs.getObject("is_numerada") != null) {
                    Map<String, Boolean> asientos = cargarAsientos(id_localidad, conn);
                    loc = new Numerada(precio, capacidad, nombre, evento, asientos);
                    
                } else {
                    NoNumerada nn = new NoNumerada(precio, capacidad, nombre, evento);
                    int tiquetesVendidos = rs.getInt("tiquetes_vendidos");
                    nn.setTiquetesVendidos(tiquetesVendidos); 
                    loc = nn;
                }

                loc.setIdLocalidad(id_localidad);
                loc.setOferta(oferta);
                
                // --- NUEVO: CARGAR CONFIGURACIÓN DELUXE ---
                // Le pasamos la conexión abierta para aprovecharla
                cargarConfiguracionDeluxe(loc, conn); 
                
                localidades.add(loc);
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
        return localidades;
    }

    // --- MÉTODOS EXISTENTES DE ASIENTOS Y VENTAS ---

    private void guardarAsientos(int id_localidad, Map<String, Boolean> asientos, Connection conn) throws SQLException {
        String sql = "INSERT INTO Asiento (id_localidad, identificador_asiento, ocupado) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Boolean> entry : asientos.entrySet()) {
                pstmt.setInt(1, id_localidad);
                pstmt.setString(2, entry.getKey());
                pstmt.setBoolean(3, entry.getValue());
                pstmt.executeUpdate();
            }
        }
    }

    private Map<String, Boolean> cargarAsientos(int id_localidad, Connection conn) throws SQLException {
        Map<String, Boolean> asientos = new HashMap<>();
        String sql = "SELECT identificador_asiento, ocupado FROM Asiento WHERE id_localidad = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_localidad);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    asientos.put(rs.getString("identificador_asiento"), rs.getBoolean("ocupado"));
                }
            }
        }
        return asientos;
    }

    public void actualizarVentaNoNumerada(NoNumerada loc) throws SQLException {
        String sql = "UPDATE NoNumerada SET tiquetes_vendidos = ? WHERE id_localidad = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loc.getTiquetesVendidos());
            pstmt.setInt(2, loc.getIdLocalidad());
            pstmt.executeUpdate();
        }
    }

    public void actualizarAsientoOcupado(int id_localidad, String identificadorAsiento) throws SQLException {
        String sql = "UPDATE Asiento SET ocupado = 1 WHERE id_localidad = ? AND identificador_asiento = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id_localidad);
            pstmt.setString(2, identificadorAsiento);
            pstmt.executeUpdate();
        }
    }
    
    // ========================================================================
    //   NUEVOS MÉTODOS PARA CONFIGURACIÓN DELUXE (PASO 3 INTEGRADO)
    // ========================================================================

    /**
     * Guarda la configuración Deluxe en la tabla ConfigDeluxe.
     */
    public void habilitarDeluxe(Localidades loc) throws SQLException {
        String sql = "INSERT INTO ConfigDeluxe (id_localidad, cantidad_limite, precio_extra, beneficios, vendidos) VALUES (?, ?, ?, ?, 0)";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, loc.getIdLocalidad());
            pstmt.setInt(2, loc.getDeluxeLimite());
            pstmt.setDouble(3, loc.getDeluxePrecioExtra());
            pstmt.setString(4, loc.getDeluxeBeneficios());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Actualiza cuántos tiquetes Deluxe se han vendido (para controlar cupo).
     */
    public void actualizarVendidosDeluxe(Localidades loc) throws SQLException {
        String sql = "UPDATE ConfigDeluxe SET vendidos = ? WHERE id_localidad = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loc.getDeluxeVendidos());
            pstmt.setInt(2, loc.getIdLocalidad());
            pstmt.executeUpdate();
        }
    }

    /**
     * Carga la configuración Deluxe si existe y la "inyecta" en el objeto Localidad.
     * Se usa internamente al cargar localidades.
     */
    private void cargarConfiguracionDeluxe(Localidades loc, Connection conn) {
        String sql = "SELECT * FROM ConfigDeluxe WHERE id_localidad = ?";
        
        // Usamos try-with-resources solo para el statement, la conexión viene de afuera
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loc.getIdLocalidad());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // ¡Encontrado! Esta localidad tiene configuración Deluxe
                    loc.setDeluxeHabilitado(true);
                    loc.setDeluxeLimite(rs.getInt("cantidad_limite"));
                    loc.setDeluxePrecioExtra(rs.getDouble("precio_extra"));
                    loc.setDeluxeBeneficios(rs.getString("beneficios"));
                    loc.setDeluxeVendidos(rs.getInt("vendidos"));
                } else {
                    // No es deluxe
                    loc.setDeluxeHabilitado(false);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cargando config deluxe para loc " + loc.getIdLocalidad() + ": " + e.getMessage());
        }
    }
}