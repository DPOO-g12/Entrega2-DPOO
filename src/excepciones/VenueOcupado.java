package excepciones;

@SuppressWarnings("serial")
public class VenueOcupado extends Exception  {

	 private String venueOcupado;

	    public VenueOcupado( String venueName )
	    {
	        this.venueOcupado = venueName;
	    }

	    @Override
	    public String getMessage( )
	    {
	        return "El venue '" + venueOcupado + "' está ocupado para ese dia";
	    }
	

	
	
	
	
	
}
