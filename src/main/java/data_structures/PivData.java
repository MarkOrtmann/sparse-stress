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

public final class PivData {

	private final int[] m_pivots;

	private final double[][] m_distances;

	public PivData(final int[] pivots, final double[][] distances) {
		m_pivots = pivots;
		m_distances = distances;
	}

	public int[] getPivots() {
		return m_pivots;
	}

	public double[][] getDistances() {
		return m_distances;
	}

}
