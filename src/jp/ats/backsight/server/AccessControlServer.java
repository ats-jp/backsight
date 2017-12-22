package jp.ats.backsight.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jp.ats.substrate.concurrent.Channel;
import jp.ats.substrate.concurrent.IOStreamServiceHandler;
import jp.ats.substrate.concurrent.IOStreamServiceHandlerFactory;
import jp.ats.substrate.concurrent.Server;
import jp.ats.substrate.concurrent.SocketAcceptor;

public class AccessControlServer {

	private final Object lock = new Object();

	private final int port;

	private final Terminal terminal;

	private Server server;

	private int state = 2;

	private StatusListener listener = new StatusListenerPlug();

	public AccessControlServer(int port, Terminal terminal) {
		this.port = port;
		this.terminal = terminal;
		restartControlServerInternal();
	}

	public void restartControlServer() {
		synchronized (lock) {
			if (isShutdowned()) restartControlServerInternal();
		}
	}

	public boolean isShutdowned() {
		synchronized (lock) {
			return server.isAcceptorShutdowned();
		}
	}

	public String getStateInfo() {
		synchronized (lock) {
			switch (state) {
			case 0:
				return "0:起動状態";
			case 1:
				return "1:一時停止";
			case 2:
				return "2:許可されたユーザー以外一時停止";
			default:
				throw new IllegalStateException();
			}
		}
	}

	private void restartControlServerInternal() {
		synchronized (lock) {
			try {
				server = new Server(new SocketAcceptor(
					AccessController.address,
					port,
					1,
					1000,
					1000 * 10), new IOStreamServiceHandlerFactory() {

					@Override
					public IOStreamServiceHandler newInstance() {
						return new AccessControlServiceHandler();
					}
				});
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			Channel channel = server.getChannel();
			channel.setMaximumWorkerCount(1);
			channel.setMaximumPoolSize(1);

			server.service();
		}
	}

	public void setListener(StatusListener listener) {
		synchronized (lock) {
			this.listener = listener;
		}
	}

	public void checkState(String target) {
		synchronized (lock) {
			switch (state) {
			case 0:
				break;
			case 1:
				throw new AccessDeniedException();
			case 2:
				if (getAllows().contains(target)) break;
				throw new AccessDeniedException();
			default:
				throw new IllegalStateException();
			}
		}
	}

	public void shutdown() {
		server.shutdown();
	}

	private Set<String> getAllows() {
		Set<String> allows = new HashSet<String>(
			Arrays.asList(terminal.getAdministratorNames()));
		return allows;
	}

	private class AccessControlServiceHandler extends IOStreamServiceHandler {

		@Override
		protected void serviceInternal(int workerID) {
			PrintWriter writer = null;
			try {
				InputStream input = getIOStream().getInputStream();
				writer = new PrintWriter(new BufferedOutputStream(
					getIOStream().getOutputStream()));
				writer.println("サーバに対する指示を選択してください (10秒で切断されます)");
				writer.println("0:起動状態に復帰");
				writer.println("1:一時停止");
				writer.println("2:許可されたユーザー以外一時停止");
				synchronized (lock) {
					writer.println("現在の状態は[" + state + "]です");
				}
				writer.print("[0|1|2] -> ");
				writer.flush();
				char stateChar = (char) input.read();
				switch (stateChar) {
				case '0':
					synchronized (lock) {
						state = 0;
						listener.resume();
						writer.println();
						writer.println("0:起動状態に復帰しました");
						writer.flush();
					}
					return;
				case '1':
					synchronized (lock) {
						state = 1;
						listener.suspend();
						writer.println();
						writer.println("1:一時停止しました");
						writer.flush();
					}
					return;
				case '2':
					synchronized (lock) {
						state = 2;
						listener.restrict();
						writer.println();
						writer.println("2:許可されたユーザー以外一時停止しました");
						writer.flush();
					}
					return;
				default:
					writer.println();
					writer.println("状態は変わりませんでした");
					writer.flush();
					return;
				}
			} catch (InterruptedIOException e) {} catch (IOException e) {
				throw new IllegalStateException(e);
			} finally {
				if (writer != null) writer.close();
			}
		}
	}

	private static class StatusListenerPlug implements StatusListener {

		@Override
		public void resume() {}

		@Override
		public void suspend() {}

		@Override
		public void restrict() {}
	};
}
