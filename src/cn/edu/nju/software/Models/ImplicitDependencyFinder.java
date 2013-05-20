/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName ImplicitDependencyFinder.java
 */
package cn.edu.nju.software.Models;

import java.util.Collection;
import java.util.Stack;

import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.search.NodeExpander;

/**
 * @Author xlx09
 * @Classname ImplicitDependencyFinder
 * @Version 1.0.0
 * @Date 2013-4-6
 */
public class ImplicitDependencyFinder implements NodeExpander<Tuple>{

	public Collection<Tuple> getImplicitDependency(Tuple tuple, EventLogRelation relation){
		final Stack<Tuple> stack = new Stack<Tuple>();
		// Initialize the tuples to the causal depencencies in the log
		for(int left:tuple.leftPart){
			for(int right:tuple.rightPart){
				if(relation.getCausalDependencies().get(new EventPair<Integer,Integer>(left,right)) == 1){
					Tuple t = new Tuple();
					t.leftPart.add(left);
					t.rightPart.add(right);
					stack.push(t);
				}
			}
		}
		return stack;
	}
	/* (non-Javadoc)
	 * @see org.processmining.framework.util.search.NodeExpander#expandNode(java.lang.Object, org.processmining.framework.plugin.Progress, java.util.Collection)
	 */
	public Collection<Tuple> expandNode(Tuple toExpand, Progress progress,
			Collection<Tuple> unmodifiableResultCollection) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.processmining.framework.util.search.NodeExpander#processLeaf(java.lang.Object, org.processmining.framework.plugin.Progress, java.util.Collection)
	 */
	public void processLeaf(Tuple leaf, Progress progress, Collection<Tuple> resultCollection) {
		// TODO Auto-generated method stub
		
	}

}
