package org.lttng.studio.state;

import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.lttng.studio.model.InetSock;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.model.task.Task;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

import com.rits.cloning.Cloner;

public class TraceEventHandlerSock extends TraceEventHandlerBase {

	private SystemModel system;
	Cloner cloner;

	public TraceEventHandlerSock() {
		super();
		this.hooks.add(new TraceHook("inet_connect"));
		this.hooks.add(new TraceHook("inet_accept"));
		this.hooks.add(new TraceHook("inet_sock_clone"));
		this.hooks.add(new TraceHook("inet_sock_delete"));
		this.hooks.add(new TraceHook("inet_sock_create"));
		cloner = new Cloner();
	}

	@Override
	public void handleInit(TraceReader reader) {
		try {
			system = (SystemModel) ModelRegistry.getInstance().getOrCreateModel(reader, SystemModel.class);
		} catch (Exception e) {
			reader.cancel(e);
		}
		system.init(reader);
	}

	@Override
	public void handleComplete(TraceReader reader) {

	}

	public void handle_inet_connect(TraceReader reader, EventDefinition event) {
		System.out.println(event);
	}

	public void handle_inet_accept(TraceReader reader, EventDefinition event) {
		System.out.println(event);
	}

	public void handle_inet_sock_clone(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		Task current = system.getTaskCpu(cpu);
		IntegerDefinition osk = (IntegerDefinition) def.get("_osk");
		IntegerDefinition nsk = (IntegerDefinition) def.get("_nsk");
		InetSock oldSock = system.getInetSock(current.getPid(), osk.getValue());
		InetSock newSock = cloner.deepClone(oldSock);
		newSock.setSk(nsk.getValue());
		system.addInetSock(current.getPid(), newSock);
		System.out.println(event);
	}

	public void handle_inet_sock_create(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		Task current = system.getTaskCpu(cpu);
		InetSock sock = new InetSock();
		IntegerDefinition sk = (IntegerDefinition) def.get("_sk");
		sock.setSk(sk.getValue());
		system.addInetSock(current.getPid(), sock);
	}

	public void handle_inet_sock_delete(TraceReader reader, EventDefinition event) {
		System.out.println(event);
	}

	public void handle_lttng_statedump_end(TraceReader reader, EventDefinition event) {
		reader.cancel();
	}
}