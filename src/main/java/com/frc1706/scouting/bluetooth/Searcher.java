package com.frc1706.scouting.bluetooth;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;

public class Searcher extends Thread {
	private DiscoveryAgent agent = null;
	private final Vector<RemoteDevice> vecDevices = new Vector<RemoteDevice>();
	private final Map<String, DeviceWatcher> deviceWatchers = new HashMap<String, DeviceWatcher>();

	@Override
	public void run() {
		System.out.println("Searching...");
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			agent = localDevice.getDiscoveryAgent();
			System.out.println("Got discovery agent: " + agent.toString() + ", starting discovery...");

			while (true) {
				RemoteDevice[] preknownDevices = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);
				Arrays.sort(preknownDevices, new Comparator<RemoteDevice>() {
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
						String devName = dev.getFriendlyName(false).toUpperCase();
						if (!(devName.contains("MOUSE") || devName.contains("IDEVICE") || devName.contains("IPAD"))) {
							if (!vecDevices.contains(dev)) {
								vecDevices.add(dev);
								final DeviceWatcher watcher = new DeviceWatcher(dev, agent);
								watcher.start();
								deviceWatchers.put(dev.getBluetoothAddress(), watcher);
								EventQueue.invokeLater(new Runnable() {
									public void run() {
										try {
											App.window.addDeviceWatcher(watcher);
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
								System.out.println("Found: " + dev.getBluetoothAddress() + " ("
										+ dev.getFriendlyName(false) + ")");
							}
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
					App.window.updateDeviceStatus();
				} catch (Exception e) {
				}
			}
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	public void sendMessageToAll(String message) {
		for (DeviceWatcher watcher : deviceWatchers.values()) {
			watcher.sendMessage(message);
		}
	}

}
