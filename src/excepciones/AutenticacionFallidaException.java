package excepciones;


@SuppressWarnings("serial")
public class AutenticacionFallidaException extends Exception {
    public AutenticacionFallidaException(String mensaje) {
        super(mensaje);
    }
}