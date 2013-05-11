import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import model.Algorithm;
import model.Dataset;
import model.MinSup;
import util.GraphUtils;
import algos.AIS;
import algos.Apriori;
import algos.AprioriHybrid;
import algos.AprioriTid;
import algos.SETM;

import com.google.common.collect.Maps;

/**
 * Run experiments on all the frequent itemset mining algorithms for various configurations.
 * Save the results and use it to plot graphs for visual analysis of the algorithms.
 * 
 * @author shishir
 *
 */
public class RunAllExperiments {

	public static void main(String[] args) throws IOException {
		int experimentRunTime = 0;
		//Dataset[] datasetsToTest = new Dataset[1];
		//datasetsToTest[0] = Dataset.T5_I2_D100K;
		//MinSup[] minSupsToTest = new MinSup[1];
		//minSupsToTest[0] = MinSup.POINT_TWO_FIVE_PERCENT;

		
		//Warm up database
		//for(MinSup minSup : MinSup.values())
		//	Apriori.runExperiment(Dataset.T10_I2_D100K, minSup);
		
		System.out.println("Apriori");
		for(Dataset dataset : Dataset.values()) {
			System.out.println(dataset.toString());
			String fileName = "Apriori_" + dataset.toString() + ".gnu";
			File file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(MinSup minSup : MinSup.values()) {
				// Apriori
				restartMySQL();
				experimentRunTime = Apriori.runExperiment(dataset, minSup);
				bw.write(minSup.getMinSupPercentage() + "\t" + experimentRunTime + "\n");
				System.out.println(minSup.getMinSupPercentage() + "\t" + experimentRunTime);
			}
			
			bw.close();
		}
		
		System.out.println("AprioriTid");
		for(Dataset dataset : Dataset.values()) {
			System.out.println(dataset.toString());
			String fileName = "AprioriTid_" + dataset.toString() + ".gnu";
			File file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(MinSup minSup : MinSup.values()) {
				// AprioriTid
				restartMySQL();
				experimentRunTime = AprioriTid.runExperiment(dataset, minSup);
				bw.write(minSup.getMinSupPercentage() + "\t" + experimentRunTime + "\n");
				System.out.println(minSup.getMinSupPercentage() + "\t" + experimentRunTime);
			}
			
			bw.close();
		}
		
		System.out.println("AprioriHybrid");
		for(Dataset dataset : Dataset.values()) {
			System.out.println(dataset.toString());
			String fileName = "AprioriHybrid_" + dataset.toString() + ".gnu";
			File file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(MinSup minSup : MinSup.values()) {
				// AprioriHybrid
				restartMySQL();
				experimentRunTime = AprioriHybrid.runExperiment(dataset, minSup);
				bw.write(minSup.getMinSupPercentage() + "\t" + experimentRunTime + "\n");
				System.out.println(minSup.getMinSupPercentage() + "\t" + experimentRunTime);
			}
			
			bw.close();
		}
		
		System.out.println("AIS");
		for(Dataset dataset : Dataset.values()) {
			System.out.println(dataset.toString());
			String fileName = "AIS_" + dataset.toString() + ".gnu";
			File file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(MinSup minSup : MinSup.values()) {
				// AIS
				restartMySQL();
				experimentRunTime = AIS.runExperiment(dataset, minSup);
				bw.write(minSup.getMinSupPercentage() + "\t" + experimentRunTime + "\n");
				System.out.println(minSup.getMinSupPercentage() + "\t" + experimentRunTime);
			}
			
			bw.close();
		}
		
		System.out.println("SETM");
		for(Dataset dataset : Dataset.values()) {
			System.out.println(dataset.toString());
			String fileName = "SETM_" + dataset.toString() + ".gnu";
			File file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for(MinSup minSup : MinSup.values()) {
				// SETM
				restartMySQL();
				experimentRunTime = SETM.runExperiment(dataset, minSup);
				bw.write(minSup.getMinSupPercentage() + "\t" + experimentRunTime + "\n");
				System.out.println(minSup.getMinSupPercentage() + "\t" + experimentRunTime);
			}
			
			bw.close();
		}
	}
	
	private static void restartMySQL()
	{
		executeCmd("net stop mysql56");
		executeCmd("net start mysql56");
	}
	
	/*
	 * Saves the results of each algorithmic experiment to be analyzed via graphs.
	 */
	private static void insertIntoAlgoRunTimeMap(
		Map<Algorithm, Map<MinSup, Integer>> algoRunTimeMap, Algorithm algo, MinSup minSup, int runTime)
	{
		Map<MinSup, Integer> minSupRunTimeMap = null;
		if(algoRunTimeMap.containsKey(algo)) {
			minSupRunTimeMap = algoRunTimeMap.get(algo);
		}
		else {
			minSupRunTimeMap = Maps.newHashMap();
		}
		
		minSupRunTimeMap.put(minSup, runTime);
		algoRunTimeMap.put(algo, minSupRunTimeMap);
	}
	
	/*
	 * Prints the details about the frequent itemset mining algorithm experiment.
	 */
	@SuppressWarnings("unused")
	private static void printExperimentStats(
			Algorithm algo, Dataset dataset, MinSup minSup, int runTime)
	{
		System.out.println(
			"{Algorithm: " + algo.toString() + ", Dataset: " + dataset.toString() + ", " +
			"MinSup: " + minSup.toString() + "%, RunTime: " + runTime + " s.}"
		);
	}
	
	public static void executeCmd(String dosCommand) {
      //final String dosCommand = "net start mysql56";
      try
	{
		clearCache(dosCommand);
	} catch (IOException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InterruptedException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  /*
	  System.out.println(dosCommand);
	  final String location = "C:\\WINDOWS";
      try {
         final Process process = Runtime.getRuntime().exec(
            dosCommand + " " + location);
         final InputStream in = process.getInputStream();
         int ch;
         while((ch = in.read()) != -1) {
            System.out.print((char)ch);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      */
   }
	
	public static void clearCache(String clearCacheCmd) throws IOException, InterruptedException
	{
		Runtime runTime = Runtime.getRuntime();
		String[] tokens = clearCacheCmd.split(" ");
		Process proc = runTime.exec(tokens);
		int exitValue = proc.waitFor();
		//System.out.println(exitValue);
		if(exitValue != 0) {
			int len = proc.getErrorStream().available(); 
			byte[] buf = new byte[len]; 
		proc.getErrorStream().read(buf); 
		System.err.println("Command error:\t\""+new String(buf)+"\""); 
		}
	}
}
