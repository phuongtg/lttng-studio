package org.lttng.studio.state;

import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.lttng.studio.model.FD;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.model.Task;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

public class TraceEventHandlerFD extends TraceEventHandlerBase {

	/* Keep tmp info until corresponding sys_exit */
	public enum EventType { SYS_CLOSE, SYS_OPEN }
	public class EventData {
		public EventType type;
		public String name;
		public long fd;
	}

	SystemModel system;

	HashMap<Long, EventData> evHistory;

	public TraceEventHandlerFD() {
		super();
		hooks.add(new TraceHook("sys_open"));
		hooks.add(new TraceHook("sys_close"));
		hooks.add(new TraceHook("sys_exit"));
	}

	@Override
	public void handleInit(TraceReader reader, CTFTrace trace) {
		try {
			system = (SystemModel) ModelRegistry.getInstance().getOrCreateModel(reader, SystemModel.class);
		} catch (Exception e) {
			reader.cancel(e);
		}
		system.init(reader);
		evHistory = new HashMap<Long, TraceEventHandlerFD.EventData>();
	}

	public void handle_sys_open(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		Task task = system.getTaskCpu(cpu);
		if (task == null)
			return;
		EventData ev = new EventData();
		ev.name = ((StringDefinition) def.get("_filename")).toString();
		ev.type = EventType.SYS_OPEN;
		evHistory.put(task.getPid(), ev);
	}

	public void handle_sys_close(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		system.getTaskCpu(cpu);
		Task task = system.getTaskCpu(cpu);
		if (task == null)
			return;
		EventData ev = new EventData();
		ev.fd = ((IntegerDefinition) def.get("_fd")).getValue();
		ev.type = EventType.SYS_CLOSE;
		evHistory.put(task.getPid(), ev);
	}

	public void handle_sys_exit(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		system.getTaskCpu(cpu);
		Task task = system.getTaskCpu(cpu);
		if (task == null)
			return;
		long ret = ((IntegerDefinition)def.get("_ret")).getValue();
		EventData ev = evHistory.remove(task.getPid());
		if (ev == null)
			return;
		switch (ev.type) {
		case SYS_CLOSE:
			if (ret == 0) {
				system.removeFD(task.getPid(), ev.fd);
				System.out.println(String.format("removeFD %d %d", task.getPid(), ev.fd));
			}
			break;
		case SYS_OPEN:
			if (ret >= 0) {
				system.addFD(task.getPid(), new FD(ret, ev.name));
				System.out.println(String.format("addFD    %d %d", task.getPid(), ret));
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void handleComplete(TraceReader reader) {
	}


}
