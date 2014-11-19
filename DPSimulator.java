/*
 * Created on Nov 12 2014
 * JAVA SE 8
 * Author: Yun-Jhong Wu
 * E-mail: yjwu@umich.edu
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

public class DPSimulator {
	@SuppressWarnings("unused")
	public static void main(String[] args) throws InterruptedException,
			IOException {
		final int n = 100000;
		final int maxIters = Integer.MAX_VALUE;
		final double alpha = 1;
		final double theta = 100;
		final double beta = 20;
		final double xi = 20;
		final int initClusters;
		final int maxNumClusters;
		final int visual = 1;
		final int eval = 0;
		final boolean singleton = false;
		final int saveChart = 3000;
		final int seed = 0;

		/* Generating data */
		System.out.print("Generating data...");
		final ArrayList<Point2D> data = new ArrayList<Point2D>();
		int[] labels = new int[n];
		CRP crp = new CRP(alpha, theta, beta, xi, seed);
		for (int i = 0; i < n; i++)
			data.add(crp.next());

		Collections.sort(data);
		for (int i = 0; i < n; i++)
			labels[i] = data.get(i).cluster;

		/* Getting parameters */
		double[] proportion = new double[crp.size.size()];
		double[] centroidx = new double[crp.moments.size()];
		double[] centroidy = new double[crp.moments.size()];

		for (int c = 0; c < proportion.length; c++) {
			proportion[c] = crp.size.get(c) / (double) n;
			centroidx[c] = crp.moments.get(c)[0];
			centroidy[c] = crp.moments.get(c)[1];
		}
		ArrayList<Integer> freq = crp.size;
		Collections.sort(freq, Collections.reverseOrder());
		System.out.println("done.");
		System.out.println(n + " data points and " + crp.moments.size()
				+ " clusters with size " + freq.toString() + " generated.");

		crp = null;
		initClusters = n;
		maxNumClusters = n; 
		
		/* Simulation */
		GibbsSampler gibbs = (singleton) ? new SingletonGibbsSampler(alpha,
				theta, beta, xi, initClusters, maxNumClusters, data)
				: new BlockGibbsSampler(alpha, theta, beta, xi, initClusters,
						maxNumClusters, data);

		ScatterPlot currentPlot = null;
		TrackNumClusters numsPlot = null;
		if (visual > 0) {
			new DistributionPlot(freq, proportion.length);
			numsPlot = new TrackNumClusters(proportion.length, initClusters);
			//currentPlot = ScatterPlot.initPlots(data, labels, gibbs.labels,
			//		singleton);
		}

		long startTime = System.nanoTime();
		for (int k = 1; k <= maxIters; k++) {
			gibbs.next(k);
			System.out
					.format("Iteration %d; %f milliseconds per iteration; %d clusters. ",
							k, (System.nanoTime() - startTime) / 1000000.0 / k,
							gibbs.clusters.size() - 1);
			System.out.println((eval > 0 && k % eval == 0) ? "residual = "
					+ gibbs.getResidual(n, proportion, centroidx, centroidy)
					: "");

			if (visual > 0 && k % visual == 0) {
				//currentPlot.updateColors(gibbs.labels);
				numsPlot.updateSeries(k, gibbs.clusters.size() - 1);
			}
			if (k == saveChart)
				ImageIO.write(numsPlot.chart.createBufferedImage(650, 400),
						"png", new File("num_of_clusters_" + n + "_"
								+ initClusters + "_"
								+ ((singleton) ? "singleton" : "block")
								+ ".png"));

		}
	}
}
