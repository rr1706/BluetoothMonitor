package com.frc1706.scouting.bluetooth;

import java.awt.*;
import java.io.IOException;
import java.util.*;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.swing.*;

/**
 * Bluetooth Device Monitor.
 *
 */
public class App {

	private static Vector<RemoteDevice> vecDevices = new Vector<RemoteDevice>();
	private static Map<String, DeviceWatcher> deviceWatchers = new HashMap<String, DeviceWatcher>();
	private static DiscoveryAgent agent = null;
	static final UUID uuid = new UUID("0000107300001000800000805F9B34F7", false);
	public static String eventID = "2019-STL";
	static AppWindow window;
	public static JTextField baseDirField;
	public static JTextField eventNameField;

	private static class Searcher extends Thread {
		@Override
		public void run() {
			try {
				LocalDevice localDevice = LocalDevice.getLocalDevice();
				agent = localDevice.getDiscoveryAgent();
				System.out.println("Got discovery agent: " + agent.toString() + ", starting discovery...");

				while (true) {
					RemoteDevice[] preknownDevices = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);
					Arrays.sort(preknownDevices, new Comparator<RemoteDevice>() {
						@Override
						public int compare(RemoteDevice o1, RemoteDevice o2) {
							try {
								return o1.getFriendlyName(false).compareTo(o2.getFriendlyName(false));
							} catch (IOException ioe) {
								return 0;
							}
						}
					});
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
									Thread.sleep(5000);
								} catch (InterruptedException e) {
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
						Thread.sleep(2000);
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
	}

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
		Searcher searcher = new Searcher();
		searcher.start();

	}

	public static void sendMessageToAll(String message) {
		for (DeviceWatcher watcher : deviceWatchers.values()) {
			watcher.sendMessage(message);
		}
	}

}
