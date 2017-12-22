package jp.ats.backsight.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.server.RMISocketFactory;

public class BacksightRMISocketFactory extends RMISocketFactory {

	private static final InetAddress localhost;

	private final InetAddress allowedClient;

	static {
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new IllegalStateException(e);
		}
	}

	BacksightRMISocketFactory(InetAddress allowedClient) {
		this.allowedClient = allowedClient;
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return new BacksightServerSocket(port);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return getDefaultSocketFactory().createSocket(host, port);
	}

	private class BacksightServerSocket extends ServerSocket {

		private BacksightServerSocket(int port) throws IOException {
			super(port, 1);
		}

		@Override
		public Socket accept() throws IOException {
			Socket socket = super.accept();
			InetAddress accepted = socket.getInetAddress();
			if (!accepted.equals(localhost) && !accepted.equals(allowedClient)) {
				System.out.println("Illegal access from [" + accepted + "]");
				socket.close();
				throw new IOException();
			}
			return socket;
		}
	}
}
