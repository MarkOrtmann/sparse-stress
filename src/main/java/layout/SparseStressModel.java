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
package layout;

import java.util.Random;

import data_structures.DoubleArray;
import data_structures.IntArray;
import data_structures.StressData;
import graph.Graph;
import helpers.Options;
import helpers.StressFactory;

public final class SparseStressModel {

	private static final int OVERLAP_SEED = 100;

	private final int BREAK_CONDITION_INTERVAL = 10;

	public double[] doLayout(final Graph g, final double[] layout, final Options options) {
		// calculate sparse stress data
		final StressData sData = StressFactory.globalSampling(g, options);
		// prepare weights
		normalizeWeights(sData.getWeights());
		// prepare layout
		scaleAvgEdgeLength(g, layout);
		// minimize sparse stress model
		doStressLayout(g, layout, sData, options.getNumOfIter(), options.useBreakCondition());
		// return the layout
		return layout;
	}

	private void doStressLayout(final Graph g, final double[] layout, final StressData stressData, final int numOfIter,
			final boolean useBC) {
		// used to shuffel the update order
		int timeToBreak = BREAK_CONDITION_INTERVAL;
		double prevStress = 0;
		int i;
		for (i = 1; i <= numOfIter; ++i) {
			minimizeStress(layout, stressData);
			if (useBC) {
				if (--timeToBreak == 1) {
					prevStress = calcIntermediateStress(g, layout, stressData.getDistances(),
							stressData.getPositions());
				}
				if (timeToBreak == 0) {
					timeToBreak = BREAK_CONDITION_INTERVAL;
					double cur = calcIntermediateStress(g, layout, stressData.getDistances(),
							stressData.getPositions());
					if ((prevStress - cur) / prevStress < 0.0001) {
						break;
					}
				}
			}
		}
	}

	private void minimizeStress(final double[] layout, final StressData stressData) {
		final DoubleArray[] dWeights = stressData.getWeights();
		final DoubleArray[] dDistances = stressData.getDistances();
		final IntArray[] dPositions = stressData.getPositions();
		double refPointX;
		double refPointY;
		double[] weights;
		double[] distances;
		int[] positions;
		double weight;
		double distance;
		int voteIndex;
		double votePointX;
		double votePointY;
		double euclideanDistance;
		for (int pos = 0; pos < dWeights.length; pos++) {
			double newXCoord = 0;
			double newYCoord = 0;
			int index = pos << 1;
			refPointX = layout[index];
			refPointY = layout[index + 1];
			weights = dWeights[pos].getData();
			distances = dDistances[pos].getData();
			positions = dPositions[pos].getData();

			for (int i = 0; i < dPositions[pos].size(); i++) {
				voteIndex = positions[i] << 1;
				votePointX = layout[voteIndex];
				votePointY = layout[voteIndex + 1];
				euclideanDistance = calcEucDist(refPointX, refPointY, votePointX, votePointY);
				if (euclideanDistance != 0) {
					weight = weights[i];
					distance = distances[i] / euclideanDistance;
					newXCoord += weight * (votePointX + distance * (refPointX - votePointX));
					newYCoord += weight * (votePointY + distance * (refPointY - votePointY));
				}
			}
			layout[index] = newXCoord;
			layout[index + 1] = newYCoord;
		}
	}

	private double calcIntermediateStress(final Graph g, final double[] layout, final DoubleArray[] distances,
			final IntArray[] positions) {
		double stress = 0;
		for (int i = 0; i < distances.length; i++) {
			final double[] distance = distances[i].getData();
			final int[] pos = positions[i].getData();
			for (int j = 0, e = distances[i].size() - g.degree(i); j < e; j++) {
				double addend = 0;
				if (distance[j] > 0) {
					addend = calcEucDist(layout, i, pos[j]) / distance[j] - 1;
				}
				stress += addend * addend;
			}
		}
		return stress;
	}

	private void normalizeWeights(final DoubleArray[] weights) {
		for (final DoubleArray weight : weights) {
			double totalWeight = 0;
			double[] w = weight.getData();
			for (int i = 0; i < weight.size(); i++) {
				totalWeight += w[i];
			}
			for (int i = 0; i < weight.size(); i++) {
				w[i] /= totalWeight;
			}
		}
	}

	private void scaleAvgEdgeLength(final Graph g, final double[] layout) {
		double avgDist = 0;
		double avgCost = 0;
		final int m = g.m();
		for (int i = 0; i < g.n(); i++) {
			final int[] index = g.getNeighbors(i);
			final double[] weight = g.getWeights(i);
			for (int j = 0; j < index.length; j++) {
				if (index[j] > i) {
					avgDist += calcEucDist(layout, i, index[j]) / m;
					avgCost += weight[j] / m;
				}
			}
		}
		double scaleFactor = avgCost / avgDist;
		for (int i = 0; i < 2 * g.n(); i++) {
			layout[i] *= scaleFactor;
		}
		removeOverlaps(layout, avgCost);
	}

	private double calcEucDist(final double[] layout, final int i, final int j) {
		int index1 = i << 1;
		int index2 = j << 1;
		return calcEucDist(layout[index1], layout[index1 + 1], layout[index2], layout[index2 + 1]);
	}

	private double calcEucDist(final double x1, final double y1, final double x2, final double y2) {
		double diff1 = x1 - x2;
		double diff2 = y1 - y2;
		final double eucDist = Math.sqrt(diff1 * diff1 + diff2 * diff2);
		if (Double.isNaN(eucDist)) {
			return 0;
		}
		return eucDist;
	}

	private void removeOverlaps(final double[] layout, final double avgCost) {
		// move overlapping nodes
		final Random rand = new Random(OVERLAP_SEED);
		for (int i = 0; i < layout.length; i++) {
			layout[i] += avgCost / 1000 * (rand.nextDouble() - 0.5);
		}
	}
}
