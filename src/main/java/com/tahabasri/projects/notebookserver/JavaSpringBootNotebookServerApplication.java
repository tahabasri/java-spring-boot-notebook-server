package com.tahabasri.projects.notebookserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JavaSpringBootNotebookServerApplication {

	/**
	 * initialize application context and deploy application in embedded server
	 * 
	 * @param args inline args
	 */
	public static void main(String[] args) {
		SpringApplication.run(JavaSpringBootNotebookServerApplication.class, args);
	}

}
