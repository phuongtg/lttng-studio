package org.lttng.studio.net.ui;


public class Message {
	long sent;
	long recv;
	Actor sender;
	Actor receiver;

	public Message(Actor sender, long sent, Actor receiver, long recv) {
		this.sent = sent;
		this.recv = recv;
		this.sender = sender;
		this.receiver = receiver;
	}

	public static Interval getWindow(Message[] msgs) {
		long min = Long.MAX_VALUE;
		long max = 0L;
		for (Message msg: msgs) {
			min = Math.min(min, msg.sent);
			max = Math.max(max, msg.recv);
		}
		return new Interval(min, max);
	}
}
