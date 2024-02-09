package edu.pitt.mdc.m1;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;


public class TestM1 {

	public static void main(String args[]) throws CloneNotSupportedException
	{
		//	Software(Integer objectId, PortType portType, int portNumber) 
		runOnTestCollection();
		runOnMdcSubset("small-set");
		runOnMdcSubset("restricted");
		//runOnMdcSubset("limited");
    }

    public static void runOnTestCollection() throws CloneNotSupportedException {
		// loop through all software to test M1-data-format-only search's ability to find the full "DFM deductive closure."
		ArrayList<Integer> softwareList = new ArrayList<>(Arrays.asList(1000,1001,1002,2000, 2001,2002)); //2000 somehow causes cycle
		Iterator<Integer> iterSoftwareList = softwareList.iterator();
		SoftwareDatasetDataFormatRepository sddfr = SoftwareDatasetDataFormatRepository.createTestCollectionInstance();
		GenerateAndTest.sddfr = sddfr;
		while(iterSoftwareList.hasNext()) {
			int softwareID = iterSoftwareList.next();
			SoftwareNode s = new SoftwareNode(softwareID);

			// get the info about software from the repository   get input ports and get output ports
			ArrayList<Integer> inputs = sddfr.getInputDataFormatsForSoftware(softwareID);
			ArrayList<Integer> outputs = sddfr.getOutputDataFormatsForSoftware(softwareID);
			Iterator<Integer> iterI = inputs.iterator();
			Iterator<Integer> iterO = outputs.iterator();

			int portCtr = 0;
			while (iterI.hasNext()) {
				//SoftwarePort(Integer softwareID, int type, Integer dataFormatID)
				SoftwarePort p = new SoftwarePort(softwareID, PortType.INPUT, iterI.next());
				p.setPortId(portCtr++);
				s.inputPorts.add(p);
			}

			portCtr = 0;
			while (iterO.hasNext()) {
				SoftwarePort p = new SoftwarePort(softwareID, PortType.OUTPUT, iterO.next());
				p.setPortId(portCtr++);
				s.outputPorts.add(p);
			}

			MutableValueGraph<Node, Integer> g = ValueGraphBuilder.directed().build();
			g.addNode(s);
			System.out.println("***********************************");
			System.out.println("SOFTWARE " + softwareID + " is triggering M1 search:");
			System.out.println("***********************************");
			GenerateAndTest.printGraph(g);
			//Should branch on whether graph is an abstract or concrete workflow, at this stage they mean a new software or a new dataset
			GenerateAndTest.backSearch(g);


		}

		System.out.println("Number of graphs generated: " + GenerateAndTest.gList.size());
		HashSet<String> graphStringSet = new HashSet<String>();
		for (MutableValueGraph<Node, Integer> gi : GenerateAndTest.gList) {
			String canonicalGraphString = GenerateAndTest.graphToString(gi);
			String nonCanonicalGraphString = GenerateAndTest.graphToStringNonStrictOrdering(gi);
			if (graphStringSet.contains(canonicalGraphString)) {
				System.out.println("*******************************");
				System.out.println("* CANONICAL GRAPH STRING ******");
				System.out.println("*******************************");
				System.out.println(canonicalGraphString);
				System.out.println("****************************");
				System.out.println("*******************************");
				System.out.println("* NON-CANONICAL GRAPH STRING **");
				System.out.println("*******************************");
				System.out.println(nonCanonicalGraphString);
				System.out.println("****************************");
				System.out.println();
			}
			graphStringSet.add(canonicalGraphString);
		}
		System.out.println("Number of unique graph strings: " + graphStringSet.size());
		/*
			Double check to make sure that we cannot force duplicates into the mix.
		*/
		graphStringSet.add(GenerateAndTest.graphToString(GenerateAndTest.gList.get(10)));
		graphStringSet.add(GenerateAndTest.graphToString(GenerateAndTest.gList.get(17)));
		System.out.println("Number of unique graph strings: " + graphStringSet.size());
	
		System.out.println("BEGIN GRAPH STRING SET");
		ArrayList<String> graphStringsAsList = new ArrayList<String>();
		graphStringsAsList.addAll(graphStringSet);
		Collections.sort(graphStringsAsList);
		for (String gs : graphStringsAsList) { System.out.println(gs); }
		System.out.println("END GRAPH STRING SET");
		
		/*	System.out.println("\n\n***** SEARCH STATISTICS *********");
		ArrayList<MutableValueGraph<Node, Integer>> gList;
		gList =GenerateAndTestGuava.getGraphList(); 
			
		System.out.println("Number of graphs = " + gList.size());
		System.out.println("Size distribution (number of software nodes)");
		
		Iterator<MutableValueGraph<Node, Integer>> iter = gList.iterator();
		
		MutableValueGraph<Node, Integer> g;
		
		// ndc means nodeCountDistribution
		ArrayList<Integer> ndc = new ArrayList<Integer>();
		// initialize with zero counts
		
		int nc;  //nodecount
		int previousNc;
		while (iter.hasNext() ) {
			g = iter.next();
			nc = g.nodes().size();
			previousNc = ndc.get(nc);
			ndc.set(nc,++previousNc);

		}
	*/	
	}

	public static void runOnMdcSubset(String subsetName) {
		DatasetManager dm = loadDatasets(
			"src/main/resources/mdc-datasets-" + subsetName + ".json");
		SoftwareManager sm = loadSoftware(
			"src/main/resources/mdc-software-" + subsetName + ".json");
		SoftwareDatasetDataFormatRepository sddfr = 
			new SoftwareDatasetDataFormatRepository(dm, sm);

		GenerateAndTest.gList = null;
		GenerateAndTest.sddfr = sddfr;
		GenerateAndTest.sm = sm;
		GenerateAndTest.dm = dm;
		Iterator<SoftwareNode> iterSoftwareList = sm.softwareNodeIterator();
		while (iterSoftwareList.hasNext()) {
			SoftwareNode s = iterSoftwareList.next();
			int softwareID = s.uid;
			String softwareTitle = s.title;
			MutableValueGraph<Node, Integer> g = ValueGraphBuilder.directed().build();
			g.addNode(s);
			System.out.println("***********************************");
			System.out.println("SOFTWARE " + softwareID + " (" + softwareTitle + ") is triggering M1 search:");
			System.out.println("***********************************");
			GenerateAndTest.printGraph(g);
			//Should branch on whether graph is an abstract or concrete workflow, at this stage they mean a new software or a new dataset
			GenerateAndTest.backSearch(g);
		}

		System.out.println("Number of graphs generated: " + GenerateAndTest.gList.size());
		HashSet<String> graphStringSet = new HashSet<String>();
		for (MutableValueGraph<Node, Integer> gi : GenerateAndTest.gList) {
			graphStringSet.add(GenerateAndTest.graphToString(gi));
		}
		/*
		*	Try to force a duplicate or two
		*/
		Random rnd = new Random();
		int dup1 = rnd.nextInt(GenerateAndTest.gList.size());
		int dup2 = rnd.nextInt(GenerateAndTest.gList.size());
		graphStringSet.add(GenerateAndTest.graphToString(GenerateAndTest.gList.get(dup1)));
		graphStringSet.add(GenerateAndTest.graphToString(GenerateAndTest.gList.get(dup2)));
		System.out.println("Number of unique graph strings: " + graphStringSet.size());

			System.out.println("BEGIN GRAPH STRING SET");
			ArrayList<String> graphStringsAsList = new ArrayList<String>();
			graphStringsAsList.addAll(graphStringSet);
			Collections.sort(graphStringsAsList);
			for (String gs : graphStringsAsList) { System.out.println(gs); }
			System.out.println("END GRAPH STRING SET");
	}

	public static DatasetManager loadDatasets(String fileName) {
		
		ArrayList<DatasetNode> dsNodes = new ArrayList<>();

		DatasetManager dm=null;
		try {
			FileReader fr = new FileReader(fileName);
			JsonReader jr = new JsonReader(fr);
			JsonElement je = JsonParser.parseReader(jr);
			Gson gs = new Gson();
			if (je.isJsonArray()) {
				JsonArray ja = je.getAsJsonArray();
				Iterator<JsonElement> i = ja.iterator();
				while (i.hasNext()) {
					JsonElement jei = i.next();
					DatasetNode dn = gs.fromJson(jei, DatasetNode.class);
					dsNodes.add(dn);
				}
			}
			System.out.println(dsNodes.size() + " datasets.");
			//System.out.println("\tDEBUG: " + dsNodes.get(0).id + "\t" + dsNodes.get(0).title + "\t" + dsNodes.get(0).formatId);
			dm = new DatasetManager(dsNodes);
			Iterator<DatasetNode> i = dm.getDatasetNodesForFormatId(Integer.valueOf(50));
			System.out.println("DEBUG: Datasets for formatId=" + 50);
			while (i.hasNext()) {
				DatasetNode d = i.next();
				System.out.println("\tDEBUG: " + d.id + "\t" + d.title + "\t" + d.formatId);
			}	
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return dm;	
	}

 	public static SoftwareManager loadSoftware(String fileName) {
		File f = new File(fileName);
		SoftwareManager sm = new SoftwareManager();
		sm.loadFromJsonFile(f);
		return sm;
 	}

}