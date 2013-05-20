package org.processmining.contexts.distributed;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.impl.AbstractGlobalContext;

public class DistributedContext extends AbstractGlobalContext {

	private final DistributedPluginContext mainPluginContext;

	public DistributedContext() {
		super();

		mainPluginContext = new DistributedPluginContext(this, "Main Plugin Context");

	}

	@Override
	public Class<? extends PluginContext> getPluginContextType() {
		return DistributedPluginContext.class;
	}

	public DistributedPluginContext getMainPluginContext() {
		// TODO Auto-generated method stub
		return mainPluginContext;
	}

}
