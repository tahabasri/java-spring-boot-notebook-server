package com.tahabasri.projects.notebookserver.bootstrap;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;
import com.tahabasri.projects.notebookserver.models.entities.Session;
import com.tahabasri.projects.notebookserver.repositories.InterpreterContextRepository;

@Component
public class BootStrapData implements CommandLineRunner{

	@Autowired
	private InterpreterContextRepository contextRepository;
	
	@Value("${default.interpreter.path}")
	private String interpreterPath;

	@Value("${default.interpreter.name}")
	private String interpreterName;

	/**
	 * Initialize application data with a pre-defined interpreter and a session
	 * 
	 */
	@Override
	public void run(String... args) throws Exception {
		Session session = new Session(321L, null, null) ;
		
		InterpreterContext context = new InterpreterContext(interpreterName, interpreterPath, null);
		List<Session> sessions = Arrays.asList(session);
		context.setSessions(sessions);
		session.setContext(context);
		
		contextRepository.save(context);
	}

	
	
}
