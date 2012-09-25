package org.lttng.studio.test.latency;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Test;
import org.lttng.studio.reader.TraceEventHandlerCounter;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.tests.basic.TestTraceset;

public class TestLatency {

	@Test
	public void testLatencyEventRequest() throws IOException, CTFReaderException {
		File traceDir = TestTraceset.getKernelTrace("burnP6-1x-1sec-k");
		TraceReader reader = new TraceReader(traceDir);
		TraceEventHandlerCounter handler = new TraceEventHandlerCounter();
		reader.register(handler);
		reader.process();
		System.out.println(handler.count);
	}
	
}
