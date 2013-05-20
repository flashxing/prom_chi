package org.processmining.contexts.distributed.remote;

import java.io.IOException;

public class AbstractServer {

	protected Service svc = null;

	public void start(int port, ServiceHandler handler) {

		if (port != 0) {
			svc = new Service(port, handler);
			try {

				//System.out.println("Server is started on port " + port);
				svc.start();
			} catch (IOException ex) {
				// TODO Auto-generated catch block

			}
		}

	}

	public void stop() {
		svc.stop();
	}

}
