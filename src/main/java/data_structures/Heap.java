/*
 * This file is part of sparse-stress.
 *
 * Copyright (C) 2016-2017 Mark Ortmann (University of Konstanz)
 *
 * sparse-stress is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sparse-stress is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with sparse-stress.  If not, see <http://www.gnu.org/licenses/>.
 */
package data_structures;

import java.util.Arrays;

public final class Heap {
	int s;
	int[] pos;
	int[] queue;
	double[] value;

	public Heap(final int size) {
		s = 0;
		pos = new int[size]; // element to position in 1-based array
		value = new double[size]; // element to value
		queue = new int[size + 1]; // 1-based array
		Arrays.fill(value, Double.POSITIVE_INFINITY);
	}

	public void upsert(final int i, final double v) {
		if (v >= value[i]) {
			return;
		}
		if (value[i] == Double.POSITIVE_INFINITY) {
			++s;
			pos[i] = s;
			queue[s] = i;
		}
		value[i] = v;
		// shift up
		int c = pos[i];
		int p = c >> 1;
		while (p >= 1 && v < value[queue[p]]) {
			// move parent down
			pos[queue[p]] = c;
			queue[c] = queue[p];
			// move pointer up
			c = p;
			p = c >> 1;
		}
		pos[i] = c;
		queue[c] = i;
	}

	public int peek() {
		return queue[1];
	}

	public int pop() {
		final int min = queue[1]; // take out root element
		final int sink = queue[s];
		final double sinkv = value[sink];
		--s;
		// shift down
		int p = 1; // parent
		int c = p << 1; // let c point to the first child
		while (c <= s) {
			// let c point to the smaller child
			if (c < s && value[queue[c]] > value[queue[c + 1]])
				++c;
			// if it is smaller than the parent
			if (sinkv > value[queue[c]]) {
				// move the child upwards
				pos[queue[c]] = p;
				queue[p] = queue[c];
				// and the pointer downwards
				p = c;
				c = p << 1;
			} else {
				c = s + 1; // break
			}
		}
		// place the root element
		pos[sink] = p;
		queue[p] = sink;
		return min;
	}

	public double value(final int i) {
		return value[i];
	}

	public boolean isEmpty() {
		return s == 0;
	}

}
