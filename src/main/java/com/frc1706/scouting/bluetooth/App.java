package com.frc1706.scouting.bluetooth;

import java.awt.EventQueue;
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
 * Bluetooth Device Monitor.
 *
 */
public class App {

	private static Vector<RemoteDevice> vecDevices = new Vector<RemoteDevice>();
	private static Map<String, DeviceWatcher> deviceWatchers = new HashMap<String, DeviceWatcher>();
	private static DiscoveryAgent agent = null;
	static final UUID uuid = new UUID("0000107300001000800000805F9B34F7", false);
	public static String eventID = "2018-STL";
	static AppWindow window;

	public static void main(String[] args) {
		System.out.println("Starting...");
		if (args.length > 0) {
			eventID = args[0];
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new AppWindow();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			agent = localDevice.getDiscoveryAgent();
			System.out.println("Got discovery agent: " + agent.toString() + ", starting discovery...");

			while (true) {
				RemoteDevice[] preknownDevices = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);
				if (preknownDevices != null && preknownDevices.length > 0) {
					for (RemoteDevice dev : preknownDevices) {
						if (!vecDevices.contains(dev)) {
							vecDevices.add(dev);
							final DeviceWatcher watcher = new DeviceWatcher(dev, agent);
							watcher.start();
							deviceWatchers.put(dev.getBluetoothAddress(), watcher);
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									try {
										window.addDeviceWatcher(watcher);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});

							try {
								Thread.sleep(15000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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
					Thread.sleep(10000);
				} catch (InterruptedException u) {
				}
				try {
					window.updateDeviceStatus();
				} catch (Exception e) {
				}
			}
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void sendMessageToAll(String message) {
		for (DeviceWatcher watcher : deviceWatchers.values()) {
			watcher.sendMessage(message);
		}
	}

}
