package com.tahabasri.projects.notebookserver.services;

import com.tahabasri.projects.notebookserver.models.ExecutionResult;
import com.tahabasri.projects.notebookserver.models.InterpretationRequest;
import com.tahabasri.projects.notebookserver.models.UserRequestInput;

/**
 * Interpreter business interface, offers main services for interpreting
 * requests
 * 
 * @author Taha BASRI
 *
 */
public interface InterpreterService {

	/**
	 * validate the good syntax and the need for a session for a specific user
	 * request, parse the request and isolate metadata from real request code
	 * 
	 * @param interpretationRequest user native request
	 * @return interpretation parsed request, holds metadata for interpreter,
	 *         request health and session
	 */
	public InterpretationRequest validateAndParseInterpretationRequest(UserRequestInput interpretationRequest);

	/**
	 * interpret user already parsed request, deals with request session (if it's
	 * required) and returns the final result
	 * 
	 * @param interpretationRequest user parsed request
	 * @return execution result with interpretation status and final result
	 */
	public ExecutionResult interpretRequest(InterpretationRequest interpretationRequest);
}
