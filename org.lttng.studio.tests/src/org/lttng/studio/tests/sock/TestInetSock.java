package org.lttng.studio.tests.sock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.junit.Test;
import org.lttng.studio.model.Inet4Sock;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.model.task.Task;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.state.StatedumpInetSockEventHandler;
import org.lttng.studio.state.TraceEventHandlerSched;
import org.lttng.studio.state.TraceEventHandlerSock;
import org.lttng.studio.tests.basic.TestTraceset;

import com.google.common.collect.BiMap;

public class TestInetSock {

	@Test
	public void testInetSockStatedump() throws Exception {
		File trace = TestTraceset.getKernelTrace("netcat-tcp-k");
		TraceReader reader = new TraceReader();
		reader.addReader(new CTFTraceReader(new CTFTrace(trace)));
		reader.register(new StatedumpInetSockEventHandler());
		reader.process();
		SystemModel model = (SystemModel) ModelRegistry.getInstance().getModel(reader, SystemModel.class);
		assertTrue(model.getInetSocks().size() > 0);
	}

	@Test
	public void testInetSockSteadyState() throws Exception {
		File trace = TestTraceset.getKernelTrace("netcat-tcp-k");
		TraceReader reader = new TraceReader();
		reader.addReader(new CTFTraceReader(new CTFTrace(trace)));
		reader.register(new TraceEventHandlerSched());
		reader.register(new TraceEventHandlerSock());
		reader.process();
		SystemModel model = (SystemModel) ModelRegistry.getInstance().getModel(reader, SystemModel.class);
		BiMap<Inet4Sock, Inet4Sock> socks = model.getInetSockIndex();
		System.out.println(socks);
		assertEquals(1, socks.size());

		Inet4Sock sock1 = socks.keySet().iterator().next();
		Inet4Sock sock2 = socks.get(sock1);
		long owner1 = model.getInetSockPid(sock1);
		long owner2 = model.getInetSockPid(sock2);
		System.out.println(owner1 + " " + owner2);
		Task peer1 = model.getTask(owner1);
		Task peer2 = model.getTask(owner2);
		System.out.println(peer1);
		System.out.println(peer2);
	}

}
