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
import com.google.gson.JsonObject;
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
		//runOnMdcSubset("precision-eval");
		runOnMdcSubset("curated");
    }

    public static void runOnTestCollection() throws CloneNotSupportedException {
		// loop through all software to test M1-data-format-only search's ability to find the full "DFM deductive closure."
		ArrayList<Integer> softwareList = new ArrayList<>(Arrays.asList( 1000,1001,1002,2000, 2001, 2002, 2002, 2003)); //1000,1001,1002,2000, 2001, 2002 somehow causes cycle
		Iterator<Integer> iterSoftwareList = softwareList.iterator();
		SoftwareDatasetDataFormatRepository sddfr = SoftwareDatasetDataFormatRepository.createTestCollectionInstance();
		GenerateAndTest.sddfr = sddfr;
		ArrayList<MutableValueGraph<Node, Integer>> gList = new ArrayList<MutableValueGraph<Node, Integer>>();
		GenerateAndTest.gList = gList;
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
			
			while (iterI.hasNext()) {
				portDataFormats = iterI.next();
				ArrayList<Integer> dataFormatIds = new ArrayList<Integer>();
				dataFormatIds.addAll(portDataFormats); // get the 1 data format for the inport into ArrayList<Integer> dataFormatIds
				SoftwarePort p = new SoftwarePort(softwareID, PortType.INPUT, dataFormatIds);
				p.setPortId(portCtr++);
				s.inputPorts.add(p);
			}
			
			portCtr = 0;
			while (iterO.hasNext()) {
				portDataFormats = iterO.next();
				ArrayList<Integer> dataFormatIds = new ArrayList<Integer>();
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
			if(GenerateAndTest.isAbstractWorkflow(g)) 
				GenerateAndTest.backSearch(g);
			else
				GenerateAndTest.forwardSearch(g);
		}

		postProcessGraphs();
	}

	public static void runOnMdcSubset(String subsetName) {
		DatasetManager dm = loadDatasets(
			"src/main/resources/mdc-datasets-" + subsetName + ".json");
		SoftwareManager sm = loadSoftware(
			"src/main/resources/mdc-software-" + subsetName + ".json");
		SoftwareDatasetDataFormatRepository sddfr = 
			new SoftwareDatasetDataFormatRepository(dm, sm);

		ArrayList<MutableValueGraph<Node, Integer>> gList = new ArrayList<MutableValueGraph<Node, Integer>>();
		GenerateAndTest.gList = gList;
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

			//branch on whether graph is an abstract or concrete workflow, at this stage they mean a new software or a new dataset
			if(GenerateAndTest.isAbstractWorkflow(g)) 
				GenerateAndTest.backSearch(g);
			else
				GenerateAndTest.forwardSearch(g);
		}

		postProcessGraphs();
	}

	public static void postProcessGraphs() {	
		System.out.println("Number of graphs generated: " + GenerateAndTest.gList.size());

		ArrayList<MutableValueGraph<Node, Integer>> gListDeDup = deduplicateGraphs(GenerateAndTest.gList);
		System.out.println("\n\nDEDUPLICATED GRAPHS SIZE: " + gListDeDup.size());

		ArrayList<String> dedupStrings = generateSortedGraphStrings(gListDeDup);
		System.out.println("DEDUPLICATED GRAPHS STRING LIST SIZE: " + dedupStrings.size() + "\n\n");

		System.out.println("BEGIN GRAPH STRING SET");
		for (String gs : dedupStrings) { System.out.println(gs); }
		System.out.println("END GRAPH STRING SET");

		generateSummaryStatistics(gListDeDup);
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
					//DatasetNode dn = gs.fromJson(jei, DatasetNode.class);
					JsonObject joi = jei.getAsJsonObject();
					JsonElement idAsJe = joi.get("id");
					JsonElement formatIdAsJe = joi.get("formatId");
					JsonElement titleAsJe = joi.get("title");
					int id = idAsJe.getAsInt();
					int formatId = formatIdAsJe.getAsInt();
					String title = titleAsJe.getAsString();
					DatasetNode dn = new DatasetNode(id, formatId, title);
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

 		System.out.print("Number of unique software across all graphs: " +
 				uniqueSoftwareIds.size() + ":\n\t");
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

 	public static ArrayList<MutableValueGraph<Node, Integer>> deduplicateGraphs(
 		ArrayList<MutableValueGraph<Node, Integer>> gList) {
 		
 		ArrayList<MutableValueGraph<Node, Integer>> gListDeDup = 
 			new ArrayList<MutableValueGraph<Node,Integer>>();

 		for (MutableValueGraph<Node,Integer> gNext : gList) {
 			if (gListDeDup.isEmpty()) {
 				gListDeDup.add(gNext);
 			} else {
 				boolean add = true;
 				for (MutableValueGraph<Node, Integer> gDeDupNext : gListDeDup) {
 					add = add && !GenerateAndTest.graphsAreEqual(gNext, gDeDupNext);
 				}
 				if (add) {
 					gListDeDup.add(gNext);
 				}
 			}
 		}
 		return gListDeDup;
 	}

 	public static ArrayList<String> generateSortedGraphStrings(
 		ArrayList<MutableValueGraph<Node, Integer>> gList) {
		
		ArrayList<String> graphStrings = new ArrayList<String>();
		for (MutableValueGraph<Node, Integer> gi : gList) {
			graphStrings.add(GenerateAndTest.graphToString(gi));
		}

		Collections.sort(graphStrings);
		return graphStrings;
 	}

}