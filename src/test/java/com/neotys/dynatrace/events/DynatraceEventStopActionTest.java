package com.neotys.dynatrace.events;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DynatraceEventStopActionTest {
	@Test
	public void shouldReturnType() {
		final DynatraceEventAction action = new DynatraceEventAction();
		assertEquals("DynatraceEvent", action.getType());
	}

}
