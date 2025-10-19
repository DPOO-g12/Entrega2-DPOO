package persistencia;

import cliente.Usuario;
import eventos.Evento;
import localidades.Localidades;
import tiquetes.Basico;
import tiquetes.Deluxe;
import tiquetes.Multiple;
import tiquetes.Tiquete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DAO para manejar las operaciones CRUD de la jerarquía de Tiquete.
 */
public class TiqueteDAO {

    /**
     * Guarda un nuevo Tiquete (de cualquier tipo) en la base de datos.
     * Utiliza una transacción para guardar en el padre, hijo y tablas de listas.
     *
     * @param tiquete El Tiquete a guardar.
     * @throws SQLException si ocurre un error.
     */
    public void guardarTiquete(Tiquete tiquete) throws SQLException {
        
        // SQL para la tabla PADRE
        String sqlTiquete = "INSERT INTO Tiquete (id_tiquete_java, precio_base, costo_servicio, " +
                            "costo_emision, precio_final, fecha, estado, transferible, " +
                            "login_cliente, id_localidad, id_evento) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmtTiquete = null;
        
        try {
            conn = ConexionSQLite.conectar();
            // --- INICIO DE LA TRANSACCIÓN ---
            conn.setAutoCommit(false);

            // --- 1. Guardar en la tabla PADRE (Tiquete) ---
            pstmtTiquete = conn.prepareStatement(sqlTiquete, Statement.RETURN_GENERATED_KEYS);
            pstmtTiquete.setString(1, tiquete.getIdTiquete()); // Tu ID de Java
            pstmtTiquete.setDouble(2, tiquete.getPrecioBase());
            pstmtTiquete.setDouble(3, tiquete.getCostoServicio());
            pstmtTiquete.setDouble(4, tiquete.getCostoEmision());
            pstmtTiquete.setDouble(5, tiquete.getPrecioFinal());
            pstmtTiquete.setString(6, tiquete.getFecha());
            pstmtTiquete.setString(7, tiquete.getEstado());
            pstmtTiquete.setBoolean(8, tiquete.isTransferible());
            pstmtTiquete.setString(9, tiquete.getCliente().getLogIn());

            // Manejar FKs opcionales (nulas para TiqueteMultiple)
            if (tiquete.getLocalidad() != null) {
                pstmtTiquete.setInt(10, tiquete.getLocalidad().getIdLocalidad());
            } else {
                pstmtTiquete.setNull(10, java.sql.Types.INTEGER);
            }
            if (tiquete.getEvento() != null) {
                pstmtTiquete.setString(11, tiquete.getEvento().getId());
            } else {
                pstmtTiquete.setNull(11, java.sql.Types.VARCHAR);
            }
            
            pstmtTiquete.executeUpdate();

            // --- 2. Obtener el ID de BD generado y guardarlo en el objeto ---
            int idGenerado;
            try (ResultSet generatedKeys = pstmtTiquete.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idGenerado = generatedKeys.getInt(1);
                    tiquete.setIdTiqueteDb(idGenerado); // ¡Actualizamos el objeto Java!
                } else {
                    throw new SQLException("No se pudo obtener el ID para el tiquete.");
                }
            }

            // --- 3. Guardar en la tabla HIJA correspondiente ---
            if (tiquete instanceof Basico) {
                Basico b = (Basico) tiquete;
                String sql = "INSERT INTO Basico (id_tiquete_db, numero_asiento) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idGenerado);
                    pstmt.setString(2, b.getNumeroAsiento());
                    pstmt.executeUpdate();
                }
                
            } else if (tiquete instanceof Deluxe) {
                Deluxe d = (Deluxe) tiquete;
                String sql = "INSERT INTO Deluxe (id_tiquete_db) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idGenerado);
                    pstmt.executeUpdate();
                }
                // Guardar su lista de beneficios
                guardarBeneficios(idGenerado, d.getBeneficiosAdicionales(), conn);
                
            } else if (tiquete instanceof Multiple) {
                Multiple m = (Multiple) tiquete;
                String sql = "INSERT INTO Multiple (id_tiquete_db) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, idGenerado);
                    pstmt.executeUpdate();
                }
                // Guardar sus listas de composición/asociación
                guardarTiquetesIncluidos(idGenerado, m.getTiquetesIncluidos(), conn);
                guardarEventosAsociados(idGenerado, m.getEventosAsociados(), conn);
            }

            // --- FIN DE LA TRANSACCIÓN ---
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // Deshacer todo si algo falla
            throw new SQLException("Error al guardar el tiquete (transacción revertida): " + e.getMessage());
        } finally {
            if (pstmtTiquete != null) pstmtTiquete.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Actualiza el estado de un Tiquete (ej. "TRANSFERIDO", "REEMBOLSADO").
     *
     * @param tiquete El tiquete con el estado actualizado.
     * @throws SQLException si ocurre un error.
     */
    public void actualizarEstadoTiquete(Tiquete tiquete) throws SQLException {
        String sql = "UPDATE Tiquete SET estado = ? WHERE id_tiquete_db = ?";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, tiquete.getEstado());
            pstmt.setInt(2, tiquete.getIdTiqueteDb());
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Actualiza el dueño (cliente) de un Tiquete (para transferencias).
     *
     * @param tiquete El tiquete con el cliente actualizado.
     * @throws SQLException si ocurre un error.
     */
    public void actualizarClienteTiquete(Tiquete tiquete) throws SQLException {
        String sql = "UPDATE Tiquete SET login_cliente = ? WHERE id_tiquete_db = ?";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, tiquete.getCliente().getLogIn());
            pstmt.setInt(2, tiquete.getIdTiqueteDb());
            pstmt.executeUpdate();
        }
    }
    
    // --- Métodos de Ayuda (Helpers para las listas) ---
    
    private void guardarBeneficios(int id_tiquete_db, List<String> beneficios, Connection conn) throws SQLException {
        String sql = "INSERT INTO Deluxe_Beneficios (id_tiquete_db, beneficio_desc) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String beneficio : beneficios) {
                pstmt.setInt(1, id_tiquete_db);
                pstmt.setString(2, beneficio);
                pstmt.addBatch(); // Agrupamos los inserts
            }
            pstmt.executeBatch(); // Los ejecutamos todos
        }
    }

    private void guardarTiquetesIncluidos(int id_tiquete_multiple, List<Tiquete> incluidos, Connection conn) throws SQLException {
        String sql = "INSERT INTO Multiple_TiquetesIncluidos (id_tiquete_multiple, id_tiquete_incluido) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Tiquete tiquete : incluidos) {
                if (tiquete.getIdTiqueteDb() == -1) {
                    throw new SQLException("El tiquete incluido " + tiquete.getIdTiquete() + " no ha sido guardado en la BD.");
                }
                pstmt.setInt(1, id_tiquete_multiple);
                pstmt.setInt(2, tiquete.getIdTiqueteDb());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void guardarEventosAsociados(int id_tiquete_multiple, List<Evento> asociados, Connection conn) throws SQLException {
        String sql = "INSERT INTO Multiple_EventosAsociados (id_tiquete_multiple, id_evento) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Evento evento : asociados) {
                pstmt.setInt(1, id_tiquete_multiple);
                pstmt.setString(2, evento.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    // TODO:
    // public List<Tiquete> cargarTodosLosTiquetes(List<Usuario> todosUsuarios, List<Localidades> todasLocalidades, List<Evento> todosEventos)
    // Este método es GIGANTESCO. Usaría múltiples JOINS para reconstruir
    // todos los tiquetes (Basico, Deluxe, Multiple) y sus listas.
}