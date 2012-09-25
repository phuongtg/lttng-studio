package org.lttng.studio.reader;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;

public class TraceEventHandlerCounter extends TraceEventHandlerBase {

	public int count;

	public TraceEventHandlerCounter(Integer priority) {
		super(priority);
		hooks.add(new TraceHook());
	}
	
	public TraceEventHandlerCounter() {
		this(0);
	}
	
	@Override
	public void handleInit(TraceReader reader, CTFTrace trace) {
		count = 0;
	}
	
	public void handle_all_event(TraceReader reader, EventDefinition event) {
		count++;
	}

	@Override
	public void handleComplete(TraceReader reader) {
	}
	
	public int getCount() {
		return count;
	}
}
