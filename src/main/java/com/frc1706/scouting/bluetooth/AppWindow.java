package com.frc1706.scouting.bluetooth;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class AppWindow {

	private JFrame frmBluetoothDeviceMonitor;
	private JTextField textField;
	private JTextField eventField;
	private JTextField baseDirField;

	private class DeviceIndex {
		String name;
		DeviceWatcher watcher;
		JLabel statusLabel;
	}

	private int lastRowUsed = 2;
	private final List<DeviceIndex> deviceList = new ArrayList<AppWindow.DeviceIndex>();

	/**
	 * Create the application.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public AppWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBluetoothDeviceMonitor = new JFrame();
		frmBluetoothDeviceMonitor.setMinimumSize(new Dimension(500, 300));
		frmBluetoothDeviceMonitor.setTitle("Bluetooth Device Monitor");
		frmBluetoothDeviceMonitor.setBounds(100, 100, 450, 300);
		frmBluetoothDeviceMonitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0 };
		frmBluetoothDeviceMonitor.getContentPane().setLayout(gridBagLayout);

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		gbc_textField.anchor = GridBagConstraints.PAGE_START;
		frmBluetoothDeviceMonitor.getContentPane().add(textField, gbc_textField);
		textField.setColumns(10);

		JButton btnSendMessage = new JButton("Send Message");
		btnSendMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				App.sendMessageToAll(textField.getText());
				textField.setText("");
			}
		});
		GridBagConstraints gbc_btnSendMessage = new GridBagConstraints();
		gbc_btnSendMessage.insets = new Insets(0, 0, 5, 0);
		gbc_btnSendMessage.gridx = 1;
		gbc_btnSendMessage.gridy = 0;
		gbc_btnSendMessage.anchor = GridBagConstraints.PAGE_START;
		frmBluetoothDeviceMonitor.getContentPane().add(btnSendMessage, gbc_btnSendMessage);

		baseDirField = new JTextField();
		GridBagConstraints gbc_baseDirField = new GridBagConstraints();
		gbc_baseDirField.insets = new Insets(0, 0, 5, 5);
		gbc_baseDirField.fill = GridBagConstraints.HORIZONTAL;
		gbc_baseDirField.gridx = 0;
		gbc_baseDirField.gridy = 1;
		frmBluetoothDeviceMonitor.getContentPane().add(baseDirField, gbc_baseDirField);
		baseDirField.setColumns(10);
		baseDirField.setText((new File(System.getProperty("user.home"), "ScoutingData")).getAbsolutePath());
		App.baseDirField = baseDirField;

		eventField = new JTextField();
		GridBagConstraints gbc_eventField = new GridBagConstraints();
		gbc_eventField.insets = new Insets(0, 0, 5, 5);
		gbc_eventField.fill = GridBagConstraints.HORIZONTAL;
		gbc_eventField.gridx = 1;
		gbc_eventField.gridy = 1;
		frmBluetoothDeviceMonitor.getContentPane().add(eventField, gbc_eventField);
		eventField.setColumns(10);
		eventField.setText(Calendar.getInstance().get(Calendar.YEAR) + "-");
		App.eventNameField = eventField;

	}

	public void setVisible(boolean v) {
		frmBluetoothDeviceMonitor.setVisible(v);
	}

	public void addDeviceWatcher(DeviceWatcher watcher) {
		JLabel nameLabel = new JLabel(watcher.getDeviceName());
		GridBagConstraints gbc_name = new GridBagConstraints();
		gbc_name.insets = new Insets(0, 0, 5, 5);
		gbc_name.gridx = 0;
		gbc_name.gridy = ++lastRowUsed;
		frmBluetoothDeviceMonitor.getContentPane().add(nameLabel, gbc_name);

		DeviceIndex index = new DeviceIndex();
		index.name = watcher.getDeviceName();
		index.watcher = watcher;
		index.statusLabel = new JLabel("Offline");
		index.statusLabel.setForeground(Color.RED);
		GridBagConstraints gbc_status = new GridBagConstraints();
		gbc_status.insets = new Insets(0, 0, 0, 5);
		gbc_status.gridx = 1;
		gbc_status.gridy = lastRowUsed;
		frmBluetoothDeviceMonitor.getContentPane().add(index.statusLabel, gbc_status);

		deviceList.add(index);
		frmBluetoothDeviceMonitor.pack();
	}

	public void updateDeviceStatus() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					for (DeviceIndex idx : deviceList) {
						idx.statusLabel.setText(idx.watcher.isOnline() ? "Online" : "Offline");
						idx.statusLabel.setForeground(idx.watcher.isOnline() ? Color.GREEN : Color.RED);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
