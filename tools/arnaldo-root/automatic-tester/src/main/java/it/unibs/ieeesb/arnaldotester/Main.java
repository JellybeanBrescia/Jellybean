package it.unibs.ieeesb.arnaldotester;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.clapper.util.classutil.AbstractClassFilter;
import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.InterfaceOnlyClassFilter;
import org.clapper.util.classutil.NotClassFilter;
import org.clapper.util.classutil.SubclassClassFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import it.unibs.ieeesb.arnaldoproject.ArnaldoProjectException;
import it.unibs.ieeesb.arnaldoproject.Greeting;
import it.unibs.ieeesb.arnaldotester.tests.TestGreeting;

/**
 * Entry point of the automatic tester
 * 
 * This class represents the entry point of the application
 * 
 * @author massi
 */
public class Main {
	
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The path (relative to CWD) representing the jar to test.
	 * Must end with ".jar".
	 * For example "common-interface-impl-example-0.1-SNAPSHOT.jar"
	 */
	private String jarName;
	/**
	 * The symbolic name of the interface each jar to test needs to implement.
	 * There must be only a single class implementing such interface inside the {@link #jarName} jar file.
	 * 
	 * For example "it.unibs.ieeesb.arnaldotester.Greeting".
	 */
	private String interfaceName;
	/**
	 * The symbolic name of the class testing any implementation of {@link #interfaceName}.
	 * This class must be located inside the current application jar; furthermore, it has to be a JUnit
	 * "test case".
	 * For example "it.unibs.ieeesb.arnaldotester.tests.TestGreeting"
	 */
	private String testToExecute;
	/**
	 * The level each logger will log information
	 */
	private Level logLevel;

	public static void main(String[] args) {
		Main m = new Main();
		m.doWork(args);
		System.exit(0);
	}

	/**
	 * Perform the actual work of the class
	 * 
	 * The function will System.exit whenever failure cannot be avoided.
	 * Simply return otherwise
	 * 
	 * @param args the command arguments on the CLI
	 */
	private void doWork(String[] args) {
		//parse command line information
		try {
			this.parseCommandLine(args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//set log level (see http://stackoverflow.com/a/23434603/1887602)
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		loggerConfig.setLevel(this.logLevel);
		ctx.updateLoggers(); 
		
		//do the dirty work
		try {
			System.out.println(this.doTest());
		} catch (MalformedURLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(2);
		} catch (ArnaldoProjectException e) {
			System.err.println(e.getMessage());
			LOGGER.fatal(e.getMessage());
			System.exit(3);
		}
	}

	/**
	 * Perform the parsing of the command arguments (CLI)
	 * 
	 * After this function, instance variables are set. If "help" has been requested in
	 * command line, the application stops immediately 
	 * 
	 * @param args the command argument fetched from CLI
	 * @throws ParseException if we cannot parse the arguments on the CLI
	 * @throws ClassNotFoundException 
	 */
	private void parseCommandLine(String[] args) throws ParseException {
		Options options = new Options();

		options.addOption("j", "jar", true, "The jar you want to test");
		options.addOption("l", "log", true, "The log level. Can be DEBUG, INFO, WARN, ERROR, CRITICAL. Default to INFO.");
		options.addOption("i", "interface", true, "The symbolic name (ie. it.unibs.ieeesb.arnaldotester.Greeting) of the interface you want to test.");
		options.addOption("t", "test-case", true, "The symbolic name (ie. it.unibs.ieeesb.arnaldotester.tests.TestGreeting) of the test you want to execute");
		options.addOption("h", "help", false, "Print an help information");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "arnaldo tester", options );
			System.exit(0);
		}
		this.jarName = cmd.getOptionValue('j');
		this.interfaceName = cmd.getOptionValue('i', "it.unibs.ieeesb.arnaldoproject.Greeting");
		this.testToExecute = cmd.getOptionValue('t', "it.unibs.ieeesb.arnaldotester.tests.TestGreeting");
		this.logLevel = Level.valueOf(cmd.getOptionValue('l', "INFO"));
		
	}

	/**
	 * Performs the test requested in {@link #testToExecute}
	 * 
	 * @return a JSON object representing the test outcome
	 * @throws MalformedURLException if something goes wrong
	 * @throws InstantiationException if something goes wrong
	 * @throws IllegalAccessException if something goes wrong
	 * @throws ClassNotFoundException if something goes wrong
	 * @throws ArnaldoProjectException if {@link #testToExecute}, {@link #interfaceName} or {@link #jarName} wer enot setup correctly
	 */
	private JSONObject doTest() throws MalformedURLException, InstantiationException, IllegalAccessException, ClassNotFoundException, ArnaldoProjectException {
		TestHelper th = TestHelper.instance();
		th.clear();
		th.setHelper(this.jarName, this.interfaceName);
		if (!th.checkHelper()) {
			throw new ArnaldoProjectException(MessageFormat.format("Can''t fetch an implementation of class {0} inside the jar {1}!", this.interfaceName, this.jarName));
		}
		
		Computer computer = new Computer();
		JUnitCore jUnitCore = new JUnitCore();
		jUnitCore.addListener(new RunListener() {

			@Override
			public void testRunStarted(Description description) throws Exception {
				super.testRunStarted(description);
			}

			@Override
			public void testRunFinished(Result result) throws Exception {
				super.testRunFinished(result);
			}

			@Override
			public void testStarted(Description description) throws Exception {
				super.testStarted(description);
				LOGGER.info("Test {0} started", description.getMethodName());
			}

			@Override
			public void testFinished(Description description) throws Exception {
				super.testFinished(description);
			}

			@Override
			public void testFailure(Failure failure) throws Exception {
				LOGGER.warn("Test {0} has encountered the following general failure {1}", failure.getDescription().getMethodName(), failure.getMessage());
				super.testFailure(failure);
			}

			@Override
			public void testAssumptionFailure(Failure failure) {
				LOGGER.warn("Test {0} has encountered a failing assertion {1}", failure.getDescription().getMethodName(), failure.getException().getMessage());
				super.testAssumptionFailure(failure);
			}

			@Override
			public void testIgnored(Description description) throws Exception {
				super.testIgnored(description);
			}
			
		});
		
		if (!this.checkTestCase(this.testToExecute)) {
			throw new ArnaldoProjectException(MessageFormat.format("Can''t find test class {0} in order to test an implementation of {1}", this.testToExecute, this.interfaceName));
		}
		
		Result result = jUnitCore.run(computer, Class.forName(this.testToExecute));
		LOGGER.info("Unit test executed. generating output");
		
		return this.generateOutput(result);
	}
	
	/**
	 * Check if {@link #testToExecute} variable has been set correctly
	 * 
	 * This mainly means if there is a class named like <tt>name</tt>
	 * 
	 * @param name the test to check
	 * @return true if <tt>name</tt> is valid, false otherwise
	 */
	private boolean checkTestCase(String name) {
		try {
			Class<?> tc = Class.forName(name);
			if (tc == null) {
				return false;
			}
			if (tc.newInstance() == null) {
				return false;
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * An utility method used to generate in a oneline-way a map instance
	 * @param keyValueMapping a sequence of objects. The sequence can be viewed as a list of pairs. The first value of the pair is the key of an object while
	 * 	the second one is the value associated to the previous key. For example <code>createMap("a", Integer.valueOf(5), "b", Boolean.valueOf(true));</code>
	 * 	will generate a map with 2 keys, "a" and "b".
	 * @return a map instance
	 */
	private static Map<String, Object> createMap(Object... keyValueMapping) {
		Map<String, Object> retVal = new HashMap<>();
		String key = "";
		Object value = null;
		
		for(int i=0; i<keyValueMapping.length; i++) {
			if ((i % 2) == 0) {
				//this is a key
				key = keyValueMapping[i].toString();
			} else {
				value = keyValueMapping[i];
				//add the key-value inside the map
				retVal.put(key, value);
			}
		}
		return retVal;
	}
	
	/**
	 * Given the <tt>result</tt> from Junit engine, the function will produce a JSONObject
	 * representing the information to print out to the stdout.
	 * 
	 * The output JSON will contain:
	 * <ul>
	 * 	<li>failure-count</li>
	 * 	<li>total-ms-used</li>
	 * 	<li>total-tests-run</li>
	 * 	<li>was-successful</li>
	 * 	<li>feailures</li>
	 * </ul>
	 * 
	 * @param result the outcome the test {@link #testToExecute} for the implementation of {@link #interfaceName}
	 * @return a JSON object
	 */
	private JSONObject generateOutput(Result result) {
		JSONObject retVal = new JSONObject();
		retVal.put("failure-count", Integer.toString(result.getFailureCount()));
		retVal.put("total-ms-used", Long.toString(result.getRunTime()));
		retVal.put("total-tests-run", Integer.toString(result.getRunCount()));
		retVal.put("was-successful", Boolean.toString(result.wasSuccessful()));
		
		JSONArray failureList = new JSONArray();
		result.getFailures().forEach(f -> {
			failureList.put(new JSONObject(createMap("name", f.getDescription().getMethodName(), "cause", f.getMessage())));
		});
		retVal.put("failures", failureList);
		
		return retVal;
	}
}
