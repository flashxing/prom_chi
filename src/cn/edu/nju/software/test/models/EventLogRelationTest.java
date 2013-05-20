/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName EventLogRelationTest.java
 */
package cn.edu.nju.software.test.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;

import cn.edu.nju.software.Models.EventLog;
import cn.edu.nju.software.Models.EventLogRelation;

/**
 * @Author xlx09
 * @Classname EventLogRelationTest
 * @Version 1.0.0
 * @Date 2013-4-6
 */
public class EventLogRelationTest {

	@Test
	public void test() {
		InputStream input = null;
		try {
			input = new FileInputStream(new File("tests/test.eventlog"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EventLog log = new EventLog();
		log.loadFromInputString(input);
		EventLogRelation relation = log.getRelation();
		relation.calRelation();
		System.out.println("post task:"+log.getPostTaskMap());
		System.out.println("event list:"+log.getEventlist());
		System.out.println("start event:"+relation.getStartTraceInfo());
		System.out.println("end event:"+relation.getEndTraceInfo());
		System.out.println("parallel relation:"+relation.getParallelRelations());
		System.out.println("causal relation:"+relation.getCausalDependencies());
	}

}
