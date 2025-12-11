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

	public void guardarEvento(Evento evento) throws SQLException {
        // SQL corregido para incluir login_organizador
        String sql = "INSERT INTO Evento (id_evento, nombre, fecha, estado, id_venue, login_organizador) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, evento.getId());
            pstmt.setString(2, evento.getNombre());
            pstmt.setString(3, evento.getFecha());
            pstmt.setString(4, evento.getEstado());
            
            // Validación de Venue
            if (evento.getVenue() != null) {
                pstmt.setInt(5, evento.getVenue().getIdVenue()); 
            } else {
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }

            // Validación de Promotor (Organizador) - ¡ESTO ES LO NUEVO!
            if (evento.getPromotor() != null) {
                pstmt.setString(6, evento.getPromotor().getLogIn());
            } else {
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            }

            pstmt.executeUpdate();
            System.out.println("Evento '" + evento.getNombre() + "' guardado correctamente.");

        } catch (SQLException e) {
             if (e.getMessage().contains("UNIQUE constraint failed")) {
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
                
                int id_venue_fk = rs.getInt("id_venue");
                String login_organizador_fk = rs.getString("login_organizador");

                // --- "Hidratación" ---
                Venue venue = findVenueById(todosVenues, id_venue_fk);
                OrganizadorEventos promotor = findOrganizadorByLogin(todosOrganizadores, login_organizador_fk);

                if (venue == null) throw new SQLException("Datos corruptos: El Venue con ID " + id_venue_fk + " no existe.");
                if (promotor == null) throw new SQLException("Datos corruptos: El Organizador '" + login_organizador_fk + "' no existe.");

                // --- ¡EL ARREGLO! ---
                // Usamos la "fábrica" que no llama a la lógica de negocio
                Evento evento = Evento.cargarDesdeDB(id_evento, nombre, fecha, venue, promotor, estado);
                
                eventos.add(evento);
            }
            
        } catch (SQLException e) {
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