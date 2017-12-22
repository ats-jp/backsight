package jp.ats.backsight;

import static jp.ats.substrate.U.castToLinkedHashMap;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class SessionInfo implements Serializable {

	private static final long serialVersionUID = -3480729679849637016L;

	private final String id;

	private final String creationTime;

	private final String lastAccessedTime;

	private final String sessionInactiveTime;

	private final String stayMinutes;

	private final String remoteUser;

	private final String remoteAddr;

	private final String userInfo;

	private final LinkedHashMap<String, String> attributes;

	public SessionInfo(
		String id,
		String creationTime,
		String lastAccessedTime,
		String sessionInactiveTime,
		String stayMinutes,
		String remoteUser,
		String remoteAddr,
		String userInfo,
		LinkedHashMap<String, String> attributes) {
		this.id = id;
		this.creationTime = creationTime;
		this.lastAccessedTime = lastAccessedTime;
		this.sessionInactiveTime = sessionInactiveTime;
		this.stayMinutes = stayMinutes;
		this.remoteUser = remoteUser;
		this.remoteAddr = remoteAddr;
		this.userInfo = userInfo;
		this.attributes = castToLinkedHashMap(attributes.clone());
	}

	public String getId() {
		return id;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public String getLastAccessedTime() {
		return lastAccessedTime;
	}

	public String getSessionInactiveTime() {
		return sessionInactiveTime;
	}

	public String getStayMinutes() {
		return stayMinutes;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public LinkedHashMap<String, String> getAttributes() {
		return castToLinkedHashMap(attributes.clone());
	}
}
