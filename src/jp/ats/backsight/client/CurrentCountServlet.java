package jp.ats.backsight.client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ats.backsight.BacksightController;

@SuppressWarnings("serial")
public abstract class CurrentCountServlet extends HttpServlet {

	private static final String responseXmlBase = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+ "<backsight>"
		+ "<current-count>{0}</current-count>"
		+ "<config-count>{1}</config-count>"
		+ "</backsight>";

	protected abstract int getCurrentCount(BacksightController controller)
		throws RemoteException;

	protected abstract int getConfigCount(BacksightController controller)
		throws RemoteException;

	@Override
	protected void doPost(
		HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {
		String site = request.getParameter("site");
		String context = request.getParameter("context");
		BacksightController controller = BacksightClient.getControllerWithoutException(
			site,
			context);

		String xml;
		if (controller == null) {
			xml = MessageFormat.format(responseXmlBase, 0, 0);
		} else {
			xml = MessageFormat.format(
				responseXmlBase,
				getCurrentCount(controller),
				getConfigCount(controller));
		}

		response.setContentType("text/xml");
		response.setCharacterEncoding("UTF-8");

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
			response.getOutputStream(),
			"UTF-8"));

		writer.write(xml);
		writer.flush();
	}
}
