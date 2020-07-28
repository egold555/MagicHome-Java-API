package org.golde.magichome.control;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Control {

	private static final int PORT = 5577;

	private final Socket socket;

	public Control(String ipAddressIn) throws IOException {
		socket = new Socket(InetAddress.getByName(ipAddressIn), PORT);
	}

	private void sendSocketCommand(byte... dataIn) {

		byte checksum = 0;
		for (byte b : dataIn) {
			checksum += b;
		}
		checksum &= 0xff;

		byte[] data = new byte[dataIn.length + 1];
		for(int i = 0; i < dataIn.length; i++) {
			data[i] = dataIn[i];
		}

		data[data.length - 1] = checksum;

		try {
			socket.getOutputStream().write(data);
			socket.getOutputStream().flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void dispose() throws IOException {
		socket.close();
	}
	
	private static byte byteify(int val) {
		return (byte)clamp(val, 0, 255);
	}

	private static int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

	///////////////////////////////////////////////////
	public void setPower(boolean on) {
		sendSocketCommand(
				(byte)0x71, //prefix
				(on ? (byte)0x23 : (byte)0x24), //on off
				(byte)0x0f //end
				);
	}

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

	public void setColor(Color color) {
		setColor(color.getRed(), color.getGreen(), color.getBlue());
	}

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
