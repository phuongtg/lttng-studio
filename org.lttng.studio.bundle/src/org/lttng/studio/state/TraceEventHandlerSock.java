package org.lttng.studio.state;

import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.lttng.studio.model.Inet4Sock;
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

	public void defineInet4Sock(EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		IntegerDefinition sk = (IntegerDefinition) def.get("_sk");
		IntegerDefinition saddr = (IntegerDefinition) def.get("_saddr");
		IntegerDefinition daddr = (IntegerDefinition) def.get("_daddr");
		IntegerDefinition sport = (IntegerDefinition) def.get("_sport");
		IntegerDefinition dport = (IntegerDefinition) def.get("_dport");
		Task task = system.getTaskCpu(event.getCPU());
		System.out.println(task);
		Inet4Sock sock = system.getInetSock(task.getPid(), sk.getValue());
		if (sock == null) {
			System.out.println("Huston, we missed inet_sock_create " + sk.getValue());
			sock = new Inet4Sock();
			sock.setSk(sk.getValue());
			system.addInetSock(task.getPid(), sock);
		}
		sock.setInet((int)saddr.getValue(), (int)daddr.getValue(),
				(int)sport.getValue(), (int)dport.getValue());
		system.indexInetSock(sock);
		//System.out.println(event);
		//System.out.println(sock);
	}

	public void handle_inet_connect(TraceReader reader, EventDefinition event) {
		defineInet4Sock(event);
	}

	public void handle_inet_accept(TraceReader reader, EventDefinition event) {
		defineInet4Sock(event);
	}

	public void handle_inet_sock_clone(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		Task current = system.getTaskCpu(cpu);
		IntegerDefinition osk = (IntegerDefinition) def.get("_osk");
		IntegerDefinition nsk = (IntegerDefinition) def.get("_nsk");
		Inet4Sock oldSock = system.getInetSock(current.getPid(), osk.getValue());
		Inet4Sock newSock = cloner.deepClone(oldSock);
		newSock.setSk(nsk.getValue());
		system.addInetSock(current.getPid(), newSock);
		System.out.println(event);
	}

	public void handle_inet_sock_create(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		int cpu = event.getCPU();
		Task current = system.getTaskCpu(cpu);
		Inet4Sock sock = new Inet4Sock();
		IntegerDefinition sk = (IntegerDefinition) def.get("_sk");
		sock.setSk(sk.getValue());
		system.addInetSock(current.getPid(), sock);
		System.out.println(event);
	}

	public void handle_inet_sock_delete(TraceReader reader, EventDefinition event) {
		System.out.println(event);
	}

}