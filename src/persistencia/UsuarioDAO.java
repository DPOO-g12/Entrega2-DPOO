package persistencia;

import cliente.Administrador;
import cliente.OrganizadorEventos;
import cliente.Usuario;
import cliente.UsuarioComprador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * DAO para manejar todas las operaciones de la base de datos
 * relacionadas con la jerarquía de Usuarios.
 */
public class UsuarioDAO {

    /**
     * Guarda un nuevo usuario (cualquier tipo) en la base de datos.
     * Utiliza una transacción para asegurar que se guarde en la tabla padre
     * Y en la tabla hija correspondiente.
     *
     * @param usuario El objeto (Administrador, Organizador, etc.) a guardar.
     * @throws SQLException si ocurre un error de SQL.
     */
    public void guardarUsuario(Usuario usuario) throws SQLException {
        
        // 1. Sentencia SQL para la tabla PADRE (Usuario)
        // Los '?' son "placeholders" (marcadores) que llenaremos después.
        String sqlUsuario = "INSERT INTO Usuario (login, contrasena, saldo) VALUES (?, ?, ?)";
        
        String sqlHijo = null; // SQL para la tabla hija
        
        // 2. Determinar qué TIPO de usuario es
        if (usuario instanceof Administrador) {
            // El 'login' se inserta en ambas tablas
            sqlHijo = "INSERT INTO Administrador (login, cobroPorEmision) VALUES (?, ?)";
        } else if (usuario instanceof OrganizadorEventos) {
            sqlHijo = "INSERT INTO OrganizadorEventos (login) VALUES (?)";
        } else if (usuario instanceof UsuarioComprador) {
            sqlHijo = "INSERT INTO UsuarioComprador (login) VALUES (?)";
        }

        Connection conn = null;
        PreparedStatement pstmtUsuario = null;
        PreparedStatement pstmtHijo = null;

        try {
            // 3. Obtener una conexión de nuestra clase de utilidad
            conn = ConexionSQLite.conectar();
            
            // --- INICIO DE LA TRANSACCIÓN ---
            // Le decimos a la BD: "No guardes nada permanentemente
            // hasta que yo te diga 'commit'".
            // Esto es VITAL. Si falla la inserción en la tabla hija,
            // podemos deshacer la inserción en la tabla padre.
            conn.setAutoCommit(false);

            // 4. Preparar y ejecutar la inserción en la tabla PADRE (Usuario)
            pstmtUsuario = conn.prepareStatement(sqlUsuario);
            pstmtUsuario.setString(1, usuario.getLogIn());
            pstmtUsuario.setString(2, usuario.getContrasena());
            pstmtUsuario.setDouble(3, usuario.getSaldo());
            pstmtUsuario.executeUpdate(); // ¡Ejecutar!

            // 5. Preparar y ejecutar la inserción en la tabla HIJA
            if (sqlHijo != null) {
                pstmtHijo = conn.prepareStatement(sqlHijo);
                
                // Llenamos los '?' de la consulta hija
                if (usuario instanceof Administrador) {
                    Administrador admin = (Administrador) usuario;
                    pstmtHijo.setString(1, admin.getLogIn());
                    pstmtHijo.setDouble(2, admin.getCobroPorEmision());
                    
                } else if (usuario instanceof OrganizadorEventos) {
                    pstmtHijo.setString(1, usuario.getLogIn());
                    
                } else if (usuario instanceof UsuarioComprador) {
                    pstmtHijo.setString(1, usuario.getLogIn());
                }
                
                pstmtHijo.executeUpdate(); // ¡Ejecutar!
            }

            // 6. ¡ÉXITO! Si llegamos aquí, ambas inserciones funcionaron.
            // Hacemos los cambios permanentes.
            conn.commit(); 
            
        } catch (SQLException e) {
            // 7. ¡ERROR! Si algo falló, deshacemos todo lo de esta transacción
            if (conn != null) {
                conn.rollback();
            }
            // Relanzamos la excepción para que la lógica de negocio se entere
            throw new SQLException("Error al guardar el usuario (transacción revertida): " + e.getMessage());
            
        } finally {
            // 8. Cerrar todo (pase lo que pase)
            if (pstmtUsuario != null) pstmtUsuario.close();
            if (pstmtHijo != null) pstmtHijo.close();
            if (conn != null) {
                conn.setAutoCommit(true); // Devolver al modo normal
                conn.close();
            }
        }
    }
    
    
    public List<Usuario> cargarTodosLosUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        
        // Este SQL usa LEFT JOIN para "pegar" las tablas hijas a la tabla padre.
        // Si 'a.login' no es nulo, es un Administrador.
        // Si 'o.login' no es nulo, es un Organizador.
        // Si 'c.login' no es nulo, es un Comprador.
        String sql = "SELECT u.login, u.contrasena, u.saldo, " +
                     "       a.login AS admin_login, a.cobroPorEmision, " +
                     "       o.login AS org_login, " +
                     "       c.login AS comp_login " +
                     "FROM Usuario u " +
                     "LEFT JOIN Administrador a ON u.login = a.login " +
                     "LEFT JOIN OrganizadorEventos o ON u.login = o.login " +
                     "LEFT JOIN UsuarioComprador c ON u.login = c.login";

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) { // ResultSet es como una tabla de Excel con los resultados

            // Iteramos sobre cada fila (cada usuario)
            while (rs.next()) {
                // Leer los datos comunes de la tabla Usuario
                String login = rs.getString("login");
                String contrasena = rs.getString("contrasena");
                double saldo = rs.getDouble("saldo");
                
                Usuario usuario = null; // El objeto que vamos a crear

                // Ahora, determinamos el tipo de objeto a crear
                if (rs.getString("admin_login") != null) {
                    // Es un Administrador
                    double cobroEmision = rs.getDouble("cobroPorEmision");
                    Administrador admin = new Administrador(login, contrasena);
                    admin.setSaldo(saldo);
                    admin.fijarCobroPorEmision(cobroEmision);
                    // TODO: Cargar el Map de porcentajes
                    usuario = admin;
                    
                } else if (rs.getString("org_login") != null) {
                    // Es un Organizador
                    OrganizadorEventos org = new OrganizadorEventos(login, contrasena, saldo);
                    // TODO: Cargar sus listas (tiquetes, eventos)
                    usuario = org;
                    
                } else if (rs.getString("comp_login") != null) {
                    // Es un UsuarioComprador
                    UsuarioComprador comp = new UsuarioComprador(login, contrasena, saldo);
                    // TODO: Cargar su lista de tiquetes
                    usuario = comp;
                }
                
                if (usuario != null) {
                    usuarios.add(usuario);
                }
            }
        }
        return usuarios; // Devolvemos la lista de usuarios reconstruidos
    }
}
