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
				"�p�����[�^�ɂ�\"suspend\"��\"resume\"��\"restrict\"�����g�p�ł��܂���B");
		}
	}

	/**
	 * �T�[�o���N����Ԃɂ��܂��B
	 */
	public static void resume(int port) throws IOException {
		process('0', port);
	}

	/**
	 * �T�[�o���ꎞ��~��Ԃɂ��܂��B resume���Ȃ�����A�ǂ�������A�N�Z�X�ł��܂���B
	 */
	public static void suspend(int port) throws IOException {
		process('1', port);
	}

	/**
	 * �A�N�Z�X�����ꂽ�[������̃��N�G�X�g�̂ݏ�������悤�ɂ��܂��B
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
