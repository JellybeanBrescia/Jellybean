package it.unibs.ieeesb.arnaldotester;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.clapper.util.classutil.AbstractClassFilter;
import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.InterfaceOnlyClassFilter;
import org.clapper.util.classutil.NotClassFilter;
import org.clapper.util.classutil.SubclassClassFilter;
import org.junit.internal.runners.model.EachTestNotifier;

import it.unibs.ieeesb.arnaldotester.tests.TestGreeting;
import junit.framework.TestCase;

/**
 * A singleton class used to communicate between {@link Main} and {@link TestCase} class
 * 
 * An example of {@link TestCase} is {@link TestGreeting}. Since parametrized constructors seems not to be
 * allowed in Junit framework, we need another method to pass variables to {@link TestCase}s.
 * One way is to have a data structure common between {@link Main} and each {@link TestCase}.
 * This data structure is the singleton {@link TestHelper}.
 * 
 * You can fetch the singleton via {@link TestHelper#instance()}.
 * 
 * If you want to set the data that will be exploted by {@link TestCase}, you need to call:
 * <pre>{@code 
 * 	TestHelper.instance().clear(); 
 * 	TestHelper.instance().setHelper("jarname.jar", "my.personal.interface.Interface");
 * }</pre>
 * 
 * Then, you can use {@link TestHelper} to fetch an implementation of the interface set in {@link #setHelper(String, String)} from the specified jar
 * via {@link #getInstance()}.
 * 
 * <p><b>Note</b>: Remember, {@link TestHelper} will generate the new instance via the empty constructor of the implementation!</p>
 * 
 * @author massi
 *
 */
public class TestHelper {
	
	private static final Logger LOGGER = LogManager.getLogger();
	private static TestHelper singleton = null;
	
	/**
	 * The absolute file of the jar containing the implementation of {@link #interfaceName}.
	 * See {@link Main}.
	 */
	private String jarName;
	/**
	 * The symbolic name of the interface whose implementation we're looking for.
	 * 
	 * See {@link Main}
	 */
	private String interfaceName;
	/**
	 * The classloader which allows us to retrieve classes from {@link #jarName}
	 */
	private ClassLoader jarClassLoader;
	
	/**
	 * Method used to retrieve the singleton instance of {@link TestHelper}
	 * 
	 * @return the singleton instance.
	 */
	public static TestHelper instance() {
		if (singleton == null) {
			singleton = new TestHelper();
		}
		return singleton;
	}
	
	private TestHelper() {
		this.clear();
	}
	
	/**
	 * Clear the data inside this instance.
	 * Set it via {@link #setHelper(String, String)}
	 */
	public void clear() {
		this.jarName = null;
		this.interfaceName = null;
		this.jarClassLoader = null;
	}
	
	/**
	 * Set the mandatory data for this instance.
	 * 
	 * Clear it via {@link #clear()}
	 * @param jarName the file path (relative to CWD) of the jar file to test
	 * @param interfaceName the symbolic name of the interface we're looking for inside <tt>jarName</tt>. For example "it.unibs.ieeesb.arnaldotester.Greeting"
	 * @throws MalformedURLException if something goes wrong
	 */
	public void setHelper(String jarName, String interfaceName) throws MalformedURLException {
		this.jarName = jarName;
		this.jarClassLoader = this.getJarFileClassLoader(this.jarName);
		this.interfaceName = interfaceName;
	}
	
	/**
	 * Check if {@link #setHelper(String, String)} functions was correct or not
	 * @return true if everything is good; false otherwise
	 */
	public boolean checkHelper() {
		Object retVal = null;
		try {
			retVal = this.getInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			return false;
		}
		return retVal != null;
	}
	
	public Object getInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return this.findInterfaceInJar(this.jarClassLoader.loadClass(this.interfaceName)).newInstance();
	}
	
	private ClassLoader getJarFileClassLoader(String jarFile) throws MalformedURLException {
		File f = new File(jarFile);
		return new URLClassLoader(new URL[]{f.toURI().toURL()}, this.getClass().getClassLoader());
	}
	
	private <CLAZZ> Class<CLAZZ> findInterfaceInJar(Class<CLAZZ> interfaceToFind) throws ClassNotFoundException {
		ClassFinder finder = new ClassFinder();
		finder.addClassPath();
		finder.add(new File(this.jarName));

		ClassFilter filter =
				new AndClassFilter(
						// Must not be an interface
						new NotClassFilter (new InterfaceOnlyClassFilter()),
						// Must implement the ClassFilter interface
						new SubclassClassFilter(interfaceToFind),
						// Must not be abstract
						new NotClassFilter (new AbstractClassFilter())
						);

		Collection<ClassInfo> foundClasses = new ArrayList<ClassInfo>();
		finder.findClasses (foundClasses, filter);

		LOGGER.info("We have found {} classes implementing the interface {}", foundClasses.size(), interfaceToFind.getName());
		if (foundClasses.size() == 0) {
			LOGGER.error("no classes named {} found inside jar {}", interfaceToFind.getName(), this.jarName);
		}
		if (foundClasses.size() > 1) {
			LOGGER.error("too many classes named {} found inside the jar {}!", interfaceToFind.getName(), this.jarName);
		}
		
		String className = foundClasses.iterator().next().getClassName();
		LOGGER.info("The class implementing the interface {} is {}", interfaceToFind.getName(), className);
		return (Class<CLAZZ>) this.jarClassLoader.loadClass(className);
	}

}
