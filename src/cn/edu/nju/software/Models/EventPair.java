/**
 * @Copyright (c) 2013, Software.nju.edu.cn, Inc.
 * All rights reserved
 * @FileName EventPair.java
 */
package cn.edu.nju.software.Models;

/**
 * @Author xlx09
 * @Classname EventPair
 * @Version 1.0.0
 * @Date 2013-3-22
 */
public class EventPair<F, S> {

	protected final S second;
	protected final F first;

	public EventPair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}

	private static boolean equals(Object x, Object y) {
		return ((x == null) && (y == null)) || ((x != null) && x.equals(y));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other) {
		return (other instanceof EventPair) && equals(first, ((EventPair<F, S>) other).first)
				&& equals(second, ((EventPair<F, S>) other).second);
	}

	@Override
	public int hashCode() {
		if (first == null) {
			return second == null ? 0 : second.hashCode() + 1;
		} else {
			return second == null ? first.hashCode() + 2 : first.hashCode() * 17 + second.hashCode();
		}
	}

	@Override
	public String toString() {
		return "(" + first + "," + second + ")";
	}

}
