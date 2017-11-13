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
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("first line does not contain the number of nodes");
			}
			String line;
			int lineNo = 0;
			try {
				while ((line = reader.readLine()) != null) {
					++lineNo;
					String[] line_split = line.split(DELIMITER);
					if(line_split.length < 2){
						throw new IllegalArgumentException("line " + lineNo + " does not obey the required format");
					}
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
