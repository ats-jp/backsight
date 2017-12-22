package jp.ats.backsight;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;

import jp.ats.substrate.U;

public class Common {

	private static final String remoteObjectName = "BacksightController";

	public static String getRemoteObjectName(String contextName) {
		return remoteObjectName + "-" + contextName;
	}

	public static String addComma(long value) {
		return new DecimalFormat("#,###").format(value);
	}

	public static String encode(String base) {
		try {
			return URLEncoder.encode(base, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String join(String[] values, String separator) {
		return U.join(values, separator);
	}
}
