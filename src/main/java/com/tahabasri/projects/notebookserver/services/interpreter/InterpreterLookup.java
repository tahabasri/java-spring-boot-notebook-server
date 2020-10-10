package com.tahabasri.projects.notebookserver.services.interpreter;

import java.util.Properties;

import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;

/**
 * Interpreter lookup utility class, this class is useful to retrieve or create
 * dynamically an instance of a given interpreter
 * 
 * @author Taha BASRI
 *
 */
public interface InterpreterLookup {

	/**
	 * Gets an instance of a pre-defined interpreter implementation, may be useful
	 * as a place where to create a new instance if no one is already found
	 * 
	 * @param context interpreter context
	 * 
	 * @return interpreter implementation instance
	 */
    Interpreter getInterpreter(InterpreterContext context);

	/**
	 * read all properties starting with "interpreter.<i><b>interpreterName</b></i>"
	 * from application properties file
	 */
	Properties readPropertiesForInterpreter(String interpreterName);
}
