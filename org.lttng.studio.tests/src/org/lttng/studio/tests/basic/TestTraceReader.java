package org.lttng.studio.tests.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Test;
import org.lttng.studio.model.EventCounter;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.reader.TraceEventHandlerCounter;
import org.lttng.studio.reader.TraceReader;

/**
 * Test simple trace reader
 * @author francis
 *
 */
public class TestTraceReader {

	@Test
	public void testSimpleTraceReaderLoad() throws Exception {
		File trace = TestTraceset.getKernelTrace("burnP6-1x-1sec-k");
		TraceReader reader = new TraceReader(trace);
		TraceEventHandlerCounter handler = new TraceEventHandlerCounter();
		reader.register(handler);
		reader.process();
		EventCounter counter = (EventCounter) ModelRegistry.getInstance().getModel(reader, EventCounter.class);
		assertTrue(counter.getCounter() > 0);
	}

	@Test
	public void testGetNbCpus() throws IOException, CTFReaderException {
		File trace = TestTraceset.getKernelTrace("burnP6-1x-1sec-k");
		TraceReader reader = new TraceReader(trace);
		reader.loadTrace();
		// assume traces comes from 8 cores CPU
		assertEquals(8, reader.getNumCpus());
	}
}
