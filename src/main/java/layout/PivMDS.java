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

import graph.Graph;

import java.util.Arrays;
import java.util.Random;

import data_structures.Heap;

public final class PivMDS {

	private static final int DIMENSIONALITY = 2;

	/**
	 * Convergence detection.
	 */
	private static final double EPSILON = 1 - 1e-10;

	private static final double FACTOR = -0.5;

	private static final long SEED = 0;

	public double[] doLayout(final Graph g, final int numberOfPivots) {

		final double[][] coord = new double[DIMENSIONALITY][g.n()];
		final double[] evals = new double[DIMENSIONALITY];
		singularValueDecomposition(getDistanceMatrix(g, numberOfPivots), coord,
				evals);
		for (int i = 0; i < DIMENSIONALITY; i++) {
			evals[i] = Math.sqrt(evals[i]);
			for (int j = 0; j < g.n(); j++) {
				coord[i][j] *= evals[i];
			}
		}
		final double[] layout = new double[g.n() << 1];
		for (int i = 0; i < g.n(); i++) {
			int pos = i << 1;
			layout[pos] = coord[0][i];
			layout[pos + 1] = coord[1][i];
		}
		return layout;
	}

	public double[][] getDistanceMatrix(final Graph g, int numberOfPivots) {
		// pivot count might be greater than component size ... therefore
		// it has to be corrected
		if (numberOfPivots > g.n()) {
			numberOfPivots = g.n();
		}
		final int n = g.n();
		// the distance matrix
		final double[][] distanceMatrix = new double[numberOfPivots][n];
		// the index of the current pivot node
		int pivotIndex = n - 1;
		// the minimum distances of a node to one of the previously chosen pivot
		// nodes
		final double[] minDistances = new double[n];
		Arrays.fill(minDistances, Double.POSITIVE_INFINITY);

		// start generating the distance matrix
		for (int i = 0; i < numberOfPivots; i++) {
			final double[] distance = distanceMatrix[i];
			calcSingleSourceShortestPath(g, pivotIndex, distance);
			// update the pivot and the minDistance array ... to ensure the
			// correctness set minDistance of the pivot node to zero
			minDistances[pivotIndex] = 0;
			for (int j = 0; j < n; j++) {
				minDistances[j] = Math.min(minDistances[j], distance[j]);
				if (minDistances[j] > minDistances[pivotIndex]) {
					pivotIndex = j;
				}
			}
		}
		// return the pivot distance matrix (this is needed for the quick
		// layout)
		centerDistanceMatrix(distanceMatrix);
		return distanceMatrix;
	}

	private void calcSingleSourceShortestPath(final Graph g,
			final int sourceIndex, final double[] distance) {
		final Heap heap = new Heap(g.n());
		final boolean marked[] = new boolean[g.n()];
		heap.upsert(sourceIndex, 0);
		while (!heap.isEmpty()) {
			// pop minimum distance node
			final int curInd = heap.pop();
			// get and store the distance
			double dist = heap.value(curInd);
			distance[curInd] = dist;
			// mark as processed
			marked[curInd] = true;
			// get the neighbors
			final int[] neighborIndex = g.getNeighbors(curInd);
			final double[] neighWeights = g.getWeights(curInd);
			for (int i = 0; i < neighborIndex.length; i++) {
				if (!marked[neighborIndex[i]]) {
					heap.upsert(neighborIndex[i], dist + neighWeights[i]);
				}
			}
		}
	}

	/**
	 * Centers the pivot matrix
	 * 
	 * @param pivotDistanceMatrix
	 *            the matrix to center
	 */
	public static void centerDistanceMatrix(final double[][] pivotDistanceMatrix) {
		final int numberOfPivots = pivotDistanceMatrix.length;
		final int componentSize = pivotDistanceMatrix[0].length;

		double normalizationFactor = 0;
		double rowColNormalizer;
		final double[] colNormalization = new double[numberOfPivots];

		for (int i = 0; i < numberOfPivots; i++) {
			rowColNormalizer = 0;
			for (int j = 0; j < componentSize; j++) {
				rowColNormalizer += pivotDistanceMatrix[i][j]
						* pivotDistanceMatrix[i][j];
			}
			normalizationFactor += rowColNormalizer;
			colNormalization[i] = rowColNormalizer / componentSize;
		}
		normalizationFactor = normalizationFactor
				/ (componentSize * numberOfPivots);
		for (int i = 0; i < componentSize; i++) {
			rowColNormalizer = 0;
			for (int j = 0; j < numberOfPivots; j++) {
				double square = pivotDistanceMatrix[j][i];
				square *= square;
				pivotDistanceMatrix[j][i] = square + normalizationFactor
						- colNormalization[j];
				rowColNormalizer += square;
			}
			rowColNormalizer /= numberOfPivots;
			for (int j = 0; j < numberOfPivots; j++) {
				pivotDistanceMatrix[j][i] = FACTOR
						* (pivotDistanceMatrix[j][i] - rowColNormalizer);
			}
		}
	}

	private void singularValueDecomposition(final double[][] input,
			final double[][] evecs, final double[] evals) {
		final int k = input.length;
		final int d = evecs.length;
		final double[][] K = new double[k][k];
		// C^TC berechnen
		selfprod(input, K);

		final double[][] tmp = new double[d][k];

		eigenValueDecomposition(K, tmp, evals);

		// eigenvalues to singular values
		for (int m = 0; m < evecs.length; m++) {
			evals[m] = Math.sqrt(evals[m]);
		}

		// C^Tx
		product(input, tmp, evecs);

		for (int m = 0; m < d; m++) {
			normalize(evecs[m]);
		}
	}

	/**
	 * calculate d\times d^T
	 * 
	 * @param d
	 *            matrix
	 * @param result
	 *            storage for d\times d^T
	 */
	public static void selfprod(final double[][] d, final double[][] result) {
		final int k = d.length;
		final int n = d[0].length;
		for (int i = 0; i < k; i++) {
			for (int j = 0; j <= i; j++) {
				double sum = 0;
				for (int m = 0; m < n; m++) {
					sum += d[i][m] * d[j][m];
				}
				result[i][j] = sum;
				result[j][i] = sum;
			}
		}
	}

	/**
	 * Computes the eigenvectors and values
	 * 
	 * @param K
	 *            Matrix
	 * @param eVecs
	 *            place for return of K's eigenvectors
	 * @param eValues
	 *            place for return of K's eigenvalues (largest first)
	 */
	public static void eigenValueDecomposition(final double[][] K,
			final double[][] eVecs, final double[] eValues) {
		randomize(eVecs);
		final int d = eValues.length;
		final int k = K.length;
		double r = 0;
		for (int m = 0; m < d; m++) {
			eValues[m] = normalize(eVecs[m]);
		}
		while (r < EPSILON) {
			if (Double.isNaN(r) || Double.isInfinite(r)) {
				throw new ArithmeticException();
			}
			final double[][] tempOld = new double[d][k];
			// alte werte merken
			for (int m = 0; m < d; m++) {
				for (int i = 0; i < k; i++) {
					tempOld[m][i] = eVecs[m][i];
					eVecs[m][i] = 0;
				}
			}
			// matrix dranmultiplizieren
			for (int m = 0; m < d; m++) {
				for (int i = 0; i < k; i++) {
					for (int j = 0; j < k; j++) {
						eVecs[m][j] += K[i][j] * tempOld[m][i];
					}
				}
			}
			// orthogonalisieren
			for (int m = 0; m < d; m++) {
				for (int p = 0; p < m; p++) {
					final double fac = prod(eVecs[p], eVecs[m])
							/ prod(eVecs[p], eVecs[p]);
					for (int i = 0; i < k; i++) {
						eVecs[m][i] -= fac * eVecs[p][i];
					}
				}
			}
			// normalisieren
			for (int m = 0; m < d; m++) {
				eValues[m] = normalize(eVecs[m]);
			}
			r = 1;
			for (int m = 0; m < d; m++) {
				r = Math.min(Math.abs(prod(eVecs[m], tempOld[m])), r);
			}
		}
	}

	/**
	 * C^T \times X
	 * 
	 * @param C
	 * @param X
	 *            return array
	 * @param result
	 */
	public static void product(final double[][] C, final double[][] X,
			final double[][] result) {
		for (int m = 0; m < result.length; m++) {
			for (int i = 0; i < C[0].length; i++) { // knoten i
				result[m][i] = 0;
				for (int j = 0; j < C.length; j++) { // pivot j
					result[m][i] += C[j][i] * X[m][j];
				}
			}
		}
	}

	public static double normalize(final double[] x) {
		final double norm = Math.sqrt(prod(x, x));

		if (norm != 0) {
			for (int i = 0; i < x.length; i++) {
				x[i] /= norm;
			}
		}
		return norm;
	}

	/**
	 * Randomizes the matrix.
	 * 
	 * @param matrix
	 *            the matrix to randomize
	 */
	public static void randomize(final double[][] matrix) {
		final Random random = new Random(SEED);
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = random.nextDouble();
			}
		}
	}

	private static double prod(final double[] x, final double[] y) {
		double result = 0;
		for (int i = 0; i < x.length; i++) {
			result += x[i] * y[i];
		}
		return result;
	}

}
