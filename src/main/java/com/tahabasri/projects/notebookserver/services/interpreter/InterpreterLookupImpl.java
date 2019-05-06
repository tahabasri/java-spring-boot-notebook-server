package com.tahabasri.projects.notebookserver.services.interpreter;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import com.tahabasri.projects.notebookserver.models.entities.InterpreterContext;

@Component
public class InterpreterLookupImpl implements InterpreterLookup {

	private static final Logger logger = LogManager.getLogger(InterpreterLookup.class);

	/**
	 * Application whole context environment
	 */
	@Autowired
	private Environment env;

	/**
	 * All interpreter instances holder
	 */
	private Map<String, Interpreter> interpreterInstances = new HashMap<>();

	/**
	 * checks if the given implementation class exists by loading it by its fully
	 * qualified name "currentPackage.<interpreterName>Interpreter"
	 * 
	 * @param interpreterName interpreter name
	 * @return class type, null if no implementation was found
	 */
	private Class<?> getInterpreterImplementationClass(String interpreterName) {
		logger.info("Searching for '" + interpreterName + "' interpreter implementation");
		String packageName = getClass().getPackage().getName();

		String firstLetter = String.valueOf(interpreterName.charAt(0));
		interpreterName = interpreterName.replaceFirst(firstLetter, firstLetter.toUpperCase());

		try {
			Class<?> cls = Class.forName(packageName + "." + interpreterName + "Interpreter");
			if (cls != null) {
				logger.info("'" + interpreterName + "' interpreter implementation was found");
				return cls;
			} else {
				logger.warn("No '" + interpreterName + "' interpreter implementation was found!");
				return null;
			}
		} catch (ClassNotFoundException e) {
			logger.error("No '" + interpreterName + "' interpreter implementation was found : " + e.getMessage());
			logger.debug("Error in loading interpreter implementation : " + e);
			return null;
		}
	}

	@Override
	public Interpreter getInterpreter(InterpreterContext context) {
		logger.info("Searching for '" + context.getInterpreterName() + "' interpreter implementation");
		Class<?> interpreterImpl = getInterpreterImplementationClass(context.getInterpreterName());
		if (interpreterImpl != null) {
			Interpreter interpreter = null;
			String instanceName = context.getInterpreterName() + "Interpreter";

			interpreter = interpreterInstances.get(instanceName);

			if (interpreter != null) {
				logger.info("An instance of '" + context.getInterpreterName() + "' interpreter was found");
				return interpreter;
			} else {
				logger.warn("No instance of '" + context.getInterpreterName()
						+ "' interpreter was found, creating one ...");
				try {
					Constructor<?> constructor = interpreterImpl
							.getConstructor(new Class[] { InterpreterContext.class });
					interpreter = (Interpreter) constructor.newInstance(context);
					interpreterInstances.put(instanceName, interpreter);
					logger.warn("A new instance of '" + context.getInterpreterName() + "' interpreter was created");
					return interpreter;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public Properties readPropertiesForInterpreter(String interpreterName) {
		String prefix = "interpreter." + interpreterName;
		logger.info("Reading all properties that starts with '" + prefix + "'");
		
		Properties props = new Properties();
		MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
		StreamSupport.stream(propSrcs.spliterator(), false).filter(ps -> ps instanceof EnumerablePropertySource)
				.map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames()).flatMap(Arrays::<String>stream)
				.filter(propName -> propName.startsWith(prefix))
				.forEach(propName -> props.setProperty(propName, env.getProperty(propName)));

		return props;
	}

}
