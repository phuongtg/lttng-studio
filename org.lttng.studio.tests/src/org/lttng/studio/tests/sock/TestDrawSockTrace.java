package org.lttng.studio.tests.sock;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.junit.Test;
import org.lttng.studio.model.Inet4Sock;
import org.lttng.studio.model.ModelRegistry;
import org.lttng.studio.model.SystemModel;
import org.lttng.studio.net.ui.Actor;
import org.lttng.studio.net.ui.Interval;
import org.lttng.studio.net.ui.Message;
import org.lttng.studio.net.ui.MessagePainter;
import org.lttng.studio.reader.TraceReader;
import org.lttng.studio.state.TraceEventHandlerNetPacket;
import org.lttng.studio.state.TraceEventHandlerSock;
import org.lttng.studio.tests.basic.TestTraceset;

public class TestDrawSockTrace extends ImageOutput {

	@Test
	public void testDrawClientServerMessages() throws Exception {
		File trace = TestTraceset.getKernelTrace("netcat-tcp-k");
		TraceReader reader = new TraceReader();
		reader.addReader(new CTFTraceReader(new CTFTrace(trace)));
		TraceEventHandlerSock handlerSocket = new TraceEventHandlerSock();
		TraceEventHandlerNetPacket handlerPacket = new TraceEventHandlerNetPacket();
		reader.register(handlerSocket);
		reader.register(handlerPacket);
		reader.process();

		SystemModel model = (SystemModel) ModelRegistry.getInstance().getModel(reader, SystemModel.class);

		ArrayList<Message> messages = handlerPacket.getMessages();
		Message[] msgs = new Message[messages.size()];
		messages.toArray(msgs);

		Collection<Actor> actors = handlerPacket.getActors();
		Interval window = Message.getWindow(msgs);
		for (Actor a: actors) {
			if (a.getId() == 0) {
				a.setInterval(window);
			} else {
				Inet4Sock sock = model.getInetSock(a.getId());
				a.setInterval(sock.getInterval());
				a.setLabel(sock.toString());
			}
		}

		MessagePainter painter = new MessagePainter();
		painter.setWindow(window);
		Actor[] actorsArray = new Actor[actors.size()];
		actors.toArray(actorsArray);
		painter.updateActorLines(actorsArray, imgHeight, border);
		painter.paintActorLines(gc, img);
		for (Message msg: msgs) {
			painter.paint(gc, img, msg);
		}
		saveImage(img, "test_draw_packet_netcat_tcp");
	}

}
