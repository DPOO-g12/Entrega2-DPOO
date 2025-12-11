package persistencia;

import eventos.Oferta;
import localidades.Localidades;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OfertaDAO {

    // 1. Guardar la Oferta en su tabla
    public int guardarOferta(Oferta oferta) throws SQLException {
        String sql = "INSERT INTO Oferta (descuento, fecha_fin, login_organizador) VALUES (?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, oferta.getDescuento());
            // Guardamos la fecha como Texto
            pstmt.setString(2, oferta.getFechaFinDescuento().toString()); 
            pstmt.setString(3, oferta.getPromotor().getLogIn());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                }
            }
        }
        return idGenerado;
    }

    // 2. Conectar la Oferta con la Localidad (Actualizar Foreign Key)
    public void asociarOfertaALocalidad(int idLocalidad, int idOferta) throws SQLException {
        String sql = "UPDATE Localidades SET id_oferta = ? WHERE id_localidad = ?";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idOferta);
            pstmt.setInt(2, idLocalidad);
            pstmt.executeUpdate();
        }
    }
}