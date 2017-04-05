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

import java.util.Arrays;

import data_structures.Heap;
import data_structures.IntArray;
import data_structures.PivData;

public final class MaxMinSSSPSampler extends Sampler {

	public MaxMinSSSPSampler(final Options options) {
		this(options.getSeed());
	}

	protected MaxMinSSSPSampler(final long seed) {
		super(seed);
	}

	@Override
	protected int[] sample(final int numOfPivots, final Graph g,
			final IntArray clusterToProcess, final int[] globalClustering) {
		return getMaxMinSamples(numOfPivots, g, clusterToProcess,
				globalClustering).getPivots();
	}

	PivData getMaxMinSamples(final int numOfPivots, final Graph g,
			final IntArray clusterToProcess, final int[] globalClustering) {
		final int size = clusterToProcess.size();
		// the pivots
		final int[] pivots = new int[numOfPivots];
		// the distance matrix
		final double[][] distanceMatrix = new double[g.n()][numOfPivots];
		// the index of the current pivot node
		final int[] cData = clusterToProcess.getData();
		// draw initial point at random
		int pivotIndex = cData[m_rand.nextInt(size)];
		// the minimum distances of a node to one of the previously chosen pivot
		// nodes
		final double[] minDistances = new double[g.n()];
		Arrays.fill(minDistances, Double.POSITIVE_INFINITY);
		// start generating the distance matrix
		for (int i = 0; i < numOfPivots; i++) {
			pivots[i] = pivotIndex;
			calcSingleSourceShortestPath(g, i, pivotIndex, distanceMatrix,
					globalClustering);
			// update the pivot and the minDistance array ... to ensure the
			// correctness set minDistance of the pivot node to zero
			minDistances[pivotIndex] = 0;
			for (int j = 0; j < size; j++) {
				final int index = cData[j];
				minDistances[index] = Math.min(minDistances[index],
						distanceMatrix[index][i]);
				if (minDistances[index] > minDistances[pivotIndex]) {
					pivotIndex = index;
				}
			}
		}
		return new PivData(pivots, distanceMatrix);
	}

	private void calcSingleSourceShortestPath(final Graph g,
			final int pivotPosition, final int sourceIndex,
			final double[][] distance, final int[] globalClustering) {
		final Heap heap = new Heap(g.n());
		final boolean marked[] = new boolean[g.n()];
		heap.upsert(sourceIndex, 0);
		while (!heap.isEmpty()) {
			// pop minimum distance node
			final int curInd = heap.pop();
			// get and store the distance
			double dist = heap.value(curInd);
			distance[curInd][pivotPosition] = dist;
			// mark as processed
			marked[curInd] = true;
			// get the neighbors
			final int[] neighborIndex = g.getNeighbors(curInd);
			final double[] neighWeights = g.getWeights(curInd);
			for (int i = 0; i < neighborIndex.length; i++) {
				if (globalClustering[sourceIndex] == globalClustering[neighborIndex[i]]
						&& !marked[neighborIndex[i]]) {
					heap.upsert(neighborIndex[i], dist + neighWeights[i]);
				}
			}
		}
	}
}
