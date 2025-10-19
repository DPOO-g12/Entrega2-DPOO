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

    /**
     * Guarda un nuevo Venue en la base de datos.
     * Retorna el objeto Venue con el ID autogenerado por la BD.
     *
     * @param venue El objeto Venue a guardar (sin ID).
     * @return El mismo objeto Venue, actualizado con el ID de la BD.
     * @throws SQLException si ocurre un error de SQL.
     */
    public Venue guardarVenue(Venue venue) throws SQLException {
        // El SQL para insertar en la tabla Venue.
        // No pasamos 'id_venue' porque es AUTOINCREMENT.
        String sql = "INSERT INTO Venue (tipo, ubicacion, capacidad_maxima, estado) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionSQLite.conectar();
            
            // Pedimos que nos devuelva las claves generadas (el id_venue)
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Asignamos los valores a los '?'
            pstmt.setString(1, venue.getTipo());
            pstmt.setString(2, venue.getUbicacion());
            pstmt.setInt(3, venue.getCapacidadMaxima());
            pstmt.setString(4, venue.getEstado()); // "PENDIENTE" o "APROBADO"

            int affectedRows = pstmt.executeUpdate();

            // Verificamos que se haya insertado
            if (affectedRows == 0) {
                throw new SQLException("Error al guardar el venue, no se insertaron filas.");
            }

            // Recuperamos el ID autogenerado
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                	int idGenerado = generatedKeys.getInt(1);
                	venue.setIdVenue(idGenerado);
                    // Aquí podríamos actualizar el objeto 'venue' con el ID de la BD si fuera necesario,
                    // pero nuestro modelo de Java no usa el ID numérico, así que solo confirmamos.
                    System.out.println("Venue guardado con ID de BD: " + generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al guardar el venue, no se obtuvo el ID.");
                }
            }
            
            return venue; // Devolvemos el objeto original

        } catch (SQLException e) {
            // Manejamos la violación de la restricción UNIQUE (ubicacion)
            if (e.getMessage().contains("UNIQUE constraint failed: Venue.ubicacion")) {
                throw new SQLException("Error: Ya existe un venue con la ubicación '" + venue.getUbicacion() + "'.");
            }
            throw e; // Relanzamos otros errores
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Actualiza el estado de un Venue existente (ej. "APROBADO").
     *
     * @param venue El objeto Venue que contiene el estado actualizado.
     * @throws SQLException si ocurre un error de SQL.
     */
    public void actualizarEstadoVenue(Venue venue) throws SQLException {
        String sql = "UPDATE Venue SET estado = ? WHERE ubicacion = ?";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, venue.getEstado());
            pstmt.setString(2, venue.getUbicacion()); // Usamos la ubicación como identificador

            pstmt.executeUpdate();
        }
    }

    /**
     * Carga (lee) todos los Venues de la base de datos.
     *
     * @return Una Lista de objetos Venue.
     * @throws SQLException si ocurre un error de SQL.
     */
    public List<Venue> cargarTodosLosVenues() throws SQLException {
        List<Venue> venues = new ArrayList<>();
        // El * (asterisco) significa "todas las columnas"
        String sql = "SELECT * FROM Venue"; 

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Iteramos sobre cada fila que devolvió la consulta
            while (rs.next()) {
                // Leemos los datos de cada columna
            	int id_venue = rs.getInt("id_venue");
                String tipo = rs.getString("tipo");
                String ubicacion = rs.getString("ubicacion");
                int capacidad = rs.getInt("capacidad_maxima");
                String estado = rs.getString("estado");
                
                // (Omitimos las restricciones por simplicidad, como en el SQL)
                List<String> restricciones = new ArrayList<>(); 

                // Creamos el objeto Java con los datos de la fila
                Venue venue = new Venue(tipo, ubicacion, capacidad, restricciones, estado);
                venue.setIdVenue(id_venue);
                venues.add(venue);
            }
        }
        return venues;
    }
}
