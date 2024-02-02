package edu.pitt.mdc.m1;
import java.util.*; 
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.Graphs;

public class GenerateAndTestGuava {

	// gList accumulates the graphs composed by M1
	static ArrayList<MutableValueGraph<Node, Integer>> gList = null;
	
	// forwardSearch is called on concrete workflows, whose only unbound ports are output ports.  
	// forwardSearch extends the workflows by instantiating uninstantiated software outputs with additional software
	// The secret to forwardSearch() is that it distinguishes between the addition of single-input software -- 
	// which it can do depth first by calling itself recursively -- and the addition of multi-input software, for which it requires
	// the assistance of backSearch() to determine whether the additional inputs of the added software can be satisfied.
	
	/* Pseudocode for forwardSearch
	 * For each uninstantiated output node {
			If DF-match-list.isEmpty() {}
			Else {
				For each item on the DF-match list {
				g1 = clone(g)create a clone of g. 
				g1.add(softwareNode)
				if (softwareNode has exactly 1 input port)
					forwardSearch(g1);
				else
					backSearch(g1);
						} //next item
					} //next uninstantiated output node;
	 */	
	
	public static boolean forwardSearch(MutableValueGraph<Node, Integer> g)
	{	
		// this variable will hold a list of df-matching software 
		// each software on the list has an input port using the data format 
		ArrayList<Software> dfMatchingSoftwareList = null;
		
		UnboundGraphOutput ugo;	// will point to a specific unbound output SoftwarePort in a SoftwareNode in graph g
		
		// get list of unbound output ports in graph g
		ArrayList<UnboundGraphOutput> unboundGraphOutputList = getUnboundGraphOutputList(g); 

		//For each unbound output port in the list
		Iterator<UnboundGraphOutput> iterUgol = unboundGraphOutputList.iterator(); 
		while (iterUgol.hasNext()) { 
			ugo = iterUgol.next();
	    	
	    	//get list of repository software w/ input port that data-format matches w/ the unbound graph output port
			dfMatchingSoftwareList = SoftwareDatasetDataFormatRepository.softwareByInputDataformat(ugo.dataFormatId); 
			// if list is empty, there are no repository software matching this unbound graph output port, so leave it unbound
			if (dfMatchingSoftwareList.isEmpty()) {  // cleaner to use (!dfMatching...) ?
			} 
			else {
				// add each df-matching software in turn to clone copies of the graph and "bind" their data-format matching ports
				Iterator<Software> iterMS = dfMatchingSoftwareList.iterator();
		    	SoftwareNode s;
				while (iterMS.hasNext()) {
		    		Software matchingSoftware = iterMS.next();
		    		
		    		// first order of business is making a clone copy of the graph
		    		MutableValueGraph<Node, Integer> g1 = TestM1.makeDagClone(g);    

		    		// get the unbound software in g1 and "bind" it to the matching software
	    			
		    		Iterator<Node> iter = g1.nodes().iterator();  
	    			SoftwareNode sUnbound = new SoftwareNode(666);  // will point to the unbound node in g1
	    			while (iter.hasNext()) {  // can't just use array index to go right to software node.  have to look through them to find it
	    				sUnbound = (SoftwareNode) iter.next();
	    				if (sUnbound.uid - ugo.softwareId ==0) {
	    					break;
	    				}
	    			}
	    			//  
	    			// add the softwareId and portNumber of the matching software to the unbound software in the graph
	    			sUnbound.outputPorts.get(ugo.arrayIndexOfPort).setBoundToObjectId(matchingSoftware.rdoId);
	    			sUnbound.outputPorts.get(ugo.arrayIndexOfPort).setBoundToSoftwarePortArrayIndex(matchingSoftware.portNumber);
	    			
	    			// create a software node for the matching software, add the info about the software port in the graph to it, then add the node to g1
	    			SoftwareNode ms = makeSoftwareNode(matchingSoftware.rdoId);
		    		ms.inputPorts.get(matchingSoftware.portNumber).setBoundToObjectId(ugo.softwareId);
		    		ms.inputPorts.get(matchingSoftware.portNumber).setBoundToSoftwarePortArrayIndex(ugo.arrayIndexOfPort);
		    		
		    		g1.addNode(ms);		
		 
		    		// if the added software only has one input, then the graph has no unbound input and qualifies as a new workflow, so add it to gList and print it to Std Out.
		    		if (ms.numInputs()==1) {
		    			gList.add(g1);
		    			System.out.println("\n" + gList.size() + ". New Graph composed during forwardSearch()");	

		    			printGraph(g1);
		    		}
		    		else {
		    			 backSearch(g1);
		    		}
		    	}
			}
		}
		return true;
	}
	
	
	// backSearch is called with abstract workflows.  
	// It searches to instantiate all uninstantiated software inputs in the workflow, grounding out in datasets, 
	// except in a future version when software input is marked as DFM-sufficient
	public static boolean backSearch(MutableValueGraph<Node, Integer> g)
	{			
		// Todo: If the number of unbound inputs >8 or the combinatorics are intractable, resort to sampling!
		
		// Load arrays from cache/hash of DF-matching datasets AND software for each unbound input port in the graph 
		// the while loop checks whether any array isEmpty().  If so, there is no way to instantiate all inputs for that graph and backSearch exits
		ArrayList<ArrayList<DigitalResearchObject>> dfMatchingInputObjectLists = new ArrayList<ArrayList<DigitalResearchObject>>(); // TODO:  refactor variable name
		ArrayList<DigitalResearchObject> oneDfMatchingInputObjectList = null;
		ArrayList<UnboundGraphInput> unboundGraphInputs = getUnboundGraphInputs(g); 

		Iterator<UnboundGraphInput> iterP = unboundGraphInputs.iterator(); 
		Integer dataFormatID;
		int numUnboundInputs = 0;
		while (iterP.hasNext()) { //Loop populates dataset and software DF-matching structures AND counts unbound inputs in g
	    	dataFormatID = iterP.next().dataFormatId; //dataFormatID of unbound input port
	    	numUnboundInputs++;
			oneDfMatchingInputObjectList = SoftwareDatasetDataFormatRepository.objectsByDataformat(dataFormatID); // returns list of datasets w/ dataFormatID and software w/ dataFormatID for output
			if (oneDfMatchingInputObjectList.isEmpty()) {
			   System.out.println("\n FYI: exiting backsearch (returns FALSE) due to zero DF-matching instantiations of an unbound input port for this graph! \n"); 
			   return false;
			} else
			dfMatchingInputObjectLists.add(oneDfMatchingInputObjectList);
		}

		// Generate all combinations of DATASETS and/or SOFTWARE that satisfy (DF only) all unbound inputs of the graph 
		// Call extendAndPrintGraph with each combo
		// At present, handles up to 8-unbound inputs, but above Arrays are general to graphs with n uninstantiated root inputs.
		
		if (numUnboundInputs<1) {
			System.out.println("\tDEBUG: No unbound inputs for");
			printGraph(g);  
		}
		
		if (numUnboundInputs > 0 && numUnboundInputs < 9) {
			Iterator<DigitalResearchObject> iterInput1 = dfMatchingInputObjectLists.get(0).iterator();  
		    while (iterInput1.hasNext()) {
		    	unboundGraphInputs.get(0).setObjectToBindTo(iterInput1.next()); // for >1 output software, need to go from iterInput1.next() to a port (or hack the change in unboundGraphInputs )
		    	if(numUnboundInputs == 1) extendAndPrintGraph(g, unboundGraphInputs);
		    	if (numUnboundInputs>1) {
		    		Iterator<DigitalResearchObject> iterInput2 = dfMatchingInputObjectLists.get(1).iterator();  
		    		while (iterInput2.hasNext()) {
		    			unboundGraphInputs.get(1).setObjectToBindTo(iterInput2.next());
		    			if(numUnboundInputs == 2) extendAndPrintGraph(g, unboundGraphInputs);
				    	if (numUnboundInputs>2) {
				    		Iterator<DigitalResearchObject> iterInput3 = dfMatchingInputObjectLists.get(2).iterator();  
				    		while (iterInput3.hasNext()) {
				    			unboundGraphInputs.get(2).setObjectToBindTo(iterInput3.next());
				    			if(numUnboundInputs == 3) extendAndPrintGraph(g, unboundGraphInputs);
						    	if (numUnboundInputs>3) {
						    		Iterator<DigitalResearchObject> iterInput4 = dfMatchingInputObjectLists.get(3).iterator();  
						    		while (iterInput4.hasNext()) {
						    			unboundGraphInputs.get(3).setObjectToBindTo(iterInput4.next());
						    			if(numUnboundInputs == 4) extendAndPrintGraph(g, unboundGraphInputs);
						    			if (numUnboundInputs>4) {
								    		Iterator<DigitalResearchObject> iterInput5 = dfMatchingInputObjectLists.get(4).iterator();  
								    		while (iterInput5.hasNext()) {
								    			unboundGraphInputs.get(4).setObjectToBindTo(iterInput5.next());	
								    			if(numUnboundInputs == 5) extendAndPrintGraph(g, unboundGraphInputs);
								    			if (numUnboundInputs>5) {
										    		Iterator<DigitalResearchObject> iterInput6 = dfMatchingInputObjectLists.get(5).iterator();  
										    		while (iterInput6.hasNext()) {
										    			unboundGraphInputs.get(5).setObjectToBindTo(iterInput6.next());
										    			if(numUnboundInputs == 6) extendAndPrintGraph(g, unboundGraphInputs);
										    			if (numUnboundInputs>6) {
												    		Iterator<DigitalResearchObject> iterInput7 = dfMatchingInputObjectLists.get(6).iterator();  
												    		while (iterInput7.hasNext()) {
												    			unboundGraphInputs.get(6).setObjectToBindTo(iterInput7.next());												    			if(numUnboundInputs == 7) extendAndPrintGraph(g, unboundGraphInputs);
												    			if (numUnboundInputs>7) {
														    		Iterator<DigitalResearchObject> iterInput8 = dfMatchingInputObjectLists.get(7).iterator();  
														    		while (iterInput8.hasNext()) {
														    			unboundGraphInputs.get(7).setObjectToBindTo(iterInput8.next());	
														    			if(numUnboundInputs == 8) extendAndPrintGraph(g, unboundGraphInputs);
														    			if (numUnboundInputs>8) {
														    				System.out.println("shouldn't be here because graph has more than 8 unbound inputs");
														    			}
														    		}
												    			}
												    		}
										    			}
										    		}
								    			}
								    		}
						    			}
						    		}
						    	}
				    		}
				    	}
		    		}
		    	}
		    }
		}
		// If you get here, there are either zero unbound inputs in the graph or > 8.
		// N.B. the software validator could (and should for the time being) be called from extendAndPrint(g). 
		return true;
	}
	        
	

	// getUnboundGraphInputs iterates through all the nodes in a graph and within them their input ports to find "unbound" inputs
	// TODO:  create a WorkflowGraph class and move this method into it.  Note that I already created an UnboundGraphInput Class
	
	public static ArrayList<UnboundGraphInput> getUnboundGraphInputs(MutableValueGraph<Node, Integer> g) { 
	
		ArrayList<UnboundGraphInput> unboundGraphInputList = new ArrayList<UnboundGraphInput>();
        
		Iterator<Node> i = g.nodes().iterator();
		Node n;
		SoftwareNode s;
		SoftwarePort p;
		UnboundGraphInput ugi;
		int nodeCtr =0;
        while (i.hasNext()) {
        	n=i.next();  //check every node's input ports to see if each is bound. If not, unboundInputPorts.add
        	if (n instanceof SoftwareNode) { 
    			//System.out.println("Software is " + s.uid + "\n");
    			s = (SoftwareNode)n;
        		Iterator<SoftwarePort> iter2 = s.inputPorts.iterator();
        		int portCtr =0;
        		while (iter2.hasNext()) {
        			p = iter2.next();
        			if (p.boundToObjectId==0) {  // could be null or zero if we don't initialize new ports to 0 !!!
        				ugi = new UnboundGraphInput(nodeCtr, portCtr, p.dataFormatID, p.softwareID); // software array index, port array index, data format UID
        				unboundGraphInputList.add(ugi);
            			//System.out.println("Port is " + p.uid + " boundTo is " + p.boundTo + "\n");        				
        			}
        			portCtr++;
        		}
        		nodeCtr++;
        	}
        	else System.out.println("WTF! A vertex isn't an instance of Node or of SoftwareNode!!\n");

        }
		return unboundGraphInputList;
	} 
	
	
	// getUnboundGraphOutputs iterates through all the nodes in a graph and within them their output ports to find "unbound" outputs
	// TODO:  create a WorkflowGraph class and move this method into it.  Note that I already created an UnboundGraphOutput Class
	
	public static ArrayList<UnboundGraphOutput> getUnboundGraphOutputList(MutableValueGraph<Node, Integer> g) { 
	
		ArrayList<UnboundGraphOutput> unboundGraphOutputList = new ArrayList<UnboundGraphOutput>();
        
		Iterator<Node> i = g.nodes().iterator();
		Node n;
		SoftwareNode s;
		SoftwarePort p;
		UnboundGraphOutput ugo;
		int nodeCtr =0;
        while (i.hasNext()) {
        	n=i.next();  //check every node's output ports to see if each is bound. If not, ???unboundOutputPorts.add s.r. unboundGraphOutputList.add(ugo)
        	if (n instanceof SoftwareNode) { 
    			//System.out.println("Software is " + s.uid + "\n");
    			s = (SoftwareNode)n;
        		Iterator<SoftwarePort> iter2 = s.outputPorts.iterator();
        		int portCtr =0;
        		while (iter2.hasNext()) {
        			p = iter2.next();
        			if (p.boundToObjectId==0) {
        				ugo = new UnboundGraphOutput(nodeCtr, portCtr, p.dataFormatID, p.softwareID); // software array index, port array index, data format UID
        				ugo.setSoftwareToBindTo(p.boundToObjectId);  //  just being explicit that it is unbound
        				unboundGraphOutputList.add(ugo);
            			//System.out.println("Port is " + p.uid + " boundTo is " + p.boundTo + "\n");        				
        			}
        			portCtr++;
        		}
        		nodeCtr++;
        	}
        	else System.out.println("A vertex isn't an instance of Node or of SoftwareNode!!\n");
        }
		return unboundGraphOutputList;
	} 
	

	// This method receives a graph g and a list of UnboundGraphInput objects, one for each "unbound" node in g (having an unbound input port).
	// Importantly, each UnboundGraphInput object contains an instance of the class DigitalResearchObject or one of its subclasses (Dataset, Software).
	// The instance describes the df-matching object selected by M1 to satisfy that unbound input.
	// You can find the instance in the obectToBindTo element of UnboundGraphInput.
	public static void extendAndPrintGraph(MutableValueGraph<Node, Integer> g, ArrayList<UnboundGraphInput> unboundGraphInputs){
		// Make a deep copy of the graph. "Bind" datasets to input ports. Make SoftwareNode(s) and bind them to software ports.
		MutableValueGraph<Node, Integer> g1 = TestM1.makeDagClone(g);    

		Iterator<UnboundGraphInput> unboundGraphInputIterator = unboundGraphInputs.iterator();
		UnboundGraphInput ugi;
		SoftwareNode s, sn;  // s is software already in graph.  sn is software to add
		
		// Start an ArrayList to hold the new SoftwareNode(s) created by the next loop.  DO NOT add them to the graph until after the loop.
		ArrayList<Node> newSoftwareNodes = new ArrayList<Node>();
		while (unboundGraphInputIterator.hasNext() ) {
			ugi = unboundGraphInputIterator.next();
			
			// bind ID of dataset or software to the unbound input port in the g1
			Iterator<Node> iter = g1.nodes().iterator();  // unfortunately value graphs convert 1 step to this mess.		
			while (iter.hasNext()) {  // a workaround to find a software node in the MutableValueGraph.
				s = (SoftwareNode) iter.next();
				// is s is the graph node we are looking for (the one specified in UnknownGraphInput)?  
				if (s.uid - ugi.softwareId ==0) {
					// then "bind" it to the the DigitalResearchObject specified in UnknownGraphInput 
					// first bind its unique id.  If the DigitalResearchObject is a Dataset, that's all we need to do
					s.inputPorts.get(ugi.arrayIndexOfPort).setBoundToObjectId(ugi.getObjectToBindTo().rdoId); 
					// if its software, we also need to...
					if (ugi.objectToBindTo instanceof Software) { 
						//bind the software input port to 
						Software x = (Software) ugi.getObjectToBindTo();  // necessary to cast ResearchDigitalObject to Software
						s.inputPorts.get(ugi.arrayIndexOfPort).setBoundToSoftwarePortArrayIndex(x.portNumber);							

						//make a SoftwareNode for software s to add to graph
						sn = makeSoftwareNode(ugi.objectToBindTo.rdoId); // sn is returned with all its ports.  Just need to set its output boundTo element

						//now bind the unbound output port of sn
						//int portNumber =  x.portNumber
						sn.outputPorts.get(x.portNumber).setBoundToObjectId(ugi.softwareId);		
						sn.outputPorts.get(x.portNumber).setBoundToSoftwarePortArrayIndex(ugi.arrayIndexOfPort);				
						newSoftwareNodes.add(sn);	
					}
					break;
				}
			}
		}
		/// Here's where we add any new software to graph g1 !!
		Iterator<Node> iter2 = newSoftwareNodes.iterator();
		while (iter2.hasNext()) g1.addNode(iter2.next());
	
		
		if (isAbstractWorkflow(g1)) {
			//System.out.println("continuing backSearch...");
			backSearch(g1);
		} 
		else { // it is a concrete workflow, so we add it to gList and print it
			if (gList == null) gList = new ArrayList<MutableValueGraph<Node, Integer>>();
			gList.add(g1); 
			System.out.println("\n" + gList.size() + ". New Graph composed during backSearch()");	
			printGraph(g1);
			// TODO: could check if there are any unbound graph outputs before calling forwardSearch, although forwardSearch in effect does that
			System.out.println("switching to forwardSearch...");
			forwardSearch(g1);
		}
	}
	
	
	public static boolean isAbstractWorkflow(MutableValueGraph<Node, Integer> g) {
		if (getUnboundGraphInputs(g).isEmpty()) return false;
	 else return true; }
	
	
	public static void printGraph(MutableValueGraph<Node, Integer> g){
		
		Iterator<Node> iter = g.nodes().iterator(); // generalize at some point to printing graphs with DatasetNode and perhaps any Node
		SoftwareNode s;
		String sw = new String("software ");
		String ds = new String("dataset ");
		String text;

		while (iter.hasNext()) {  
			s = (SoftwareNode) iter.next();
			Iterator<SoftwarePort> iterIp = s.inputPorts.iterator();
			Iterator<SoftwarePort> iterOp = s.outputPorts.iterator();
			System.out.println(" Software " + s.uid);
			
			while (iterIp.hasNext())  {
				SoftwarePort p = iterIp.next();
				if (p.boundToObjectId==0)
					System.out.println("    Input port[" + p.portID + "] of Software " + p.softwareID + " is unbound"); 	
				else {
					if(p.boundToObjectId < 1000) 
						System.out.println("    Input port[" + p.portID + "] of Software " + p.softwareID + " is bound to dataset " + p.boundToObjectId ); 	
					else {text = sw;
					System.out.println("    Input port[" + p.portID + "] of Software " + p.softwareID + " is bound to " + text + p.boundToObjectId  + ", port[" + p.boundToSoftwarePortArrayIndex + "]"); 	
					}
					}
			}
	        if(!iterOp.hasNext()) System.out.println("    Output ports: none"); 	

			while (iterOp.hasNext()) { 
				SoftwarePort p = iterOp.next();
				if (p.boundToObjectId==0)
					System.out.println("    Output port[" + p.portID + "] of Software " + p.softwareID + " is unbound"); 	
				else
					System.out.println("    Output port[" + p.portID + "] of Software " + p.softwareID + " is bound to software " + p.boundToObjectId  + ", port[" + p.boundToSoftwarePortArrayIndex + "]"); 						
			}
		} 
	}
	
	public static ArrayList<MutableValueGraph<Node, Integer>> getGraphList() {
		return gList;
	}

	
	
	public static SoftwareNode makeSoftwareNode(Integer softwareID) {
		SoftwareNode sn  = new SoftwareNode(softwareID);
		//get the input ports for the software and add them to the SoftwareNode
		Iterator<Integer> iterI = SoftwareDatasetDataFormatRepository.getInputDataFormatsForSoftware(softwareID).iterator();
		Integer portID =0;   // more accurate to say that input ports are numbered from 0 within each software.
		while (iterI.hasNext()) {
			SoftwarePort ip = new SoftwarePort(softwareID,PortType.OUTPUT,iterI.next()); //softwareID, type (0=output, 1=input), dataformatID  
			ip.setPortId(portID++);
			sn.inputPorts.add(ip);
		}
		//get the output ports for the software and add them to the SoftwareNode
		Iterator<Integer> iterO = SoftwareDatasetDataFormatRepository.getOutputDataFormatsForSoftware(softwareID).iterator();
		portID =0;
		while (iterO.hasNext()) {
			SoftwarePort op = new SoftwarePort(softwareID,PortType.INPUT,iterO.next()); // softwareID, type (0=output, 1=input), dataformatID  
			op.setPortId(portID++);
			sn.outputPorts.add(op);
		}
		return sn;
	}
		
}

	
