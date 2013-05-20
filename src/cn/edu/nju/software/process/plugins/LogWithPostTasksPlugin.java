/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName LogWithPostTasksPlugin.java
 */
package cn.edu.nju.software.process.plugins;

import java.io.InputStream;

import org.jfree.util.Log;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import cn.edu.nju.software.Models.EventLog;


/**
 * @Author xlx09
 * @Classname LogWithPostTasksPlugin
 * @Version 1.0.0
 * @Date 2013-3-20
 */
@Plugin(
		name = "Import EventLogWithPostTasks", 
		parameterLabels = { "Filename" }, 
		returnLabels = { "EventLog" }, 
		returnTypes = { EventLog.class }
)
/**
 * the Import Plugin
 * need the input file be *.mul
 */
@UIImportPlugin(
		description = "eventlog",
		extensions = { "eventlog" }
)
public class LogWithPostTasksPlugin extends AbstractImportPlugin {
			
	/* (non-Javadoc)
	 * @see org.processmining.framework.abstractplugins.AbstractImportPlugin#importFromStream(org.processmining.framework.plugin.PluginContext, java.io.InputStream, java.lang.String, long)
	 */
	@Override
	protected EventLog  importFromStream(
			final PluginContext context,final InputStream input, final String filename,
			final long fileSizeInBytes) {
		try {
			context.getFutureResult(0).setLabel("EventLog imported from " + filename);
            EventLog eventLog=new EventLog();
            eventLog.loadFromInputString(input);
	        Log.debug("Get LogWithPostTasks Successfull"); 
			return eventLog;
		} catch (Exception e) {
			/**
			 * Failed to get LogWithPostTasks
			 */
			Log.error("Failed to get LogWithPostTasks", e);
			e.printStackTrace();
		}
		return null;
	}
}
