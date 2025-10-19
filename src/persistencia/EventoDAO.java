package persistencia;

import cliente.OrganizadorEventos;
import eventos.Evento;
import eventos.Venue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para manejar las operaciones CRUD de la entidad Evento.
 */
public class EventoDAO {

    /**
     * Guarda un nuevo Evento en la base de datos.
     *
     * @param evento El objeto Evento a guardar.
     * @throws SQLException si ocurre un error de SQL.
     */
    public void guardarEvento(Evento evento) throws SQLException {
        // El SQL para insertar en la tabla Evento
        String sql = "INSERT INTO Evento (id_evento, nombre, fecha, estado, id_venue, login_organizador) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Asignamos los valores a los '?'
            pstmt.setString(1, evento.getId());
            pstmt.setString(2, evento.getNombre());
            pstmt.setString(3, evento.getFecha());
            pstmt.setString(4, evento.getEstado());
            
            // Gracias al refactor, ahora podemos obtener el ID del Venue
            pstmt.setInt(5, evento.getVenue().getIdVenue()); 
            // Y el login del promotor
            pstmt.setString(6, evento.getPromotor().getLogIn());

            pstmt.executeUpdate();

        } catch (SQLException e) {
             if (e.getMessage().contains("UNIQUE constraint failed: Evento.id_evento")) {
                throw new SQLException("Error: Ya existe un evento con el ID '" + evento.getId() + "'.");
            }
            throw e;
        }
    }

    /**
     * Actualiza el estado de un Evento existente (ej. "CANCELADO").
     *
     * @param evento El objeto Evento que contiene el estado actualizado.
     * @throws SQLException si ocurre un error de SQL.
     */
    public void actualizarEstadoEvento(Evento evento) throws SQLException {
        String sql = "UPDATE Evento SET estado = ? WHERE id_evento = ?";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, evento.getEstado());
            pstmt.setString(2, evento.getId()); // Usamos el ID de negocio

            pstmt.executeUpdate();
        }
    }

    /**
     * Carga (lee) todos los Eventos de la base de datos.
     *
     * @param todosVenues Una lista de todos los Venues ya cargados (para "hidratar").
     * @param todosOrganizadores Una lista de todos los Organizadores ya cargados.
     * @return Una Lista de objetos Evento.
     * @throws SQLException si ocurre un error de SQL.
     */
    public List<Evento> cargarTodosLosEventos(
        List<Venue> todosVenues, 
        List<OrganizadorEventos> todosOrganizadores
    ) throws SQLException {
        
        List<Evento> eventos = new ArrayList<>();
        String sql = "SELECT * FROM Evento"; 

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Leemos los datos de la fila
                String id_evento = rs.getString("id_evento");
                String nombre = rs.getString("nombre");
                String fecha = rs.getString("fecha");
                String estado = rs.getString("estado");
                
                // Leemos las Claves Foráneas (los IDs)
                int id_venue_fk = rs.getInt("id_venue");
                String login_organizador_fk = rs.getString("login_organizador");

                // --- "Hidratación" ---
                // Buscamos los objetos completos en las listas que nos pasaron
                Venue venue = findVenueById(todosVenues, id_venue_fk);
                OrganizadorEventos promotor = findOrganizadorByLogin(todosOrganizadores, login_organizador_fk);

                // Validamos que los datos sean íntegros
                if (venue == null) throw new SQLException("Datos corruptos: El Venue con ID " + id_venue_fk + " no existe.");
                if (promotor == null) throw new SQLException("Datos corruptos: El Organizador '" + login_organizador_fk + "' no existe.");

                // Creamos el objeto Evento
                Evento evento = new Evento(id_evento, nombre, fecha, venue, promotor);
                evento.setEstado(estado); // Aplicamos el estado guardado
                
                eventos.add(evento);
            }
        } catch (Exception e) {
            // Capturamos la excepción del constructor de Evento (VenueOcupado, etc.)
            // Aunque al Cargar, no debería pasar.
            throw new SQLException("Error al cargar evento: " + e.getMessage());
        }
        
        return eventos;
    }
    
    // --- Métodos de Ayuda (Helpers) ---

    private Venue findVenueById(List<Venue> venues, int id) {
        for (Venue v : venues) {
            if (v.getIdVenue() == id) {
                return v;
            }
        }
        return null; // No encontrado
    }

    private OrganizadorEventos findOrganizadorByLogin(List<OrganizadorEventos> organizadores, String login) {
        for (OrganizadorEventos o : organizadores) {
            if (o.getLogIn().equals(login)) {
                return o;
            }
        }
        return null; // No encontrado
    }
}