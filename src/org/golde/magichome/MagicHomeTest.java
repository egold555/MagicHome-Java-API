package org.golde.magichome;

import java.awt.Color;
import java.io.IOException;

import org.golde.magichome.control.Control;
import org.golde.magichome.discovery.Discovery;

public class MagicHomeTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		Discovery d = new Discovery();
		d.scan();

		Control c = new Control("192.168.1.217");
		//c.setColor(Color.RED);
		//c.setWhites(0, 255);

		for(int i = 0; i < 255; i++) {
			Color color = new Color(Color.HSBtoRGB(i / 255F, 1, 1));
			c.setColor(color);
			Thread.sleep(20);
		}

	}

}
