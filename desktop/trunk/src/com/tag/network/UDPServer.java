package com.tag.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer implements Runnable {

	public static final int DEFAULT_PORT = 20011;

	private DatagramSocket socket;
	private PacketReceivedListener listener;

	public UDPServer() throws SocketException {
		this(DEFAULT_PORT);
	}

	public UDPServer(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}

	public void setPort(int port) throws SocketException {
		if (socket.isClosed())
			throw new IllegalStateException();
		this.socket = new DatagramSocket(port);
	}

	@Override
	public void run() {
		while (isRunning()) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				exceptionOccurred(e);
			}
			if (listener != null)
				listener.packetReceived(packet);
		}
	}

	public boolean isRunning() {
		return true;
	}

	/**
	 * 
	 * @param e
	 * @return <code>true</code> to <code>break</code>
	 */
	protected boolean exceptionOccurred(IOException e) {
		e.printStackTrace();
		return true;
	}

	public void setPacketReceivedListener(PacketReceivedListener l) {
		this.listener = l;
	}

	public static interface PacketReceivedListener {

		void packetReceived(DatagramPacket packet);

	}

}