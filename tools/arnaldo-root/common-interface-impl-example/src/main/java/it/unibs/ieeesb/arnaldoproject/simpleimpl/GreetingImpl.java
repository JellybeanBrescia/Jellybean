package it.unibs.ieeesb.arnaldoproject.simpleimpl;

import java.text.MessageFormat;

import it.unibs.ieeesb.arnaldoproject.Greeting;

public class GreetingImpl implements Greeting{

	public String getGreeting() {
		return "Hello world!";
	}

	public String getGreeting(String name) {
		return MessageFormat.format("Hello {}!", name);
	}

}
