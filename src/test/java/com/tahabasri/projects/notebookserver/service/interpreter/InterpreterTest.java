package com.tahabasri.projects.notebookserver.service.interpreter;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.tahabasri.projects.notebookserver.models.ExecutionResult;
import com.tahabasri.projects.notebookserver.models.InterpretationRequest;
import com.tahabasri.projects.notebookserver.models.UserRequestInput;
import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;
import com.tahabasri.projects.notebookserver.services.interpreter.Interpreter;
import com.tahabasri.projects.notebookserver.services.interpreter.InterpreterLookup;
import com.tahabasri.projects.notebookserver.services.interpreter.InterpreterLookupImpl;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class InterpreterTest {

	@TestConfiguration
	static class InterpreterTestConfiguration {
		@Bean
		public InterpreterLookup interpreterLookup() {
			return new InterpreterLookupImpl();
		}
	}

	@Autowired
	private InterpreterLookup interpreterLookup;

	@Autowired
	private Environment env;

	@Test
	public void testInterpretPythonCode() {
		// this still depend on external resource, to change if someone got time :)
		String pythonExec = env.getProperty("default.interpreter.path");
		InterpreterContext context = new InterpreterContext("python", pythonExec, null);

		Interpreter interpreter = interpreterLookup.getInterpreter(context);

		interpreter.setProperties(interpreterLookup.readPropertiesForInterpreter(context.getInterpreterName()));

		UserRequestInput userRequestInput = new UserRequestInput("%python print (1+1)", "");
		InterpretationRequest interpretationRequest = new InterpretationRequest(userRequestInput);

		ExecutionResult result = new ExecutionResult();
		boolean interpretation = interpreter.interpret(interpretationRequest, context, result);

		assertTrue(interpretation);
	}

}
