package com.frc1706.scouting.bluetooth;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class DeviceWatcher extends Thread implements DiscoveryListener {

	private RemoteDevice remoteDevice = null;
	private boolean done = false;
	private DeviceMonitor monitor = null;
	private String connectionURL = "";
	private boolean isSearching = false;
	private final DiscoveryAgent agent;

	public DeviceWatcher(RemoteDevice remoteDevice, DiscoveryAgent agent) {
		this.remoteDevice = remoteDevice;
		this.agent = agent;
	}

	public void sendMessage(String message) {
		if (isOnline()) {
			monitor.sendMessage(message);
		}
	}

	public boolean isOnline() {
		return monitor != null && monitor.isAlive();
	}

	@Override
	public void run() {
		while (!done) {

			if (monitor == null || !monitor.isAlive()) {
				if (connectionURL == null || connectionURL.length() == 0) {
					if (!isSearching) {
						try {
							isSearching = true;
							UUID[] uuidSet = new UUID[1];
							uuidSet[0] = App.uuid;
							agent.searchServices(null, uuidSet, remoteDevice, this);
						} catch (BluetoothStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							isSearching = false;
						}
					}
				} else {
					try {
						StreamConnection streamConnection = (StreamConnection) Connector.open(connectionURL);
						monitor = new DeviceMonitor(streamConnection, remoteDevice);
						monitor.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// Sleep a bit before continuing.
			try {
				sleep(10000);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * @return the done
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * @param done
	 *            the done to set
	 */
	public void setDone(boolean done) {
		this.done = done;
	}

	public void deviceDiscovered(RemoteDevice arg0, DeviceClass arg1) {

	}

	public void inquiryCompleted(int arg0) {

	}

	public void serviceSearchCompleted(int arg0, int arg1) {
		isSearching = false;
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		if (servRecord != null && servRecord.length > 0) {
			connectionURL = servRecord[0].getConnectionURL(0, false);
			System.out.println("Found: " + connectionURL + " on device " + remoteDevice.getBluetoothAddress());
		}
	}

	public String getDeviceName() {
		String ret = "";
		try {
			ret = remoteDevice.getFriendlyName(false);
			if (ret == null || ret.trim().length() == 0) {
				ret = remoteDevice.getBluetoothAddress();
			}
		} catch (Exception e) {
			return remoteDevice.getBluetoothAddress();
		}
		return ret;
	}

}
