package com.tahabasri.projects.notebookserver.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Holds user request information, defines mainly the request body and the
 * session identifier
 * 
 * @author Taha BASRI
 *
 */
public class UserRequestInput {
	private String code;
	@JsonIgnore
	private String sessionId;

	public UserRequestInput() {
	}

	public UserRequestInput(String code, String sessionId) {
		this.code = code;
		this.sessionId = sessionId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		return "InterpretationRequest [code=" + code + ", sessionId=" + sessionId + "]";
	}

}
