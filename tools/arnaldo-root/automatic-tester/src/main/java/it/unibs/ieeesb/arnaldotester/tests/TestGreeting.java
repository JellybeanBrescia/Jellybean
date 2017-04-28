package it.unibs.ieeesb.arnaldotester.tests;

import org.junit.Test;

import it.unibs.ieeesb.arnaldoproject.Greeting;
import it.unibs.ieeesb.arnaldotester.TestHelper;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class TestGreeting {
	
	private Greeting g;

	@Before
	public void setup() throws Exception {
		//MANDATORY: be sure to call this line to fetch an instance of the implementation
		this.g = (Greeting) TestHelper.instance().getInstance();
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void test() {
		assertThat(5, is(5));
	}
	
	@Test
	public void test2() {
		assertThat(this.g.getGreeting(), is("Hello world!"));
	}
	
	@Test
	public void testFailing() {
		assertThat(5, is(3));
	}
}
