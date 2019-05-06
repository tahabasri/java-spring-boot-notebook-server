package com.tahabasri.projects.notebookserver.services.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tahabasri.projects.notebookserver.models.ExecutionResult;
import com.tahabasri.projects.notebookserver.models.InterpretationRequest;
import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;

public class PythonInterpreter extends Interpreter {

	private static final Logger logger = LogManager.getLogger(Interpreter.class);

	public PythonInterpreter(InterpreterContext context) {
		super(context);
	}

	@Override
	public boolean interpret(InterpretationRequest interpretationRequest, InterpreterContext context,
			ExecutionResult result) {
		logger.info("Interpreting request by " + context.getInterpreterName() + " interpreter");
		String code = getFullSessionCode(context, interpretationRequest);
		return execute(code, context, result);
	}

	/**
	 * gets all code that resides in session, this helps with saving variables and
	 * sessions states
	 * 
	 * @param context               interpreter context
	 * @param interpretationRequest user parsed request
	 * @return full code concatenated by interpreter separator (or default if none
	 *         was given), request request code if no session is needed
	 */
	private String getFullSessionCode(InterpreterContext context, InterpretationRequest interpretationRequest) {
		logger.debug("Reading interpreter separator");
		
		String separator = getProperty("separator");
		separator = separator != null ? separator : Interpreter.DEFAULT_SEPARATOR;

		if (getSession() != null) {
			logger.debug("Reading all session code lines");
			StringBuilder codeBuilder = new StringBuilder();
			for (String code : getSession().getCodeLines()) {
				codeBuilder.append(code.replaceAll("\"", "'"));
				codeBuilder.append(separator);
			}
			codeBuilder.append(interpretationRequest.getCode().replaceAll("\"", "'"));
			return codeBuilder.toString();
		}
		
		logger.warn("No session for the request, executing request code solo!");
		return interpretationRequest.getCode();
	}

	/**
	 * initialize interpreter executor, sets it timeout from properties file (or
	 * default value if none was set
	 * 
	 * @param executor executor implementation
	 * @param output   interpreter configured output
	 */
	private void initializeExecutor(DefaultExecutor executor, ByteArrayOutputStream output) {
		logger.debug("Initializeing interpreter executor");
		PumpStreamHandler psh = new PumpStreamHandler(output);

		// read property from external properties file
		String timeoutValue = getProperty("timeout");
		int timeout = timeoutValue != null ? Integer.valueOf(timeoutValue) : Interpreter.DEFAULT_TIMEOUT;

		logger.info("Timeout for the interpreter is : " + timeoutValue);
		
		ExecuteWatchdog watchDog = new ExecuteWatchdog(timeout);

		executor.setStreamHandler(psh);
		executor.setWatchdog(watchDog);
	}

	/**
	 * execute the interpretation request
	 * 
	 * @param code    user code (session if defined)
	 * @param context interpreter context
	 * @param result  interpretation result
	 * @return true if interpretation was good, false otherwise
	 */
	public boolean execute(String code, InterpreterContext context, ExecutionResult result) {
		DefaultExecutor executor = new DefaultExecutor();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		initializeExecutor(executor, output);

		String executorPath = context.getInterpreterPath();
		CommandLine cl = new CommandLine(executorPath);
		cl.addArguments("-c");
		cl.addArguments("\"" + code + "\"", true);
		try {
			logger.info("Executing interpretation...");
			executor.execute(cl);
			result.setResultType(ExecutionResult.RESULT_OK);
			result.setResultContent(output.toString().trim());
			logger.info("Interpretation was executed successfully");
			return true;
		} catch (ExecuteException e) {
			result.setResultContent("Error executing command, due to syntax or execution time : " + e.getMessage());
			result.setResultType(ExecutionResult.RESULT_ERROR);
			logger.debug("Error interpreting the request : " + e);
		} catch (IOException e) {
			result.setResultContent("Error executing command, : " + e.getMessage());
			result.setResultType(ExecutionResult.RESULT_ERROR);
			logger.debug("Error interpreting the request : " + e);
		}
		return false;
	}
}
