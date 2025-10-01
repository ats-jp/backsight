package jp.ats.backsight.server;

import jakarta.servlet.ServletException;

@SuppressWarnings("serial")
public class DuplicateRequestException extends ServletException {

	DuplicateRequestException(String message) {
		super(message);
	}
}
