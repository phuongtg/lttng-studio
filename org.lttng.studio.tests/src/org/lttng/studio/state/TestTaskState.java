package org.lttng.studio.state;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.model.task.Task;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.tests.basic.TestTraceset;

public class TestTaskState {

	@Test
	public void testHandleState() throws Exception {
		File traceDir = TestTraceset.getKernelTrace("burnP6-1x-1sec-k");
		TraceReader reader = new TraceReader();
		reader.addTrace(traceDir);

		SystemModel model = (SystemModel) ModelRegistry.getInstance().getOrCreateModel(reader, SystemModel.class);
		// Phase 1: build initial state
		StatedumpEventHandler h0 = new StatedumpEventHandler();
		reader.register(h0);
		reader.process();
		reader.clearHandlers();

		// Phase 2: update current state
		TraceEventHandlerSched h1 = new TraceEventHandlerSched();
		reader.register(h1);
		reader.process();

		Collection<Task> tasks = model.getTasks();
		HashSet<Task> burn = new HashSet<Task>();
		for (Task task: tasks) {
			if (task.getName().endsWith("burnP6"))
				System.out.println(task);
		}
	}

}
