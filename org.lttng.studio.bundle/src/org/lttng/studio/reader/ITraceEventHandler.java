package org.lttng.studio.reader;

import java.util.Set;

import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;

public interface ITraceEventHandler extends Comparable<ITraceEventHandler> {
	
	public Set<TraceHook> getHooks();
	
	public void handleInit(TraceReader reader);
	
	public void handleComplete(TraceReader reader);

	public Integer getPriority();
}
