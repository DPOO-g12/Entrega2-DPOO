package persistencia;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class InicializadorBD {

    public static void crearTablas() {
        String[] sqls = {
            // 1. Usuarios
            "CREATE TABLE IF NOT EXISTS Usuario (login TEXT PRIMARY KEY, contrasena TEXT, saldo REAL)",
            "CREATE TABLE IF NOT EXISTS Administrador (login TEXT PRIMARY KEY, cobroPorEmision REAL, FOREIGN KEY(login) REFERENCES Usuario(login))",
            "CREATE TABLE IF NOT EXISTS OrganizadorEventos (login TEXT PRIMARY KEY, FOREIGN KEY(login) REFERENCES Usuario(login))",
            "CREATE TABLE IF NOT EXISTS UsuarioComprador (login TEXT PRIMARY KEY, FOREIGN KEY(login) REFERENCES Usuario(login))",

            // 2. Venues y Eventos
            "CREATE TABLE IF NOT EXISTS Venue (id_venue INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT, ubicacion TEXT, capacidad INTEGER, estado TEXT)",
            "CREATE TABLE IF NOT EXISTS Evento (id_evento TEXT PRIMARY KEY, nombre TEXT, fecha TEXT, estado TEXT, id_venue INTEGER, login_organizador TEXT, FOREIGN KEY(id_venue) REFERENCES Venue(id_venue))",
            
            // 3. Localidades (Tabla Padre) - ¡CORREGIDO: AGREGADO id_oferta!
            "CREATE TABLE IF NOT EXISTS Localidades (" +
                "id_localidad INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "precio REAL, " +
                "capacidad_max INTEGER, " +
                "tickets_vendidos INTEGER, " +
                "id_evento TEXT, " +
                "id_oferta INTEGER, " + // <--- ¡AQUÍ ESTÁ LA COLUMNA FALTANTE!
                "FOREIGN KEY(id_evento) REFERENCES Evento(id_evento))",

            // 4. Tablas Hijas de Localidad
            "CREATE TABLE IF NOT EXISTS Numerada (id_localidad INTEGER PRIMARY KEY, FOREIGN KEY(id_localidad) REFERENCES Localidades(id_localidad))",
            "CREATE TABLE IF NOT EXISTS NoNumerada (id_localidad INTEGER PRIMARY KEY, tiquetes_vendidos INTEGER, FOREIGN KEY(id_localidad) REFERENCES Localidades(id_localidad))",
            
            // 5. TIQUETES
            "CREATE TABLE IF NOT EXISTS Tiquete (" +
                "id_tiquete_db INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_tiquete_java TEXT, " +
                "precio_base REAL, " +
                "costo_servicio REAL, " +
                "costo_emision REAL, " +
                "precio_final REAL, " +
                "fecha TEXT, " +
                "estado TEXT, " +
                "transferible INTEGER, " +
                "impreso INTEGER DEFAULT 0, " + 
                "login_cliente TEXT, " +
                "id_localidad INTEGER, " +
                "id_evento TEXT, " +
                "FOREIGN KEY(login_cliente) REFERENCES Usuario(login))",

            // 6. Tipos de Tiquetes
            "CREATE TABLE IF NOT EXISTS Basico (id_tiquete_db INTEGER PRIMARY KEY, numero_asiento TEXT, FOREIGN KEY(id_tiquete_db) REFERENCES Tiquete(id_tiquete_db))",
            "CREATE TABLE IF NOT EXISTS Deluxe (id_tiquete_db INTEGER PRIMARY KEY, FOREIGN KEY(id_tiquete_db) REFERENCES Tiquete(id_tiquete_db))",
            "CREATE TABLE IF NOT EXISTS Multiple (id_tiquete_db INTEGER PRIMARY KEY, FOREIGN KEY(id_tiquete_db) REFERENCES Tiquete(id_tiquete_db))",
            
            // 7. Extras
            "CREATE TABLE IF NOT EXISTS Deluxe_Beneficios (id_tiquete_db INTEGER, beneficio_desc TEXT)",
            "CREATE TABLE IF NOT EXISTS Multiple_TiquetesIncluidos (id_tiquete_multiple INTEGER, id_tiquete_incluido INTEGER)",
            "CREATE TABLE IF NOT EXISTS Multiple_EventosAsociados (id_tiquete_multiple INTEGER, id_evento TEXT)",
            "CREATE TABLE IF NOT EXISTS OfertaReventa (id_oferta INTEGER PRIMARY KEY AUTOINCREMENT, id_tiquete_db INTEGER, login_vendedor TEXT, precio REAL, estado TEXT)",
            
            // 8. TABLA DE OFERTAS (Promociones) - Agregada por seguridad
            "CREATE TABLE IF NOT EXISTS Oferta (id_oferta INTEGER PRIMARY KEY AUTOINCREMENT, descuento REAL, fecha_fin TEXT, login_organizador TEXT)",
            
         // 9. CONFIGURACIÓN DELUXE POR LOCALIDAD (NUEVA TABLA)
            "CREATE TABLE IF NOT EXISTS ConfigDeluxe (id_localidad INTEGER PRIMARY KEY, cantidad_limite INTEGER, precio_extra REAL, beneficios TEXT, vendidos INTEGER DEFAULT 0, FOREIGN KEY(id_localidad) REFERENCES Localidades(id_localidad))"
        };

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            
            for (String sql : sqls) {
                stmt.execute(sql);
            }
            System.out.println(">>> BASE DE DATOS ESTRUCTURADA CORRECTAMENTE <<<");

        } catch (SQLException e) {
            System.err.println("Error creando tablas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}