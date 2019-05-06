package com.tahabasri.projects.notebookserver.models.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Interpreter context, holds interpreter name, interpreter executor full path
 * and all sessions that use the given interpreter
 * 
 * @author Taha BASRI
 *
 */
@Entity
public class InterpreterContext {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String interpreterName;
	private String interpreterPath;
	@OneToMany(mappedBy = "context", cascade = CascadeType.ALL)
	private List<Session> sessions;

	public InterpreterContext() {
	}

	public InterpreterContext(String interpreterName, String interpreterPath, List<Session> sessions) {
		this.interpreterName = interpreterName;
		this.interpreterPath = interpreterPath;
		this.sessions = sessions;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInterpreterName() {
		return interpreterName;
	}

	public void setInterpreterName(String interpreterName) {
		this.interpreterName = interpreterName;
	}

	public String getInterpreterPath() {
		return interpreterPath;
	}

	public void setInterpreterPath(String interpreterPath) {
		this.interpreterPath = interpreterPath;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	@Override
	public String toString() {
		return "InterpreterContext [id=" + id + ", interpreterName=" + interpreterName + ", interpreterPath="
				+ interpreterPath + ", sessions=" + sessions + "]";
	}

}
