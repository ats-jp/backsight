package jp.ats.backsight.server;

import static jp.ats.substrate.U.newLinkedList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LogIterator implements Iterator<String> {

	private final LinkedList<File> files = newLinkedList();

	private final Pattern pattern;

	private BufferedReader reader;

	private String line;

	LogIterator(File directory, String prefix, Date login, String id) {
		pattern = Pattern.compile("^[^\\t]+\\t"
			+ "[^\\t]+\\t"
			+ "[^\\t]+\\t"
			+ id
			+ "\\t");

		final Pattern filePattern = Pattern.compile("^"
			+ prefix
			+ "-"
			+ "(\\d{8}).log$");

		if (directory == null || !directory.isDirectory()) return;

		final int loginDate = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(login));

		files.addAll(Arrays.asList(directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) return false;
				Matcher matcher = filePattern.matcher(file.getName());
				if (!matcher.matches()) return false;
				return loginDate <= Integer.parseInt(matcher.group(1));
			}
		})));

		prepare();
	}

	@Override
	public synchronized boolean hasNext() {
		if (reader == null) return false;
		try {
			while ((line = reader.readLine()) != null) {
				if (pattern.matcher(line).find()) return true;
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		prepare();
		return hasNext();
	}

	@Override
	public synchronized String next() {
		if (line == null) throw new NoSuchElementException();
		return line;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private void prepare() {
		if (files.size() == 0) {
			reader = null;
			return;
		}

		File file = files.removeFirst();
		try {
			if (reader != null) reader.close();
			reader = new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
