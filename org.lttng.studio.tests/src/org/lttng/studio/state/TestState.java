package org.lttng.studio.state;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.lttng.studio.model.FD;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.model.Task;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.tests.basic.TestTraceset;

public class TestState {

	@Test
	public void testStatedumpTask() throws Exception {
		File traceDir = TestTraceset.getKernelTrace("burnP6-1x-1sec-k");
		TraceReader reader = new TraceReader(traceDir);
		StatedumpEventHandler handler = new StatedumpEventHandler();
		reader.register(handler);
		reader.process();
		SystemModel system = (SystemModel) ModelRegistry.getInstance().getModel(reader, SystemModel.class);
		Collection<Task> tasks = system.getTasks();
		assertTrue(tasks.size() > 0);
	}

	@Test
	public void testStatedumpFDs() throws Exception {
		File traceDir = TestTraceset.getKernelTrace("burnP6-1x-1sec-k");
		TraceReader reader = new TraceReader(traceDir);
		StatedumpEventHandler handler = new StatedumpEventHandler();
		reader.register(handler);
		reader.process();
		SystemModel system = (SystemModel) ModelRegistry.getInstance().getModel(reader, SystemModel.class);
		Set<FD> fds = system.getFDs();
		assertTrue(fds.size() > 0);
	}

	@Test
	public void testRetrieveCurrentTask() throws Exception {
		File traceDir = TestTraceset.getKernelTrace("burnP6-1x-1sec-k");
		TraceReader reader = new TraceReader(traceDir);
		TraceEventHandlerProcess handler = new TraceEventHandlerProcess();
		reader.register(handler);
		reader.process();
		SystemModel model = (SystemModel) ModelRegistry.getInstance().getModel(reader, SystemModel.class);
		assertTrue(model.getCurrentTid(0) >= 0);
	}

}
