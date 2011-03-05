package com.tag.upnp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.annotations.UpnpStateVariable;

import com.tag.network.UDPServer;

@UpnpService(serviceId = @UpnpServiceId("Touchpad"), serviceType = @UpnpServiceType(value = "Touchpad", version = 1))
public class TouchpadService {

	@UpnpStateVariable(defaultValue = "0")
	private boolean status = false;

	@UpnpStateVariable(defaultValue = "username")
	private String username;

	@UpnpStateVariable(defaultValue = "localhost")
	private String hostName;

	@UpnpStateVariable(defaultValue = "localhost")
	private String hostAddress;

	@UpnpStateVariable(defaultValue = "20011")
	private int port;

	{
		String username = System.getProperty("user.name");
		setUsername(username);
		try {
			InetAddress localHost = InetAddress.getLocalHost();

			String name = localHost.getHostName();
			setHostName(name);

			String address = localHost.getHostAddress();
			setHostAddress(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		setPort(UDPServer.DEFAULT_PORT);
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
	public boolean getStatus() {
		return status;
	}

	private void setUsername(String username) {
		this.username = username;
	}

	private void setHostName(String hostName) {
		this.hostName = hostName;
	}

	private void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	private void setPort(int port) {
		if (port < 0 || port > 0xFFFF)
			throw new IllegalArgumentException("port out of range: " + port);
		this.port = port;
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "Username"))
	public String getUsername() {
		return this.username;
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "HostName"))
	public String getHostName() {
		return this.hostName;
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "HostAddress"))
	public String getHostAddress() {
		return this.hostAddress;
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "Port"))
	public int getPort() {
		return this.port;
	}

}