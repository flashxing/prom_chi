package org.processmining.contexts.distributed;

import org.processmining.framework.plugin.PluginContextID;
import org.processmining.framework.plugin.events.Logger;

public class LogListener implements Logger {

	public void log(Throwable t, PluginContextID contextID) {
		// TODO Auto-generated method stub

		System.out.println(contextID.toString() + " " + t.getMessage());
		t.printStackTrace();
	}

	public void log(String message, PluginContextID contextID, MessageLevel messageLevel) {
		// TODO Auto-generated method stub
		System.out.println(contextID.toString() + " " + message);
	}

}
