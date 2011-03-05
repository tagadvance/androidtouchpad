package com.tag.network;

import java.io.IOException;
import java.net.URL;

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
import org.teleal.cling.registry.RegistrationException;

import com.tag.upnp.TouchpadService;

public class UpnpServiceBroadcast extends UpnpServiceImpl {

	private static final String MANUFACTURER = "Tag";
	private static final String SERVICE_NAME = "TouchpadService";
	public static final int VERSION = 1;
	private static final String MODEL_NAME = SERVICE_NAME;
	private static final String MODEL_DESCRIPTION = "touchpad locator service";
	private static final String MODEL_NUMBER = "v1";
	private static final String FRIENDLY_NAME = "TouchpadService Locator Service";

	public UpnpServiceBroadcast() throws RegistrationException,
			LocalServiceBindingException, ValidationException, IOException {
		super();

		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				shutdown();
			}

		}));

		// Add the bound local device to the registry
		registry.addDevice(createDevice());
	}

	@SuppressWarnings("unchecked")
	private LocalDevice createDevice() throws ValidationException,
			LocalServiceBindingException, IOException {
		UDN udn = UDN.uniqueSystemIdentifier(SERVICE_NAME);
		DeviceIdentity identity = new DeviceIdentity(udn);

		DeviceType type = new UDADeviceType(SERVICE_NAME, VERSION);

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
		ManufacturerDetails manufacturerDetails = new ManufacturerDetails(
				MANUFACTURER);
		ModelDetails modelDetails = new ModelDetails(MODEL_NAME,
				MODEL_DESCRIPTION, MODEL_NUMBER);
		return new DeviceDetails(FRIENDLY_NAME, manufacturerDetails,
				modelDetails);
	}

	private Icon createIcon() throws IOException {
		String mimeType = "image/png";
		int width = 48, height = 48, depth = 32;
		URL url = getClass().getResource("touchpad48.png");
		return new Icon(mimeType, width, height, depth, url);
	}

}