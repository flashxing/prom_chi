/**
 * 
 */
package org.processmining.contexts.distributed.remote.next;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author christian
 * 
 */
public interface ServiceHandler {

	public void handleRequest(DataInputStream in, DataOutputStream out) throws IOException;

}
