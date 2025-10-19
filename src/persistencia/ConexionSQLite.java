package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Clase de utilidad para manejar la conexión a la base de datos SQLite.
 */
public class ConexionSQLite {

    // Esta es la "cadena de conexión". Le dice a JDBC qué driver usar (sqlite)
    // y dónde está nuestro archivo (tiquetera.db).
    private static final String URL = "jdbc:sqlite:tiquetera.db";

    /**
     * Establece una conexión con la base de datos 'tiquetera.db'.
     * @return un objeto Connection
     * @throws SQLException si ocurre un error al conectar.
     */
    public static Connection conectar() throws SQLException {
        try {
            // Esto "registra" el driver que descargamos
            Class.forName("org.sqlite.JDBC"); 
            
            // Esto intenta conectarse al archivo .db
            return DriverManager.getConnection(URL);
            
        } catch (ClassNotFoundException e) {
            // Esto pasa si olvidaste añadir el .jar al Build Path
            throw new SQLException("Error: Driver JDBC de SQLite no encontrado. ¿Olvidaste añadir el .jar?", e);
        }
    }
}



