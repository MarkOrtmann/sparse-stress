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
package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import graph.Graph;
import helpers.Options;

public final class GraphReader {

	private static final String DELIMITER = ",";
	private static final double DEFAULT_LENGTH = 1;

	@SuppressWarnings("unchecked")
	public static Graph readGraph(final Options options) throws IllegalArgumentException {

		final File file = options.getFile();
		final boolean weighted = options.isWeighted();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			final ArrayList<Pair>[] eList;
			try {
				eList = new ArrayList[Integer.parseInt(reader.readLine())];
				for (int i = 0; i < eList.length; i++) {
					eList[i] = new ArrayList<Pair>();
				}
			} catch (IOException e) {
				throw new IllegalArgumentException("first line has to contain the number of nodes");
			}
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					String[] line_split = line.split(DELIMITER);
					double weight = DEFAULT_LENGTH;
					if (weighted) {
						try {
							weight = Double.parseDouble(line_split[2]);
						} catch (ArrayIndexOutOfBoundsException e) {
							throw new IllegalArgumentException("the graph has no weights");
						} catch (NumberFormatException e) {
							throw new IllegalArgumentException("weights have to be numbers");
						}
					}
					add(eList, Integer.parseInt(line_split[0]), Integer.parseInt(line_split[1]), weight);

				}
				reader.close();
			} catch (IOException e) {
				throw new IllegalArgumentException("file does not match required format");
			}
			return new Graph(eList, weighted);
		} catch (IOException e) {
			throw new IllegalArgumentException("file not found");
		}
	}

	private static void add(ArrayList<Pair>[] eList, int index1, int index2, double weight) {
		eList[index1].add(new Pair(index2, weight));
		eList[index2].add(new Pair(index1, weight));
	}
}
