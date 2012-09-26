package org.lttng.studio.state;

import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.model.Task;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

/*
 * Provides the current task running on a CPU according to scheduling events
 */

public class TraceEventHandlerProcess extends TraceEventHandlerBase {

	SystemModel system;
	private int schedSwitchUnkownTask;

	public TraceEventHandlerProcess() {
		super();
		hooks.add(new TraceHook("sched_switch"));
		hooks.add(new TraceHook("sched_process_fork"));
	}

	@Override
	public void handleInit(TraceReader reader, CTFTrace trace) {
		try {
			system = (SystemModel) ModelRegistry.getInstance().getOrCreateModel(reader, SystemModel.class);
		} catch (Exception e) {
			reader.cancel(e);
		}
		system.init(reader);
		schedSwitchUnkownTask = 0;
	}

	public void handle_sched_switch(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		IntegerDefinition next = (IntegerDefinition) def.get("_next_tid");
		system.setCurrentTid(cpu, next.getValue());
		Task task = system.getTask(next.getValue());
		if (task == null)
			schedSwitchUnkownTask++;
	}

	public void handle_sched_process_fork(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		IntegerDefinition parent = (IntegerDefinition) def.get("_parent_tid");
		IntegerDefinition child = (IntegerDefinition) def.get("_child_tid");
		ArrayDefinition name = (ArrayDefinition) def.get("_child_comm");
		Task task = new Task();
		task.setName(name.toString());
		task.setPid(parent.getValue());
		task.setPpid(parent.getValue());
		task.setTid(child.getValue());
		system.putTask(task);
	}

	@Override
	public void handleComplete(TraceReader reader) {
	}

	public int getSchedSwitchUnkownTask() {
		return schedSwitchUnkownTask;
	}

}
