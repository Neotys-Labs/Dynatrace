package com.neotys.dynatrace.DynatraceEvents;

/**
 * Created by anouvel on 14/12/2017.
 */
class DynatraceException extends Exception {
	public DynatraceException() {
	}

	//Constructor that accepts a message
	DynatraceException(final String message) {
		super(message);
	}
}
