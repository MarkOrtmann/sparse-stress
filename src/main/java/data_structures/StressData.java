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

public final class StressData {

	private DoubleArray[] m_distances;

	private DoubleArray[] m_weights;

	private IntArray[] m_positions;

	public StressData(final int capacity) {
		m_distances = new DoubleArray[capacity];
		m_weights = new DoubleArray[capacity];
		m_positions = new IntArray[capacity];
	}

	public DoubleArray[] getDistances() {
		return m_distances;
	}

	public DoubleArray[] getWeights() {
		return m_weights;
	}

	public IntArray[] getPositions() {
		return m_positions;
	}

	public void init(final int index, final int size) {
		m_distances[index] = new DoubleArray(size);
		m_weights[index] = new DoubleArray(size);
		m_positions[index] = new IntArray(size);
	}

}
