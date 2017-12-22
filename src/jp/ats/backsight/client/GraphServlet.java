package jp.ats.backsight.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ats.substrate.U;

@SuppressWarnings("serial")
public class GraphServlet extends HttpServlet {

	private static final Pattern pattern = Pattern.compile("(\\d+)_(\\d+)\\.png$");

	private int threshold = 75;

	private int width = 102;

	private int height = 20;

	@Override
	public void init(ServletConfig config) throws ServletException {
		String thresholdString = config.getInitParameter("threshold");
		if (U.isAvailable(thresholdString)) {
			threshold = Integer.parseInt(thresholdString);
		}

		String widthString = config.getInitParameter("width");
		if (U.isAvailable(widthString)) {
			width = Integer.parseInt(widthString);
		}

		String heightString = config.getInitParameter("height");
		if (U.isAvailable(heightString)) {
			height = Integer.parseInt(heightString);
		}
	}

	@Override
	protected void service(
		HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {
		OutputStream out = response.getOutputStream();

		Matcher matcher = pattern.matcher(request.getRequestURI());
		matcher.find();

		try {
			response.setContentType("image/png");
			ImageIO.write(
				createGraph(
					width,
					height,
					Integer.parseInt(matcher.group(1)),
					Integer.parseInt(matcher.group(2)),
					threshold),
				"png",
				out);
			out.flush();
		} finally {
			out.close();
		}
	}

	private static BufferedImage createGraph(
		int width,
		int height,
		float numerator,
		float denominator,
		int threshold) {
		int percent;
		String text;
		if (denominator == 0) {
			percent = 0;
			text = "";
		} else {
			percent = Math.min(Math.round(numerator * 100 / denominator), 100);
			text = (int) numerator + " / " + (int) denominator;
		}

		BufferedImage image = new BufferedImage(
			width,
			height,
			BufferedImage.TYPE_INT_BGR);

		Graphics2D graph = image.createGraphics();

		Color graphColor;
		if (percent >= threshold) {
			graphColor = Color.RED;
		} else {
			graphColor = Color.GREEN;
		}

		try {
			graph.setBackground(Color.WHITE);
			graph.clearRect(1, 1, width - 2, height - 2);

			int fill = Math.round((width - 2) * numerator / denominator);
			fill = fill > width - 2 ? width - 2 : fill;

			graph.setColor(graphColor);
			graph.fillRect(1, 1, fill, height - 2);

			graph.setColor(Color.BLACK);

			if (text.length() > 0) {
				char[] chars = text.toCharArray();
				graph.setFont(new Font(Font.DIALOG, Font.PLAIN, height - 4));
				FontMetrics fontMetrics = graph.getFontMetrics();
				int charWidth = fontMetrics.charsWidth(chars, 0, chars.length);
				int yPosition = height - fontMetrics.getDescent();
				graph.setColor(Color.BLACK);
				graph.drawString(
					text,
					((float) (width - charWidth)) / 2,
					yPosition);
			}

			graph.drawImage(image, 0, 0, null);
		} finally {
			graph.dispose();
		}

		return image;
	}
}
