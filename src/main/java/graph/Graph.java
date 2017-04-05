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
package graph;

import io.Pair;

import java.util.List;

public final class Graph {

	private final int m_n;

	private int m_m;

	private final int[][] m_edges;

	private final double[][] m_weights;

	private final boolean m_weighted;

	public Graph(final List<Pair>[] edgeList, final boolean weighted) {
		m_n = edgeList.length;
		m_m = 0;
		m_edges = new int[m_n][];
		m_weights = new double[m_n][];
		for (int i = 0; i < edgeList.length; i++) {
			final List<Pair> eList = edgeList[i];
			int[] edges = new int[eList.size()];
			double[] weights = new double[eList.size()];
			int pos = 0;
			for (final Pair p : eList) {
				edges[pos] = p.getIndex();
				weights[pos] = p.getWeight();
				++pos;
				++m_m;
			}
			m_edges[i] = edges;
			m_weights[i] = weights;
		}
		m_m /= 2;
		m_weighted = weighted;
	}

	public int n() {
		return m_n;
	}

	public int m() {
		return m_m;
	}

	public boolean isWeighted() {
		return m_weighted;
	}

	public int degree(final int index) {
		return m_edges[index].length;
	}

	public int[] getNeighbors(final int index) {
		return m_edges[index];
	}

	public double[] getWeights(final int index) {
		return m_weights[index];
	}
}
