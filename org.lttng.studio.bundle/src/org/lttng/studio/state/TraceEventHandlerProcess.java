package org.lttng.studio.state;

import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

/*
 * Provides the current task running on a CPU according to scheduling events
 */

public class TraceEventHandlerProcess extends TraceEventHandlerBase {

	SystemModel system;

	public TraceEventHandlerProcess() {
		super();
		hooks.add(new TraceHook("sched_switch"));
	}

	@Override
	public void handleInit(TraceReader reader, CTFTrace trace) {
		int nbCpus = reader.getNumCpus();
		try {
			system = (SystemModel) ModelRegistry.getInstance().getOrCreateModel(reader, SystemModel.class);
		} catch (Exception e) {
			reader.cancel(e);
		}
		system.init(nbCpus);
	}

	public void handle_sched_switch(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		IntegerDefinition next = (IntegerDefinition) def.get("_next_tid");
		system.setCurrentTid(cpu, next.getValue());
	}

	@Override
	public void handleComplete(TraceReader reader) {
	}

}
