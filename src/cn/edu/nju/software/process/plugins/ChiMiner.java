/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName ChiMiner.java
 */
package cn.edu.nju.software.process.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.search.MultiThreadedSearcher;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

import cn.edu.nju.software.Models.EventLog;
import cn.edu.nju.software.Models.EventLogRelation;
import cn.edu.nju.software.Models.EventPair;
import cn.edu.nju.software.Models.Tuple;

/**
 * @Author xlx09
 * @Classname ChiMiner
 * @Version 1.0.0
 * @Date 2013-3-22
 */
public class ChiMiner implements NodeExpander<Tuple>{
	/**
     * the relation of the event log with post tasks
     */
    private EventLogRelation relation;

    private List<Integer> eventlist;

    @Plugin(
            name = "Chi Miner", 
            parameterLabels = { "Input Event log with post tasks"}, 
            returnLabels = { "Output Processed Result" }, 
            returnTypes = { Petrinet.class }, 
            userAccessible = true, 
            help = "Input a event log with post tasks as the source"
    )
    @UITopiaVariant(
            affiliation = "software.nju.edu.cn", 
            author = "xlx09", 
            email = "xlx09@softeware.nju.edu.cn"
    )
    /**
     * @author xlx09
     * @param context
     * @param multisetList
     * @return
     * @return_typ Petrinet
     * @throws 
     * @version 1.0.0
     */
    public Petrinet doMining(UIPluginContext context, EventLog  log ) {
        this.relation = log.getRelation();
        this.relation.calRelation();
        //eventlist has all the event id¡£
        this.eventlist = new ArrayList<Integer>();
        for(int i = 0; i < log.getTaskNum(); ++i){
        	this.eventlist.add(i);
        }
        final Progress progress = context.getProgress();
		progress.setMinimum(0);
		progress.setMaximum(5);
		progress.setIndeterminate(false);
		
		final Stack<Tuple> stack = new Stack<Tuple>();
		
		// Initialize the tuples to the causal depencencies in the log
		for (EventPair<Integer, Integer> causal : relation.getCausalDependencies().keySet()) {
			if (progress.isCancelled()) {
				context.getFutureResult(0).cancel(true);
				return null;
			}
			System.out.print("causal is:"+causal);
			if (!eventlist.contains(causal.getFirst()) || !eventlist.contains(causal.getSecond())) {
				System.out.println("continue");
				continue;
			}
			Tuple tuple = new Tuple();
			tuple.leftPart.add(causal.getFirst());
			tuple.rightPart.add(causal.getSecond());
			tuple.maxRightIndex = eventlist.indexOf(causal.getSecond());
			tuple.maxLeftIndex = eventlist.indexOf(causal.getFirst());
			stack.push(tuple);
		}
		System.out.println(stack);
		progress.inc();
		
		// Expand the tuples
		final List<Tuple> result = new ArrayList<Tuple>();

		MultiThreadedSearcher<Tuple> searcher = new MultiThreadedSearcher<Tuple>(this,
				MultiThreadedSearcher.BREADTHFIRST);

		searcher.addInitialNodes(stack);
		try {
			searcher.startSearch(context.getExecutor(), progress, result);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			context.getFutureResult(0).cancel(true);
			return null;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);
		if (progress.isCancelled()) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		
		//find the implict dependency
		Stack<Tuple> resultBak = new Stack<Tuple>();
		resultBak.addAll(result);
		for(Tuple tuple : resultBak){
			Set<Integer> left = tuple.leftPart;
			Set<Integer> implictSet = new HashSet<Integer>();
			Set<Integer> explictSet = new HashSet<Integer>();
			Set<Integer> right = tuple.rightPart;
			Set<Integer> rightImplictSet = new HashSet<Integer>();
			for(Integer i: left){
				for(Integer j: right)
				{
					if (hasImplictDepRetaion(i, j)){
						implictSet.add(i);
						rightImplictSet.add(j);
					}
				}
			}
			for(Integer i: left){
				if (!implictSet.contains(i)){
					explictSet.add(i);
				}
			}
			if (!explictSet.isEmpty() && !implictSet.isEmpty() && (rightImplictSet == right)){
				result.remove(tuple);
				Tuple newTuple = new Tuple();
				newTuple.leftPart = implictSet;
				newTuple.rightPart = right;
				result.add(newTuple);
				Tuple new2Tuple = newTuple.clone();
				newTuple.leftPart = explictSet;
				result.add(new2Tuple);
			}
			
		}
		// Add transitions 
		Map<Integer, Transition> class2transition = new HashMap<Integer, Transition>();
		Petrinet net = PetrinetFactory.newPetrinet("Petrinet from Eventlog, mined with ChiMiner");
//		context.getFutureResult(0).setLabel(net.getLabel());
//		context.getFutureResult(1).setLabel("Initial Marking of " + net.getLabel());
		for (String eventname : log.getNameIdMap().keySet()) {
			Transition transition = net.addTransition(eventname);
			class2transition.put(log.getNameIdMap().get(eventname), transition);
		}
		System.out.println(class2transition);
		progress.inc();
		
		int placeNum = 0;
		Map<Tuple, Place> tuple2place = new HashMap<Tuple, Place>();
		// Add places for each tuple
		for (Tuple tuple : result) {
			Place p = net.addPlace(tuple.toString());
			for (int eventClass : tuple.leftPart) {
				net.addArc(class2transition.get(eventClass), p);
			}
			for (int eventClass : tuple.rightPart) {
				net.addArc(p, class2transition.get(eventClass));
			}
			tuple2place.put(tuple, p);
		}
		progress.inc();

		Marking m = new Marking();
		// Add initial and final place
		Place pstart = net.addPlace("Start");
		for (int eventId : relation.getStartTraceInfo().keySet()) {
			String eventClass = log.getTasklist().get(eventId);
			net.addArc(pstart, class2transition.get(eventId));
		}
		m.add(pstart);

		Place pend = net.addPlace("End");
		for (int eventId : relation.getEndTraceInfo().keySet()) {
			String eventClass = log.getTasklist().get(eventId);
			net.addArc(class2transition.get(eventId), pend);
		}
		progress.inc();
	
		return net;
    }

	/* (non-Javadoc)
	 * @see org.processmining.framework.util.search.NodeExpander#expandNode(java.lang.Object, org.processmining.framework.plugin.Progress, java.util.Collection)
	 */
	public Collection<Tuple> expandNode(Tuple toExpand, Progress progress,
			Collection<Tuple> unmodifiableResultCollection) {
		// TODO Auto-generated method stub
		Collection<Tuple> tuples = new HashSet<Tuple>();

		int startIndex = toExpand.maxLeftIndex + 1;
		for (int i = startIndex; i < eventlist.size(); i++) {

			if (progress.isCancelled()) {
				return tuples;
			}

			int toAdd = eventlist.get(i);

			if (canExpandLeft(toExpand, toAdd)) {
				// Ok, it is safe to add toAdd
				// to the left part of the tuple
				Tuple newTuple = toExpand.clone();
				newTuple.leftPart.add(toAdd);
				newTuple.maxLeftIndex = i;
				tuples.add(newTuple);
			}
		}

		startIndex = toExpand.maxRightIndex + 1;
		for (int i = startIndex; i < eventlist.size(); i++) {

			if (progress.isCancelled()) {
				return tuples;
			}

			int toAdd = eventlist.get(i);

			if (canExpandRight(toExpand, toAdd)) {
				//find the implicit dependency
				if (hasImplicitDependency(toExpand, toAdd)){
					Tuple newTuple =new Tuple();
					newTuple.rightPart.add(toAdd);
					newTuple.leftPart.addAll(toExpand.rightPart);
					newTuple.maxLeftIndex = toExpand.maxRightIndex;
					newTuple.maxRightIndex = i;
				}
				// Ok, it is safe to add toAdd
				// to the right part of the tuple
				Tuple newTuple = toExpand.clone();
				newTuple.rightPart.add(toAdd);
				newTuple.maxRightIndex = i;
				tuples.add(newTuple);
			}

		}

		return tuples;
	}

	/**
	 * @author=xlx09
	 * @param toExpand
	 * @param toAdd
	 * @return
	 * @return_typ boolean
	 * @throws 
	 * @version 1.0.0
	 */
	private boolean hasImplicitDependency(Tuple toExpand, int toAdd) {
		// TODO Auto-generated method stub
		for(int right:toExpand.leftPart){
			if (!(relation.getCausalDependencies().get(new EventPair<Integer,Integer>(right, toAdd)) ==1)){
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.processmining.framework.util.search.NodeExpander#processLeaf(java.lang.Object, org.processmining.framework.plugin.Progress, java.util.Collection)
	 */
	public void processLeaf(Tuple leaf, Progress progress, Collection<Tuple> resultCollection) {
		// TODO Auto-generated method stub
		synchronized (resultCollection) {
			Iterator<Tuple> it = resultCollection.iterator();
			boolean largerFound = false;
			while (!largerFound && it.hasNext()) {
				Tuple t = it.next();
				if (t.isSmallerThan(leaf)) {
					it.remove();
					continue;
				}
				largerFound = leaf.isSmallerThan(t);
			}
			if (!largerFound) {
				resultCollection.add(leaf);
			}
		}
	}
	
	private boolean canExpandLeft(Tuple toExpand, int toAdd) {
		// Check if the event class in toAdd has a causal depencendy 
		// with all elements of the rightPart of the tuple.
		for (int right : toExpand.rightPart) {
			if (!hasCausalRelation(toAdd, right)) {
				return false;
			}
		}

		// Check if the event class in toAdd does not have a relation 
		// with any of the elements of the leftPart of the tuple.
		for (int left : toExpand.leftPart) {
			if (hasRelation(toAdd, left)) {
				return false;
			}
		}

		return true;
	}

	private boolean canExpandRight(Tuple toExpand, int toAdd) {
		// Check if the event class in toAdd has a causal depencendy 
		// from all elements of the leftPart of the tuple.
		for (int left : toExpand.leftPart) {
			if (!hasCausalRelation(left, toAdd)) {
				return false;
			}
		}

		// Check if the event class in toAdd does not have a relation 
		// with any of the elements of the rightPart of the tuple.
		for (int right : toExpand.rightPart) {
			if (hasRelation(right, toAdd)) {
				return false;
			}
		}

		return true;
	}
	
	private boolean hasRelation(int from, int to) {
		if (from != to) {
			if (hasCausalRelation(from, to)) {
				return true;
			}
			if (hasCausalRelation(to, from)) {
				return true;
			}
		}
		if (relation.getParallelRelations().containsKey(new EventPair<Integer, Integer>(from, to))) {
			return true;
		}
		return false;

	}
	
	private boolean hasImplictDepRetaion(int from, int to){
		if(relation.getCausalDependencies().get(new EventPair<Integer, Integer>(from, to)) == 1){
			return true;
		}
		return false;
	}

	private boolean hasCausalRelation(int from, int to) {
		if (relation.getCausalDependencies().containsKey(new EventPair<Integer, Integer>(from, to))) {
			return true;
		}
		return false;

	}
}
