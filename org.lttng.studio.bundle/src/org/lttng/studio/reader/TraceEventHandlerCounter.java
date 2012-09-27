package org.lttng.studio.reader;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.lttng.studio.model.EventCounter;
import org.lttng.studio.model.ModelRegistry;

public class TraceEventHandlerCounter extends TraceEventHandlerBase {

	EventCounter counter;

	public TraceEventHandlerCounter(Integer priority) {
		super(priority);
		hooks.add(new TraceHook());
	}

	public TraceEventHandlerCounter() {
		this(0);
	}

	@Override
	public void handleInit(TraceReader reader) {
		try {
			counter = (EventCounter) ModelRegistry.getInstance().getOrCreateModel(reader, EventCounter.class);
		} catch (Exception e) {
			reader.cancel(e);
		}
		counter.reset();
	}

	public void handle_all_event(TraceReader reader, EventDefinition event) {
		counter.increment();
	}

	@Override
	public void handleComplete(TraceReader reader) {
	}

	public long getCounter() {
		return counter.getCounter();
	}
}
