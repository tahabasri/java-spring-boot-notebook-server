package com.tahabasri.projects.notebookserver.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;
import com.tahabasri.projects.notebookserver.models.entities.Session;

@RunWith(SpringRunner.class)
@TestPropertySource(locations="classpath:application.properties")
@DataJpaTest
public class InterpreterContextRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private InterpreterContextRepository contextRepository;

	@Autowired
	private SessionRepository sessionRepository;

	private InterpreterContext context;

	private final String interpreterName = "python";
	private Long sessionId;

	@Before
	public void setUp() {
		Session session = new Session(156L, null, Arrays.asList("import math", "print 1+1"));

		session = entityManager.merge(session);

		context = new InterpreterContext(interpreterName, "pythonFullPath", null);
		List<Session> sessions = Collections.singletonList(session);
		context.setSessions(sessions);

		context = entityManager.merge(context);

		sessionId = (Long) entityManager.getId(session);
		entityManager.flush();
	}

	@Test
	public void testGettingByName() {
		InterpreterContext searchedForContext = contextRepository.findByInterpreterName(interpreterName);

		assertThat(searchedForContext.getInterpreterName()).isEqualTo(context.getInterpreterName());
	}

	@Test
	public void testGettingSessionByName() {
		InterpreterContext searchedForContext = contextRepository.findByInterpreterName(interpreterName);

		if (searchedForContext != null) {
			List<Session> sessions = searchedForContext.getSessions();
			Session session = sessions.stream().filter(s -> sessionId.equals(s.getId())).findAny().orElse(null);
			assertNotNull(session);
		} else {
			fail("Context not found");
		}
	}

	@Test
	public void testAddNewSession() {
		InterpreterContext searchedForContext = contextRepository.findByInterpreterName(interpreterName);

		long sessionId = 951L;

		if (searchedForContext != null) {
			List<Session> sessions = searchedForContext.getSessions();
			int actualSize = sessions.size();
			sessions.add(new Session(sessionId, searchedForContext, new ArrayList<>()));

			searchedForContext.setSessions(sessions);
			searchedForContext = contextRepository.saveAndFlush(searchedForContext);

			assertThat(searchedForContext.getSessions().size()).isEqualTo(actualSize + 1);
		} else {
			fail("Context not found");
		}
	}

	@Test
	public void testClearSessionData() {
		InterpreterContext searchedForContext = contextRepository.findByInterpreterName(interpreterName);

		if (searchedForContext != null) {
			List<Session> sessions = searchedForContext.getSessions();
			Session session = sessions.stream().filter(s -> sessionId.equals(s.getId())).findAny().orElse(null);

			if (session != null) {
				List<String> codeLines = session.getCodeLines();

				if (codeLines != null && !codeLines.isEmpty()) {
					session.setCodeLines(new ArrayList<>());

					session = sessionRepository.saveAndFlush(session);

					assertThat(session.getCodeLines()).isEmpty();
				} else {
					fail("No lines found");
				}
			} else {
				fail("Session not found");
			}
		} else {
			fail("Context not found");
		}
	}
}
