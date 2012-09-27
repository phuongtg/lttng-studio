package org.lttng.studio.test.latency;

import java.io.File;

import org.junit.Test;
import org.lttng.studio.latency.model.LatencyEventHandler;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.state.StatedumpEventHandler;
import org.lttng.studio.state.TraceEventHandlerProcess;
import org.lttng.studio.tests.basic.TestTraceset;

public class TestLatency {

	@Test
	public void testLatencyEventRequest() throws Exception {
		File kernel = TestTraceset.getKernelTrace("wk-heartbeat-k-u");
		File heartbeat = TestTraceset.getUSTTrace("wk-heartbeat-k-u");
		TraceReader reader = new TraceReader();
		reader.addTrace(kernel);
		reader.addTrace(heartbeat);
		
		// Phase 1: build initial state
		StatedumpEventHandler h0 = new StatedumpEventHandler();
		reader.register(h0);
		reader.process();
		reader.clearHandlers();

		// Phase 2: update current state
		TraceEventHandlerProcess h1 = new TraceEventHandlerProcess();
		LatencyEventHandler h2 = new LatencyEventHandler();		
		reader.register(h1);
		reader.register(h2);
		reader.process();
	}

}
