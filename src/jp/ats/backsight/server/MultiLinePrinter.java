package jp.ats.backsight.server;

import jp.ats.substrate.U;

public class MultiLinePrinter {

	public static void print(String header, String line) {
		String[] lines = line.split(U.LINE_SEPARATOR);
		synchronized (System.out) {
			for (int i = 0; i < lines.length; i++) {
				System.out.println(header + lines[i]);
			}
		}
	}
}
