package com.tag.upnp;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.registry.RegistrationException;

import com.tag.network.UDPServer;
import com.tag.network.UDPServer.PacketReceivedListener;
import com.tag.network.UpnpServiceBroadcast;

public class TouchpadServer implements PacketReceivedListener {

	private static final Logger log = Logger.getLogger(TouchpadServer.class
			.getName());

	public static final int STRING_EVENT = 1;

	private Robot robot;
	private KeyStroke keyStroke;
	private UDPServer server;
	private TrayIcon trayIcon;

	public TouchpadServer() throws AWTException, IOException {
		this.robot = new Robot();
		this.keyStroke = new KeyStroke(robot);

		server = new UDPServer(UDPServer.DEFAULT_PORT) {

			@Override
			protected boolean exceptionOccurred(IOException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
				return false;
			}

		};
		server.setPacketReceivedListener(this);
		Thread thread = new Thread(server);
		thread.setDaemon(true);
		thread.start();

		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					new UpnpServiceBroadcast();
				} catch (RegistrationException e) {
					log.log(Level.WARNING, e.getMessage(), e);
				} catch (LocalServiceBindingException e) {
					log.log(Level.WARNING, e.getMessage(), e);
				} catch (ValidationException e) {
					log.log(Level.WARNING, e.getMessage(), e);
				} catch (IOException e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				}
			}

		});
		thread.setDaemon(true);
		thread.start();

		createTrayIcon();
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
					log.log(Level.WARNING, "default", key);
			}
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
	}

	private void handleMouseEvent(int event, DataInputStream in)
			throws IOException {
		int mod = in.readInt(), button = in.readInt();

		boolean isPressed = (event & MouseEvent.MOUSE_PRESSED) == MouseEvent.MOUSE_PRESSED;
		boolean isShiftDown = (mod & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK;
		boolean isCtrlDown = (mod & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK;
		boolean isAltDown = (mod & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK;

		int mask = getButtonMask(button);

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

	private static int getButtonMask(int button) {
		switch (button) {
			case MouseEvent.BUTTON1:
				return InputEvent.BUTTON1_MASK;
			case MouseEvent.BUTTON2:
				return InputEvent.BUTTON2_MASK;
			case MouseEvent.BUTTON3:
				return InputEvent.BUTTON3_MASK;
		}
		throw new IllegalArgumentException("invalid button " + button);
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
			keyStroke.typeString(keys);
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

	private void createTrayIcon() throws IOException, AWTException {
		if (!SystemTray.isSupported()) {
			log.log(Level.WARNING, "system tray is not supported");
			return;
		}

		final SystemTray tray = SystemTray.getSystemTray();
		URL url = getClass().getResource("touchpad16.png");
		Image image = ImageIO.read(url);

		PopupMenu popup = new PopupMenu();
		MenuItem exitItem = new MenuItem("Exit");
		popup.add(exitItem);

		trayIcon = new TrayIcon(image, "Touchpad", popup);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(new MouseAdapter() {
		});
		tray.add(trayIcon);

		exitItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				System.exit(0);
			}

		});

		log.log(Level.INFO, "tray icon added to system tray");
	}

	public static void main(String[] args) {
		try {
			new TouchpadServer();
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}