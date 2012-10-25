package org.lttng.studio.state;

import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.lttng.studio.model.Inet4Sock;
import org.lttng.studio.net.ui.Actor;
import org.lttng.studio.net.ui.Message;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

import com.google.common.collect.ArrayListMultimap;

public class TraceEventHandlerNetPacket extends TraceEventHandlerBase {

	ArrayListMultimap<Long, Message> msgs;
	ArrayListMultimap<Long, EventDefinition> evHistory;
	HashMap<Long, Inet4Sock> sockMap;
	HashMap<Long, Actor> actorMap;

	// FIXME: missing TCP ACK to match send/recv

	public TraceEventHandlerNetPacket() {
		super();
		this.hooks.add(new TraceHook("inet_sock_local_in"));
		this.hooks.add(new TraceHook("inet_sock_local_out"));
	}

	@Override
	public void handleInit(TraceReader reader) {
		msgs = ArrayListMultimap.create();
		evHistory = ArrayListMultimap.create();
		sockMap = new HashMap<Long, Inet4Sock>();
		actorMap = new HashMap<Long, Actor>();
	}

	public void handle_inet_sock_local_in(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		IntegerDefinition sk = (IntegerDefinition) def.get("_sk");
		//msgs.put(sk.getValue(), new Message());
	}

	public void handle_inet_sock_local_out(TraceReader reader, EventDefinition event) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		IntegerDefinition sk = (IntegerDefinition) def.get("_sk");
		//evHistory.put(key, value);
	}

}
