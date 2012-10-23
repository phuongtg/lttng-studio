package org.lttng.studio.tests.sock;

import java.io.File;

import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.junit.Test;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.state.StatedumpInetSockEventHandler;
import org.lttng.studio.tests.basic.TestTraceset;

public class TestDrawSockTrace extends ImageOutput {

	@Test
	public void testDrawClientServerMessages() throws Exception {
		File trace = TestTraceset.getKernelTrace("netcat-tcp-k");
		TraceReader reader = new TraceReader();
		reader.addReader(new CTFTraceReader(new CTFTrace(trace)));
		reader.register(new StatedumpInetSockEventHandler());
		reader.process();
		SystemModel model = (SystemModel) ModelRegistry.getInstance().getModel(reader, SystemModel.class);
		System.out.println(model.getInetSocks());
		saveImage(img, "test_draw_netcat");
	}

}
