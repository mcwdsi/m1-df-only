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
import java.util.Set;

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
	//	runOnMdcSubset("small-set");
	//	runOnMdcSubset("restricted");
	//	runOnMdcSubset("limited");
    }

    public static void runOnTestCollection() throws CloneNotSupportedException {
		// loop through all software to test M1-data-format-only search's ability to find the full "DFM deductive closure."
		ArrayList<Integer> softwareList = new ArrayList<>(Arrays.asList(1000,1001,1002,2000, 2001, 2002)); //1000,1001,1002,2000, 2001, 2002 somehow causes cycle
		Iterator<Integer> iterSoftwareList = softwareList.iterator();
		SoftwareDatasetDataFormatRepository sddfr = SoftwareDatasetDataFormatRepository.createTestCollectionInstance();
		GenerateAndTest.sddfr = sddfr;
		while(iterSoftwareList.hasNext()) {
			int softwareID = iterSoftwareList.next();
			SoftwareNode s = new SoftwareNode(softwareID);

			// get the info about software from the repository   get input ports and get output ports
			ArrayList<ArrayList<Integer>> inputs = sddfr.getInputDataFormatsForSoftware(softwareID);
			ArrayList<ArrayList<Integer>> outputs = sddfr.getOutputDataFormatsForSoftware(softwareID);
			Iterator<ArrayList<Integer>> iterI = inputs.iterator();
			Iterator<ArrayList<Integer>> iterO = outputs.iterator();

			int portCtr = 0; 
			ArrayList<Integer> portDataFormats;
			ArrayList<Integer> dataFormatIds = new ArrayList<Integer>();
			while (iterI.hasNext()) {
				portDataFormats = iterI.next();
				dataFormatIds.addAll(portDataFormats); // get the 1 data format for the inport into ArrayList<Integer> dataFormatIds
				SoftwarePort p = new SoftwarePort(softwareID, PortType.INPUT, dataFormatIds);
				p.setPortId(portCtr++);
				s.inputPorts.add(p);
			}
			
			// have to hack any multiDF ports. Here's where we replace inport DF list.
			// Specifically, we replace the one-DF list of dataformats for SoftwareNode 2002 input[0] with a list of data formats 3 and 6
			/*
			I moved this to creation of test repository in SoftwareDatasetDataFomratRepository
			if(softwareID==2002) {
				ArrayList<Integer> x = new ArrayList<Integer>();
				x.add(3);
				x.add(6);
				s.inputPorts.get(0).setDataFormats(x);;
			}
			*/
			
			portCtr = 0;
			while (iterO.hasNext()) {
				portDataFormats = iterO.next();
				dataFormatIds.addAll(portDataFormats); // get the 1 data format for the inport into ArrayList<Integer> dataFormatIds
				SoftwarePort p = new SoftwarePort(softwareID, PortType.OUTPUT, dataFormatIds);
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
		ArrayList<MutableValueGraph<Node, Integer>> gListDeDuplicated = 
			new ArrayList<MutableValueGraph<Node, Integer>>();
		for (MutableValueGraph<Node, Integer> gi : GenerateAndTest.gList) {
			boolean added = graphStringSet.add(GenerateAndTest.graphToString(gi));
			//System.out.println(added);
			if (added)
				gListDeDuplicated.add(gi);
		}
	
		System.out.println("Number of unique graph strings: " + graphStringSet.size());

		System.out.println("BEGIN GRAPH STRING SET");
		ArrayList<String> graphStringsAsList = new ArrayList<String>();
		graphStringsAsList.addAll(graphStringSet);
		Collections.sort(graphStringsAsList);
		for (String gs : graphStringsAsList) { System.out.println("\t" + gs); }
		System.out.println("END GRAPH STRING SET");

		generateSummaryStatistics(gListDeDuplicated);
		
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
		ArrayList<MutableValueGraph<Node, Integer>> gListDeDuplicated = 
			new ArrayList<MutableValueGraph<Node, Integer>>();
		for (MutableValueGraph<Node, Integer> gi : GenerateAndTest.gList) {
			boolean added = graphStringSet.add(GenerateAndTest.graphToString(gi));
			//System.out.println(added);
			if (added)
				gListDeDuplicated.add(gi);
		}
	
		System.out.println("Number of unique graph strings: " + graphStringSet.size());

		System.out.println("BEGIN GRAPH STRING SET");
		ArrayList<String> graphStringsAsList = new ArrayList<String>();
		graphStringsAsList.addAll(graphStringSet);
		Collections.sort(graphStringsAsList);
		for (String gs : graphStringsAsList) { System.out.println("\t" + gs); }
		System.out.println("END GRAPH STRING SET");

		generateSummaryStatistics(gListDeDuplicated);
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
			//System.out.println(dsNodes.size() + " datasets.");
			//System.out.println("\tDEBUG: " + dsNodes.get(0).id + "\t" + dsNodes.get(0).title + "\t" + dsNodes.get(0).formatId);
			dm = new DatasetManager(dsNodes);
			/*
			Iterator<DatasetNode> i = dm.getDatasetNodesForFormatId(Integer.valueOf(50));
			System.out.println("DEBUG: Datasets for formatId=" + 50);
			while (i.hasNext()) {
				DatasetNode d = i.next();
				System.out.println("\tDEBUG: " + d.id + "\t" + d.title + "\t" + d.formatId);
			}
			*/	
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

 	public static void generateSummaryStatistics(ArrayList<MutableValueGraph<Node, Integer>> gList) {
 		HashSet<Integer> uniqueSoftwareIds = new HashSet<Integer>();
 		HashMap<Integer, Integer> numberOfNodesToNumberOfGraphs = new HashMap<Integer, Integer>();

 		for (MutableValueGraph<Node, Integer> g: gList) {
 			Integer numNodes = g.nodes().size();
 			if (numberOfNodesToNumberOfGraphs.containsKey(numNodes)) {
 				Integer numGraphs = numberOfNodesToNumberOfGraphs.get(numNodes);
 				numGraphs++;
 				numberOfNodesToNumberOfGraphs.put(numNodes, numGraphs);
 			} else {
 				numberOfNodesToNumberOfGraphs.put(numNodes, Integer.valueOf(1));
 			}

 			Iterator<Node> nodeIter = g.nodes().iterator();
 			while(nodeIter.hasNext()) {
 				Node n = nodeIter.next();
 				SoftwareNode sn = (SoftwareNode)n;
 				uniqueSoftwareIds.add(sn.uid);
 			}
 		}

 		System.out.print("Ids of unique software across all graphs:\n\t");
 		for (Integer id : uniqueSoftwareIds) {
 			System.out.print(id + ", ");
 		}
 		System.out.println();


 		Set<Integer> numNodeSet = numberOfNodesToNumberOfGraphs.keySet();
 		ArrayList<Integer> numNodeList = new ArrayList<Integer>();
 		numNodeList.addAll(numNodeSet);
 		Collections.sort(numNodeList);
 		for (Integer i : numNodeList) {
 			Integer numGraphs = numberOfNodesToNumberOfGraphs.get(i);
 			System.out.println("There are " + numGraphs + " graphs with " + i + " nodes.");
 		}
 	}

}