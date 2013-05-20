/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName EventLog.java
 */
package cn.edu.nju.software.Models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jfree.util.Log;

/**
 * @Author xlx09
 * @Classname EventLog
 * @Version 1.0.0
 * @Date 2013-3-20
 */
public class EventLog {
    /**
     * to save the taskname->taskid
     */
    private Map<String, Integer> nameIdMap = new HashMap<String, Integer>();
    private Map<Integer,ArrayList<Integer>> eventlist = new HashMap<Integer, ArrayList<Integer>>();


	/**
     * to save the post tasks set of each task
     */
    private Map<Integer, HashSet<Integer> > postTaskMap = new HashMap<Integer, HashSet<Integer> >();

 //   private Map<EventPair<Integer, Integer>, Integer> causalDependencies = new HashMap<EventPair<Integer, Integer>,Integer>();
	
    private int taskNum;


	/**
     * to save the taskid->taskname
     */
    private ArrayList<String> tasklist = new ArrayList<String>();
    

	public EventLogRelation getRelation(){
        EventLogRelation relation = new EventLogRelation();
        relation.setLog(this);
//        relation.setCausalDependencies(this.getCausalDependencies());
        relation.setDependency(this.calDependency());
        relation.setParallelism(this.calParallelism());
        return relation;
    }
    
    
    /**
     * @author=xlx09
     * @param InputStream input
     * @return_typ void
     * @throws 
     * @version 1.0.0
     * @Todo to read the log data from the InputStream
     */
    public void loadFromInputString(InputStream input){
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line = "";
        try {
            while ((line = br.readLine()) != null){
                String[] tasks = line.split("\t");
                if (tasks.length != ChiDefine.EACHLOGLENGTH){
                	System.out.println("This "+line+" is invalid");
                    Log.warn("This "+line+" is invalid");
                    continue;
                }
                
                if (!nameIdMap.keySet().contains(tasks[ChiDefine.FIRSTTASKINDEX])){
                    nameIdMap.put(tasks[ChiDefine.FIRSTTASKINDEX], tasklist.size());
                    tasklist.add(tasks[ChiDefine.FIRSTTASKINDEX]);
                }
                
                int caseId = Integer.parseInt(tasks[ChiDefine.IDINDEX]);
                if (!this.eventlist.containsKey(caseId)){
                	eventlist.put(caseId, new ArrayList<Integer>());
                }
                eventlist.get(caseId).add(nameIdMap.get(tasks[ChiDefine.FIRSTTASKINDEX]));
                
                String[] postTasks = tasks[ChiDefine.POSTTASKINDEX].split(",");
                HashSet<Integer> postTaskSet;
                if (postTaskMap.containsKey(nameIdMap.get(tasks[ChiDefine.FIRSTTASKINDEX]))){
                	postTaskSet = postTaskMap.get(nameIdMap.get(tasks[ChiDefine.FIRSTTASKINDEX]));
                }else{
                	postTaskSet = new HashSet<Integer>();
                }
                for(int i = 0; i < postTasks.length; i++){
                    if (!nameIdMap.keySet().contains(postTasks[i])){
                        nameIdMap.put(postTasks[i], tasklist.size());
                        tasklist.add(postTasks[i]);
                    }
                    postTaskSet.add(nameIdMap.get(postTasks[i]));
                }
                postTaskMap.put(nameIdMap.get(tasks[ChiDefine.FIRSTTASKINDEX]), postTaskSet);
            }
            this.taskNum = tasklist.size();
            Log.debug("Load log successful!\nnameIdMap is :"+nameIdMap.toString()+"\npostTaskMap is :"+postTaskMap.toString());
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.warn("Failed to read log", e);
            e.printStackTrace();
        }
    }
    
    /**
	 * @return the postTaskMap
	 */
	public Map<Integer, HashSet<Integer>> getPostTaskMap() {
		return postTaskMap;
	}

	/**
	 * @param postTaskMap the postTaskMap to set
	 */
	public void setPostTaskMap(Map<Integer, HashSet<Integer>> postTaskMap) {
		this.postTaskMap = postTaskMap;
	}
    
    /**
	 * @return the tasklist
	 */
	public ArrayList<String> getTasklist() {
		return tasklist;
	}

	/**
	 * @param tasklist the tasklist to set
	 */
	public void setTasklist(ArrayList<String> tasklist) {
		this.tasklist = tasklist;
	}
	
    
    /**
	 * @return the nameIdMap
	 */
	public Map<String, Integer> getNameIdMap() {
		return nameIdMap;
	}

	/**
	 * @param nameIdMap the nameIdMap to set
	 */
	public void setNameIdMap(Map<String, Integer> nameIdMap) {
		this.nameIdMap = nameIdMap;
	}
    
    private int[][] calDependency(){
        int[][] dependency = new int[ChiDefine.MAXTASKNUM][ChiDefine.MAXTASKNUM];
        for(int i = 0; i < taskNum; i++){
            for(int j = 0; j < taskNum; j++){
                if ((postTaskMap.get(i) != null)&&(postTaskMap.get(i).contains(j))){
                    dependency[i][j] = 1;
                }
            }
        }
        return dependency;
    }
    
    private int[][] calParallelism(){
        int[][] parallelism = new int[ChiDefine.MAXTASKNUM][ChiDefine.MAXTASKNUM];
        
        return parallelism;
    }
    
	public Map<Integer, ArrayList<Integer>> getEventlist() {
		return eventlist;
	}


	public void setEventlist(Map<Integer, ArrayList<Integer>> eventlist) {
		this.eventlist = eventlist;
	}
	
	public int getTaskNum() {
		return taskNum;
	}


	public void setTaskNum(int taskNum) {
		this.taskNum = taskNum;
	}
    
//    public Set<EventPair<Integer, Integer>> getCausalDependencies() {
//    	for(int i = 0; i < this.taskNum; ++i){
//    		for(int j = 0; j < this.taskNum; ++j){
//    			if ()
//    		}
//    	}
//		return causalDependencies;
//	}
//
//
//	public void setCausalDependencies(Set<EventPair<Integer, Integer>> causalDependencies) {
//		this.causalDependencies = causalDependencies;
//	}
}
