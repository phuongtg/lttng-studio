package org.lttng.studio.model;

import org.lttng.studio.reader.TraceReader;

public interface ITraceModel {

	public void reset();
	public void init(TraceReader reader);

}
