package persistencia;

import eventos.Venue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para manejar las operaciones CRUD de la entidad Venue.
 */
public class VenueDAO {

    public Venue guardarVenue(Venue venue) throws SQLException {
        // CORRECCIÓN: Cambiamos 'capacidad_maxima' por 'capacidad' para coincidir con la BD
        String sql = "INSERT INTO Venue (tipo, ubicacion, capacidad, estado) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, venue.getTipo());
            pstmt.setString(2, venue.getUbicacion());
            pstmt.setInt(3, venue.getCapacidadMaxima()); // En Java se llama así, está bien
            pstmt.setString(4, venue.getEstado()); 

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error al guardar el venue, no se insertaron filas.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    venue.setIdVenue(idGenerado);
                    System.out.println("Venue guardado con ID de BD: " + idGenerado);
                } else {
                    throw new SQLException("Error al guardar el venue, no se obtuvo el ID.");
                }
            }
            
            return venue;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                throw new SQLException("Error: Ya existe un venue con la ubicación '" + venue.getUbicacion() + "'.");
            }
            throw e;
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }

    public void actualizarEstadoVenue(Venue venue) throws SQLException {
        String sql = "UPDATE Venue SET estado = ? WHERE ubicacion = ?";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, venue.getEstado());
            pstmt.setString(2, venue.getUbicacion());
            pstmt.executeUpdate();
        }
    }

    public List<Venue> cargarTodosLosVenues() throws SQLException {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT * FROM Venue"; 

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id_venue = rs.getInt("id_venue");
                String tipo = rs.getString("tipo");
                String ubicacion = rs.getString("ubicacion");
                
                // CORRECCIÓN: Leemos la columna 'capacidad'
                int capacidad = rs.getInt("capacidad"); 
                
                String estado = rs.getString("estado");
                
                List<String> restricciones = new ArrayList<>(); 

                Venue venue = new Venue(tipo, ubicacion, capacidad, restricciones, estado);
                venue.setIdVenue(id_venue);
                venues.add(venue);
            }
        }
        return venues;
    }
}

