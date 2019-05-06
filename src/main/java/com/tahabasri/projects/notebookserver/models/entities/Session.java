package com.tahabasri.projects.notebookserver.models.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * User session managed by application back-end and persisted, holds the
 * identifier passed as request field (?sessionId=X), the corresponding context
 * and a list of all code lines used by the session (all expressions that does
 * not return a value to the user output)
 * 
 * @author Taha BASRI
 *
 */
@Entity
public class Session {
	@Id
	private Long id;

	@ManyToOne
	@JoinColumn
	private InterpreterContext context;

	@ElementCollection
	protected List<String> codeLines = new ArrayList<>();

	public Session() {
	}

	public Session(Long id, InterpreterContext context, List<String> codeLines) {
		this.id = id;
		this.context = context;
		this.codeLines = codeLines;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public InterpreterContext getContext() {
		return context;
	}

	public void setContext(InterpreterContext context) {
		this.context = context;
	}

	public List<String> getCodeLines() {
		return codeLines;
	}

	public void setCodeLines(List<String> codeLines) {
		this.codeLines = codeLines;
	}

	@Override
	public String toString() {
		return "Session [id=" + id + ", codeLines=" + codeLines + "]";
	}

}
