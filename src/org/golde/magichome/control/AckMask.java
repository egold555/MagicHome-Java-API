package org.golde.magichome.control;

import lombok.Getter;

@Getter
public class AckMask {
	
	private AckMask() {}

	private boolean power;
	private boolean color;
	private boolean pattern;
	private boolean custom_pattern;
	
	public static AckMask get(int mask) {
		AckMask toReturn = new AckMask();
		toReturn.power = (mask & 0x01) > 0;
		toReturn.color = (mask & 0x02) > 0;
		toReturn.pattern = (mask & 0x04) > 0;
		toReturn.custom_pattern = (mask & 0x08) > 0;
		return toReturn;
	}
	
}
