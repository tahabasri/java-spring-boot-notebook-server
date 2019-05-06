package com.tahabasri.projects.notebookserver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.tahabasri.projects.notebookserver.models.ExecutionResult;
import com.tahabasri.projects.notebookserver.models.InterpretationRequest;
import com.tahabasri.projects.notebookserver.models.UserRequestInput;
import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;
import com.tahabasri.projects.notebookserver.models.entities.Session;
import com.tahabasri.projects.notebookserver.repositories.InterpreterContextRepository;
import com.tahabasri.projects.notebookserver.repositories.SessionRepository;
import com.tahabasri.projects.notebookserver.services.InterpreterService;
import com.tahabasri.projects.notebookserver.services.InterpreterServiceImpl;
import com.tahabasri.projects.notebookserver.services.interpreter.Interpreter;
import com.tahabasri.projects.notebookserver.services.interpreter.InterpreterLookup;
import com.tahabasri.projects.notebookserver.services.interpreter.PythonInterpreter;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class InterpreterServiceTest {

	@TestConfiguration
	static class InterpreterServiceTestConfiguration {

		@Bean
		public InterpreterService interpreterService() {
			return new InterpreterServiceImpl();
		}
	}

	@Autowired
	private InterpreterService interpreterService;

	@MockBean
	private InterpreterContextRepository contextRepository;

	@MockBean
	private SessionRepository sessionRepository;

	@MockBean
	private InterpreterLookup interpreterLookup;

	@Before
	public void setUp() throws Exception {
		ReflectionTestUtils.setField(interpreterService, "codeRequestPattern", "%[a-z]{3,}[ ]//S.+");

		String pythonExec = "D:/dev/data/notebook/interpreter/python/python.exe";

		InterpreterContext context = new InterpreterContext("python", pythonExec, null);
		Session session = new Session(951L, context, Arrays.asList("import math", "print 1+1"));
		session.setId(159753L);
		List<Session> sessions = Arrays.asList(session);
		context.setSessions(sessions);

		Mockito.when(contextRepository.findByInterpreterName("python")).thenReturn(context);

		Interpreter pythonInterpreter = new PythonInterpreter(context);
		Mockito.when(interpreterLookup.getInterpreter(context)).thenReturn(pythonInterpreter);
	}

	@Test
	public void testValidateAndParseInterpretationRequest() {
		UserRequestInput interpretationRequest = new UserRequestInput("%python print 1+1", "159753");

		InterpretationRequest request = interpreterService.validateAndParseInterpretationRequest(interpretationRequest);
		assertEquals(request.getStatus(), InterpretationRequest.INTERPRETATION_REQUEST_GOOD);
	}

	@Test
	public void testValidateAndParseInterpretationRequest_WrongSyntax() {
		UserRequestInput interpretationRequest = new UserRequestInput("%python123 print 1+1", "159753");

		InterpretationRequest request = interpreterService.validateAndParseInterpretationRequest(interpretationRequest);
		if (request != null) {
			assertEquals(request.getStatus(), InterpretationRequest.INTERPRETATION_REQUEST_WRONG_SYNTAX);
		} else {
			assertNull(request);
		}
	}

	@Test
	public void testValidateAndParseInterpretationRequest_WrongInterpreter() {
		UserRequestInput interpretationRequest = new UserRequestInput("%java print 1+1", "159753");

		InterpretationRequest request = interpreterService.validateAndParseInterpretationRequest(interpretationRequest);
		assertEquals(request.getStatus(), InterpretationRequest.INTERPRETATION_REQUEST_NO_INTERPRETER_FOUND);
	}

	@Test
	public void testValidateAndParseInterpretationRequest_NoSessionParameter() {
		UserRequestInput interpretationRequest = new UserRequestInput("%python print 1+1", null);

		InterpretationRequest request = interpreterService.validateAndParseInterpretationRequest(interpretationRequest);
		assertEquals(request.getStatus(), InterpretationRequest.INTERPRETATION_REQUEST_NO_NEED_FOR_SESSION);
	}

	@Test
	public void testValidateAndParseInterpretationRequest_NoSessionFound() {
		UserRequestInput interpretationRequest = new UserRequestInput("%python print 1+1", "159753456");

		InterpretationRequest request = interpreterService.validateAndParseInterpretationRequest(interpretationRequest);
		assertEquals(request.getStatus(), InterpretationRequest.INTERPRETATION_REQUEST_NO_SESSION_ID);
	}

	@Test
	public void testInterpretRequest() {
		UserRequestInput interpretationRequest = new UserRequestInput("%python print (1+1)", null);

		InterpretationRequest request = interpreterService.validateAndParseInterpretationRequest(interpretationRequest);
		request.setStatus(InterpretationRequest.INTERPRETATION_REQUEST_NO_NEED_FOR_SESSION);

		ExecutionResult result = interpreterService.interpretRequest(request);

		assertThat(result.getResultType()).isEqualToIgnoringCase(ExecutionResult.RESULT_OK);
	}

}
