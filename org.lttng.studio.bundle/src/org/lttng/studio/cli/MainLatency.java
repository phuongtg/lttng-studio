package org.lttng.studio.cli;

import java.io.File;

import org.lttng.studio.latency.model.LatencyEventHandler;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.state.StatedumpEventHandler;
import org.lttng.studio.state.TraceEventHandlerProcess;

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

		/*
		List<CTFTraceReader> ctfTraceReaders = reader.getCTFTraceReaders();
		for (CTFTraceReader r: ctfTraceReaders) {
			CTFClock clock = r.getTrace().getClock();

			Field field;
			HashMap<String, Object> v = null;
			try {
				field = clock.getClass().getDeclaredField("properties");
				field.setAccessible(true);
				v = (HashMap<String, Object>) field.get(clock);
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println(v);
		}
		return;
		*/

		// Phase 1: build initial state
		StatedumpEventHandler h0 = new StatedumpEventHandler();
		reader.register(h0);
		reader.process();
		reader.clearHandlers();

		// Phase 2: update current state
		TraceEventHandlerProcess h1 = new TraceEventHandlerProcess();
		LatencyEventHandler h2 = new LatencyEventHandler();
		h2.monitorEvent("npt:loop");
		reader.register(h1);
		reader.register(h2);
		reader.process();

		h2.printLatencyTable();
		h2.printStatsTable();
		h2.printHighLatency();
	}

}
