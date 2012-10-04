package org.lttng.studio.model;

import com.google.common.net.InetAddresses;

public class InetSock {

	private long sk;
	private InetAddresses addr;

	public long getSk() {
		return sk;
	}

	public void setSk(long sk) {
		this.sk = sk;
	}

	public InetAddresses getInetAddr() {
		return addr;
	}

	public void setInetAddr(InetAddresses addr) {
		this.addr = addr;
	}

}
