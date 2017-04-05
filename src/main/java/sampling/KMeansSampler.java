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
import java.util.HashSet;

import data_structures.IntArray;

public final class KMeansSampler extends Sampler {

	private static final int MAX_ITER = 50;

	public int m_numberOfSources;

	private long m_seed;

	public KMeansSampler(final Options options) {
		super(options.getSeed());
		m_seed = options.getSeed();
		m_numberOfSources = options.getNumOfKMeansSources();
	}

	@Override
	protected int[] sample(final int numOfPivots, final Graph g,
			final IntArray clusterToProcess, final int[] globalClustering) {
		final double[][] features = new MaxMinSSSPSampler(m_seed)
				.getMaxMinSamples(Math.min(m_numberOfSources, numOfPivots), g,
						clusterToProcess, globalClustering).getDistances();
		// move according max min sampling
		m_rand.nextInt(clusterToProcess.size());
		return getPivots(numOfPivots, clusterToProcess.getData(),
				clusterToProcess.size(), features);
	}

	private int[] getPivots(final int numOfPivots, final int[] nodesToProcess,
			final int size, final double[][] features) {
		final int[] pivots = getInitialSamples(numOfPivots, nodesToProcess,
				size, features);
		final int[] newPivots = new int[numOfPivots];
		final int[] clAssignment = new int[features.length];
		final double[][] meanFeature = new double[numOfPivots][features[nodesToProcess[0]].length];
		int rep = MAX_ITER;
		boolean changed = true;
		while (rep-- > 0 && changed) {
			assingClusters(numOfPivots, pivots, clAssignment, nodesToProcess,
					size, features);
			calcMean(numOfPivots, meanFeature, clAssignment, nodesToProcess,
					size, features);
			findClosestNode(numOfPivots, meanFeature, clAssignment,
					nodesToProcess, size, features, newPivots);
			changed = updateCluster(numOfPivots, pivots, newPivots);
		}
		return pivots;
	}

	private boolean updateCluster(final int numOfPivots, final int[] pivots,
			final int[] newPivots) {
		boolean changed = false;
		for (int i = 0; i < numOfPivots; i++) {
			changed |= pivots[i] != newPivots[i];
			pivots[i] = newPivots[i];
		}
		return changed;
	}

	private void findClosestNode(final int numOfPivots,
			final double[][] meanFeature, final int[] clAssignment,
			final int[] nodesToProcess, final int size,
			final double[][] features, final int[] newPivots) {
		final double[] minDist = new double[numOfPivots];
		Arrays.fill(minDist, Double.POSITIVE_INFINITY);
		int index;
		int pivIndex;
		double dist;
		for (int i = 0; i < size; i++) {
			index = nodesToProcess[i];
			pivIndex = clAssignment[index];
			dist = calcDist(features[index], meanFeature[pivIndex]);
			if (minDist[pivIndex] > dist) {
				minDist[pivIndex] = dist;
				newPivots[pivIndex] = index;
			}
		}
		// reset the mean features
		for (int i = 0; i < numOfPivots; i++) {
			Arrays.fill(meanFeature[i], 0);
		}
	}

	private void calcMean(final int numOfPivots, final double[][] meanFeature,
			final int[] clAssignment, final int[] nodesToProcess,
			final int size, final double[][] features) {
		final int[] clSize = new int[numOfPivots];
		int index;
		int clIndex;
		double[] f;
		double[] meanF;
		int cSize;
		for (int i = 0; i < size; i++) {
			index = nodesToProcess[i];
			clIndex = clAssignment[index];
			++clSize[clIndex];
			f = features[index];
			meanF = meanFeature[clIndex];
			for (int j = 0, e = f.length; j < e; j++) {
				meanF[j] += f[j];
			}
		}
		for (int i = 0; i < numOfPivots; i++) {
			meanF = meanFeature[i];
			cSize = clSize[i];
			for (int j = 0, e = meanF.length; j < e; j++) {
				meanF[j] /= cSize;
			}
		}
	}

	private void assingClusters(final int numOfPivots, final int[] pivots,
			final int[] clAssignment, final int[] nodesToProcess,
			final int size, final double[][] features) {
		double minDist;
		int index;
		double[] cFeature;
		double dist;
		for (int i = 0; i < size; i++) {
			minDist = Double.POSITIVE_INFINITY;
			index = nodesToProcess[i];
			cFeature = features[index];
			for (int j = 0; j < numOfPivots; j++) {
				dist = calcDist(cFeature, features[pivots[j]]);
				if (minDist > dist) {
					minDist = dist;
					clAssignment[index] = j;
				}
			}
		}
	}

	private double calcDist(final double[] cFeature, final double[] oFeature) {
		double dist = 0;
		double diff;
		for (int i = 0, e = cFeature.length; i < e; i++) {
			diff = cFeature[i] - oFeature[i];
			dist += diff * diff;
		}
		return dist;
	}

	private int[] getInitialSamples(final int numOfSamples,
			final int[] nodesToProcess, final int size,
			final double[][] features) {
		final int[] pivots = new int[numOfSamples];
		final HashSet<FeatureVector> set = new HashSet<FeatureVector>();
		int pos = 0;
		int processed = 0;
		while (processed < numOfSamples && pos < size) {
			if (!set.contains(new FeatureVector(features[nodesToProcess[pos]]))) {
				pivots[processed] = nodesToProcess[pos];
				processed++;
				set.add(new FeatureVector(features[nodesToProcess[pos]]));
			}
			pos++;
		}
		if (processed < numOfSamples) {
			for (int i = 0; i < size; i++) {
				pos = nodesToProcess[i];
				for (int j = 0; j < features[pos].length; j++) {
					features[pos][j] += (m_rand.nextDouble() - 0.5) / 1000d;
				}
			}
			return getInitialSamples(numOfSamples, nodesToProcess, size,
					features);
		}
		while (pos < size) {
			++pos;
			int randVal = m_rand.nextInt(pos);
			if (randVal < numOfSamples
					&& !set.contains(new FeatureVector(
							features[nodesToProcess[pos - 1]]))) {
				set.remove(new FeatureVector(features[pivots[randVal]]));
				set.add(new FeatureVector(features[nodesToProcess[pos - 1]]));
				pivots[randVal] = nodesToProcess[pos - 1];
			}
		}
		return pivots;
	}

	private final class FeatureVector {

		final double[] m_featureVec;

		FeatureVector(final double[] featureVec) {
			m_featureVec = featureVec;
		}

		@Override
		public boolean equals(final Object obj) {
			FeatureVector other = (FeatureVector) obj;
			if (m_featureVec.length != other.m_featureVec.length) {
				return false;
			}
			for (int i = 0; i < m_featureVec.length; i++) {
				if (m_featureVec[i] != other.m_featureVec[i]) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int hashCode() {
			int code = 1;
			for (double feature : m_featureVec) {
				code *= feature;
			}
			return code;
		}
	}
}
