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
package helpers;

import graph.Graph;

import java.util.Arrays;
import java.util.HashSet;

import data_structures.DoubleArray;
import data_structures.Heap;
import data_structures.IntArray;
import data_structures.StressData;

public final class MSSP {

	private static final int DEFAULT_SIZE = 200;

	static public void mssp(final Graph g, final StressData stressData, final int[] pivots, final int nInCl,
			final boolean hasNeighborTerm, final boolean includeOwnPivot) {

		// number of pivots
		final int pC = pivots.length;

		// number of nodes
		final int n = g.n();

		// stress data elements
		final DoubleArray[] dDist = stressData.getDistances();
		final DoubleArray[] dWeights = stressData.getWeights();
		final IntArray[] iPosition = stressData.getPositions();
		// hanging pointer for the sizes
		final int[] hangingPointer = new int[pC];

		// cluster assignment
		final int[] clAssignment = new int[n];
		Arrays.fill(clAssignment, -1);
		final int[] clSize = new int[pC];
		final IntArray visited = new IntArray(nInCl);

		// the sorted distances
		final DoubleArray[] sDistances = new DoubleArray[pC];

		// the heap for shortest path calculation
		final Heap heap = new Heap(pC * n);
		final boolean marked[] = new boolean[pC * n];

		for (int i = 0; i < pC; i++) {
			sDistances[i] = new DoubleArray(n);
			heap.upsert(i * n + pivots[i], 0);
		}

		// if model uses neighbor term we have to remove pivots
		@SuppressWarnings("unchecked")
		final HashSet<Integer>[] pivNeigh = new HashSet[pC];
		if (hasNeighborTerm) {
			for (int i = 0; i < pC; i++) {
				final HashSet<Integer> hSet = new HashSet<Integer>();
				hSet.add(pivots[i]);
				for (final int neigh : g.getNeighbors(pivots[i])) {
					hSet.add(neigh);
				}
				pivNeigh[i] = hSet;
			}
		}

		// current distance
		double dist = 0;
		double cDistBlock = 0;
		IntArray iBlock = new IntArray(DEFAULT_SIZE);
		IntArray iProcBlock = new IntArray(DEFAULT_SIZE);

		while (!heap.isEmpty()) {
			// pop minimum distance node
			final int curInd = heap.pop();
			// the distance
			dist = heap.value(curInd);

			// if we changed the distance block
			if (cDistBlock != dist) {
				stressPartitioning(pivots, hasNeighborTerm, includeOwnPivot, n, dDist, dWeights, iPosition,
						hangingPointer, clAssignment, clSize, sDistances, cDistBlock, iBlock, pivNeigh, dist,
						iProcBlock);
				iBlock.clear();
				iProcBlock.clear();
				cDistBlock = dist;
			}
			iProcBlock.add(curInd);
			// source pivot
			final int pivIndex = curInd / n;
			// node index
			final int nIndex = curInd - pivIndex * n;
			// if not assigned to cluster yet add it for cluster addition
			if (clAssignment[nIndex] < 0) {
				iBlock.add(curInd);
			}
			if (pivIndex == 0) {
				visited.add(nIndex);
			}
			// mark as processed
			marked[curInd] = true;
			// get the neighbors
			final int[] neighborIndex = g.getNeighbors(nIndex);
			final double[] neighWeights = g.getWeights(nIndex);
			for (int i = 0; i < neighborIndex.length; i++) {
				final int neighIndex = curInd - nIndex + neighborIndex[i];
				if (!marked[neighIndex]) {
					heap.upsert(neighIndex, dist + neighWeights[i]);
				}
			}
		}
		stressPartitioning(pivots, hasNeighborTerm, includeOwnPivot, n, dDist, dWeights, iPosition, hangingPointer,
				clAssignment, clSize, sDistances, cDistBlock, iBlock, pivNeigh, dist, iProcBlock);
	}

	private static void stressPartitioning(final int[] pivots, final boolean hasNeighborTerm,
			final boolean includeOwnPivot, final int n, final DoubleArray[] dDist, final DoubleArray[] dWeights,
			final IntArray[] iPosition, final int[] hangingPointer, final int[] clAssignment, final int[] clSize,
			final DoubleArray[] sDistances, final double cDistBlock, final IntArray iBlock,
			final HashSet<Integer>[] pivNeigh, final double dist, final IntArray iProcBlock) {
		assignToSmallestCluster(n, clAssignment, clSize, iBlock, sDistances, cDistBlock);
		addStressTerms(pivots, hasNeighborTerm, n, dDist, dWeights, iPosition, hangingPointer, iProcBlock, pivNeigh,
				cDistBlock, includeOwnPivot, clAssignment);
		moveHangingPointer(sDistances, hangingPointer, dist / 2);

	}

	private static void addStressTerms(final int[] pivots, final boolean hasNeighborTerm, final int n,
			final DoubleArray[] dDist, final DoubleArray[] dWeights, final IntArray[] iPosition,
			final int[] hangingPointer, final IntArray iBlock, final HashSet<Integer>[] pivNeigh, final double dist,
			final boolean includeOwnPivot, final int[] clAssignment) {
		final int[] block = iBlock.getData();
		for (int i = 0; i < iBlock.size(); i++) {
			final int index = block[i];
			// source pivot
			final int pivIndex = index / n;
			// node index
			final int nIndex = index - pivIndex * n;
			// store the weight
			if ((dist > 0) && (includeOwnPivot || clAssignment[nIndex] != pivIndex)
					&& (!hasNeighborTerm || !pivNeigh[pivIndex].contains(nIndex))) {
				// store distance
				dDist[nIndex].add(dist);
				dWeights[nIndex].add(hangingPointer[pivIndex] / (dist * dist));
				// store the position
				iPosition[nIndex].add(pivots[pivIndex]);
			}
		}
	}

	private static void moveHangingPointer(DoubleArray[] sDistances, int[] hangingPointer, final double cDistBlock) {
		for (int i = 0; i < sDistances.length; i++) {
			final int size = sDistances[i].size();
			final double[] dist = sDistances[i].getData();
			while (hangingPointer[i] != size && dist[hangingPointer[i]] <= cDistBlock) {
				++hangingPointer[i];
			}
		}
	}

	private static void assignToSmallestCluster(final int n, final int[] clAssignment, final int[] clSize,
			IntArray iBlock, final DoubleArray[] sDistances, final double distance) {
		final int[] block = iBlock.getData();
		for (int i = 0; i < iBlock.size(); i++) {
			final int index = block[i];
			// source pivot
			final int pivIndex = index / n;
			// node index
			final int nIndex = index - pivIndex * n;
			// get and store the distance
			if (clAssignment[nIndex] < 0) {
				clAssignment[nIndex] = pivIndex;
				++clSize[pivIndex];
				sDistances[pivIndex].add(distance);
			}
			if (clSize[clAssignment[nIndex]] > (clSize[pivIndex] + 1)) {
				sDistances[clAssignment[nIndex]].moveBack();
				--clSize[clAssignment[nIndex]];
				sDistances[pivIndex].add(distance);
				clAssignment[nIndex] = pivIndex;
				++clSize[pivIndex];
			}
		}
	}
}
