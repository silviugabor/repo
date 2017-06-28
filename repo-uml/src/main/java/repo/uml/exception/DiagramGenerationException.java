package repo.uml.exception;

public class DiagramGenerationException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public DiagramGenerationException(String message) {
		super(message);
	}
	
	public DiagramGenerationException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}
