package org.lttng.studio.test.latency;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.lttng.studio.latency.model.LatencyEventHandler;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.state.StatedumpEventHandler;
import org.lttng.studio.state.TraceEventHandlerSched;
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
		TraceEventHandlerSched h1 = new TraceEventHandlerSched();
		LatencyEventHandler h2 = new LatencyEventHandler();
		h2.monitorEvent("heartbeat:msg");
		reader.register(h1);
		reader.register(h2);
		reader.process();
		assertEquals(2, h2.getStatsTable().size());
		h2.printLatencyTable();
		h2.printStatsTable();
		h2.printHighLatency();
	}

}
