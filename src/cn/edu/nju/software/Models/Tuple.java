/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName Tuple.java
 */
package cn.edu.nju.software.Models;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author xlx09
 * @Classname Tuple
 * @Version 1.0.0
 * @Date 2013-3-21
 */
public class Tuple {

	public Set<Integer> leftPart = new HashSet();

	public Set<Integer> rightPart = new HashSet();

	public int maxRightIndex = 0;
	public int maxLeftIndex = 0;

	/**
	 *Default Constructor 
	 */
	public Tuple() {

	}

	/**
	 * @author=xlx09
	 * @param tuple
	 * @return if this contains tuple,then return true; else return false;
	 * @return_typ boolean
	 * @throws 
	 * @version 1.0.0
	 */
	public boolean isSmallerThan(Tuple tuple) {
		return tuple.leftPart.containsAll(leftPart) && tuple.rightPart.containsAll(rightPart);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Tuple clone() {
		Tuple clone = new Tuple();
		clone.leftPart.addAll(leftPart);
		clone.rightPart.addAll(rightPart);
		clone.maxRightIndex = maxRightIndex;
		clone.maxLeftIndex = maxLeftIndex;
		return clone;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return leftPart.hashCode() + 37 * rightPart.hashCode() + maxRightIndex + maxLeftIndex;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Tuple) {
			Tuple t = (Tuple) o;
			return (t.maxRightIndex == maxRightIndex) && (t.maxLeftIndex == maxLeftIndex)
					&& t.leftPart.equals(leftPart) && t.rightPart.equals(rightPart);
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + leftPart.toString() + "} --> {" + rightPart.toString() + "}";
	}
}