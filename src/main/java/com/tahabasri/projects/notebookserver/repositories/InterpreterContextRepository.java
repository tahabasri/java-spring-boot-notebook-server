package com.tahabasri.projects.notebookserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;

/**
 * Interpreter context data access interface, helps in retrieving and persisting
 * interpreter context entities
 * 
 * @author Taha BASRI
 *
 */
public interface InterpreterContextRepository extends JpaRepository<InterpreterContext, Long> {
	InterpreterContext findByInterpreterName(String interpreterName);
}
