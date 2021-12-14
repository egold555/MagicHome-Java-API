package org.golde.magichome.control;

import java.io.IOException;

public interface ChangeListener {

	public void onDataRecieved(byte[] data);
	public void onTimeout();
	public void onError(IOException e);
	
}
