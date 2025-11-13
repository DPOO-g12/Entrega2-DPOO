package excepciones;

@SuppressWarnings("serial")
public class FondosInsuficientesException extends Exception {
    public FondosInsuficientesException(String mensaje) {
        super(mensaje);
    }
    
}