package com.neotys.dynatrace.monitoring;

/**
 * Created by anouvel on 14/12/2017.
 */
class DynatraceStatException extends Exception
{
	//Parameterless Constructor
	public DynatraceStatException() {}

	//Constructor that accepts a message
	public DynatraceStatException(String message)
	{
		super(message);
	}
}
