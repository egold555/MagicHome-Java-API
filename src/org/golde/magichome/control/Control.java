package org.golde.magichome.control;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Control {

	private static final int PORT = 5577;

	private Socket socket;

	private final String ipAddress;

	private static final Map<Byte, String> MODES = new HashMap<Byte, String>();

	static {
		MODES.put((byte)0x25, "seven_color_cross_fade");
		MODES.put((byte)0x26, "red_gradual_change");
		MODES.put((byte)0x27, "green_gradual_change");
		MODES.put((byte)0x28, "blue_gradual_change");
		MODES.put((byte)0x29, "yellow_gradual_change");
		MODES.put((byte)0x2a, "cyan_gradual_change");
		MODES.put((byte)0x2b, "purple_gradual_change");
		MODES.put((byte)0x2c, "white_gradual_change");
		MODES.put((byte)0x2d, "red_green_cross_fade");
		MODES.put((byte)0x2e, "red_blue_cross_fade");
		MODES.put((byte)0x2f, "green_blue_cross_fade");
		MODES.put((byte)0x30, "seven_color_strobe_flash");
		MODES.put((byte)0x31, "red_strobe_flash");
		MODES.put((byte)0x32, "green_strobe_flash");
		MODES.put((byte)0x33, "blue_stobe_flash");
		MODES.put((byte)0x34, "yellow_strobe_flash");
		MODES.put((byte)0x35, "cyan_strobe_flash");
		MODES.put((byte)0x36, "purple_strobe_flash");
		MODES.put((byte)0x37, "white_strobe_flash");
		MODES.put((byte)0x38, "seven_color_jumping");
		//custom
		MODES.put((byte)0x61, "color");
		MODES.put((byte)0x62, "special");
		MODES.put((byte)0x60, "custom");

	}

	/**
	 * Create a new device to control
	 * @param ipAddressIn IP address of the device
	 * @throws IOException thrown if we fail to create the socket
	 */
	public Control(String ipAddress) {
		this.ipAddress = ipAddress;

	}

	/**
	 * Connects to the light
	 * @throws IOException
	 */

	public void connect() throws IOException {
		socket = new Socket(InetAddress.getByName(ipAddress), PORT);
	}

	/**
	 * Sends a command via sockets to the device
	 * @param dataIn the data packet in
	 */
	private void sendSocketCommand(byte... dataIn) throws IOException {
		this.sendSocketCommand(null, dataIn);
	}

	/**
	 * Sends a command via sockets to the device
	 * @param dataIn the data packet in
	 */
	private void sendSocketCommand(Consumer<byte[]> dataCallback, byte... dataIn) throws IOException {

		if(socket == null) {
			throw new SocketException("Socket isn't connected. Make sure you call connect() first!");
		}

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
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();

		if(dataCallback != null) {
			new InputStreamReader(socket.getInputStream(), 14, new ChangeListener() {

				@Override
				public void onTimeout() {
					System.out.println("MagicHome - Timeout.");
					//dataCallback.accept(new byte[0]); //some sort of other callback incase we get no data back?
				}

				@Override
				public void onError(IOException e) {
					System.out.println("Something bad happened!");
					e.printStackTrace();
				}

				@Override
				public void onDataRecieved(byte[] data) {
					dataCallback.accept(data);
				}
			}).start();
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
	 * Unsign a byte value
	 * @param val the vlaue to unsign
	 * @return the unsigned version of said input value
	 */
	private static int unsign(byte val) {
		return (val & 0xFF);
	}

	/**
	 * Turn the light on and off
	 * @param on power on the light
	 */
	public void setPower(boolean on) throws IOException {
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
	public void setWhites(int ww, int cw) throws IOException {
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
	public void setColor(Color color) throws IOException {
		setColor(color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * Sets the color of the light
	 * @param r red
	 * @param g green
	 * @param b blue
	 */
	public void setColor(int r, int g, int b) throws IOException {
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

	public void query(QueryCallback callback) throws IOException {


		sendSocketCommand(dataCallback -> {

			if(dataCallback.length != 14) {
				System.err.println("Expected data to be 14 bytes long. I got: " + dataCallback.length);
				return;
			}

			byte type = dataCallback[1];
			boolean on = (dataCallback[2] == 0x23);
			String mode = MODES.get(dataCallback[3]);

			int speed = unsign(dataCallback[5]);
			clamp(speed, 1, 31);
			speed -= 1;
			speed = (100 - speed / 300);

			int r = unsign(dataCallback[6]);
			int g = unsign(dataCallback[7]);
			int b = unsign(dataCallback[8]);
			int ww = unsign(dataCallback[9]);
			int cw = unsign(dataCallback[11]);

			callback.onDataRecieved(type, on, mode, speed, r, g, b, ww, cw);

		}, (byte)0x81, (byte)0x8a, (byte)0x8b);
	}

}
