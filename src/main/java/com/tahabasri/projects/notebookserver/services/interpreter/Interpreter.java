package com.tahabasri.projects.notebookserver.services.interpreter;

import java.io.File;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tahabasri.projects.notebookserver.models.ExecutionResult;
import com.tahabasri.projects.notebookserver.models.InterpretationRequest;
import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;
import com.tahabasri.projects.notebookserver.models.entities.Session;

/**
 * Main interpreter class, holds common attributes and methods used by all
 * interpreters
 * 
 * @author Taha BASRI
 *
 */
public abstract class Interpreter {

	private static final Logger logger = LogManager.getLogger(Interpreter.class);

	/**
	 * Default interpreter processing timeout
	 */
	public static final Integer DEFAULT_TIMEOUT = 5000;
	/**
	 * Default interpreter code separator
	 */
	public static final String DEFAULT_SEPARATOR = "|";

	/**
	 * Interpreter specific properties: all the properties that stars with
	 * "interpreter.<i><b>interpreterName</i></b>"
	 */
	private Properties properties;
	/**
	 * Request session, to be modified by each request
	 */
	private Session session;
	/**
	 * Interpreter name
	 */
	private final String name;

	/**
	 * initialize interpreter by setting its name and verify if the interpreter has
	 * a good process executor in files system
	 * 
	 * @param context interpreter context
	 * 
	 * @throws ExceptionInInitializerError when unable to create new instance
	 */
	public Interpreter(InterpreterContext context) {
		this.name = context.getInterpreterName();
		logger.debug("Initializing interpreter '" + this.name + "'");
		if (!hasExecutorAvailable(context.getInterpreterPath())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error in initializing interpreter '" + this.name + "' : wrong executor path : "
						+ context.getInterpreterPath());
			}
			throw new ExceptionInInitializerError("Error while initializing Interpreter");
		}
	}

	/**
	 * tests if the interpreter execution process is available in files system
	 * 
	 * @param executorPath path to executable
	 * @return true if its valid, false otherwise
	 */
	private boolean hasExecutorAvailable(String executorPath) {
		File executor = new File(executorPath);
		logger.debug("Interpreter executor is OK");
		return executor.exists();
	}

	/**
	 * reads an interpreter specific property (properties that stars with
	 * "interpreter.<i>interpreterName</i>.<b>key</b>")
	 * 
	 * @param key only suffix parameter, prefix is pre-defined by the interpreter
	 *            context
	 * @return property value if found, null otherwise
	 */
	protected String getProperty(String key) {
		String prefix = "interpreter." + name + ".";
		if (key != null && getProperties() != null) {
			String fullKey = prefix + key;
			logger.info("Reading property : " + fullKey);
			return getProperties().getProperty(fullKey);
		}
		logger.warn("No properties file");
		return null;
	}

	/**
	 * interprets the request depending on interpreter specific implementation (to
	 * be defined in a concrete class implementing this service), if the request has
	 * good syntax, the interpreter process is launched in background.
	 * 
	 * A result object must be passed as an argument which will be filled after
	 * execution and depending on the final result
	 * 
	 * @param interpretationRequest the user parsed request
	 * @param context               interpreter context
	 * @param result                object to be populated with execution result
	 * 
	 * @return true if the interpretation has been executed successfully, false
	 *         otherwise
	 */
	public abstract boolean interpret(InterpretationRequest interpretationRequest, InterpreterContext context,
			ExecutionResult result);

	/**
	 * gets all interpreter properties that stars with
	 * "interpreter.<i><b>interpreterName</i></b>" (to be used by the interpreter
	 * specific implementation as needed)
	 * 
	 * @return interpreter properties
	 */
	protected Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	protected Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
