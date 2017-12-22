package jp.ats.backsight.server;

import static jp.ats.substrate.U.newLinkedList;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Logger {

	private final List<String> buffer = newLinkedList();

	private final long interval;

	private final File directory;

	private final String prefix;

	private boolean closed = false;

	private Thread thread;

	public Logger(long interval, File directory, String prefix) {
		this.interval = interval;
		this.directory = directory;
		this.prefix = prefix;
		start();
		System.out.println("ログ出力を開始しました");
	}

	public synchronized void destroy() {
		if (thread != null) thread.interrupt();
	}

	public synchronized void log(String log) {
		if (thread == null) {
			MultiLinePrinter.print("[Backsight Logger] [" + prefix + "]", log);
			return;
		}

		synchronized (buffer) {
			if (closed) throw new IllegalStateException("既にアプリケーションは終了しています");
			buffer.add(log);
			//bufferがたまっている場合、何らかのエラーで出力スレッドが
			//死んでいる可能性があるので、死んでいたら再起動させる
			if (buffer.size() > 300) start();
		}
	}

	private class LogWriter implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
					flush();
					Thread.sleep(interval);
				}
			} catch (InterruptedException e) {}
			synchronized (buffer) {
				closed = true;
			}
			flush();
			System.out.println("ログ出力は正常に終了しました");
		}
	}

	private synchronized void start() {
		if (thread != null && thread.isAlive()) return;
		thread = new Thread(new LogWriter(), "BacksightLogger-" + prefix);
		thread.setDaemon(true);
		thread.start();
	}

	private void flush() {
		List<String> output;
		synchronized (buffer) {
			if (buffer.size() == 0) return;
			output = newLinkedList();
			output.addAll(buffer);
			buffer.clear();
		}
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(List<String> output) throws IOException {
		File log = new File(directory, prefix
			+ "-"
			+ new SimpleDateFormat("yyyyMMdd").format(new Date())
			+ ".log");
		log.createNewFile();
		PrintWriter writer = new PrintWriter(new BufferedOutputStream(
			new FileOutputStream(log.getAbsolutePath(), true)));
		for (String line : output) {
			writer.println(line);
		}
		writer.flush();
		writer.close();
	}

	public File getDirectory() {
		return directory;
	}

	public String getPrefix() {
		return prefix;
	}
}
