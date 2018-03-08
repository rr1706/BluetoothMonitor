package com.frc1706.scouting.bluetooth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;

/**
 * Hello world!
 *
 */
public class App {

	private static Object lock = new Object();
	private static Vector<RemoteDevice> vecDevices = new Vector();
	private static String connectionURL = null;
	private static Map<String, DeviceWatcher> deviceWatchers = new HashMap<String, DeviceWatcher>();
	private static DiscoveryAgent agent = null;
	static final UUID uuid = new UUID("0000107300001000800000805F9B34F7", false);
	public static String eventID = "2018-STL";

	public static void main(String[] args) {
		System.out.println("Starting...");
		if (args.length > 0) {
			eventID = args[0];
		}
		App app = new App();
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			System.out.println("Got Local Device " + localDevice.getFriendlyName());
			agent = localDevice.getDiscoveryAgent();
			System.out.println("Got discovery agent: " + agent.toString() + ", starting discovery...");

			while (true) {
				RemoteDevice[] preknownDevices = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);
				if (preknownDevices != null && preknownDevices.length > 0) {
					for (RemoteDevice dev : preknownDevices) {
						if (!vecDevices.contains(dev)) {
							vecDevices.add(dev);
							DeviceWatcher watcher = new DeviceWatcher(dev, agent);
							watcher.start();
							deviceWatchers.put(dev.getBluetoothAddress(), watcher);
							System.out.println(
									"Found: " + dev.getBluetoothAddress() + " (" + dev.getFriendlyName(false) + ")");

						}
					}
				}
				int deviceCount = vecDevices.size();

				if (deviceCount <= 0) {
					System.out.println("No Devices Found .");
					System.exit(0);
				}

				try {
					Thread.sleep(30000);
				} catch (InterruptedException u) {
				}
			}

		} catch (BluetoothStateException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
