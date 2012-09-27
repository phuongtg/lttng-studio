package org.lttng.studio.tests.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

public class TestMaxSet {

	public class MaxSet {
		private final int size;
		TreeSet<Long> set;

		public MaxSet(int size) {
			this.size = size;
			this.set = new TreeSet<Long>();
		}
		public void add(long data) {
			if (set.isEmpty()) {
				set.add(data);
			} else if (data > set.first()) {
				set.add(data);
				if (set.size() > size)
					set.pollFirst();
			}
		}
		public SortedSet<Long> getSet() {
			return set;
		}
	}

	@Test
	public void testMax() {
		int size = 10;
		TreeSet<Long> exp = new TreeSet<Long>();
		MaxSet set = new MaxSet(size);
		for(long i = -100; i < 100; i++) {
			set.add(i);
		}
		for (long i = 90; i < 100; i++) {
			exp.add(i);
		}
		assertTrue(set.getSet().containsAll(exp));
		assertTrue(exp.containsAll(set.getSet()));
		assertEquals(size, set.getSet().size());
	}

}
