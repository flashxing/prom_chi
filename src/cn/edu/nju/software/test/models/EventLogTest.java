/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName EventLogTest.java
 */
package cn.edu.nju.software.test.models;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Test;

import cn.edu.nju.software.Models.EventLog;

/**
 * @Author xlx09
 * @Classname EventLogTest
 * @Version 1.0.0
 * @Date 2013-4-6
 */
public class EventLogTest {

	/**
	 * Test method for {@link cn.edu.nju.software.Models.EventLog#loadFromInputString(java.io.InputStream)}.
	 */
	@Test
	public void testLoadFromInputString() {
		InputStream input = null;
		try {
			input = new FileInputStream(new File("tests/test.eventlog"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EventLog log = new EventLog();
		log.loadFromInputString(input);
		assertTrue(log.getTaskNum()==5);
//		System.out.println(log.getTasklist().get(0));
		assertTrue(log.getTasklist().get(0).equals("A"));
		assertTrue(log.getTasklist().get(1).equals("C"));
		assertTrue(log.getTasklist().get(2).equals("D"));
		assertTrue(log.getTasklist().get(3).equals("B"));
		assertTrue(log.getTasklist().get(4).equals("E"));
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(0);
		list.add(1);
		list.add(2);
		list.add(1);
		System.out.println(log.getEventlist().get(1));
		System.out.println(log.getPostTaskMap().get(2));
		assertTrue(log.getEventlist().get(1).equals(list));
		log.getRelation();
		
	}

}
