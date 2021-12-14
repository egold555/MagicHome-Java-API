package org.golde.magichome.control;

public interface QueryCallback {
	
	public void onDataRecieved(byte type, boolean on, String mode, int speed, int r, int g, int b, int ww, int cw);
}
