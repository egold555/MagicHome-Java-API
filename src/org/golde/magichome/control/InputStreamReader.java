package org.golde.magichome.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

//Modified from this https://stackoverflow.com/questions/28137972/is-there-an-event-in-java-socket-when-socket-receive-data
public class InputStreamReader extends Thread {

	private final InputStream in;
	private final ChangeListener listener;
	private final int maxDataSize;

	private static final int RESPONSE_TIMEOUT = 500;
	private boolean didRespond = false;

	public InputStreamReader(InputStream in, int maxDataSize, ChangeListener listener) {
		this.in = in;
		this.maxDataSize = maxDataSize;
		this.listener = listener;
	}

	@Override
	public void run() {

		Thread th = this;

		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				if(!didRespond) {
					listener.onTimeout();
					th.stop(); //seems like a bad idea, probs is.
				}
				
			}
		}, RESPONSE_TIMEOUT);

		byte[] bytes = new byte[maxDataSize];
		try {
			in.read( bytes );
		} 
		catch (IOException e) {
			listener.onError(e);
		}

		listener.onDataRecieved(bytes);
		didRespond = true;
	}

}
