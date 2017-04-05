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

public final class DoubleArray {

	private double[] m_data;

	private int m_size;

	public DoubleArray(final int capacity) {
		m_data = new double[capacity];
		m_size = 0;
	}

	public void add(final double val) {
		if (m_size == m_data.length) {
			resize();
		}
		m_data[m_size] = val;
		++m_size;
	}

	private void resize() {
		final double[] tmp = new double[m_size << 1];
		for (int i = 0; i < m_size; i++) {
			tmp[i] = m_data[i];
		}
		m_data = tmp;
	}

	public int size() {
		return m_size;
	}

	public double[] getData() {
		return m_data;
	}

	public void moveBack() {
		--m_size;
	}

	@Override
	public String toString() {
		return Arrays.toString(m_data);
	}
}