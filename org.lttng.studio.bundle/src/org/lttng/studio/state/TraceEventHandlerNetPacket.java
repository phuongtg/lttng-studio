package org.lttng.studio.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.net.ui.Actor;
import org.lttng.studio.net.ui.Message;
import org.lttng.studio.reader.TraceEventHandlerBase;
import org.lttng.studio.reader.TraceHook;
import org.lttng.studio.reader.TraceReader;

public class TraceEventHandlerNetPacket extends TraceEventHandlerBase {

	enum Type { SEND, RECV };

	class Event {
		public long ts;
		public long sk;
		public int seq;
		public Type type;
		public Event(long ts, long sk, int seq, Type type) {
			this.ts = ts;
			this.sk = sk;
			this.seq = seq;
			this.type = type;
		}
	};

	// FIXME: seq is not unique, should add port?

	ArrayList<Message> msgs;
	HashMap<Integer, Event> match; // (seq, ev)
	HashMap<Long, Actor> actorMap;
	private SystemModel system;

	public TraceEventHandlerNetPacket() {
		super();
		this.hooks.add(new TraceHook("inet_sock_local_in"));
		this.hooks.add(new TraceHook("inet_sock_local_out"));
	}

	@Override
	public void handleInit(TraceReader reader) {
		try {
			system = (SystemModel) ModelRegistry.getInstance().getOrCreateModel(reader, SystemModel.class);
		} catch (Exception e) {
			reader.cancel(e);
		}
		system.init(reader);
		msgs = new ArrayList<Message>();
		match = new HashMap<Integer, TraceEventHandlerNetPacket.Event>();
		actorMap = new HashMap<Long, Actor>();
	}

	@Override
	public void handleComplete(TraceReader reader) {
		/*
		System.out.println("Sockets:");
		for (Long sk: actorMap.keySet()) {
			System.out.println(Long.toHexString(sk));
		}
		System.out.println("Messages:");
		for (Message msg: msgs) {
			System.out.println(msg);
		}
		*/
	}

	public Event makeEvent(EventDefinition event, Type type) {
		HashMap<String, Definition> def = event.getFields().getDefinitions();
		long sk = ((IntegerDefinition) def.get("_sk")).getValue();
		int seq = (int) ((IntegerDefinition) def.get("_seq")).getValue();
		return new Event(event.getTimestamp(), sk, seq, type);
	}

	private Actor getOrCreateActor(long sk) {
		Actor actor = actorMap.get(sk);
		if (actor == null) {
			actor = new Actor(Long.toHexString(sk), sk);
			actorMap.put(sk, actor);
		}
		return actor;
	}

	public void handle_inet_sock_local_in(TraceReader reader, EventDefinition event) {
		Event recv = makeEvent(event, Type.RECV);
		if (recv.sk == 0)
			return;

		Event send = match.remove(recv.seq);
		if (send == null)
			return;
		//System.out.println("RECV " + recv.sk + " " + recv.seq);

		assert(send.seq == recv.seq);
		assert(send.type == Type.SEND);
		assert(recv.type == Type.RECV);
		Actor sender = getOrCreateActor(send.sk);
		Actor receiver = getOrCreateActor(recv.sk);
		msgs.add(new Message(sender, send.ts, receiver, recv.ts));
	}

	public void handle_inet_sock_local_out(TraceReader reader, EventDefinition event) {
		Event send = makeEvent(event, Type.SEND);
		//System.out.println("SEND " + send.sk + " " + send.seq);
		match.put(send.seq, send);
	}

	public ArrayList<Message> getMessages() {
		return msgs;
	}

	public Collection<Actor> getActors() {
		return actorMap.values();
	}

}
