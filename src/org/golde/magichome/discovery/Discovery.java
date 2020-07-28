package org.golde.magichome.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import lombok.Getter;

public class Discovery {

	private static final int DISCOVERY_PORT = 48899;

	private static final int DEFAULT_TIMEOUT = 500;

	private static final String SOCKET_DISCOVERY_MESSAGE = "HF-A11ASSISTHREAD";

	@Getter
	private boolean scanned = false;

	@Getter
	private Set<DiscoveredDevice> clients = new HashSet<DiscoveredDevice>();

	static boolean needToStop = false;

	public void scan() {this.scan(DEFAULT_TIMEOUT);}
	public void scan(int timeout) {

		try {
			DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
			socket.setBroadcast(true);

			for(String broadcastIP : getNetworkInterfaces()) {
				socket.send(new DatagramPacket(SOCKET_DISCOVERY_MESSAGE.getBytes(StandardCharsets.UTF_8), SOCKET_DISCOVERY_MESSAGE.getBytes(StandardCharsets.UTF_8).length, InetAddress.getByName(broadcastIP), DISCOVERY_PORT));

			}

			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					System.out.println("Timeout reached -- Socket closed");
					needToStop = true;
					socket.close();
					System.exit(0);
				}
			}, timeout);


			new Thread() {
				public void run() {
					try {
						while(!needToStop) {

							int incomingLength = socket.getReceiveBufferSize();
							byte[] incomingBuffer = new byte[incomingLength];
							DatagramPacket recieved = new DatagramPacket(incomingBuffer, incomingLength);
							socket.receive(recieved);

							String recievdString = new String(recieved.getData(), StandardCharsets.UTF_8);

							String[] split = recievdString.split("\\,");
							if(split.length == 3) {
								String add = split[0];
								String id = split[1];
								String model = split[2];
								System.out.println("Device found: " + add + " | " + id + " | " + model);
							}

							//System.out.println("Recieved: " + recievdString);

						}
					}
					catch(SocketException e) {

					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

		} 
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	//Not efficient at all
	public Set<String> getNetworkInterfaces() throws SocketException {

		Set<String> toReturn = new HashSet<String>();

		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if(!inetAddress.isLoopbackAddress() && !inetAddress.toString().contains(":")) {

					String toString = inetAddress.toString().replace("/", "");
					String[] arr = toString.split("\\.");
					arr[3] = "255";
					String newIP = arr[0] + "." + arr[1] + "." + arr[2] + "." + arr[3];
					toReturn.add(newIP);
				}

			}
		}

		toReturn.add("255.255.255.255");

		return toReturn;


	}


}
