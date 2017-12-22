package jp.ats.backsight.client;

import jp.ats.substrate.U;

@SuppressWarnings("serial")
public class ApplicationException extends Exception {

	private static final ThreadLocal<String> holder = new ThreadLocal<String>();

	public ApplicationException(String message) {
		super(message);
		holder.set(message);
	}

	public static String getExceptionMessage() {
		String message = holder.get();
		holder.set("");
		return U.care(message);
	}
}
