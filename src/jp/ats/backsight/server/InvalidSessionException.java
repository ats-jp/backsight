package jp.ats.backsight.server;

@SuppressWarnings("serial")
public class InvalidSessionException extends RuntimeException {

	public InvalidSessionException(String message) {
		super(message);
	}
}
