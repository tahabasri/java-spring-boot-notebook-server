package com.tahabasri.projects.notebookserver.models;

/**
 * Manages the request information after validation and parsing, using this
 * class helps with monitoring request state (status) and adds metadata to the
 * request :
 * <ul>
 * <li>interpreter name</li>
 * <li>session id if it was given by the user</li>
 * </ul>
 * 
 * @author Taha BASRI
 *
 */
public class InterpretationRequest {

	/**
	 * Information status indicating that the request has good syntax but no session
	 * needed to execute it
	 */
	public static int INTERPRETATION_REQUEST_NO_NEED_FOR_SESSION = -800;
	/**
	 * Information status indicating that the request has good syntax but no session
	 * was already found in interpreter context, it will be required to assign new
	 * session for the given request
	 */
	public static int INTERPRETATION_REQUEST_NO_SESSION_ID = -850;
	/**
	 * Error status indicating that no interpreter was found for the given request
	 */
	public static int INTERPRETATION_REQUEST_NO_INTERPRETER_FOUND = -900;
	/**
	 * Error status indicating that the request has wrong syntax when matching it to
	 * the default request pattern
	 */
	public static int INTERPRETATION_REQUEST_WRONG_SYNTAX = -950;
	/**
	 * Information status indicating that the request has good syntax and it is
	 * ready to be interpreted
	 */
	public static int INTERPRETATION_REQUEST_GOOD = -1000;

	private String interpreterName;
	private String code;
	private String sessionId;
	private int status;

	/**
	 * Initialize the request with data from user input, validate the syntax, assign
	 * session identifier if required retrieves the interpreter name and parse the
	 * given code. Finally, a status flag is raised depending on request health
	 * 
	 * @param userRequestInput
	 */
	public InterpretationRequest(UserRequestInput userRequestInput) {
		String requestTokens[] = userRequestInput.getCode().split(" ", 2);
		if (requestTokens.length >= 2) {
			String interpreterName = requestTokens[0].replace("%", "").trim();
			String code = requestTokens[1].trim();

			setInterpreterName(interpreterName);
			setCode(code);

			if (userRequestInput.getSessionId() != null) {
				setSessionId(userRequestInput.getSessionId());

				setStatus(INTERPRETATION_REQUEST_GOOD);
			} else {
				setStatus(INTERPRETATION_REQUEST_NO_NEED_FOR_SESSION);
			}
		} else {
			setStatus(INTERPRETATION_REQUEST_WRONG_SYNTAX);
		}
	}

	public String getInterpreterName() {
		return interpreterName;
	}

	public void setInterpreterName(String interpreterName) {
		this.interpreterName = interpreterName;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Helper method to inform if the request is good to be interpreted by testing
	 * if request status is good not depending on session requirement
	 * 
	 * @return true if good, false otherwise
	 */
	public boolean isGoodForInterpretation() {
		return isGood() || isNewSession();
	}

	/**
	 * Helper method to inform if the request needs a session to be created (a
	 * session is required either way)
	 * 
	 * @return true if good, false otherwise
	 */
	public boolean isNewSession() {
		return status == INTERPRETATION_REQUEST_NO_SESSION_ID;
	}

	/**
	 * Helper method to inform if the request has good syntax
	 * 
	 * @return true if good, false otherwise
	 */
	public boolean isGood() {
		return status == INTERPRETATION_REQUEST_GOOD || status == INTERPRETATION_REQUEST_NO_NEED_FOR_SESSION;
	}

	/**
	 * Helper method to inform if the request has no status flag yet, this is useful
	 * for checking the initialization phase
	 * 
	 * @return true if no status was given, false otherwise
	 */
	public boolean isBlank() {
		return status == 0;
	}

	@Override
	public String toString() {
		return "InterpretationRequest [interpreterName=" + interpreterName + ", code=" + code + ", sessionId="
				+ sessionId + ", status=" + status + "]";
	}

}
