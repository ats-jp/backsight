package jp.ats.backsight.server;

import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class DuplicateRequestException extends ServletException {

	DuplicateRequestException(String message) {
		super(message);
	}
}
