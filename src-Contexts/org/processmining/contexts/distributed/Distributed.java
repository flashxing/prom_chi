package org.processmining.contexts.distributed;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import org.processmining.contexts.distributed.middleware.connect.InformationSender;
import org.processmining.contexts.distributed.remote.next.AbstractServer;
import org.processmining.contexts.distributed.remote.next.ServiceHandler;
import org.processmining.contexts.distributed.remote.next.prom.ProMServiceHandler;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.CommandLineArgumentList;

public class Distributed {

	public static int port;

	@Plugin(name = "Distributed", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(final CommandLineArgumentList commandlineArguments) {
		try {
			final DistributedContext globalContext = new DistributedContext();

			//PluginManager pluginManager = globalContext.getPluginManager();

			//ProvidedObjectManager providedObjectManager = globalContext.getProvidedObjectManager();

			LogListener log = new LogListener();

			globalContext.getMainPluginContext().getLoggingListeners().add(log);
			//providedObjectManager.
			//System.out.println(globalContext.getMainPluginContext().getPluginContextType());
			/*
			 * TODO the actual execution The context class is the one to keep
			 * track of the mechanism of initializing plugins, receiving data,
			 * etc. Like this any plug-in can be executed
			 */
			/*
			 * Here we start the server to listen for any inputs
			 */

			Thread serverThread = new Thread() {
				public void run() {

					CmdLineParser parser = new CmdLineParser();

					CmdLineParser.Option o1 = parser.addIntegerOption('p', "port");
					CmdLineParser.Option o2 = parser.addBooleanOption('g', "GridMiddlewareActive");
					CmdLineParser.Option o3 = parser.addStringOption('h', "host");
					CmdLineParser.Option o4 = parser.addIntegerOption('l', "portGM");

					try {
						parser.parse(commandlineArguments.toStringArray());
					} catch (IllegalOptionValueException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(1);
					} catch (UnknownOptionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(1);
					}

					port = (Integer) parser.getOptionValue(o1);

					boolean gm = false;

					try {
						gm = (Boolean) parser.getOptionValue(o2);
					} catch (NullPointerException e) {
						gm = false;
					}
					//System.out.println("gm = "+gm);					
					AbstractServer server = new AbstractServer();
					ServiceHandler handler = new ProMServiceHandler(globalContext.getMainPluginContext());
					server.start(port, handler);

					if (gm) {
						InformationSender is = new InformationSender(globalContext.getMainPluginContext(),
								(String) parser.getOptionValue(o3), (Integer) parser.getOptionValue(o4));
						is.start();
					}
					GridMiddlewareLogs.initialize();
				}

			};
			serverThread.start();

			while (true) {
			}

		} catch (Throwable t) {
			t.printStackTrace();
			System.err.println(t);
			System.exit(1);
		}

		System.exit(0);
		return null;
	}

	public static void main(String[] args) throws Exception {
		Boot.boot(Distributed.class, DistributedPluginContext.class, args);
	}
}
