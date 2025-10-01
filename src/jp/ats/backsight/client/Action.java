package jp.ats.backsight.client;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface Action {

	void execute(HttpServletRequest request, HttpServletResponse response)
		throws ApplicationException, ServletException;
}
