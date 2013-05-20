package org.processmining.contexts.distributed.middleware.connect;

import java.io.IOException;

import org.processmining.framework.plugin.PluginContext;

public class InformationSender {

	protected boolean running;
	protected Thread serverThread;
	private final PluginContext context;
	//private GMClient gmClient;
	private final String host;
	private final int port;

	public InformationSender(PluginContext context, String host, int port) {
		this.context = context;
		this.host = host;
		this.port = port;
	}

	public void start() {
		running = true;
		serverThread = new Thread() {
			public void run() {
				while (running == true) {
					// here is the actual operation
					try {
						sleep(10000);
						Thread clientThread = new Thread() {
							public void run() {
								try {
									GMClient gmClient = new GMClient(host, port);
									gmClient.sendInformation(context);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};

						clientThread.start();

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		serverThread.start();
	}

	public void stop() {
		if (serverThread != null) {
			if (serverThread.isAlive()) {
				running = false;
			}
		}
	}

}
