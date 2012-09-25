package org.lttng.studio.state;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.Test;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.model.Task;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.tests.basic.TestTraceset;

public class TestState {

	@Test
	public void testStatedump() throws IOException, CTFReaderException {
		File traceDir = TestTraceset.getKernelTrace("burnP6-1x-1sec-k");
		TraceReader reader = new TraceReader(traceDir);
		StatedumpEventHandler handler = new StatedumpEventHandler();
		reader.register(handler);
		reader.process();
		SystemModel system = handler.getSystem();
		HashMap<Long, Task> tasks = system.getTasks();
		System.out.println(tasks);
	}
	
}
