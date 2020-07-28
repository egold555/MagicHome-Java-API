package org.golde.magichome.control;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Control {

	private static final int PORT = 5577;

	private final Socket socket;

	/**
	 * Create a new device to control
	 * @param ipAddressIn IP address of the device
	 * @throws IOException thrown if we fail to create the socket
	 */
	public Control(String ipAddressIn) throws IOException {
		socket = new Socket(InetAddress.getByName(ipAddressIn), PORT);
	}

	/**
	 * Sends a command via sockets to the device
	 * @param dataIn the data packet in
	 */
	private void sendSocketCommand(byte... dataIn) {

		//calc the checksum
		byte checksum = 0;
		for (byte b : dataIn) {
			checksum += b;
		}
		checksum &= 0xff;

		//append the checksum to the end of the array
		byte[] data = new byte[dataIn.length + 1];
		for(int i = 0; i < dataIn.length; i++) {
			data[i] = dataIn[i];
		}

		data[data.length - 1] = checksum;

		//try sending the data
		try {
			socket.getOutputStream().write(data);
			socket.getOutputStream().flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the socket
	 * @throws IOException
	 */
	public void dispose() throws IOException {
		socket.close();
	}
	
	/**
	 * Convert an int to a byte, and applies a clamp from 0-255
	 * @param val value from 0-255. If not in range, value will be clamped
	 * @return a byte with the corisponding value
	 */
	private static byte byteify(int val) {
		return (byte)clamp(val, 0, 255);
	}

	/**
	 * Clamp function. Java should have a Math.clamp :P
	 * @param val value in
	 * @param min min value
	 * @param max max value
	 * @return clammped value
	 */
	private static int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

	/**
	 * Turn the light on and off
	 * @param on power on the light
	 */
	public void setPower(boolean on) {
		sendSocketCommand(
				(byte)0x71, //prefix
				(on ? (byte)0x23 : (byte)0x24), //on off
				(byte)0x0f //end
				);
	}

	/**
	 * Set the warm and cool white values
	 * @param ww Warm white
	 * @param cw Cool white
	 */
	public void setWhites(int ww, int cw) {
		sendSocketCommand(
				(byte)0x31, //prefix
				(byte)0, //red -- unused
				(byte)0, //green -- unused
				(byte)0, //blue -- unused
				byteify(ww), //warm white
				byteify(cw), //cool white
				(byte)0x0f, //wacky mask idk
				(byte)0x0f //end
				);
	}

	/**
	 * Sets the color of the light
	 * @param color color of the light
	 */
	public void setColor(Color color) {
		setColor(color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * Sets the color of the light
	 * @param r red
	 * @param g green
	 * @param b blue
	 */
	public void setColor(int r, int g, int b) {
		sendSocketCommand(
				(byte)0x31, //prefix
				byteify(r), //red
				byteify(g), //green
				byteify(b), //blue
				(byte)0,  //warm white -- ignored
				(byte)0, //cool white -- ignored
				(byte)0xf0, //wacky mask idk
				(byte)0x0f //end
				);
	}

}
