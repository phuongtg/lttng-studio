package org.lttng.studio.tests.sock;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.junit.Test;
import org.lttng.studio.model.Inet4Sock;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.state.StatedumpInetSockEventHandler;
import org.lttng.studio.state.TraceEventHandlerSock;
import org.lttng.studio.tests.basic.TestTraceset;

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
		reader.register(new TraceEventHandlerSock());
		reader.process();
		SystemModel model = (SystemModel) ModelRegistry.getInstance().getModel(reader, SystemModel.class);
		System.out.println(model.getInetSocks());
		Collection<Inet4Sock> socks = model.getInetSocks();
		Inet4Sock sock1 = null, sock2 = null;
		boolean found = false;
		// TODO: needs hashmap lookup for scalability
		for (Inet4Sock s1: socks) {
			for (Inet4Sock s2: socks) {
				if (s1.isComplement(s2)) {
					found = true;
					sock1 = s1;
					sock2 = s2;
					break;
				}
			}
		}
		System.out.println(sock1);
		System.out.println(sock2);
		assertTrue(found);
	}

}
