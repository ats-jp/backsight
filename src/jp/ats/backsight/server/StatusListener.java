package jp.ats.backsight.server;

public interface StatusListener {

	public void resume();

	public void suspend();

	public void restrict();
}
