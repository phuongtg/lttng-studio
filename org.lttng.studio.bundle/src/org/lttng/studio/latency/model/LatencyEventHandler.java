package org.lttng.studio.latency.model;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

public class LatencyEventHandler extends TraceEventHandlerBase {
	public long count;
	
	public LatencyEventHandler() {
		super();
		this.hooks.add(new TraceHook());
	}
	
	@Override
	public void handleInit(TraceReader reader, CTFTrace trace) {
		count = 0;
	} 

	@Override
	public void handleComplete(TraceReader reader) {
		System.out.println(count);
	}
	
	public void handle_all_event(TraceReader reader, EventDefinition event) {
		count++;
	}
}
