package org.lttng.studio.model;

import java.net.InetAddress;

public class InetSock {

	private long sk;
	private InetAddress addr;

	public long getSk() {
		return sk;
	}

	public void setSk(long sk) {
		this.sk = sk;
	}

	public InetAddress getInetAddr() {
		return addr;
	}

	public void setInetAddr(InetAddress addr) {
		this.addr = addr;
	}

	@Override
	public String toString() {
		return String.format("[0x%s,%s]", Long.toHexString(sk), addr != null ? addr.toString() : null);
	}
}
