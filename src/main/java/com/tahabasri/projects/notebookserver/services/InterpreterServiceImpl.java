package com.tahabasri.projects.notebookserver.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tahabasri.projects.notebookserver.models.ExecutionResult;
import com.tahabasri.projects.notebookserver.models.InterpretationRequest;
import com.tahabasri.projects.notebookserver.models.UserRequestInput;
import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;
import com.tahabasri.projects.notebookserver.models.entities.Session;
import com.tahabasri.projects.notebookserver.repositories.InterpreterContextRepository;
import com.tahabasri.projects.notebookserver.repositories.SessionRepository;
import com.tahabasri.projects.notebookserver.services.interpreter.Interpreter;
import com.tahabasri.projects.notebookserver.services.interpreter.InterpreterLookup;

@Service
public class InterpreterServiceImpl implements InterpreterService {

	private static final Logger logger = LogManager.getLogger(InterpreterService.class);

	/**
	 * default request pattern to be matched against when validating request syntax
	 */
	@Value("${global.request.pattern}")
	private String codeRequestPattern;

	@Autowired
	private InterpreterContextRepository contextRepository;
	@Autowired
	private SessionRepository sessionRepository;
	@Autowired
	private InterpreterLookup interpreterLookup;

	@Override
	public InterpretationRequest validateAndParseInterpretationRequest(UserRequestInput interpretationRequest) {
		if (validateRequestCode(interpretationRequest.getCode())) {
			logger.info("User code has good syntaxt, parsing it ...");
			InterpretationRequest request = new InterpretationRequest(interpretationRequest);

			if (request.isBlank()) {
				logger.warn("User code was not fully parsed, mark it as having wrong syntaxt");
				return requestWithStatus(request, InterpretationRequest.INTERPRETATION_REQUEST_WRONG_SYNTAX);
			}

			if (request.isGoodForInterpretation()) {
				return parseRequest(request);
			}
		}
		return null;
	}

	@Override
	public ExecutionResult interpretRequest(InterpretationRequest interpretationRequest) {
		InterpreterContext context = contextRepository
				.findByInterpreterName(interpretationRequest.getInterpreterName());

		boolean noNeedForSession = interpretationRequest
				.getStatus() == InterpretationRequest.INTERPRETATION_REQUEST_NO_NEED_FOR_SESSION;

		if (noNeedForSession) {
			logger.warn("No session is required for the request!");
		}

		return noNeedForSession ? interpretRequest(interpretationRequest, context)
				: interpretRequestSessionAware(interpretationRequest, context);
	}

	/**
	 * validates the request code by evaluating it against a pre-defined pattern
	 * 
	 * @param code request code
	 * @return true if code has good syntaxt, false otherwise
	 */
	private boolean validateRequestCode(String code) {
		logger.info("Validating user code");
		boolean validValue = code != null && code != null && !code.isEmpty();

		if (codeRequestPattern != null) {
			logger.debug("Parsing code special caracters");
			codeRequestPattern = codeRequestPattern.replaceAll("//", "\\\\");
		}

		return validValue && code.matches(codeRequestPattern);
	}

	/**
	 * parses request, searches for an existing session and assign a status flag to
	 * the request
	 * 
	 * @param request user request
	 * @return user parsed request
	 */
	private InterpretationRequest parseRequest(InterpretationRequest request) {
		InterpreterContext context = contextRepository.findByInterpreterName(request.getInterpreterName());
		int status;

		if (context != null) {
			if (request.getStatus() == InterpretationRequest.INTERPRETATION_REQUEST_NO_NEED_FOR_SESSION) {
				logger.info("Parsing ... Request has good syntaxt but does not need a session");
				status = request.getStatus();
			} else {
				Session session = retrieveSessionForRequest(context, request.getSessionId());

				if (session != null) {
					logger.info("Parsing ... Request has good syntaxt, and has a session in interpreter context");
					status = InterpretationRequest.INTERPRETATION_REQUEST_GOOD;
				} else {
					logger.info(
							"Parsing ... Request has good syntaxt, but no session was found int interpreter context");
					status = InterpretationRequest.INTERPRETATION_REQUEST_NO_SESSION_ID;
				}
			}
		} else {
			logger.warn("Parsing ... Request has good syntaxt, but no interpreter was found for it");
			status = InterpretationRequest.INTERPRETATION_REQUEST_NO_INTERPRETER_FOUND;
		}

		logger.info("Done parsing");
		return requestWithStatus(request, status);
	}

	/**
	 * helper method to set status
	 * 
	 * @param request interpretation request
	 * @param status  status (best when using InterpreterRequest constants)
	 * @return request after marking its status flag
	 */
	private InterpretationRequest requestWithStatus(InterpretationRequest request, int status) {
		request.setStatus(status);
		return request;
	}

	/**
	 * interpret request and returns the final result
	 * 
	 * @param interpretationRequest interpretation parsed request
	 * @param context               interpreter context
	 * @return final execution result which holds interpretation status and its
	 *         result content
	 */
	private ExecutionResult interpretRequest(InterpretationRequest interpretationRequest, InterpreterContext context) {
		ExecutionResult result = new ExecutionResult();

		if (interpretationRequest.isGood()) {
			logger.info("Request is ready for interpretation");
			Interpreter interpreter = interpreterLookup.getInterpreter(context);
			if (interpreter != null) {
				logger.info("Attach properties to " + context.getInterpreterName() + "interpreter");
				interpreter.setProperties(interpreterLookup.readPropertiesForInterpreter(context.getInterpreterName()));
				interpreter.interpret(interpretationRequest, context, result);
			}
		}

		return result;
	}

	/**
	 * helper method to interpret request while dealing with request session, this
	 * helps preserve variables and sessions states
	 * 
	 * @param interpretationRequest interpretation request
	 * @param context               interpreter context
	 * @return final execution result which holds interpretation status and its
	 *         result content
	 */
	private ExecutionResult interpretRequestSessionAware(InterpretationRequest interpretationRequest,
			InterpreterContext context) {
		ExecutionResult result = new ExecutionResult();
		if (interpretationRequest.isNewSession()) {
			logger.info("Request needs a new session, creating one ...");
			createNewSession(context, interpretationRequest);
			logger.info("New session was created");
		}

		if (interpretationRequest.isGood()) {
			logger.info("Request is ready for interpretation");
			Interpreter interpreter = interpreterLookup.getInterpreter(context);
			if (interpreter != null) {
				logger.info("Attach properties to " + context.getInterpreterName() + "interpreter");
				interpreter.setProperties(interpreterLookup.readPropertiesForInterpreter(context.getInterpreterName()));

				logger.info("Attach session to interpreter");
				Session requestSession = retrieveSessionForRequest(context, interpretationRequest.getSessionId());
				interpreter.setSession(requestSession);

				boolean interpretation = interpreter.interpret(interpretationRequest, context, result);

				// save code for given session only if :
				// - its interpretation is good
				// - does not return result content (variable initialization for example)
				if (interpretation && result.getResultContent().isEmpty()) {
					logger.info("Saving interpreted code in session (Non Terminal Expression) ...");
					saveSessionCode(requestSession, interpretationRequest);
				} else {
					logger.warn("No need to save code in session (Terminal Expression)");
				}
			}
		}
		return result;
	}

	/**
	 * creates a new session for the given request
	 * 
	 * @param context               interpreter context
	 * @param interpretationRequest interpretation request
	 */
	private void createNewSession(InterpreterContext context, InterpretationRequest interpretationRequest) {
		Session session = new Session(getSessionIdentifier(interpretationRequest.getSessionId()), context,
				new ArrayList<>());

		List<Session> sessions = context.getSessions();
		sessions.add(session);
		context.setSessions(sessions);

		contextRepository.saveAndFlush(context);

		logger.debug("Marking the request as being ready for interpretation");
		interpretationRequest.setStatus(InterpretationRequest.INTERPRETATION_REQUEST_GOOD);
	}

	/**
	 * saves request code for the user given session
	 * 
	 * @param session
	 * @param interpretationRequest
	 */
	private void saveSessionCode(Session session, InterpretationRequest interpretationRequest) {
		List<String> codeLines = session.getCodeLines();
		codeLines.add(interpretationRequest.getCode());
		session.setCodeLines(codeLines);

		sessionRepository.saveAndFlush(session);
	}

	/**
	 * gets the request specific session
	 * 
	 * @param context          interpreter context
	 * @param requestSessionId request session identifier
	 * @return session instance if found, null otherwise
	 */
	private Session retrieveSessionForRequest(InterpreterContext context, String requestSessionId) {
		if (requestSessionId != null && !requestSessionId.isEmpty()) {

			Long sessionId = getSessionIdentifier(requestSessionId);
			if (sessionId != null) {

				List<Session> sessions = context.getSessions();

				if (sessions != null) {
					Session session = sessions.stream().filter(s -> sessionId.longValue() == s.getId()).findAny()
							.orElse(null);

					return session != null ? session : null;
				}
			}

		}
		return null;
	}

	/**
	 * helper method to parse session identifier
	 * 
	 * @param requestSessionId request session identifier
	 * @return parsed request session identifier
	 */
	private Long getSessionIdentifier(String requestSessionId) {
		try {
			Long sessionId = Long.valueOf(requestSessionId);
			return sessionId;
		} catch (Exception e) {
			logger.error("Error in parsing the sessionId " + e.getMessage());
			return null;
		}
	}
}
