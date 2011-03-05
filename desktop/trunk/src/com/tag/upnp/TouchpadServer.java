package com.tag.upnp;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.URL;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.binding.LocalServiceBinder;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;

import com.tag.network.UDPServer;
import com.tag.network.UDPServer.PacketReceivedListener;

public class TouchpadServer implements PacketReceivedListener {

	public static final int VERSION = 1;

	public static final int STRING_EVENT = 1;

	private UDPServer server;
	private Robot robot;

	public TouchpadServer() {
		try {
			server = new UDPServer(UDPServer.DEFAULT_PORT);
			server.setPacketReceivedListener(this);
			server.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		try {
			this.robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		Thread serverThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final UpnpService upnpService = new UpnpServiceImpl();
					Runtime.getRuntime().addShutdownHook(
							new Thread(new Runnable() {

								@Override
								public void run() {
									upnpService.shutdown();
								}

							}));

					// Add the bound local device to the registry
					Registry registry = upnpService.getRegistry();
					registry.addDevice(createDevice());
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}

		});
		serverThread.setDaemon(false);
		serverThread.start();
	}

	@Override
	public void packetReceived(DatagramPacket packet) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				packet.getData(), 0, packet.getLength()));
		try {
			int key = in.readInt();
			switch (key) {
				case MouseEvent.MOUSE_PRESSED:
				case MouseEvent.MOUSE_RELEASED:
					handleMouseEvent(key, in);
					break;
				case MouseEvent.MOUSE_MOVED:
					handleMouseMotionEvent(in);
					break;
				case MouseEvent.MOUSE_WHEEL:
					handleMouseWheelEvent(in);
					break;
				case KeyEvent.KEY_PRESSED:
				case KeyEvent.KEY_RELEASED:
					handleKeyEvent(key, in);
					break;
				case STRING_EVENT:
					handleStringEvent(in);
					break;
				default:
					System.err.println("default? " + key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	private void handleMouseEvent(int event, DataInputStream in)
			throws IOException {
		int mod = in.readInt(), button = in.readInt();

		boolean isPressed = (event & MouseEvent.MOUSE_PRESSED) == MouseEvent.MOUSE_PRESSED;
		boolean isShiftDown = (mod & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK;
		boolean isCtrlDown = (mod & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK;
		boolean isAltDown = (mod & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK;

		int mask;
		switch (button) {
			case MouseEvent.BUTTON1:
				mask = InputEvent.BUTTON1_MASK;
				break;
			case MouseEvent.BUTTON2:
				mask = InputEvent.BUTTON2_MASK;
				break;
			case MouseEvent.BUTTON3:
				mask = InputEvent.BUTTON3_MASK;
				break;
			default:
				return;
		}

		if (isPressed) {
			if (isShiftDown)
				robot.keyPress(KeyEvent.VK_SHIFT);
			if (isCtrlDown)
				robot.keyPress(KeyEvent.VK_CONTROL);
			if (isAltDown)
				robot.keyPress(KeyEvent.VK_ALT);
			robot.mousePress(mask);
			if (isAltDown)
				robot.keyRelease(KeyEvent.VK_ALT);
			if (isCtrlDown)
				robot.keyRelease(KeyEvent.VK_CONTROL);
			if (isShiftDown)
				robot.keyRelease(KeyEvent.VK_SHIFT);
		} else {
			robot.mouseRelease(mask);
		}
	}

	private void handleMouseMotionEvent(DataInputStream in) throws IOException {
		int x = in.readShort(), y = in.readShort();
		Point p = MouseInfo.getPointerInfo().getLocation();
		p.x += x;
		p.y += y;
		robot.mouseMove(p.x, p.y);
	}

	private void handleMouseWheelEvent(DataInputStream in) throws IOException {
		byte wheelAmount = in.readByte();
		robot.mouseWheel(wheelAmount);
	}

	private void handleStringEvent(DataInputStream in) throws IOException {
		String keys = in.readUTF();
		if (keys != null)
			KeyStroke.typeString(robot, keys);
	}

	private void handleKeyEvent(int event, DataInputStream in)
			throws IOException {
		int keyCode = in.readInt();
		switch (event) {
			case KeyEvent.KEY_PRESSED:
				robot.keyPress(keyCode);
				break;
			case KeyEvent.KEY_RELEASED:
				robot.keyRelease(keyCode);
				break;
		}
	}

	@SuppressWarnings("unchecked")
	private LocalDevice createDevice() throws ValidationException,
			LocalServiceBindingException, IOException {
		UDN udn = UDN.uniqueSystemIdentifier("TouchpadService");
		DeviceIdentity identity = new DeviceIdentity(udn);

		DeviceType type = new UDADeviceType("TouchpadService", VERSION);

		DeviceDetails deviceDetails = createDeviceDetails();

		Icon icon = createIcon();

		LocalServiceBinder binder = new AnnotationLocalServiceBinder();
		LocalService<TouchpadService> touchpadService = binder
				.read(TouchpadService.class);

		ServiceManager<TouchpadService> serviceManager = new DefaultServiceManager<TouchpadService>(
				touchpadService, TouchpadService.class);
		touchpadService.setManager(serviceManager);

		return new LocalDevice(identity, type, deviceDetails, icon,
				touchpadService);
	}

	private DeviceDetails createDeviceDetails() {
		String manufacturer = "Tag";
		ManufacturerDetails manufacturerDetails = new ManufacturerDetails(
				manufacturer);
		String modelName = "TouchpadService";
		String modelDescription = "touchpad locator service";
		String modelNumber = "v1";
		ModelDetails modelDetails = new ModelDetails(modelName,
				modelDescription, modelNumber);
		String friendlyName = "TouchpadService Locator Service";
		return new DeviceDetails(friendlyName, manufacturerDetails,
				modelDetails);
	}

	private Icon createIcon() throws IOException {
		String mimeType = "image/png";
		int width = 48, height = 48, depth = 8;
		URL url = getClass().getResource("touchpad.png");
		return new Icon(mimeType, width, height, depth, url);
	}

	public static void main(String[] args) throws Exception {
		new TouchpadServer();
	}

}