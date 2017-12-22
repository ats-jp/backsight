package jp.ats.backsight.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class AccessController {

	static final String address = "127.0.0.1";

	public static void main(String[] args) throws IOException {
		String command = args[0];
		int port = Integer.parseInt(args[1]);
		if ("suspend".equals(command)) {
			suspend(port);
		} else if ("resume".equals(command)) {
			resume(port);
		} else if ("restrict".equals(command)) {
			restrict(port);
		} else {
			throw new IllegalArgumentException(
				"パラメータには\"suspend\"か\"resume\"か\"restrict\"しか使用できません。");
		}
	}

	/**
	 * サーバを起動状態にします。
	 */
	public static void resume(int port) throws IOException {
		process('0', port);
	}

	/**
	 * サーバを一時停止状態にします。 resumeしない限り、どこからもアクセスできません。
	 */
	public static void suspend(int port) throws IOException {
		process('1', port);
	}

	/**
	 * アクセス許可された端末からのリクエストのみ処理するようにします。
	 */
	public static void restrict(int port) throws IOException {
		process('2', port);
	}

	private static void process(char methodType, int port) throws IOException {
		@SuppressWarnings("resource")
		Socket socket = new Socket(address, port);
		skip(socket.getInputStream());
		OutputStream output = socket.getOutputStream();
		output.write(methodType);
		output.flush();
	}

	private static void skip(final InputStream input) {
		new Thread() {

			@Override
			public void run() {
				try {
					while (input.read() != -1) {}
				} catch (IOException e) {}
			}
		}.start();
	}
}
