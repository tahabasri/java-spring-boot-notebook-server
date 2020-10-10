package com.tahabasri.projects.notebookserver.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tahabasri.projects.notebookserver.models.ExecutionResult;
import com.tahabasri.projects.notebookserver.models.InterpretationRequest;
import com.tahabasri.projects.notebookserver.models.UserRequestInput;
import com.tahabasri.projects.notebookserver.services.InterpreterService;

/**
 * Web interface to interact with the application business logic
 *
 * @author Taha BASRI
 */
@RestController
@RequestMapping(ExecutorController.BASE_URL)
public class ExecutorController {

    private static final Logger logger = LogManager.getLogger(ExecutorController.class);
    /**
     * REST base URL suffix, used to access this controller's end points, the full
     * URL is : http://IP:port<i>BASE_URL</i>
     */
    public static final String BASE_URL = "/api/v1/execute";

    @Autowired
    private Environment env;

    @Autowired
    private InterpreterService interpreterService;

    /**
     * Used to checks if the service is fully working
     *
     * @return "Alive!" message if all good, none or error message otherwise
     */
    @SuppressWarnings("SameReturnValue")
    @GetMapping
    public String status() {
        return "Alive!";
    }

    /**
     * 'execute' end point : to be called to interpret user given code request, a
     * <b>sessionId</b> field may be passed in URL to benefit from session aware
     * interpretation (preserving variables state)
     *
     * @param request   user request in the form of
     *                  {code:{%'interpreter-name''whitespace''code''}}
     * @param sessionId session field value if given
     * @return executed interpretation result if syntax is good and execution is OK,
     * error message otherwise
     */
    @PostMapping
    public ExecutionResult execute(@RequestBody UserRequestInput request,
                                   @RequestParam(required = false) String sessionId) {
        logger.info("Calling '/execute' endpoint ...");

        request.setSessionId(sessionId);

        InterpretationRequest interpretationRequest = interpreterService.validateAndParseInterpretationRequest(request);

        ExecutionResult executionResult;
        if (interpretationRequest != null && interpretationRequest.isGoodForInterpretation()) {
            executionResult = interpreterService.interpretRequest(interpretationRequest);
        } else {
            return new ExecutionResult(ExecutionResult.RESULT_ERROR,
                    String.format("Couldn't parse input code, check that it matches following regex : '%s'", env.getProperty("global.request.pattern")));
        }

        return executionResult;
    }

}
