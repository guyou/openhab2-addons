package org.openhab.binding.freeboxv5;

import org.openhab.binding.freeboxv5.model.PhoneStatus;

public interface PhoneStatusListener {

	void update(PhoneStatus status);
	
}
