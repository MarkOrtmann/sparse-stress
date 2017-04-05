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
import data_structures.IntArray;

public final class RandomSampler extends Sampler {

	public RandomSampler(final Options options) {
		super(options.getSeed());
	}

	@Override
	protected int[] sample(final int numOfPivots, final Graph g,
			final IntArray clusterToProcess, final int[] globalClustering) {
		final int clusterSize = clusterToProcess.size();
		final int[] data = clusterToProcess.getData();
		final int[] pivots = new int[numOfPivots];
		int i = 0;
		for (; i < numOfPivots; i++) {
			pivots[i] = data[i];
		}
		while (i < clusterSize) {
			++i;
			int randVal = m_rand.nextInt(i);
			if (randVal < numOfPivots) {
				pivots[randVal] = data[i - 1];
			}
		}
		return pivots;
	}
}
