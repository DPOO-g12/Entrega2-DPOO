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


    public void guardarTiquete(Tiquete tiquete) throws SQLException {
        
        // SQL para la tabla PADRE
        String sqlTiquete = "INSERT INTO Tiquete (id_tiquete_java, precio_base, costo_servicio, " +
                            "costo_emision, precio_final, fecha, estado, transferible, impreso" +
                            "login_cliente, id_localidad, id_evento) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
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
            pstmtTiquete.setBoolean(9, tiquete.isImpreso());
            pstmtTiquete.setString(10, tiquete.getCliente().getLogIn());

            // Manejar FKs opcionales (nulas para TiqueteMultiple)
            if (tiquete.getLocalidad() != null) {
                pstmtTiquete.setInt(11, tiquete.getLocalidad().getIdLocalidad());
            } else {
                pstmtTiquete.setNull(11, java.sql.Types.INTEGER);
            }
            if (tiquete.getEvento() != null) {
                pstmtTiquete.setString(12, tiquete.getEvento().getId());
            } else {
                pstmtTiquete.setNull(12, java.sql.Types.VARCHAR);
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
    

    public void actualizarEstadoTiquete(Tiquete tiquete) throws SQLException {
        String sql = "UPDATE Tiquete SET estado = ? WHERE id_tiquete_db = ?";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, tiquete.getEstado());
            pstmt.setInt(2, tiquete.getIdTiqueteDb());
            pstmt.executeUpdate();
        }
    }
    

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

    public List<Tiquete> cargarTodosLosTiquetes(
            Map<String, Usuario> mapaUsuarios,
            Map<Integer, Localidades> mapaLocalidades,
            Map<String, Evento> mapaEventos) throws SQLException {
        
        // Fase 1: Cargar todos los tiquetes y ponerlos en un mapa
        Map<Integer, Tiquete> mapaTiquetes = new HashMap<>(); // ID_DB -> Tiquete
        
        String sql = "SELECT t.*, " +
                     "       b.numero_asiento, " +
                     "       d.id_tiquete_db AS is_deluxe, " +
                     "       m.id_tiquete_db AS is_multiple " +
                     "FROM Tiquete t " +
                     "LEFT JOIN Basico b ON t.id_tiquete_db = b.id_tiquete_db " +
                     "LEFT JOIN Deluxe d ON t.id_tiquete_db = d.id_tiquete_db " +
                     "LEFT JOIN Multiple m ON t.id_tiquete_db = m.id_tiquete_db";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexionSQLite.conectar();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                // 1. Leer datos de la tabla Tiquete (Padre)
                int id_db = rs.getInt("id_tiquete_db");
                String id_java = rs.getString("id_tiquete_java");
                double pBase = rs.getDouble("precio_base");
                double pServicio = rs.getDouble("costo_servicio");
                double pEmision = rs.getDouble("costo_emision");
                double pFinal = rs.getDouble("precio_final");
                String fecha = rs.getString("fecha");
                String estado = rs.getString("estado");
                boolean transferible = rs.getBoolean("transferible");
                boolean impreso = rs.getBoolean("impreso");
                
                // 2. Hidratar los objetos FK
                Usuario cliente = mapaUsuarios.get(rs.getString("login_cliente"));
                Localidades loc = mapaLocalidades.get(rs.getInt("id_localidad"));
                Evento evt = mapaEventos.get(rs.getString("id_evento"));
                
                Tiquete tiquete = null;

                // 3. Decidir qué tipo de tiquete crear
                if (rs.getObject("is_multiple") != null) {
                    tiquete = Multiple.cargarDesdeDB(id_db, id_java, pBase, pServicio, pEmision, pFinal, fecha, estado, transferible, cliente, new ArrayList<>(), new ArrayList<>());
                } else if (rs.getObject("is_deluxe") != null) {
                    tiquete = Deluxe.cargarDesdeDB(id_db, id_java, pBase, pServicio, pEmision, pFinal, fecha, estado, transferible, cliente, loc, evt, new ArrayList<>());
                } else {
                    String numAsiento = rs.getString("numero_asiento");
                    tiquete = Basico.cargarDesdeDB(id_db, id_java, pBase, pServicio, pEmision, pFinal, fecha, estado, transferible, cliente, loc, evt, numAsiento);
                }
                
                if (tiquete != null) {
                    tiquete.setImpreso(impreso); // <--- ASIGNAR AL OBJETO
                    mapaTiquetes.put(id_db, tiquete);
                }
                
                if (tiquete != null) {
                    mapaTiquetes.put(id_db, tiquete);
                }
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            // Dejamos la conexión abierta para la Fase 2
        }

        // --- Fase 2: Hidratar las Listas ---
        try {
            hidratarBeneficios(conn, mapaTiquetes);
            hidratarTiquetesIncluidos(conn, mapaTiquetes);
            hidratarEventosAsociados(conn, mapaTiquetes, mapaEventos);
        } finally {
            if (conn != null) conn.close();
        }
        
        // Devolvemos los valores del mapa como una lista
        return new ArrayList<>(mapaTiquetes.values());
    }

    // --- Helpers de Hidratación ---
    
    private void hidratarBeneficios(Connection conn, Map<Integer, Tiquete> mapaTiquetes) throws SQLException {
        String sql = "SELECT * FROM Deluxe_Beneficios";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id_tiquete_db = rs.getInt("id_tiquete_db");
                String beneficio = rs.getString("beneficio_desc");
                
                Deluxe tiqueteDeluxe = (Deluxe) mapaTiquetes.get(id_tiquete_db);
                if (tiqueteDeluxe != null) {
                    tiqueteDeluxe.getBeneficiosAdicionales().add(beneficio);
                }
            }
        }
    }

    private void hidratarTiquetesIncluidos(Connection conn, Map<Integer, Tiquete> mapaTiquetes) throws SQLException {
        String sql = "SELECT * FROM Multiple_TiquetesIncluidos";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id_multiple = rs.getInt("id_tiquete_multiple");
                int id_incluido = rs.getInt("id_tiquete_incluido");
                
                Multiple tiqueteMultiple = (Multiple) mapaTiquetes.get(id_multiple);
                Tiquete tiqueteIncluido = mapaTiquetes.get(id_incluido);
                
                if (tiqueteMultiple != null && tiqueteIncluido != null) {
                    tiqueteMultiple.getTiquetesIncluidos().add(tiqueteIncluido);
                }
            }
        }
    }

    private void hidratarEventosAsociados(Connection conn, Map<Integer, Tiquete> mapaTiquetes, Map<String, Evento> mapaEventos) throws SQLException {
        String sql = "SELECT * FROM Multiple_EventosAsociados";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id_multiple = rs.getInt("id_tiquete_multiple");
                String id_evento = rs.getString("id_evento");
                
                Multiple tiqueteMultiple = (Multiple) mapaTiquetes.get(id_multiple);
                Evento evento = mapaEventos.get(id_evento);
                
                if (tiqueteMultiple != null && evento != null) {
                    tiqueteMultiple.getEventosAsociados().add(evento);
                }
            }
        }
    }
    
    public void actualizarImpresoTiquete (Tiquete tiquete) throws SQLException {
    	
    	String sql = "UPDATE Tiquete SET impreso = ? WHERE id_tiquete_db = ?";
    	
    	try (Connection conn = ConexionSQLite.conectar();
    		PreparedStatement pstmt = conn.prepareStatement(sql)) {
    		
    		pstmt.setBoolean(1, tiquete.isImpreso());
    		pstmt.setInt(2, tiquete.getIdTiqueteDb());
    		
    		pstmt.executeUpdate();
    	}
    	
    	
    }
 
    
    
}