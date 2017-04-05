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
import java.io.IOException;
import java.io.PrintStream;

import graph.Graph;
import helpers.Options;
import helpers.StressCalculator;
import io.GraphReader;
import layout.PivMDS;
import layout.SparseStressModel;

public class Main {

	public static void main(String[] args) throws IOException {
		Options options = null;
		PrintStream pW = System.err;
		try {
			options = new Options(args);
			if (options.showHelp()) {
				pW = System.out;
				Options.showHelp(pW);
				return;
			}
			options.isInitialized();
		} catch (final ArrayIndexOutOfBoundsException e) {
			pW.println("missing argument\n");
			pW = System.out;
			Options.showHelp(pW);
			return;
		} catch (final Exception e) {
			pW.println(e.getMessage());
			pW = System.out;
			Options.showHelp(pW);
			return;
		}
		Graph g = null;
		try {
			g = GraphReader.readGraph(options);
		} catch (final IllegalArgumentException e) {
			pW.println(e.getMessage());
			pW = System.out;
			Options.showHelp(pW);
			return;
		}
		if ((long) g.n() * (long) options.getNumOfPiv() != (long) (g.n() * options.getNumOfPiv())) {
			pW.println("The current implementation of MSSP uses int's up to n*p for node indexing. "
					+ "The given graph and defined number of pivots exceeds the limits of Integer.MAX_VALUE. "
					+ "Please use less pivots or adapt the MSSP code.");
			return;
		}
		final long start = System.currentTimeMillis();
		// calculate pivot mds layout
		final double[] layout = new PivMDS().doLayout(g, options.getNumOfPivotsMDS());
		// calculate sparse stress layout
		new SparseStressModel().doLayout(g, layout, options);
		final double time = (System.currentTimeMillis() - start) / 1000d;
		pW = System.out;
		for (int i = 0; i < g.n(); i++) {
			pW.println(options.getFactor() * layout[i << 1] + "," + options.getFactor() * layout[(i << 1) + 1]);
		}
		pW = System.err;
		pW.println("time: " + time);
		if (options.calcStress()) {
			pW.println("optimally rescaled stress: " + StressCalculator.calcStress(g, layout));
		}
	}

}
