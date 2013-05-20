package org.processmining.contexts.distributed;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.PluginLifeCycleEventListener;

public class PluginListenerDistributedImpl implements PluginLifeCycleEventListener {

	protected boolean completed = false;
	protected boolean cancelled = false;

	protected PluginContext pContext;

	public PluginListenerDistributedImpl(PluginContext childContext) {
		// TODO Auto-generated constructor stub
		System.out.println("Create plugin listener form context" + childContext.getID());
		pContext = childContext;
	}

	public void pluginCancelled(PluginContext context) {
		// TODO Auto-generated method stub
		System.out.println("Cancel plugin form context" + context.getID());

		//if (pContext == context){
		cancelled = true;
		//}
	}

	public void pluginCompleted(PluginContext context) {
		// TODO Auto-generated method stub
		System.out.println("Completed plugin form context" + context.getID());
		//if (pContext.equals(context)){
		completed = true;
		//}
	}

	public void pluginCreated(PluginContext context) {
		// TODO Auto-generated method stub
		System.out.println("Create plugin form context" + context.getID());
	}

	public void pluginDeleted(PluginContext context) {
		// TODO Auto-generated method stub
		System.out.println("Delete plugin form context" + context.getID());
	}

	public void pluginFutureCreated(PluginContext context) {
		// TODO Auto-generated method stub
		System.out.println("Future created plugin form context" + context.getID());
	}

	public void pluginResumed(PluginContext context) {
		// TODO Auto-generated method stub
		System.out.println("Resume plugin form context" + context.getID());
	}

	public void pluginStarted(PluginContext context) {
		// TODO Auto-generated method stub
		System.out.println("Start plugin form context" + context.getID());
	}

	public void pluginSuspended(PluginContext context) {
		// TODO Auto-generated method stub
		System.out.println("Suspend plugin form context" + context.getID());
	}

	public void pluginTerminatedWithError(PluginContext context, Throwable t) {
		// TODO Auto-generated method stub
		System.out.println("Terminate with errors plugin form context" + context.getID());
		//if (pContext.equals(context)){
		cancelled = true;
		//}
	}

	public boolean isCompleted() {
		return completed;
	}

	public boolean isCancelled() {
		return cancelled;
	}

}
