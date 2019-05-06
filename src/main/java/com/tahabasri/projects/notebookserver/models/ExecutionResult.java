package com.tahabasri.projects.notebookserver.models;

/**
 * Interpretation result to send to the final user, it contains :
 * <ul>
 * <li>result status : good result or error</li>
 * <li>result content : the interpretation result if status is OK, error message
 * otherwise</li>
 * </ul>
 * 
 * @author Taha BASRI
 *
 */
public class ExecutionResult {
	public static final String RESULT_OK = "result";
	public static final String RESULT_ERROR = "error";

	private String resultType;
	private String resultContent;

	public ExecutionResult() {
	}

	public ExecutionResult(String resultType, String resultContent) {
		this.resultType = resultType;
		this.resultContent = resultContent;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	public String getResultContent() {
		return resultContent;
	}

	public void setResultContent(String resultContent) {
		this.resultContent = resultContent;
	}

	@Override
	public String toString() {
		return "ExecutionResult [resultType=" + resultType + ", resultContent=" + resultContent + "]";
	}

}
