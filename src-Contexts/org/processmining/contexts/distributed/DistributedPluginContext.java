package org.processmining.contexts.distributed;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.processmining.framework.plugin.GlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.impl.AbstractPluginContext;

public class DistributedPluginContext extends AbstractPluginContext {

	private final Executor executor;

	public DistributedPluginContext(GlobalContext context, String label) {
		super(context, label);
		// This context is NOT a child of another context,
		// hence should behave in an asynchronous way.
		executor = Executors.newFixedThreadPool(1);//
		//	Executors.newCachedThreadPool();
		progress = new DistributedProgressBar(this);
	}

	protected DistributedPluginContext(DistributedPluginContext context, String label) {
		super(context, label);
		progress = new DistributedProgressBar(this);
		// This context is a child of another context,
		// hence should behave in a synchronous way.
		if (context.getParentContext() == null) {
			// this context is on the first level below the user-initiated
			// plugins
			executor = Executors.newCachedThreadPool();
		} else {
			// all subtasks take the pool of the parent.
			executor = context.getExecutor();
		}
	}

	public Executor getExecutor() {
		return executor;
	}

	@Override
	public Progress getProgress() {
		return progress;
	}

	@Override
	public DistributedContext getGlobalContext() {
		return (DistributedContext) super.getGlobalContext();
	}

	@Override
	public DistributedPluginContext getRootContext() {
		return (DistributedPluginContext) super.getRootContext();
	}

	protected PluginContext createTypedChildContext(String label) {
		// TODO Auto-generated method stub
		return new DistributedPluginContext(this, label);
	}

}
