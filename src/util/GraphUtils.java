package util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import model.Algorithm;
import model.Dataset;
import model.MinSup;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Utility functions for generating graphs to visually analyse the run time of frequent itemset
 * mining algorithms for various configurations.
 * 
 * @author shishir
 *
 */
public class GraphUtils{

	/*
	 * Main function to draw the graph for various frequent itemset mining algorithm runs. These
	 * charts are created using FreeChart library.
	 * 
	 * @param algoRunTimeMap - Map that contains the run time values for each experiment.
	 * @param dataset        - Graphs have to be drawn for each dataset.
	 * 
	 * @return Saves the graph of this dataset experiment on the filesystem.
	 */
	public static void drawGraph(Map<Algorithm, Map<MinSup, Integer>> algoRunTimeMap, Dataset dataset)
	{
		String chartTitle = dataset.toString();
		chartTitle = chartTitle.replace("_", ".");
		XYDataset graphDataset = createGraphDataset(algoRunTimeMap, dataset);
		JFreeChart chart = createMinSupChart(graphDataset, chartTitle, "Minimum Support(%)", "Time(sec)");
		
		try {
			String fileLoc = new File(".").getCanonicalPath() + "/graphs/" + chartTitle + ".png";
			System.out.println("Adding chart for dataset : " + dataset.toString() + " at " + fileLoc);
			File chartFile = new File(fileLoc);
			ChartUtilities.saveChartAsPNG(chartFile, chart, 870, 700);
		} catch (IOException e) {
			System.out.println(
				"Chart creation failed for dataset : " + dataset.toString() + ". Reason : " + e 
			);
		}
	}
	
	public static void drawPerPassGraph(
			Map<String, Map<Integer, Integer>> algoPerPassRunStatsMap, String chartTitle,
			String xaxisTitle, String yaxisTitle, boolean isYAxisLogScale)
	{
		XYDataset graphDataset = createPerPassGraphDataset(algoPerPassRunStatsMap, isYAxisLogScale);
		JFreeChart chart = createPerPassChart(graphDataset, chartTitle, xaxisTitle, yaxisTitle);
		
		try {
			String fileLoc = new File(".").getCanonicalPath() + "/graphs/" + chartTitle + ".png";
			System.out.println("Adding chart : " + chartTitle + " at " + fileLoc);
			File chartFile = new File(fileLoc);
			ChartUtilities.saveChartAsPNG(chartFile, chart, 870, 700);
		} catch (IOException e) {
			System.out.println(
				"Chart creation failed for : " + chartTitle + ". Reason : " + e 
			);
		}
	}

	/*
	 * Creates the dataset in the format expected by JFreeCharts from the input parameters.
	 */
	private static XYDataset createGraphDataset(Map<Algorithm, Map<MinSup, Integer>> algoRunTimeMap, Dataset dataset)
	{
		XYSeriesCollection graphDataset = new XYSeriesCollection();
		for(Map.Entry<Algorithm, Map<MinSup, Integer>> entry : algoRunTimeMap.entrySet()) {
			Algorithm algo = entry.getKey();
			XYSeries algoSeriesName = new XYSeries(algo.toString());
			Map<MinSup, Integer> minSupRunTimeMap = entry.getValue();
			for(Map.Entry<MinSup, Integer> entry2 : minSupRunTimeMap.entrySet()) {
				MinSup minSup = entry2.getKey();
				int exptRunTime = entry2.getValue();
				algoSeriesName.add(minSup.getMinSupPercentage(), exptRunTime);
			}
			
			graphDataset.addSeries(algoSeriesName);
		}
		
		return graphDataset;
	}
	
	/*
	 * Creates a dataset that contains the per pass execution stats of various algorithms. These
	 * stats can be - number of itemsets generated in each pass or the execution time for each pass.
	 */
	private static XYSeriesCollection createPerPassGraphDataset(
			Map<String, Map<Integer, Integer>> algoPerPassStatsMap, boolean isYAxisLogScale)
	{
		XYSeriesCollection graphDataset = new XYSeriesCollection();
		for(Map.Entry<String, Map<Integer, Integer>> entry : algoPerPassStatsMap.entrySet()) {
			String algo = entry.getKey();
			XYSeries algoSeriesName = new XYSeries(algo.toString());
			for(Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
				int passNum = entry2.getKey();
				int passStat = entry2.getValue();
				
				if(passStat != 0 && isYAxisLogScale) {
					algoSeriesName.add(passNum, Math.log10(passStat));
				}
				else {
					algoSeriesName.add(passNum, passStat);	
				}
				
			}
			
			graphDataset.addSeries(algoSeriesName);
		}

		return graphDataset;
	}

	private static JFreeChart createPerPassChart(XYDataset dataset, String title, String xaxisLabel, String yaxisLabel)
	{
		JFreeChart chart = createChart(dataset, title, xaxisLabel, yaxisLabel);
		
		XYPlot plot = chart.getXYPlot();
		NumberAxis xaxis = (NumberAxis) plot.getDomainAxis();
		xaxis.setTickUnit(new NumberTickUnit(1));
		xaxis.setLowerBound(1);
		plot.setDomainAxis(xaxis);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseShapesVisible(true);

		return chart;
	}

	private static JFreeChart createMinSupChart(XYDataset dataset, String title, String xaxisLabel, String yaxisLabel)
	{
		JFreeChart chart = createChart(dataset, title, xaxisLabel, yaxisLabel);

        // We want values of higher minimum support percentages to be shown first, in
        // accordance with the graphs in the paper.
		chart.getXYPlot().getDomainAxis().setInverted(true);

		return chart;
	}

	/*
	 * Configure the various chart options here.
	 */
	private static JFreeChart createChart(XYDataset dataset, String title, String xaxisLabel, String yaxisLabel)
	{
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            title,      // chart title
            xaxisLabel,     // x axis label
            yaxisLabel,             // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // We wanted the colours to be a bit darker. So, that is why explicitly setting the
        // series colours here.
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRenderer().setSeriesPaint(0, Color.RED);
        plot.getRenderer().setSeriesPaint(1, Color.BLUE);
        plot.getRenderer().setSeriesPaint(2, Color.BLACK);
        plot.getRenderer().setSeriesPaint(3, Color.ORANGE);
        plot.getRenderer().setSeriesPaint(4, Color.MAGENTA);

        return chart;
	}
}
