package jp.ats.backsight.server;

class PasswordEntryLine extends Line {

	private final LineFactory lineFactory;

	PasswordEntryLine(
		LineFactory lineFactory,
		String remoteAddress,
		String userId,
		String sessionId,
		String method,
		String path) {
		super(remoteAddress, userId, sessionId, method, path);
		this.lineFactory = lineFactory;
	}

	@Override
	void addParameter(String key, String value) {
		if (lineFactory.contains(key)) {
			super.addParameter(key, "");
		} else {
			super.addParameter(key, value);
		}
	}
}
