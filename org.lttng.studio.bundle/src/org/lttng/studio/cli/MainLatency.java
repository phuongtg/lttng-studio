package org.lttng.studio.cli;

import java.io.File;

import org.lttng.studio.latency.model.LatencyEventHandler;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.state.StatedumpEventHandler;
import org.lttng.studio.state.TraceEventHandlerSched;

public class MainLatency {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TraceReader reader = new TraceReader();
		for (String path: args) {
			reader.addTrace(new File(path));
		}

		// Phase 1: build initial state
		StatedumpEventHandler h0 = new StatedumpEventHandler();
		reader.register(h0);
		reader.process();
		reader.clearHandlers();

		// Phase 2: update current state
		TraceEventHandlerSched h1 = new TraceEventHandlerSched();
		LatencyEventHandler h2 = new LatencyEventHandler();
		h2.monitorEvent("npt:loop");
		reader.register(h1);
		reader.register(h2);
		reader.process();

		// Display results
		h2.printLatencyTable();
		h2.printStatsTable();
		h2.printHighLatency();
	}

}
