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

import helpers.StressFactory.SAMPLING_STYLE;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

public class Options {

	private static final long SEED = 0;

	private static final int PIVOTS_MDS = 200;

	private int m_numOfPiv;

	private int m_numOfIter;

	private int m_bC;

	private double m_factor;

	private SAMPLING_STYLE m_sampleStyle;

	private File m_file;

	private boolean m_showHelp;

	private int m_weightedGraph;

	private int m_numPivotsMDS;

	private long m_seed;

	private int m_numOfKMeansSources;

	private boolean m_calcStress;

	public Options() {
		m_numOfPiv = -1;
		m_numOfIter = -1;
		m_bC = -1;
		m_factor = -1;
		m_weightedGraph = -1;
		m_showHelp = false;
		m_file = null;
		m_numOfKMeansSources = -1;
		m_numPivotsMDS = PIVOTS_MDS;
		m_seed = SEED;
		m_calcStress = false;
	}

	public Options(String[] args) {
		this();
		parse(args);
	}

	public static void showHelp(PrintStream pW) {
		pW.println("Usage: sparse_stress -p P -s SAMPLER -f FACTOR -i ITER -/+b -/+w                     [-r SEED] [-m MDS] [-c] INPUT");
		pW.println("       sparse_stress -p P -s kmeans  -f FACTOR -i ITER -/+b -/+w --features FEATURES [-r SEED] [-m MDS] [-c] INPUT\n");
		pW.println("The INPUT graph must be specified according the input format");
		pW.println("The OUTPUT is the layout on standard out; optimally rescaled stress value and running time in seconds on standard error\n");
		pW.println("Options:");
		pW.println("  -p P                - sample P pivots");
		pW.println("  -s SAMPLER          - sampler (random, maxmin, kmeans)");
		pW.println("  -f FACTOR           - scale the layout by constant factor");
		pW.println("  -i ITER             - set the maximum iterations");
		pW.println("  -b                  - break condition: -b stops after ITER iterations; +b stops once sparse stress converges, but after at most ITER iterations");
		pW.println("  -w                  - Indicates whether the INPUT is an unweighted graph (-w) or weighted (+w)");
		pW.println("  --features FEATURES - number of entries sampled from the shortest-path distance matrix");
		pW.println("  -r SEED             - SEED value to be used by the sampler (default 0)");
		pW.println("  -m MDS              - number of pivots used by PivotMDS (default 200)");
		pW.println("  -c                  - calculates the stress value of the final layout");
		pW.println("  -h, --help, -?      - print help message");
	}

	private void parse(String[] args) {
		int i = 0;
		if (args.length == 0) {
			showHelp(true);
			return;
		}
		while (i < args.length) {
			switch (args[i]) {
			case "-p":
				setNumOfPiv(Integer.parseInt(args[i + 1]));
				i += 2;
				break;
			case "-s":
				setSampleStyle(SAMPLING_STYLE
						.valueOf(args[i + 1].toUpperCase()));
				i += 2;
				break;
			case "-f":
				setFactor(Double.parseDouble(args[i + 1]));
				i += 2;
				break;
			case "-i":
				setNumOfIter(Integer.parseInt(args[i + 1]));
				i += 2;
				break;
			case "-b":
				setBC(false);
				i += 1;
				break;
			case "+b":
				setBC(true);
				i += 1;
				break;
			case "-w":
				weighted(false);
				++i;
				break;
			case "+w":
				weighted(true);
				++i;
				break;
			case "-?":
			case "-h":
			case "--help":
				showHelp(true);
				return;
			case "-r":
				setSeed(Long.parseLong(args[i + 1]));
				i += 2;
				break;
			case "-m":
				setNumPivotsMDS(Integer.parseInt(args[i + 1]));
				i += 2;
				break;
			case "--features":
				setNumberOfKMeansSources(Integer.parseInt(args[i + 1]));
				i += 2;
				break;
			case "-c":
				setCalcStress(true);
				i += 1;
				break;
			default:
				setFile(new File(args[i]));
				++i;
				break;
			}
		}
	}

	public void setCalcStress(boolean calcStress) {
		m_calcStress = true;
	}

	public boolean calcStress() {
		return m_calcStress;
	}

	public int getNumOfPiv() {
		return m_numOfPiv;
	}

	public void setNumOfPiv(int numOfPiv) {
		if (numOfPiv <= 0) {
			throw new IllegalArgumentException(
					"number of pivots has to be greater than 0");
		}
		m_numOfPiv = numOfPiv;
	}

	public int getNumOfIter() {
		return m_numOfIter;
	}

	public void setNumOfIter(int numOfIter) {
		if (numOfIter <= 0) {
			throw new IllegalArgumentException(
					"number of iterations has to be greater than 0");
		}
		m_numOfIter = numOfIter;
	}

	public boolean useBreakCondition() {
		if (m_bC == 0) {
			return false;
		}
		return true;
	}

	public void setBC(boolean bC) {
		if (bC) {
			m_bC = 1;
		} else {
			m_bC = 0;
		}
	}

	public double getFactor() {
		return m_factor;
	}

	public void setFactor(double factor) {
		if (factor <= 0) {
			throw new IllegalArgumentException(
					"scaling factor has to be greater than 0");

		}
		m_factor = factor;
	}

	public SAMPLING_STYLE getSampleStyle() {
		return m_sampleStyle;
	}

	public void setSampleStyle(SAMPLING_STYLE sampleStyle) {
		Objects.requireNonNull(
				sampleStyle,
				"sampling option is not valid "
						+ Arrays.toString(SAMPLING_STYLE.values()).replaceAll(
								"\\[\\]", ""));
		m_sampleStyle = sampleStyle;
	}

	public void setFile(final File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException("file not found");
		}
		if (file.isDirectory()) {
			throw new IllegalArgumentException("file is directory");
		}
		m_file = file;
	}

	public File getFile() {
		return m_file;
	}

	public void showHelp(final boolean showHelp) {
		m_showHelp = showHelp;
	}

	public boolean showHelp() {
		return m_showHelp;
	}

	public void weighted(final boolean weighted) {
		if (weighted) {
			m_weightedGraph = 1;
		} else {
			m_weightedGraph = 0;
		}
	}

	public boolean isWeighted() {
		if (m_weightedGraph == 0) {
			return false;
		}
		return true;
	}

	public void setNumPivotsMDS(final int numPivotsMDS) {
		if (numPivotsMDS <= 0) {
			throw new IllegalArgumentException(
					"number of pivots (MDS) has to be greater than 0");
		}
		m_numPivotsMDS = numPivotsMDS;
	}

	public int getNumOfPivotsMDS() {
		return m_numPivotsMDS;
	}

	public void setNumberOfKMeansSources(final int numOfKMeansSources) {
		if (numOfKMeansSources <= 0) {
			throw new IllegalArgumentException(
					"number of kmeans sources has to be greater than 0");
		}
		m_numOfKMeansSources = numOfKMeansSources;
	}

	public void setSeed(final long seed) {
		if (seed < 0) {
			throw new IllegalArgumentException(
					"seed value has to be greater than 0");
		}
		m_seed = seed;
	}

	public long getSeed() {
		return m_seed;
	}

	public boolean isInitialized() {
		String failure = "";
		if (m_numOfPiv <= 0) {
			failure += "number of pivots (sparse stress) not initialized\n";
		}
		if (m_numOfIter <= 0) {
			failure += "number of iterations not initialized\n";
		}
		if (m_bC < 0) {
			failure += "break condition not specified\n";
		}
		if (m_factor <= 0) {
			failure += "scaling factor not specified\n";
		}

		if (m_file == null) {
			failure += "input file missing\n";
		}

		if (m_weightedGraph < 0) {
			failure += "weighted graph indicator missing\n";
		}

		if (m_numPivotsMDS <= 0) {
			failure += "number of pivots (MDS) not initialized\n";
		}
		if (m_sampleStyle == null) {
			failure += "sampler not specified\n";
		}
		if (m_seed < 0) {
			failure += "seed incidicator missing\n";
		}
		if (m_numOfKMeansSources < 0 && m_sampleStyle == SAMPLING_STYLE.KMEANS) {
			failure += "the number of features for k-means sampling has to be specified";
		}
		if (!failure.isEmpty()) {
			throw new IllegalArgumentException(failure);
		}
		return true;
	}

	public int getNumOfKMeansSources() {
		return m_numOfKMeansSources;
	}
}
