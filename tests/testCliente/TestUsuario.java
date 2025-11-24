package testCliente;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import cliente.UsuarioComprador;
import cliente.Usuario;

public class TestUsuario {
	
	 // Usamos una implementación concreta para probar la clase abstracta

	    @Test
	    void testGettersYSettersFaltantes() {
	        // 1. Crear una instancia concreta (usamos UsuarioComprador como vehículo)
	        String loginInicial = "userTest";
	        String passInicial = "12345";
	        double saldoInicial = 100.0;
	        
	        Usuario usuario = new UsuarioComprador(loginInicial, passInicial, saldoInicial);

	        // 2. Cubrir getContrasena (Línea roja)
	        assertEquals(passInicial, usuario.getContrasena(), "Debe devolver la contraseña inicial");

	        // 3. Cubrir setContrasena (Línea roja)
	        String nuevoPass = "newPass999";
	        usuario.setContrasena(nuevoPass);
	        assertEquals(nuevoPass, usuario.getContrasena(), "Debe actualizar la contraseña");

	        // 4. Cubrir setLogIn (Línea roja)
	        String nuevoLogin = "userUpdated";
	        usuario.setLogIn(nuevoLogin);
	        assertEquals(nuevoLogin, usuario.getLogIn(), "Debe actualizar el login");
	    }
	}



