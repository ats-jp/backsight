package jp.ats.backsight.server;

@SuppressWarnings("serial")
public class SessionCountOverException extends RuntimeException {

	public SessionCountOverException(String message) {
		super(message);
	}
}
