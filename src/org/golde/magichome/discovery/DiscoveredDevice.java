package org.golde.magichome.discovery;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiscoveredDevice {

	private String address;
	private String id;
	private String model;
	
}
