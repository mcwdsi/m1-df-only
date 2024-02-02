package edu.pitt.mdc.m1;

import java.util.ArrayList;
import java.util.Iterator;

public class SoftwareDatasetDataFormatRepository {
	

	// Given a data format, this method returns a heterogeneous list of datasets and software using that format.
	// The returned software uses the data format for at least one OUTPUT

		public static ArrayList<DigitalResearchObject> objectsByDataformat(Integer dataFormatID){
			ArrayList<DigitalResearchObject> objectList = new ArrayList<DigitalResearchObject>();
			switch(dataFormatID) {
			//Dataset( String title, Integer datasetId, Integer dataFormat)
			//Software(String title, Integer softwareId, PortType portType, int portNumber)

			case 1: 	// Data Format 1
				objectList.add(new Dataset("Dataset 100", 100, 1));  
				objectList.add(new Dataset("Dataset 101", 101, 1));
				objectList.add(new Dataset("Dataset 102", 102, 1));
				objectList.add(new Software("Software 1000", 1000, PortType.OUTPUT, 0));  
				objectList.add(new Software("Software 1001", 1001, PortType.OUTPUT, 0));
				objectList.add(new Software("Software 1002", 1002, PortType.OUTPUT, 0));
				objectList.add(new Software("Software 2001", 2001, PortType.OUTPUT, 0));
				break;
				
			case 2:    // Data format 2
				objectList.add(new Dataset("Dataset 200", 200, 2));
				objectList.add(new Dataset("Dataset 201", 201, 2));
				objectList.add(new Dataset("Dataset 202", 202, 2));
				objectList.add(new Software("Software 2000", 2000, PortType.OUTPUT, 0));
				break;
			
			case 3:
				objectList.add(new Dataset("Dataset 300", 300, 3));
				objectList.add(new Dataset("Dataset 301", 301, 3));
				objectList.add(new Dataset("Dataset 302", 302, 3));
				break;
			
			case 4:
				objectList.add(new Dataset("Dataset 400", 400, 4));
				break;
			case 5:
				//objectList.add(new Dataset("Dataset 500", 500, 5));
				break;
			case 6:
				objectList.add(new Dataset("Dataset 600", 600, 6));   // TODO: comment out to to see if the code breaks 
				objectList.add(new Software("Software 1000", 1000, PortType.OUTPUT, 1)); // software 1000 has two outputs.  Make sure it is consistent with getOutputDataFormatsForSoftware
				break;
			}
			return objectList;
		}
			

		// Given a data format, this method returns software that use that data format for at least 1 input
		// If a software were to have 2 inputs using the same data format, and they were both represented as lines in this file
		// this method would return two instances of Software
		// N.B.  "Software" isn't a great name for the class.
		
		// Software(String title, Integer softwareId, PortType portType, int portNumber)
		
		public static ArrayList<Software> softwareByInputDataformat(Integer dataFormatID){
			ArrayList<Software> softwareList = new ArrayList<Software>();
			switch(dataFormatID) {
			case 1: 	// Data Format 1

				break;
				
			case 2:     // Data format 2
				softwareList.add(new Software("Software 1000", 1000, PortType.INPUT, 0));  
				softwareList.add(new Software("Software 1001", 1001, PortType.INPUT, 0));   
				break;
			
			case 3:
				softwareList.add(new Software("Software 1000", 1000, PortType.INPUT, 1));  
				softwareList.add(new Software("Software 2000", 2000, PortType.INPUT, 0)); 
				softwareList.add(new Software("Software 2002", 2002, PortType.INPUT, 0)); 
				break;
			
			case 4:
				softwareList.add(new Software("Software 2000", 2000, PortType.INPUT, 1)); 
				break;
				
			case 5:
				softwareList.add(new Software("Software 1002", 1002, PortType.INPUT, 0)); 
				break;
				
			case 6:
				softwareList.add(new Software("Software 2001", 2001, PortType.INPUT, 0)); 
				break;
			}
			return softwareList;
		}	
		
		
		

		
		
		// *** DO NOT DELETE makeSoftwareNode uses this method ****
		// Software are "declared" in this and the following method
		// This method declares the inputs for each software
		// Call this method with a software ID to get the data format for each of its inputs
		// Limitations: Exactly 1 DF per input;  code handles ??? up to 2-input software
		
		public static ArrayList<Integer> getInputDataFormatsForSoftware(Integer softwareID){
			ArrayList<Integer> inputsAndFormats = new ArrayList<Integer>();
			switch(softwareID) {
			case 1000: 	// Software 1000
				inputsAndFormats.add(2);   	 //DF IDs.  One row for each input/data format
				inputsAndFormats.add(3);
				//inputsAndFormats.add(4);
				break;
			case 1001:
				//inputsAndFormats.add(1);
				inputsAndFormats.add(2);
				//inputsAndFormats.add(3);
				break;
			case 1002:
				inputsAndFormats.add(5);
				//inputsAndFormats.add(4);
				//software.add(60);
				break;
			case 2000:
				inputsAndFormats.add(3);
				inputsAndFormats.add(4);
				//software.add(60);
				break;
			case 2001:
				inputsAndFormats.add(6);
				//inputsAndFormats.add(4);
				//software.add(60);
				break;
			case 2002:
				inputsAndFormats.add(3);
				//inputsAndFormats.add(4);
				//software.add(60);
				break;
			}
				return inputsAndFormats;
		}
		
		
		// *** DO NOT DELETE:  makeSoftwareNode uses this method ****
		// GET RID OF THIS COMPLEXITY by creating the structure dynamically by calling the first method in this file
		
		// This method declares the outputs for each software
		// Call this method with a software ID to get the data format for each of its outputs
		// Limitations: Exactly 1 output per software, exactly 1 DF per output
		// MAKE SURE NO SOFTWARE HAS THE SAME INPUT AND OUTPUT FORMATS
		// overloaded identifier conventions:  abcd  a-data format; d=makes identifier unique, given data format
		
		public static ArrayList<Integer> getOutputDataFormatsForSoftware(Integer softwareID){
			ArrayList<Integer> outputsAndFormats = new ArrayList<Integer>();
			switch(softwareID) {
			case 1000: 	// Software ID
				outputsAndFormats.add(1);   //Data format ID.  One row for each output/data format
				outputsAndFormats.add(6); // software 1000 has 2 outputs
				break;
			case 1001:
				outputsAndFormats.add(1);  /// need another format
				outputsAndFormats.add(6);  // software 1001 has 2 outputs
				break;
			case 1002:
				outputsAndFormats.add(1);
				break;
			case 2000:
				outputsAndFormats.add(2);
				break;
			case 2001:
				outputsAndFormats.add(1);
				break;
			case 2002:
			//	outputsAndFormats.add(2);
				break;
			} 
				return outputsAndFormats;
		}
		
}
