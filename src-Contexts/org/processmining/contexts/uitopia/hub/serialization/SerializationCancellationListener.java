package org.processmining.contexts.uitopia.hub.serialization;

import org.processmining.contexts.uitopia.hub.overlay.ProgressOverlayDialog.CancellationListener;

public class SerializationCancellationListener implements CancellationListener {

	private boolean cancelled = false;

	public synchronized void cancel() {
		cancelled = true;
	}

	public synchronized boolean isCanceled() {
		return cancelled;
	}

}

