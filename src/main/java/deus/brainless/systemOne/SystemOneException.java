package deus.brainless.systemOne;

public class SystemOneException extends RuntimeException {
	public SystemOneException(String message) {
		super(message);
	}

	public SystemOneException(String message, Throwable cause) {
		super(message, cause);
	}
}
