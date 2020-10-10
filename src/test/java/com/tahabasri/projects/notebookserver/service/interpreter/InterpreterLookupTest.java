package com.tahabasri.projects.notebookserver.service.interpreter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;
import com.tahabasri.projects.notebookserver.services.interpreter.Interpreter;
import com.tahabasri.projects.notebookserver.services.interpreter.InterpreterLookup;
import com.tahabasri.projects.notebookserver.services.interpreter.InterpreterLookupImpl;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class InterpreterLookupTest {

	@TestConfiguration
	static class InterpreterLookupTestConfiguration {
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
	public void testGetNewInterpreterWithBadExecutionPath() {
		InterpreterContext context = new InterpreterContext("python", "pythonBadFullPath", null);
		Interpreter interpreter = interpreterLookup.getInterpreter(context);
		assertNull(interpreter);
	}

	@Test
	public void testGetNewInterpreterWithNoInterpreterImplementationExecutionPath() {
		String python = env.getProperty("default.interpreter.path");
		InterpreterContext context = new InterpreterContext("unknown", python, null);
		Interpreter interpreter = interpreterLookup.getInterpreter(context);
		assertNull(interpreter);
	}

	@Test
	public void testGetNewInterpreter() {
		String pythonExec = env.getProperty("default.interpreter.path");
		InterpreterContext context = new InterpreterContext("python", pythonExec, null);

		Interpreter interpreter = interpreterLookup.getInterpreter(context);
		assertNotNull(interpreter);
	}

	@Test
	public void testGetAlreadyCreatedInterpreter() {
		String pythonExec = env.getProperty("default.interpreter.path");
		InterpreterContext context = new InterpreterContext("python", pythonExec, null);

		Interpreter interpreter = interpreterLookup.getInterpreter(context);

		assertThat(interpreterLookup.getInterpreter(context)).isEqualTo(interpreter);
	}

	@Test
	public void testReadPropertiesForInterpreter() {
		String property = "interpreter.python.timeout";
		assertNotNull(interpreterLookup.readPropertiesForInterpreter("python").getProperty(property));
	}
}
