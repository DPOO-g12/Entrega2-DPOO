package persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // <--- Importante para que funcionen los mapas

import tiquetes.*; 
import cliente.Usuario;
import cliente.UsuarioComprador; 
import eventos.Evento;
import localidades.Localidades;

public class TiqueteDAO {

    // ========================================================================
    //                          GUARDAR (INSERT)
    // ========================================================================
    
    public void guardarTiquete(Tiquete tiquete) throws SQLException {
        // SQL con la coma corregida
        String sql = "INSERT INTO Tiquete (id_tiquete_java, precio_base, costo_servicio, costo_emision, precio_final, fecha, estado, transferible, impreso, login_cliente, id_localidad, id_evento) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConexionSQLite.conectar();
            conn.setAutoCommit(false); 
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, tiquete.getIdTiquete());
            pstmt.setDouble(2, tiquete.getPrecioBase());
            pstmt.setDouble(3, tiquete.getCostoServicio());
            pstmt.setDouble(4, tiquete.getCostoEmision());
            pstmt.setDouble(5, tiquete.getPrecioFinal());
            pstmt.setString(6, tiquete.getFecha());
            pstmt.setString(7, tiquete.getEstado());
            pstmt.setBoolean(8, tiquete.isTransferible());
            pstmt.setBoolean(9, tiquete.isImpreso());
            pstmt.setString(10, tiquete.getCliente().getLogIn());
            
            if (tiquete.getLocalidad() != null) pstmt.setInt(11, tiquete.getLocalidad().getIdLocalidad());
            else pstmt.setNull(11, java.sql.Types.INTEGER);
            
            if (tiquete.getEvento() != null) pstmt.setString(12, tiquete.getEvento().getId());
            else pstmt.setNull(12, java.sql.Types.VARCHAR);
            
            pstmt.executeUpdate();
            
            int idGenerado = -1;
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                    tiquete.setIdTiqueteDb(idGenerado);
                }
            }
            
            guardarTipoEspecifico(conn, tiquete, idGenerado);
            
            conn.commit(); 
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    private void guardarTipoEspecifico(Connection conn, Tiquete t, int idDb) throws SQLException {
        String sqlHijo = null;
        if (t instanceof Basico) {
            sqlHijo = "INSERT INTO Basico (id_tiquete_db, numero_asiento) VALUES (?, ?)";
            try (PreparedStatement pstH = conn.prepareStatement(sqlHijo)) {
                pstH.setInt(1, idDb);
                pstH.setString(2, ((Basico)t).getNumeroAsiento()); 
                pstH.executeUpdate();
            }
        } 
        else if (t instanceof Deluxe) {
            sqlHijo = "INSERT INTO Deluxe (id_tiquete_db) VALUES (?)";
            try (PreparedStatement pstH = conn.prepareStatement(sqlHijo)) {
                pstH.setInt(1, idDb);
                pstH.executeUpdate();
            }
        } 
        else if (t instanceof Multiple) {
            sqlHijo = "INSERT INTO Multiple (id_tiquete_db) VALUES (?)";
            try (PreparedStatement pstH = conn.prepareStatement(sqlHijo)) {
                pstH.setInt(1, idDb);
                pstH.executeUpdate();
            }
        }
    }

    // ========================================================================
    //                        ACTUALIZACIONES
    // ========================================================================

    public void actualizarImpresoTiquete(Tiquete tiquete) throws SQLException {
        String sql = "UPDATE Tiquete SET impreso = ? WHERE id_tiquete_db = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, tiquete.isImpreso());
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
    
    public void actualizarEstadoTiquete(Tiquete tiquete) throws SQLException {
        String sql = "UPDATE Tiquete SET estado = ? WHERE id_tiquete_db = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tiquete.getEstado());
            pstmt.setInt(2, tiquete.getIdTiqueteDb());
            pstmt.executeUpdate();
        }
    }

    // ========================================================================
    //                          CARGAR (SELECT) - CORREGIDO
    // ========================================================================

    /**
     * Carga todos los tiquetes usando los MAPAS que ya tienes preparados en TiqueteraApp.
     */
    public List<Tiquete> cargarTodosLosTiquetes(
            Map<String, Usuario> mapUsuarios, 
            Map<Integer, Localidades> mapLocalidades, 
            Map<String, Evento> mapEventos) {
        
        List<Tiquete> tiquetes = new ArrayList<>();
        
        String sql = "SELECT t.*, " +
                     "b.id_tiquete_db AS es_basico, b.numero_asiento, " + 
                     "d.id_tiquete_db AS es_deluxe, " +
                     "m.id_tiquete_db AS es_multiple " +
                     "FROM Tiquete t " +
                     "LEFT JOIN Basico b ON t.id_tiquete_db = b.id_tiquete_db " +
                     "LEFT JOIN Deluxe d ON t.id_tiquete_db = d.id_tiquete_db " +
                     "LEFT JOIN Multiple m ON t.id_tiquete_db = m.id_tiquete_db";

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // 1. Datos crudos
                int idDb = rs.getInt("id_tiquete_db");
                String idJava = rs.getString("id_tiquete_java");
                double precioBase = rs.getDouble("precio_base");
                double costoServ = rs.getDouble("costo_servicio");
                double costoEmi = rs.getDouble("costo_emision");
                double precioFinal = rs.getDouble("precio_final");
                String fecha = rs.getString("fecha");
                String estado = rs.getString("estado");
                boolean transf = rs.getBoolean("transferible");
                boolean impreso = rs.getBoolean("impreso");
                
                String loginCliente = rs.getString("login_cliente");
                int idLoc = rs.getInt("id_localidad");
                String idEvento = rs.getString("id_evento");

                // 2. Buscar objetos directos en los MAPAS (¡Más rápido!)
                Usuario cliente = mapUsuarios.get(loginCliente);
                Evento evento = mapEventos.get(idEvento);
                Localidades localidad = mapLocalidades.get(idLoc);

                Tiquete t = null;

                // 3. Reconstruir según el tipo (Usando tus métodos estáticos)
                
                if (rs.getString("es_basico") != null) {
                    String asiento = rs.getString("numero_asiento");
                    if (asiento == null) asiento = "General";
                    
                    t = Basico.cargarDesdeDB(idDb, idJava, precioBase, costoServ, costoEmi, 
                                             precioFinal, fecha, estado, transf, 
                                             cliente, localidad, evento, asiento);
                } 
                else if (rs.getString("es_deluxe") != null) {
                    List<String> beneficios = new ArrayList<>(); 
                    t = Deluxe.cargarDesdeDB(idDb, idJava, precioBase, costoServ, costoEmi, 
                                             precioFinal, fecha, estado, transf, 
                                             cliente, localidad, evento, beneficios);
                }
                else if (rs.getString("es_multiple") != null) {
                    List<Tiquete> tiquetesInc = new ArrayList<>();
                    List<Evento> eventosAso = new ArrayList<>();
                    t = Multiple.cargarDesdeDB(idDb, idJava, precioBase, costoServ, costoEmi, 
                                               precioFinal, fecha, estado, transf, 
                                               cliente, tiquetesInc, eventosAso);
                }

                // 4. Ajustes Finales
                if (t != null) {
                    t.setImpreso(impreso); 
                    
                    if (cliente instanceof UsuarioComprador) {
                        ((UsuarioComprador) cliente).agregarTiquete(t);
                    }
                    tiquetes.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error cargando tiquetes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tiquetes;
    }
}