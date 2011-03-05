package com.tag.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer extends Thread {

	public static final int DEFAULT_PORT = 20011;

	private DatagramSocket socket;
	PacketReceivedListener listener;

	private Thread thread;
	private boolean running;
	
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

	public void start() {
		if (thread != null)
			throw new IllegalStateException("server already started");
		this.thread = new Thread(this);
		thread.setDaemon(false);
		thread.start();
	}

	@Override
	public void run() {
		while (!running) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
				if (listener != null)
					listener.packetReceived(packet);
			} catch (IOException e) {
				caughtIOException(e);
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void exit() {
		this.running = true;
	}

	protected void caughtIOException(IOException e) {
		e.printStackTrace();
	}

	public void setPacketReceivedListener(PacketReceivedListener l) {
		this.listener = l;
	}

	public static interface PacketReceivedListener {

		void packetReceived(DatagramPacket packet);

	}

}