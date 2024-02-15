package edu.pitt.mdc.m1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SoftwareDatasetDataFormatRepository {
	
	HashMap<Integer, ArrayList<DigitalResearchObject>> objectList;
	HashMap<Integer, ArrayList<Software>> softwareList;
	HashMap<Integer, ArrayList<ArrayList<Integer>>> inputsAndFormats;
	HashMap<Integer, ArrayList<ArrayList<Integer>>> outputsAndFormats;
	ArrayList<Integer> dataServices;

	public static SoftwareDatasetDataFormatRepository createTestCollectionInstance() {
		SoftwareDatasetDataFormatRepository testCollection = 
			new SoftwareDatasetDataFormatRepository();
		testCollection.objectList = createObjectListTestCollection();
		testCollection.softwareList = createSoftwareListTestCollection();
		testCollection.inputsAndFormats = createInputsAndFormatsTestCollection();
		testCollection.outputsAndFormats = createOutputsAndFormatsTestCollection();
		testCollection.dataServices = createDataServicesTestCollection();
		return testCollection;
	}

	private SoftwareDatasetDataFormatRepository() {
	}

	/*
		TODO, update the constructor to take a DatasetManager and a SoftwareManager
			and build the Hashes from there.
	*/
	public SoftwareDatasetDataFormatRepository(DatasetManager dm,
		SoftwareManager sm) {
		buildObjectList(dm, sm); //also builds outputsAndFormats
		buildSoftwareList(sm);   //also builds inputsAndFormats
		this.dataServices = sm.dataServicesIds;
	}

	public static HashMap<Integer, ArrayList<DigitalResearchObject>> createObjectListTestCollection() {
		
		HashMap<Integer, ArrayList<DigitalResearchObject>> formatToObjectList = 
			new HashMap<Integer, ArrayList<DigitalResearchObject>>();
		//Dataset( String title, Integer datasetId, Integer dataFormat)
		//Software(String title, Integer softwareId, PortType portType, int portNumber)

		ArrayList<DigitalResearchObject> objectList1 = new ArrayList<DigitalResearchObject>();
			objectList1.add(new Dataset("Dataset 100", 100, 1));  
			objectList1.add(new Dataset("Dataset 101", 101, 1));
			objectList1.add(new Dataset("Dataset 102", 102, 1));
			objectList1.add(new Software("Software 1000", 1000, PortType.OUTPUT, 0, 1));  
			objectList1.add(new Software("Software 1001", 1001, PortType.OUTPUT, 0, 1));
			objectList1.add(new Software("Software 1002", 1002, PortType.OUTPUT, 0, 1));
			objectList1.add(new Software("Software 2001", 2001, PortType.OUTPUT, 0, 1));
		formatToObjectList.put(Integer.valueOf(1), objectList1);
			
		ArrayList<DigitalResearchObject> objectList2 = new ArrayList<DigitalResearchObject>();
			objectList2.add(new Dataset("Dataset 200", 200, 2));
			objectList2.add(new Dataset("Dataset 201", 201, 2));
			objectList2.add(new Dataset("Dataset 202", 202, 2));
			objectList2.add(new Software("Software 2000", 2000, PortType.OUTPUT, 0, 2));
		formatToObjectList.put(Integer.valueOf(2), objectList2);	
		
		ArrayList<DigitalResearchObject> objectList3 = new ArrayList<DigitalResearchObject>();
			objectList3.add(new Dataset("Dataset 300", 300, 3));
			objectList3.add(new Dataset("Dataset 301", 301, 3));
			objectList3.add(new Dataset("Dataset 302", 302, 3));
		formatToObjectList.put(Integer.valueOf(3), objectList3);
		
		ArrayList<DigitalResearchObject> objectList4 = new ArrayList<DigitalResearchObject>();
			objectList4.add(new Dataset("Dataset 400", 400, 4));
		formatToObjectList.put(Integer.valueOf(4), objectList4);

		ArrayList<DigitalResearchObject> objectList5 = new ArrayList<DigitalResearchObject>();
			//objectList5.add(new Dataset("Dataset 500", 500, 5));
		formatToObjectList.put(Integer.valueOf(5), objectList5);	

		ArrayList<DigitalResearchObject> objectList6 = new ArrayList<DigitalResearchObject>();
			objectList6.add(new Dataset("Dataset 600", 600, 6));   // TODO: comment out to to see if the code breaks 
			objectList6.add(new Software("Software 1000", 1000, PortType.OUTPUT, 1, 6)); // software 1000 has two outputs.  Make sure it is consistent with getOutputDataFormatsForSoftware
		formatToObjectList.put(Integer.valueOf(6), objectList6);	
		
		return formatToObjectList;
	}

	// Given a data format, this method returns a heterogeneous list of datasets and software using that format.
	// The returned software uses the data format for at least one OUTPUT
		public ArrayList<DigitalResearchObject> objectsByDataformat(Integer dataFormatID){
			if (!this.objectList.containsKey(dataFormatID)) {
				ArrayList<DigitalResearchObject> list = new ArrayList<DigitalResearchObject>();
				this.objectList.put(dataFormatID, list);
			}			
			ArrayList<DigitalResearchObject> x = new ArrayList<DigitalResearchObject>();
			x.addAll(this.objectList.get(dataFormatID));
			return x;
		}
			
	public static HashMap<Integer, ArrayList<Software>> createSoftwareListTestCollection() {
		
		HashMap<Integer, ArrayList<Software>> formatToSoftwareList = 
			new HashMap<Integer, ArrayList<Software>>();

		ArrayList<Software> softwareList1 = new ArrayList<Software>();
		formatToSoftwareList.put(Integer.valueOf(1), softwareList1);

		ArrayList<Software> softwareList2 = new ArrayList<Software>();  // Data format 2
			softwareList2.add(new Software("Software 1000", 1000, PortType.INPUT, 0, 2));  
			softwareList2.add(new Software("Software 1001", 1001, PortType.INPUT, 0, 2));   
		formatToSoftwareList.put(Integer.valueOf(2), softwareList2);

		ArrayList<Software> softwareList3 = new ArrayList<Software>();
			softwareList3.add(new Software("Software 1000", 1000, PortType.INPUT, 1, 3));  
			softwareList3.add(new Software("Software 2000", 2000, PortType.INPUT, 0, 3)); 
			softwareList3.add(new Software("Software 2002", 2002, PortType.INPUT, 0, 3)); 
		formatToSoftwareList.put(Integer.valueOf(3), softwareList3);	
		
		ArrayList<Software> softwareList4 = new ArrayList<Software>();
			softwareList4.add(new Software("Software 2000", 2000, PortType.INPUT, 1, 4)); 
		formatToSoftwareList.put(Integer.valueOf(4), softwareList4);			
			
		ArrayList<Software> softwareList5 = new ArrayList<Software>();
			softwareList5.add(new Software("Software 1002", 1002, PortType.INPUT, 0, 5)); 
		formatToSoftwareList.put(Integer.valueOf(5), softwareList5);			
			
		ArrayList<Software> softwareList6 = new ArrayList<Software>();
			softwareList6.add(new Software("Software 2001", 2001, PortType.INPUT, 0, 6)); 
			softwareList6.add(new Software("Software 2002", 2002, PortType.INPUT, 0, 6)); //making this input port multi-data-format (3 and 6)
		formatToSoftwareList.put(Integer.valueOf(6), softwareList6);			
		
		return formatToSoftwareList;			
	}

		// Given a data format, this method returns software that use that data format for at least 1 input
		// If a software were to have 2 inputs using the same data format, and they were both represented as lines in this file
		// this method would return two instances of Software
		// N.B.  "Software" isn't a great name for the class.
		
		// Software(String title, Integer softwareId, PortType portType, int portNumber)
		
		public ArrayList<Software> softwareByInputDataformat(Integer dataFormatID){
			if (!this.softwareList.containsKey(dataFormatID)) {
				ArrayList<Software> list = new ArrayList<Software>();
				this.softwareList.put(dataFormatID, list);
			}
			ArrayList<Software> x = new ArrayList<Software>();
			x.addAll(this.softwareList.get(dataFormatID));
			return x;
		}	
		

		// This method assumes one data format per port.  It has to be replaced.
	public static HashMap<Integer, ArrayList<ArrayList<Integer>>> createInputsAndFormatsTestCollection() {
		HashMap<Integer, ArrayList<ArrayList<Integer>>> softwareIdToInputFormatIds =
			new HashMap<Integer, ArrayList<ArrayList<Integer>>>();

		ArrayList<ArrayList<Integer>> inputsAndFormats1000 = new ArrayList<ArrayList<Integer>>();
			inputsAndFormats1000.add(new ArrayList<Integer>(Arrays.asList(2)));   	 //DF IDs.  One row for each input/data format
			inputsAndFormats1000.add(new ArrayList<Integer>(Arrays.asList(3)));
		softwareIdToInputFormatIds.put(Integer.valueOf(1000), inputsAndFormats1000);
		
		ArrayList<ArrayList<Integer>> inputsAndFormats1001 = new ArrayList<ArrayList<Integer>>();
			//inputsAndFormats.add(1);
			inputsAndFormats1001.add(new ArrayList<Integer>(Arrays.asList(2)));
		softwareIdToInputFormatIds.put(Integer.valueOf(1001), inputsAndFormats1001);

		ArrayList<ArrayList<Integer>> inputsAndFormats1002 = new ArrayList<ArrayList<Integer>>();
			inputsAndFormats1002.add(new ArrayList<Integer>(Arrays.asList(5)));
			//inputsAndFormats.add(4);
			//software.add(60);
		softwareIdToInputFormatIds.put(Integer.valueOf(1002), inputsAndFormats1002);

		ArrayList<ArrayList<Integer>> inputsAndFormats2000 = new ArrayList<ArrayList<Integer>>();
			inputsAndFormats2000.add(new ArrayList<Integer>(Arrays.asList(3)));
			inputsAndFormats2000.add(new ArrayList<Integer>(Arrays.asList(4)));
			//software.add(60);
		softwareIdToInputFormatIds.put(Integer.valueOf(2000), inputsAndFormats2000);

		ArrayList<ArrayList<Integer>> inputsAndFormats2001 = new ArrayList<ArrayList<Integer>>();
			inputsAndFormats2001.add(new ArrayList<Integer>(Arrays.asList(6)));
			//inputsAndFormats.add(4);
			//software.add(60);
		softwareIdToInputFormatIds.put(Integer.valueOf(2001), inputsAndFormats2001);
		
		ArrayList<ArrayList<Integer>> inputsAndFormats2002 = new ArrayList<ArrayList<Integer>>();
			inputsAndFormats2002.add(new ArrayList<Integer>(Arrays.asList(3, 6))); //we added format 6 as an option to 2002's lone input!
			//inputsAndFormats.add(4);
			//software.add(60);
		softwareIdToInputFormatIds.put(Integer.valueOf(2002), inputsAndFormats2002);
		
		return softwareIdToInputFormatIds;
	}
		// *** DO NOT DELETE makeSoftwareNode uses this method ****
		// Software are "declared" in this and the following method
		// This method declares the inputs for each software
		// Call this method with a software ID to get the data format for each of its inputs
		// Limitations: Exactly 1 DF per input;  code handles ??? up to 2-input software
		
		public ArrayList<ArrayList<Integer>> getInputDataFormatsForSoftware(Integer softwareID){
			ArrayList<ArrayList<Integer>> x = new ArrayList<ArrayList<Integer>>();
			for (ArrayList<Integer> nextPortFormats : this.inputsAndFormats.get(softwareID)) {
				ArrayList<Integer> formats = new ArrayList<Integer>();
				formats.addAll(nextPortFormats);
				x.add(formats);
			}
			return x;
		}
		
	
	public static HashMap<Integer, ArrayList<ArrayList<Integer>>> createOutputsAndFormatsTestCollection() {
		HashMap<Integer, ArrayList<ArrayList<Integer>>> softwareIdToOutputFormatIds =
			new HashMap<Integer, ArrayList<ArrayList<Integer>>>();

		ArrayList<ArrayList<Integer>> outputsAndFormats1000 = new ArrayList<ArrayList<Integer>>();
			outputsAndFormats1000.add(new ArrayList<Integer>(Arrays.asList(1)));   //Data format ID.  One row for each output/data format
			outputsAndFormats1000.add(new ArrayList<Integer>(Arrays.asList(6))); // software 1000 has 2 outputs
		softwareIdToOutputFormatIds.put(Integer.valueOf(1000), outputsAndFormats1000);

		ArrayList<ArrayList<Integer>> outputsAndFormats1001 = new ArrayList<ArrayList<Integer>>();
			outputsAndFormats1001.add(new ArrayList<Integer>(Arrays.asList(1)));  /// need another format
			//outputsAndFormats1001.add(6);  // software 1001 has 2 outputs
		softwareIdToOutputFormatIds.put(Integer.valueOf(1001), outputsAndFormats1001);

		ArrayList<ArrayList<Integer>> outputsAndFormats1002 = new ArrayList<ArrayList<Integer>>();
			outputsAndFormats1002.add(new ArrayList<Integer>(Arrays.asList(1)));
		softwareIdToOutputFormatIds.put(Integer.valueOf(1002), outputsAndFormats1002);
		
		ArrayList<ArrayList<Integer>> outputsAndFormats2000 = new ArrayList<ArrayList<Integer>>();
			outputsAndFormats2000.add(new ArrayList<Integer>(Arrays.asList(2)));
		softwareIdToOutputFormatIds.put(Integer.valueOf(2000), outputsAndFormats2000);

		ArrayList<ArrayList<Integer>> outputsAndFormats2001 = new ArrayList<ArrayList<Integer>>();
			outputsAndFormats2001.add(new ArrayList<Integer>(Arrays.asList(1)));
		softwareIdToOutputFormatIds.put(Integer.valueOf(2001), outputsAndFormats2001);

		ArrayList<ArrayList<Integer>> outputsAndFormats2002 = new ArrayList<ArrayList<Integer>>();
		//	outputsAndFormats2002.add(2);
		softwareIdToOutputFormatIds.put(Integer.valueOf(2002), outputsAndFormats2002);
	
		return softwareIdToOutputFormatIds;	
	}

		// *** DO NOT DELETE:  makeSoftwareNode uses this method ****
		// GET RID OF THIS COMPLEXITY by creating the structure dynamically by calling the first method in this file
		
		// This method declares the outputs for each software
		// Call this method with a software ID to get the data format for each of its outputs
		// Limitations: Exactly 1 output per software, exactly 1 DF per output
		// MAKE SURE NO SOFTWARE HAS THE SAME INPUT AND OUTPUT FORMATS
		// overloaded identifier conventions:  abcd  a-data format; d=makes identifier unique, given data format
		
		public ArrayList<ArrayList<Integer>> getOutputDataFormatsForSoftware(Integer softwareID){
			ArrayList<ArrayList<Integer>> x = new ArrayList<ArrayList<Integer>>();
			for (ArrayList<Integer> nextPortFormats : this.outputsAndFormats.get(softwareID)){
				ArrayList<Integer> formats = new ArrayList<Integer>();
				formats.addAll(nextPortFormats);
				x.add(formats);
			}
			return x;
		}

	protected void buildObjectList(DatasetManager dm, SoftwareManager sm) {
		this.objectList = new HashMap<Integer, ArrayList<DigitalResearchObject>>();
		this.outputsAndFormats = new HashMap<Integer, ArrayList<ArrayList<Integer>>>();

		Set<Integer> datasetFormats = dm.getUniqueDataFormats();
		for (int nextFormatId : datasetFormats) {
			ArrayList<DigitalResearchObject> droList = 
				new ArrayList<DigitalResearchObject>();
			Iterator<Dataset> datasets = dm.getDatasetsForFormatId(nextFormatId);
			while (datasets.hasNext()) {
				Dataset d = datasets.next();
				droList.add(d);
			}
			this.objectList.put(nextFormatId, droList);
		}

		Iterator<SoftwareNode> softwareNodes = sm.softwareNodeIterator();
		while (softwareNodes.hasNext()) {
			SoftwareNode sn = softwareNodes.next();
			String title = sn.getTitle();
			int id = sn.uid;

			Iterator<SoftwarePort> outputs = sn.outputPorts.iterator();
			int portNum = 0;
			ArrayList<ArrayList<Integer>> snOutputsAndFormats = new ArrayList<ArrayList<Integer>>();
			while (outputs.hasNext()) {
				SoftwarePort output = outputs.next();
				ArrayList<Integer> outputFormatIds = output.getDataFormats();
				snOutputsAndFormats.add(outputFormatIds);
				for (Integer formatId : outputFormatIds) {
					Software s = new Software(title, id, PortType.OUTPUT, portNum, formatId);
					ArrayList<DigitalResearchObject> droList;
					if (!this.objectList.containsKey(formatId)) {
						droList = new ArrayList<DigitalResearchObject>();
						this.objectList.put(formatId, droList);
					} else {
						droList = this.objectList.get(formatId);
					}
					droList.add(s);
				}
				portNum++;
			}
			this.outputsAndFormats.put(id, snOutputsAndFormats);
		}
	}
	
	protected void buildSoftwareList(SoftwareManager sm) {
		this.softwareList = new HashMap<Integer, ArrayList<Software>>();
		this.inputsAndFormats = new HashMap<Integer, ArrayList<ArrayList<Integer>>>();
		
		Iterator<SoftwareNode> softwareNodes = sm.softwareNodeIterator();
		while (softwareNodes.hasNext()) {
			SoftwareNode sn = softwareNodes.next();
			String title = sn.getTitle();
			int id = sn.uid;

			Iterator<SoftwarePort> inputs = sn.inputPorts.iterator();
			int portNum = 0;
			ArrayList<ArrayList<Integer>> snInputsAndFormats = new ArrayList<ArrayList<Integer>>();
			while (inputs.hasNext()) {
				SoftwarePort input = inputs.next();
				ArrayList<Integer> inputFormatIds = input.getDataFormats();
				snInputsAndFormats.add(inputFormatIds);
				for (Integer formatId : inputFormatIds) {
					Software s = new Software(title, id, PortType.INPUT, portNum, formatId);
					ArrayList<Software> sList;
					if (!this.softwareList.containsKey(formatId)) {
						sList = new ArrayList<Software>();
						this.softwareList.put(formatId, sList);
					} else {
						sList = this.softwareList.get(formatId);
					}
					sList.add(s);
				}
				portNum++;
			}

			this.inputsAndFormats.put(id, snInputsAndFormats);
		}
	}		

	public static ArrayList<Integer> createDataServicesTestCollection() {
		ArrayList<Integer> dataServices = new ArrayList<Integer>(Arrays.asList(1000));
		return dataServices;
	}
	
	public boolean isDataService(Integer softwareId) {
		if (this.dataServices.contains(softwareId))                      
			return true;
		else
			return false;
	}
}
