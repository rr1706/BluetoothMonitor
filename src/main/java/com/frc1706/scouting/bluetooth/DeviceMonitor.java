package com.frc1706.scouting.bluetooth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

public class DeviceMonitor extends Thread {
	private final StreamConnection connection;
	private boolean done = false;
	private String message = null;
	private RemoteDevice device = null;
	private String deviceName = null;

	public DeviceMonitor(StreamConnection conn, RemoteDevice remoteDevice) {
		connection = conn;
		device = remoteDevice;
		try {
			deviceName = device.getFriendlyName(false);
		} catch (IOException ioe) {

		}
	}

	public void sendMessage(String message) {
		System.err.println("Message queued for device " + message);
		this.message = message;
	}

	@Override
	public void run() {
		try {
			System.out.println("Device " + device.getBluetoothAddress() + ":" + deviceName
					+ " has come online.");
			OutputStream outStream = connection.openOutputStream();
			InputStream inStream = connection.openInputStream();
			PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outStream));
			BufferedReader bReader2 = new BufferedReader(new InputStreamReader(inStream));
			while (!done) {
				int extraBytesAvailable = inStream.available();
				if (extraBytesAvailable > 0) {
					byte[] buf = new byte[extraBytesAvailable];
					int exBytesRead = inStream.read(buf, 0, extraBytesAvailable);
					System.err.println("Extra input found: " + new String(buf, 0, exBytesRead));
				}
				if (message != null) {
					System.err.println("Sending toast message: " + message);
					pWriter.println("toast " + message);
					pWriter.flush();
					message = null;
				}
				pWriter.println("list");
				pWriter.flush();

				// read response
				String lineRead;
				Collection<RemoteFile> remoteFiles = new ArrayList<RemoteFile>();
				while ((lineRead = bReader2.readLine()) != null) {
					System.out.println(lineRead);
					if ("---".equalsIgnoreCase(lineRead)) {
						break;
					} else {
						String[] parts = lineRead.split(" ");
						if (parts.length == 3) {
							int fileLength = Integer.parseInt(parts[0]);
							String checksum = parts[1];
							String name = parts[2];
							RemoteFile file = new RemoteFile(name, fileLength, checksum);
							remoteFiles.add(file);
						}
					}
				}

				if (remoteFiles.size() > 0) {
					for (RemoteFile file : remoteFiles) {
						File baseDir = new File(System.getProperty("user.home"), "ScoutingData");
						File dataDir = new File(baseDir, App.eventID);
						if (!dataDir.exists()) {
							dataDir.mkdirs();
						}
						File f = new File(dataDir, file.getName());
						if (!f.exists() || f.length() != file.getLength()) {
							// Need to download file from device
							pWriter.println("get " + f.getName());
							pWriter.flush();

							char[] cbuf = new char[file.getLength()];
							int bytesRead = 0;
							StringBuffer sb = new StringBuffer();
							while (sb.length() < file.getLength()) {
								bytesRead = bReader2.read(cbuf);
								if (bytesRead > 0) {
									sb.append(cbuf, 0, bytesRead);
								}
							}
							file.setContents(sb.toString());

							// If the file came correctly, then we write it to
							// our file system
							if (file.isValid()) {
								FileWriter out = new FileWriter(f);
								out.write(file.getContents());
								out.flush();
								out.close();

								pWriter.println("delete " + file.getName());
								pWriter.flush();
							}
						}
					}
				}
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException ioe) {
			if (ioe.getMessage().equals("")) {
				ioe.printStackTrace();
			}
		} finally {
			System.out.println("Device " + device.getBluetoothAddress() + ":" + deviceName + " has gone offline.");
		}

	}

}
