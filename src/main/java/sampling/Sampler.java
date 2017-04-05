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
package sampling;

import graph.Graph;
import helpers.Options;

import java.util.Random;

import data_structures.IntArray;

public abstract class Sampler {

	protected final Random m_rand;

	protected Sampler(final long seed) {
		m_rand = new Random(seed);
	}

	public int[] samplePivots(final Options options, final Graph g) {
		final IntArray clusterToProcess = new IntArray(g.n());
		for (int i = 0; i < g.n(); i++) {
			clusterToProcess.add(i);
		}
		return samplePivots(options.getNumOfPiv(), g, clusterToProcess,
				new int[g.n()]);
	}

	public int[] samplePivots(final int numOfPivots, final Graph g,
			final IntArray clusterToProcess, final int[] globalClustering) {
		return sample(Math.min(numOfPivots, clusterToProcess.size()), g,
				clusterToProcess, globalClustering);
	}

	abstract protected int[] sample(final int numOfPivots, final Graph g,
			final IntArray clusterToProcess, final int[] globalClustering);
}
