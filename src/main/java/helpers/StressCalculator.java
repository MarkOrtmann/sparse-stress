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
import data_structures.Heap;

public class StressCalculator {

	public static double calcStress(final Graph g, final double[] layout) {
		return calcStress(g, spap(g), layout);
	}

	public static double calcStress(final Graph g, final double[][] spap, final double[] layout) {
		double stress = 0;
		double nom = 0;
		double denom = 0;
		for (int i = 0; i < spap.length; i++) {
			for (int j = i + 1; j < spap.length; j++) {
				double toAdd = sqrtDist(layout, i, j) / spap[i][j];
				nom += toAdd;
				denom += dist(layout, i, j) / (spap[i][j] * spap[i][j]);
			}
		}
		double factor = nom / denom;
		for (int i = 0; i < layout.length; i++) {
			layout[i] *= factor;
		}
		stress = 0;
		for (int i = 0; i < spap.length; i++) {
			for (int j = i + 1; j < spap.length; j++) {
				double addend = sqrtDist(layout, i, j) / spap[i][j] - 1;
				stress += addend * addend;
			}
		}
		return stress;
	}

	public static String calcStressValues(final Graph g, final double[][] spap, final double[] layout) {
		double stress = 0;
		double nom = 0;
		double denom = 0;
		for (int i = 0; i < spap.length; i++) {
			for (int j = i + 1; j < spap.length; j++) {
				double toAdd = sqrtDist(layout, i, j) / spap[i][j];
				nom += toAdd;
				denom += dist(layout, i, j) / (spap[i][j] * spap[i][j]);
				double addend = toAdd - 1;
				stress += addend * addend;
			}
		}
		String res = stress + ",";
		double factor = nom / denom;
		for (int i = 0; i < layout.length; i++) {
			layout[i] *= factor;
		}
		stress = 0;
		for (int i = 0; i < spap.length; i++) {
			for (int j = i + 1; j < spap.length; j++) {
				double addend = sqrtDist(layout, i, j) / spap[i][j] - 1;
				stress += addend * addend;
			}
		}
		res += stress + "," + 2 * stress / (g.n() * (g.n() + 1));
		return res;
	}

	private static double sqrtDist(final double[] layout, int i, int j) {
		double res = dist(layout, i, j);
		if (res == 0) {
			return 0;
		}
		return Math.sqrt(res);
	}

	private static double dist(final double[] layout, int i, int j) {
		i = i << 1;
		j = j << 1;
		double res = (layout[i] - layout[j]) * (layout[i] - layout[j]);
		++i;
		++j;
		return res + (layout[i] - layout[j]) * (layout[i] - layout[j]);
	}

	public static double[][] spap(final Graph g) {
		final double[][] spap = new double[g.n()][g.n()];
		for (int i = 0; i < g.n(); i++) {
			double[] distance = spap[i];
			final Heap heap = new Heap(g.n());
			final boolean marked[] = new boolean[g.n()];
			heap.upsert(i, 0);
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
				for (int j = 0; j < neighborIndex.length; j++) {
					if (!marked[neighborIndex[j]]) {
						heap.upsert(neighborIndex[j], dist + neighWeights[j]);
					}
				}
			}

		}
		return spap;
	}

}
