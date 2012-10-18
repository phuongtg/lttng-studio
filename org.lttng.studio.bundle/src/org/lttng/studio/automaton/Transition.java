/*
 * dk.brics.automaton
 *
 * Copyright (c) 2001-2011 Anders Moeller
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.lttng.studio.automaton;

import java.io.Serializable;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.rits.cloning.Cloner;

/**
 * <tt>Automaton</tt> transition.
 * <p>
 * A transition, which belongs to a source state, consists of a Unicode character interval
 * and a destination state.
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
 */
public class Transition implements Serializable, Cloneable {

	static final long serialVersionUID = 40001;

	long min;
	long max;

	State to;

	/**
	 * Constructs a new singleton interval transition.
	 * @param c transition character
	 * @param to destination state
	 */
	public Transition(long c, State to)	{
		min = max = c;
		this.to = to;
	}

	/**
	 * Constructs a new transition.
	 * Both end points are included in the interval.
	 * @param min transition interval minimum
	 * @param max transition interval maximum
	 * @param to destination state
	 */
	public Transition(long min, long max, State to)	{
		if (max < min) {
			long t = max;
			max = min;
			min = t;
		}
		this.min = min;
		this.max = max;
		this.to = to;
	}

	/** Returns minimum of this transition interval. */
	public long getMin() {
		return min;
	}

	/** Returns maximum of this transition interval. */
	public long getMax() {
		return max;
	}

	/** Returns destination of this transition. */
	public State getDest() {
		return to;
	}

	/**
	 * Checks for equality.
	 * @param obj object to compare with
	 * @return true if <tt>obj</tt> is a transition with same
	 *         character interval and destination state as this transition.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Transition) {
			Transition t = (Transition)obj;
			return t.min == min && t.max == max && t.to == to;
		} else
			return false;
	}

	/**
	 * Returns hash code.
	 * The hash code is based on the character interval (not the destination state).
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		HashFunction hf = Hashing.goodFastHash(32);
		return hf.newHasher().putLong(min).putLong(max).hash().asInt();
	}

	/**
	 * Clones this transition.
	 * @return clone with same character interval and destination state
	 */
	@Override
	public Transition clone() {
		Cloner cloner = new Cloner();
		return cloner.deepClone(this);
	}

	static void appendCharString(long c, StringBuilder b) {
		// FIXME: use event map to resolve the event name
		b.append(c);
	}

	/**
	 * Returns a string describing this state. Normally invoked via
	 * {@link Automaton#toString()}.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		appendCharString(min, b);
		if (min != max) {
			b.append("-");
			appendCharString(max, b);
		}
		b.append(" -> ").append(to.number);
		return b.toString();
	}

	void appendDot(StringBuilder b) {
		b.append(" -> ").append(to.number).append(" [label=\"");
		appendCharString(min, b);
		if (min != max) {
			b.append("-");
			appendCharString(max, b);
		}
		b.append("\"]\n");
	}
}
