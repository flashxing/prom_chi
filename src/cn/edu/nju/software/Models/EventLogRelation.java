/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName EventLogRelation.java
 */
package cn.edu.nju.software.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @Author xlx09
 * @Classname EventLogRelation
 * @Version 1.0.0
 * @Date 2013-3-20
 */
public class EventLogRelation {
    
    private EventLog log;


	/**
     * to save the relation of a=>wb and a/->wb
     * the 0 represent false;
     * 1 represent a=>wb
     * 2 represent a/->wb
     */
    private int[][] dependency = new int[LamdaDefine.MAXTASKNUM][LamdaDefine.MAXTASKNUM];
    
    
	/**
     * 
     */
    private Map<EventPair<Integer, Integer>, Integer> causalDependencies = new HashMap<EventPair<Integer, Integer>,Integer>();
    private Map<EventPair<Integer, Integer>, Integer> nextTaskMap = new HashMap<EventPair<Integer, Integer>,Integer>();
    private Map<EventPair<Integer, Integer>, Integer> parallelRelations = new HashMap<EventPair<Integer, Integer>, Integer>();
    private Map<Integer, Integer> startTraceInfo = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> endTraceInfo = new HashMap<Integer, Integer>();
    

	/**
     * to save the relation of parallelism
     * 0 represent false;
     * 1 represent parallelism
     * 2 represent potential parallelism
     */
    private int[][] parallelism = new int[LamdaDefine.MAXTASKNUM][LamdaDefine.MAXTASKNUM];
    
    /**
     * Constructor
     */
    public EventLogRelation(){
        
    }
    
    public void calRelation(){
    	calNextTaskMap();
    	calDependencies();
    	calStartAndEndTrace();
    	calParallelRelations();
    }
    
    /**
	 * @author=xlx09
	 * @return_typ void
	 * @throws 
	 * @version 1.0.0
	 */
	private void calParallelRelations() {
		// TODO Auto-generated method stub
		for(int i = 0; i < log.getTaskNum(); ++i){
			for(int j = 0; j < log.getTaskNum(); ++j){
				if (i != j){
				for(HashSet<Integer> set:log.getPostTaskMap().values()){
					if (set.contains(i) && set.contains(j)){
						boolean parallel = true;
						for(HashSet<Integer> other:log.getPostTaskMap().values()){
							if (!(other.contains(i))&&(other.contains(j))){
								parallel = false;
								break;
							}
						}
						if (parallel){
							this.parallelRelations.put(new EventPair<Integer, Integer>(i,j), 2);
						}
					}
				}
				if((this.nextTaskMap.keySet().contains(new EventPair<Integer,Integer>(i,j)))
					&&(this.nextTaskMap.keySet().contains(new EventPair<Integer,Integer>(j,i)))
					&&(!this.causalDependencies.keySet().contains(new EventPair<Integer,Integer>(i,j)))){	
					this.parallelRelations.put(new EventPair<Integer, Integer>(i,j), 1);
				}
				}
			}
		}
	}

	/**
	 * @author=xlx09
	 * @return_typ void
	 * @throws 
	 * @version 1.0.0
	 */
	private void calStartAndEndTrace() {
		// TODO Auto-generated method stub
		for(Map.Entry<Integer, ArrayList<Integer>> entrySet: log.getEventlist().entrySet()){
			int caseId = entrySet.getKey();
			ArrayList<Integer> list = entrySet.getValue();
			this.startTraceInfo.put(list.get(0), 1);
			this.endTraceInfo.put(list.get(list.size()-1), 1);
		}
	}

	/**
	 * @author=xlx09
	 * @return_typ void
	 * @throws 
	 * @version 1.0.0
	 */
	private void calNextTaskMap() {
		// TODO Auto-generated method stub
		for(int i:log.getEventlist().keySet()){
			ArrayList<Integer> list = log.getEventlist().get(i);
			int size = list.size();
			for(int j = 1; j < size; ++j){
				this.nextTaskMap.put(new EventPair<Integer,Integer>(list.get(j-1), list.get(j)), 1);
			}
		}
	}

	/**
	 * @author=xlx09
	 * @return_typ void
	 * @throws 
	 * @version 1.0.0
	 */
	private void calDependencies() {
		// TODO Auto-generated method stub
		for(int i  = 0; i < log.getTaskNum(); ++i){
			for(int j = 0; j < log.getTaskNum(); ++j){
				if (log.getPostTaskMap().get(i).contains(j)){
					EventPair<Integer, Integer> pair = new EventPair<Integer,Integer>(i,j);
                    this.causalDependencies.put(pair, 1);
                    if (!this.nextTaskMap.containsKey(pair)){
                    	this.causalDependencies.put(pair, 2);
                    }
                }
				
			}
		}
	}

	/**
     * @author=xlx09
     * @param first 
     * @param second
     * @return is the first//wsecond
     * @return_typ boolean
     * @throws 
     * @version 1.0.0
     */
    public boolean isParallelism(int first, int second){
        return parallelism[first][second] == 1;
    }
    
    /**
     * @author=xlx09
     * @param first
     * @param second
     * @return is the first=>wsecond
     * @return_typ boolean
     * @throws 
     * @version 1.0.0
     */
    public boolean isDependency(int first, int second){
        return dependency[first][second] == 1;
    }
    
    public void setDependency(int[][] dependency){
        this.dependency = dependency;
    }
    
    public void setParallelism(int[][] parallelism){
        this.parallelism = parallelism;
    }
    
    public Map<EventPair<Integer, Integer>,Integer> getCausalDependencies() {
		return causalDependencies;
	}

	public void setCausalDependencies(Map<EventPair<Integer, Integer>,Integer> causalDependencies) {
		this.causalDependencies = causalDependencies;
	}
	
    public Map<EventPair<Integer, Integer>, Integer> getParallelRelations() {
		return parallelRelations;
	}

	public void setParallelRelations(Map<EventPair<Integer, Integer>, Integer> parallelRelations) {
		this.parallelRelations = parallelRelations;
	}
	
    public Map<Integer, Integer> getStartTraceInfo() {
		return startTraceInfo;
	}

	public void setStartTraceInfo(Map<Integer, Integer> startTraceInfo) {
		this.startTraceInfo = startTraceInfo;
	}

	public Map<Integer, Integer> getEndTraceInfo() {
		return endTraceInfo;
	}

	public void setEndTraceInfo(Map<Integer, Integer> endTraceInfo) {
		this.endTraceInfo = endTraceInfo;
	}

	public EventLog getLog() {
		return log;
	}

	public void setLog(EventLog log) {
		this.log = log;
	}
}
