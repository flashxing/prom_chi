package org.processmining.contexts.uitopia.packagemanager;

import java.io.File;
import java.net.URL;

import org.deckfour.uitopia.ui.main.Overlayable;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.packages.events.PackageManagerListener;

public class PMListener implements PackageManagerListener {

	private final Overlayable overlayable;

	public PMListener(Overlayable overlayable) {
		this.overlayable = overlayable;

	}

	private PMOverlay pmOverlay;

	public synchronized void exception(Throwable t) {
		exception(t.getMessage());
	}

	public synchronized void exception(String exception) {
		pmOverlay.addText("Error: " + exception);
	}

	public synchronized void startDownload(String packageName, URL url, PackageDescriptor pack) {
		pmOverlay.setPackage(pack);
		pmOverlay.addText("Downloading: " + packageName);
	}

	public synchronized void startInstall(String packageName, File folder, PackageDescriptor pack) {
		pmOverlay.setPackage(pack);
		pmOverlay.addText("Installing: " + packageName);
	}

	public synchronized void sessionComplete(boolean error) {
		PackageManager.getInstance().setCanceller(null);
		pmOverlay.finishedInstall(error);
		pmOverlay.getResultBlocking();
	}

	public synchronized void sessionStart() {
		pmOverlay = new PMOverlay(overlayable);
		PackageManager.getInstance().setCanceller(pmOverlay);
		pmOverlay.addText("Started package manager session");
		overlayable.showOverlay(pmOverlay);

	}

	public synchronized void finishedInstall(String packageName, File folder, PackageDescriptor pack) {
		pmOverlay.addText("Succesfully installed: " + packageName);
	}
}
