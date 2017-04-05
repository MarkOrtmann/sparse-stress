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

import sampling.KMeansSampler;
import sampling.MaxMinSSSPSampler;
import sampling.RandomSampler;
import data_structures.DoubleArray;
import data_structures.IntArray;
import data_structures.StressData;

public final class StressFactory {

	public enum SAMPLING_STYLE {
		RANDOM, MAXMIN, KMEANS;
	}

	public static StressData globalSampling(final Graph g, final Options options) {
		final StressData sData = new StressData(g.n());
		// init the sizes
		for (int i = 0; i < g.n(); i++) {
			sData.init(i, options.getNumOfPiv() + g.degree(i));
		}
		// get pivots
		int[] pivots = null;
		switch (options.getSampleStyle()) {
		case KMEANS:
			pivots = new KMeansSampler(options).samplePivots(options, g);
			break;
		case MAXMIN:
			pivots = new MaxMinSSSPSampler(options).samplePivots(options, g);
			break;
		case RANDOM:
			pivots = new RandomSampler(options).samplePivots(options, g);
			break;
		default:
			throw new IllegalArgumentException("this enum is not existent");
		}
		// not necessary but for debugging reasons nice
		Arrays.sort(pivots);
		// calculate MSSP
		MSSP.mssp(g, sData, pivots, g.n(), true, true);
		// add neighbor terms
		addNeighborTerms(g, sData, pivots);
		// return stress data
		return sData;
	}

	private static void addNeighborTerms(final Graph g, final StressData sData, int[] pivots) {
		for (int i = 0; i < g.n(); i++) {
			final int[] neigh = g.getNeighbors(i);
			final double[] weight = g.getWeights(i);
			final DoubleArray sD = sData.getDistances()[i];
			final DoubleArray sW = sData.getWeights()[i];
			final IntArray sP = sData.getPositions()[i];
			for (int j = 0; j < neigh.length; j++) {
				sD.add(weight[j]);
				sW.add(1d / (weight[j] * weight[j]));
				sP.add(neigh[j]);
			}
		}
	}

}
