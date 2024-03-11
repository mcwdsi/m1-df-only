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
			int id = sn.softwareId;

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
			int id = sn.softwareId;

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
	
	public boolean isDataService(Integer softwareId) {
		if (this.dataServices.contains(softwareId))                      
			return true;
		else
			return false;
	}
}
