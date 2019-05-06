package com.tahabasri.projects.notebookserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tahabasri.projects.notebookserver.models.entities.Session;

/**
 * Session data access interface, helps in retrieving and persisting session
 * entities
 * 
 * @author Taha BASRI
 *
 */
public interface SessionRepository extends JpaRepository<Session, Long> {
}
