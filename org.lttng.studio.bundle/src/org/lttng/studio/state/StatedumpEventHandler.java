package org.lttng.studio.state;

import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.model.Task;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

/*
 * Populate initial state of the system with statedump
 */

public class StatedumpEventHandler extends TraceEventHandlerBase {

	private SystemModel system;

	public StatedumpEventHandler() {
		super();
		this.hooks.add(new TraceHook("lttng_statedump_start"));
		this.hooks.add(new TraceHook("lttng_statedump_end"));
		this.hooks.add(new TraceHook("lttng_statedump_file_descriptor"));
		this.hooks.add(new TraceHook("lttng_statedump_process_state"));
	}

	@Override
	public void handleInit(TraceReader reader, CTFTrace trace) {
		try {
			system = (SystemModel) ModelRegistry.getInstance().getOrCreateModel(reader, SystemModel.class);
		} catch (Exception e) {
			reader.cancel(e);
		}
	}

	@Override
	public void handleComplete(TraceReader reader) {
	}

	public void handle_lttng_statedump_start(TraceReader reader, EventDefinition event) {
	}

	public void handle_lttng_statedump_end(TraceReader reader, EventDefinition event) {
		reader.cancel();
	}

	public void handle_lttng_statedump_file_descriptor(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		IntegerDefinition pid = (IntegerDefinition) def.get("_pid");
		StringDefinition filename = (StringDefinition) def.get("_filename");
		IntegerDefinition fd = (IntegerDefinition) def.get("_fd");

		//system.addFileDescriptor(pid.getValue(), fd.getValue(), filename.getValue());
		//System.out.println(String.format("%d %s", pid.getValue(), filename.getValue()));
	}

	public void handle_lttng_statedump_process_state(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		IntegerDefinition pid = (IntegerDefinition) def.get("_pid");
		IntegerDefinition tid = (IntegerDefinition) def.get("_tid");
		IntegerDefinition ppid = (IntegerDefinition) def.get("_ppid");
		IntegerDefinition type = (IntegerDefinition) def.get("_type");
		IntegerDefinition mode = (IntegerDefinition) def.get("_mode");
		IntegerDefinition submode = (IntegerDefinition) def.get("_submode");
		IntegerDefinition status = (IntegerDefinition) def.get("_status");
		ArrayDefinition name = (ArrayDefinition) def.get("_name");

		Task task = new Task(tid.getValue());
		system.putTask(task);
		task.setStart(event.getTimestamp());
		task.setPid(pid.getValue());
		task.setPpid(ppid.getValue());
		task.setExecution_mode(mode.getValue());
		task.setExecution_submode(submode.getValue());
		task.setProcess_status(status.getValue());
		task.setThread_type(type.getValue());
		task.setName(name.toString());
	}

	public SystemModel getSystem() {
		return system;
	}

	public void setSystem(SystemModel system) {
		this.system = system;
	}
}
