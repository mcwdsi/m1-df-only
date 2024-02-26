package edu.pitt.mdc.m1;
import java.util.*; 
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

public class GenerateAndTest {

	static SoftwareDatasetDataFormatRepository sddfr;
	static SoftwareManager sm = null;
	static DatasetManager dm = null;

	// gList accumulates the graphs composed by M1
	static ArrayList<MutableValueGraph<Node, Integer>> gList = null;

	public static boolean forwardSearch(MutableValueGraph<Node, Integer> g)
	{	
		UnboundGraphOutput ugo;	// will point to a specific unbound output SoftwarePort in a SoftwareNode in graph g

		// get list of unbound outports in graph g
		ArrayList<UnboundGraphOutput> unboundGraphOutputList = getUnboundGraphOutputs(g); 

		//For each unbound outport in the list
		Iterator<UnboundGraphOutput> ugoIter = unboundGraphOutputList.iterator(); 
		while (ugoIter.hasNext()) { 
			ugo = ugoIter.next();

			// for each data format of the unbound outport
			ArrayList<Software> dfMatchingSoftwareList = null;
			ArrayList<Software> consolidatedDfMatchingSoftwareList = new ArrayList<Software>();
			ArrayList<Integer> dataFormatIds = ugo.dataFormatIds;
			Iterator<Integer> dfIter = dataFormatIds.iterator(); 
			while (dfIter.hasNext()) { 
				Integer dataFormatId = dfIter.next();
				//concatenate lists of repository software w/ inport DF matching each of the multiple data formats of the unbound graph output port
				dfMatchingSoftwareList = sddfr.softwareByInputDataformat(dataFormatId); 
				if(!dfMatchingSoftwareList.isEmpty()) {
					consolidatedDfMatchingSoftwareList.addAll(dfMatchingSoftwareList);
				}
			}
			// if list is not empty, there are repository software matching this unbound graph output port, so proceed to exhaustive binding
			if (!consolidatedDfMatchingSoftwareList.isEmpty()) {  
				// add each df-matching software in turn to clone copies of the graph and "bind" their data-format matching ports
				Iterator<Software> iterMS = dfMatchingSoftwareList.iterator();
				SoftwareNode s;
				while (iterMS.hasNext()) {
					Software matchingSoftware = iterMS.next();

					// first order of business is making a clone copy of the graph
					MutableValueGraph<Node, Integer> g1 = makeDagClone(g);    

					// get the unbound software in g1 and "bind" it to the matching software
					SoftwareNode sUnbound = null;
					Integer nodeId = ugo.getSoftwareNode().nodeId;
					Iterator<Node> iter2 = g1.nodes().iterator();
					while (iter2.hasNext()) {
						SoftwareNode snNext = (SoftwareNode)iter2.next();
						if (snNext.nodeId.equals(nodeId)) {
							sUnbound = snNext;
							break;
						}
					}
					
					// add the softwareId and portNumber of the matching software to the unbound software in the graph
					sUnbound.outputPorts.get(ugo.arrayIndexOfPort).setBoundToObjectId(matchingSoftware.rdoId);
					sUnbound.outputPorts.get(ugo.arrayIndexOfPort).setBoundToSoftwarePortArrayIndex(matchingSoftware.portNumber);
					sUnbound.outputPorts.get(ugo.arrayIndexOfPort).setBoundViaDataFormatId(matchingSoftware.getDataFormat());

					// create a software node for the matching software, add the info about the software port in the graph to it, then add the node to g1
					SoftwareNode ms = makeSoftwareNode(matchingSoftware.rdoId);
					ms.inputPorts.get(matchingSoftware.portNumber).setBoundToObjectId(ugo.node.softwareId);
					ms.inputPorts.get(matchingSoftware.portNumber).setBoundToSoftwarePortArrayIndex(ugo.arrayIndexOfPort);
					ms.inputPorts.get(matchingSoftware.portNumber).setBoundViaDataFormatId(matchingSoftware.getDataFormat());

					g1.addNode(ms);		

					// if the added software only has one input, then the graph has no unbound input and qualifies as a new workflow, so add it to gList and print it to Std Out.
					if (ms.numInputs()==1 && test(g1)) {
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
	
	
	public static boolean forwardSearchFromDataset(Integer datasetId) {
		MutableValueGraph<Node, Integer>  g;

		ArrayList<Software> dfMatchingSoftwareList; 
		Integer dataFormatId = (Integer)datasetId/100;  // Hack only works for Test collection  
		dfMatchingSoftwareList = sddfr.softwareByInputDataformat(dataFormatId); //exactly how forwardSearch gets a list
		Iterator<Software> dfmsIter = dfMatchingSoftwareList.iterator();

		if (!dfMatchingSoftwareList.isEmpty()) { 
			//For each instance of Software in dfMatchSoftware
			Software dfms;  // a data-format matching instance of Software
			SoftwareNode sn; // an instance of SoftwareNode
			while(dfmsIter.hasNext()) {
				dfms = dfmsIter.next();
				sn = makeSoftwareNode(dfms.rdoId);

				//dfms knows which sn inport connects to the dataset 
				sn.inputPorts.get(dfms.portNumber).setBoundToObjectId(datasetId);  		
				g = ValueGraphBuilder.directed().build();
				g.addNode(sn);

				//If s has exactly one input port,
				if (sn.inputPorts.size()==1)
					forwardSearch(g); 
				else
					backSearch(g);
			}
		}
		return(true);
	}

	public static boolean graphsAreEqual(MutableValueGraph<Node, Integer> g1, MutableValueGraph<Node, Integer> g2) {
		int g1NumNodes = g1.nodes().size();
		int g2NumNodes = g2.nodes().size();
		boolean eq = g1NumNodes == g2NumNodes;
		if (eq) {
			Set<Node> g1Nodes = g1.nodes();
			Set<Node> g2Nodes = g2.nodes();
			ArrayList<Node> g1NodeArray = new ArrayList<Node>();
			g1NodeArray.addAll(g1Nodes);
			ArrayList<Node> g2NodeArray = new ArrayList<Node>();
			g2NodeArray.addAll(g2Nodes);
			for (int i=0; i<g1NumNodes; i++) {
				boolean g1NodeInG2 = false;
				for (int j=0; j<g2NumNodes; j++) {
					g1NodeInG2 = g1NodeInG2 || g1NodeArray.get(i).equals(g2NodeArray.get(j));
				}
				eq = eq && g1NodeInG2;
				if (!eq) break;
			}
		}
		return eq;
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
		UnboundGraphInput ugi;

		Iterator<UnboundGraphInput> iterP = unboundGraphInputs.iterator(); 
		Integer dataFormatID;
		//Integer secondDataFormatId = 0;  // hack
		int numUnboundInputs = 0;

		//for each unbound graph inport, create a list of software and datasets that data-format match the graph inport data formats (plural)

		while (iterP.hasNext()) { //This loop populates dataset and software DF-matching structures AND counts unbound inputs in g
			ugi = iterP.next();
			numUnboundInputs++;  // DOUBLE CHECK THIS STATEMENT IS IN THE RIGHT PLACE
			ArrayList<Integer> dataFormatIds = ugi.getDataFormatIds(); 
			Iterator<Integer> dfIter = dataFormatIds.iterator();
			ArrayList<DigitalResearchObject> consolidatedDfMatchingInputObjectList = new ArrayList<DigitalResearchObject>();
			// for each data format for that port
			while(dfIter.hasNext()) {
				dataFormatID = dfIter.next();	
				oneDfMatchingInputObjectList = sddfr.objectsByDataformat(dataFormatID); // returns list of datasets w/ dataFormatID and software w/ dataFormatID for output
				if (!oneDfMatchingInputObjectList.isEmpty())
					consolidatedDfMatchingInputObjectList.addAll(oneDfMatchingInputObjectList); 
			}

			// This conditional exits the recursion if any of the requisite matching lists is empty.
			if (consolidatedDfMatchingInputObjectList.isEmpty()) {
				System.out.println("****consolidatedDfMatchingInputObjectList isEmpty.  Subtract 1 from this to get array index: " + numUnboundInputs);
				return false;
			} else {
				dfMatchingInputObjectLists.add(consolidatedDfMatchingInputObjectList);
			}
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
				UnboundGraphInput ugit2 = unboundGraphInputs.get(0);
				if(numUnboundInputs == 1) extendAndPrintGraph(g, unboundGraphInputs);
				if (numUnboundInputs>1) {
					Iterator<DigitalResearchObject> iterInput2 = dfMatchingInputObjectLists.get(1).iterator();  
					while (iterInput2.hasNext()) {
						unboundGraphInputs.get(1).setObjectToBindTo(iterInput2.next());
						UnboundGraphInput ugitemp = unboundGraphInputs.get(1);
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
		UnboundGraphInput ugi;

		// check each node's input ports to see if each is bound (test is == 0.) 
		// If unbound, make an instance of UnboundGraphInport and add to list using unboundGraphInportList.add(ugi)
		while (i.hasNext()) {
			Node n=i.next();  
			if (n instanceof SoftwareNode) { 
				SoftwareNode s = (SoftwareNode)n;
				//System.out.println("Software is " + s.uid + "\n");
				Iterator<SoftwarePort> iter2 = s.inputPorts.iterator();
				int portCtr = 0;
				while (iter2.hasNext()) {
					SoftwarePort p = iter2.next();
					//System.out.println("Port is " + p.portID + " boundTo is " + p.boundToObjectId + " type is " + p.type); 
					if (p.boundToObjectId==0) {  // could be null or zero if we don't initialize new ports to 0 !!!
						ugi = new UnboundGraphInput(portCtr, p.getDataFormats(), s); // software array index, port array index, data format UID
						unboundGraphInputList.add(ugi);
						//System.out.println("Port is " + p.uid + " boundTo is " + p.boundTo + "\n");        				
					}
					portCtr++;
				}
			}
			else System.out.println("WTF! A vertex isn't an instance of Node or of SoftwareNode!!\n");

		}
		return unboundGraphInputList;
	} 


	// getUnboundGraphOutputs iterates through all the nodes in a graph and within them their output ports to find "unbound" outputs
	// TODO:  create a WorkflowGraph class and move this method into it.  Note that I already created an UnboundGraphOutput Class

	public static ArrayList<UnboundGraphOutput> getUnboundGraphOutputs(MutableValueGraph<Node, Integer> g) { 

		ArrayList<UnboundGraphOutput> unboundGraphOutputList = new ArrayList<UnboundGraphOutput>();

		Iterator<Node> i = g.nodes().iterator();
		Node n;
		SoftwareNode s;
		SoftwarePort p;
		UnboundGraphOutput ugo;

		while (i.hasNext()) {
			n=i.next();  //check every node's output ports to see if each is bound. If not, ???unboundOutputPorts.add s.r. unboundGraphOutputList.add(ugo)
			if (n instanceof SoftwareNode) { 
				//System.out.println("Software is " + s.uid + "\n");
				s = (SoftwareNode)n;
				Iterator<SoftwarePort> iter2 = s.outputPorts.iterator();
				int portCtr = 0;
				while (iter2.hasNext()) {
					p = iter2.next();
					if (p.boundToObjectId==0) {
						ugo = new UnboundGraphOutput(portCtr, p.getDataFormats(), s); // software array index, port array index, data format UID
						ugo.setSoftwareToBindTo(p.boundToObjectId);  //  just being explicit that it is unbound
						unboundGraphOutputList.add(ugo);
						//System.out.println("Port is " + p.uid + " boundTo is " + p.boundTo + "\n");        				
					}
					portCtr++;
				}
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
			MutableValueGraph<Node, Integer> g1 = makeDagClone(g);    

			Iterator<UnboundGraphInput> unboundGraphInputIterator = unboundGraphInputs.iterator();
			UnboundGraphInput ugi;
			SoftwareNode s, sn;  // s is software already in graph.  sn is software to add

			// Start an ArrayList to hold the new SoftwareNode(s) created by the next loop.  DO NOT add them to the graph until after the loop.
			ArrayList<Node> newSoftwareNodes = new ArrayList<Node>();
			while (unboundGraphInputIterator.hasNext() ) {
				ugi = unboundGraphInputIterator.next();

				//get the software node for this ugi
				Integer nodeId = ugi.getSoftwareNode().nodeId;
				Iterator<Node> iter2 = g1.nodes().iterator();
				s = null;
				while (iter2.hasNext()) {
					SoftwareNode snNext = (SoftwareNode)iter2.next();
					if (snNext.nodeId.equals(nodeId)) {
						s = snNext;
						break;
					}
				}
				
				// then "bind" it to the the DigitalResearchObject specified in UnknownGraphInput 
				// first bind its unique id.  If the DigitalResearchObject is a Dataset, that's all we need to do
				s.inputPorts.get(ugi.arrayIndexOfPort).setBoundToObjectId(ugi.getObjectToBindTo().rdoId); 
	
				// if its software, we also need to...
				if (ugi.objectToBindTo instanceof Software) { 
					//bind the software input port to 
					Software x = (Software) ugi.getObjectToBindTo();  // necessary to cast ResearchDigitalObject to Software
					s.inputPorts.get(ugi.arrayIndexOfPort).setBoundToSoftwarePortArrayIndex(x.portNumber);
					s.inputPorts.get(ugi.arrayIndexOfPort).setBoundViaDataFormatId(x.getDataFormat());

					//make a SoftwareNode for software s to add to graph
					sn = makeSoftwareNode(ugi.objectToBindTo.rdoId); // sn is returned with all its ports.  Just need to set its output boundTo element

					//now bind the unbound output port of sn
					//int portNumber =  x.portNumber
					sn.outputPorts.get(x.portNumber).setBoundToObjectId(ugi.getSoftwareNode().softwareId);		
					sn.outputPorts.get(x.portNumber).setBoundToSoftwarePortArrayIndex(ugi.arrayIndexOfPort);
					sn.outputPorts.get(x.portNumber).setBoundViaDataFormatId(x.getDataFormat());

					newSoftwareNodes.add(sn);	
				}
			}
			/// Here's where we add any new software to graph g1 !!
			Iterator<Node> iter2 = newSoftwareNodes.iterator();
			while (iter2.hasNext()) g1.addNode(iter2.next());

			if (isBackwardsExtendableWorkflow(g1)) {
				int graphSize = gList.size() + 1;
				//System.out.println("In extendAndPrintGraph, about to call backSearch on graph " + graphToString(g1));
				//if (graphSize == 126) {
				//	printGraph(g1);
				//}
				backSearch(g1);
			} 

			if (!isAbstractWorkflow(g1) && test(g1)) {
				gList.add(g1); 
				System.out.println("\n" + gList.size() + ". New Graph composed during backSearch()");	
				printGraph(g1);
				// check if there are any unbound graph outputs before calling forwardSearch, although forwardSearch in effect does that
				if(hasUnboundGraphOutputs(g1)) {
					System.out.println("switching to forwardSearch...");
					forwardSearch(g1);
				}
			}
		}

		public static boolean test(MutableValueGraph<Node, Integer> g) {
			return true;
		}

		public static boolean isAbstractWorkflow(MutableValueGraph<Node, Integer> g) {
			ArrayList<UnboundGraphInput> ugiList = getUnboundGraphInputs(g);
			if (ugiList.isEmpty()) return false;

			/*
				If any ugi is associated with non-data-service, then it's abstract

				So any false value to call of isDataService() will result in
					!isDataService() evaluating to true, and the entire
					disjunct is true, and so the graph is abstract.
			*/
			boolean isAbstract = false;
			for (UnboundGraphInput ugi : ugiList) {
				isAbstract = isAbstract || !sddfr.isDataService(ugi.getSoftwareNode().softwareId);
			}
			return isAbstract;
		}

		public static boolean isBackwardsExtendableWorkflow(MutableValueGraph<Node, Integer> g) {
			return !getUnboundGraphInputs(g).isEmpty();
		}

		public static boolean hasUnboundGraphOutputs(MutableValueGraph<Node, Integer> g) {
			if (getUnboundGraphOutputs(g).isEmpty()) return false;
			else return true; }

		public static void printGraph(MutableValueGraph<Node, Integer> g){
			Iterator<Node> iter = g.nodes().iterator(); // generalize at some point to printing graphs with DatasetNode and perhaps any Node
			SoftwareNode s;

			while (iter.hasNext()) {  
				s = (SoftwareNode) iter.next();
				Iterator<SoftwarePort> iterIp = s.inputPorts.iterator();
				Iterator<SoftwarePort> iterOp = s.outputPorts.iterator();
				String softwareInfo = s.softwareId + " (" + s.title + ")";
				System.out.println(" Software " + softwareInfo);

				while (iterIp.hasNext())  {
					SoftwarePort p = iterIp.next();
					if (p.boundToObjectId==0)
						System.out.println("    Inport[" + p.portID + "] of Software " + softwareInfo + " is unbound"); 	
					else {
						if(p.boundToObjectId < 1000) {
							String datasetInfo = (dm == null) ? Integer.toString(p.boundToObjectId) : 
								Integer.toString(p.boundToObjectId) + " (" + dm.getTitleForDataset(p.boundToObjectId) + ")";
							System.out.println("    Inport[" + p.portID + "] of Software " + p.softwareID + " is bound to dataset " + datasetInfo ); 	
						} else {
							String sBoundToInfo = (sm == null) ? Integer.toString(p.boundToObjectId) :
								Integer.toString(p.boundToObjectId) + " (" + sm.getTitleForSoftware(p.boundToObjectId) + ")";
							System.out.println("    Inport[" + p.portID + "] of Software " + p.softwareID + " is bound to software " + sBoundToInfo  + 
								", Outport[" + p.boundToSoftwarePortArrayIndex + "] via data format " + p.getBoundViaDataFormatId()); 	
						}
					}
				}
				if(!iterOp.hasNext()) System.out.println("    Outports: none"); 	

				while (iterOp.hasNext()) { 
					SoftwarePort p = iterOp.next();
					if (p.boundToObjectId==0)
						System.out.println("    Outport[" + p.portID + "] of Software " + p.softwareID + " is unbound"); 	
					else {
						String sBoundToInfo = (sm == null) ? Integer.toString(p.boundToObjectId) :
								Integer.toString(p.boundToObjectId) + " (" + sm.getTitleForSoftware(p.boundToObjectId) + ")";
						System.out.println("    Outport[" + p.portID + "] of Software " + p.softwareID + " is bound to software " + sBoundToInfo  
							+ ", Inport[" + p.boundToSoftwarePortArrayIndex + "] via data format " + p.getBoundViaDataFormatId());
					}
				}
			} 
		}

		public static String graphToString(MutableValueGraph<Node, Integer> g) {
			ArrayList<Node> nodeList = new ArrayList<Node>();
			nodeList.addAll(g.nodes());
			Collections.sort(nodeList);

			Iterator<Node> iter = nodeList.iterator();
			SoftwareNode s;

			ArrayList<String> nodeString = new ArrayList<String>();
			while (iter.hasNext()) {  
				StringBuilder sb = new StringBuilder();
				s = (SoftwareNode) iter.next();
				Iterator<SoftwarePort> iterIp = s.inputPorts.iterator();
				Iterator<SoftwarePort> iterOp = s.outputPorts.iterator();
				String softwareInfo = s.softwareId + " (" + s.title + ")";
				sb.append("***");
				sb.append("Software ");
				sb.append(softwareInfo);
				sb.append("*** ");

				while (iterIp.hasNext())  {
					SoftwarePort p = iterIp.next();
					if (p.boundToObjectId==0)
						sb.append("    Inport[" + p.portID + "] of Software " + p.softwareID + " is unbound"); 	
					else {
						if(p.boundToObjectId < 1000) {
							String datasetInfo = (dm == null) ? Integer.toString(p.boundToObjectId) : 
								Integer.toString(p.boundToObjectId) + " (" + dm.getTitleForDataset(p.boundToObjectId) + ")";
							sb.append("    Inport[" + p.portID + "] of Software " + p.softwareID + " is bound to dataset " + datasetInfo ); 	
						} else {
							String sBoundToInfo = (sm == null) ? Integer.toString(p.boundToObjectId) :
								Integer.toString(p.boundToObjectId) + " (" + sm.getTitleForSoftware(p.boundToObjectId) + ")";
							sb.append("    Inport[" + p.portID + "] of Software " + p.softwareID + " is bound to software " + sBoundToInfo  + 
								", Outport[" + p.boundToSoftwarePortArrayIndex + "] via data format " + p.getBoundViaDataFormatId());
						}
					}
				}
				if(!iterOp.hasNext()) sb.append("    Outports: none"); 	

				while (iterOp.hasNext()) { 
					SoftwarePort p = iterOp.next();
					if (p.boundToObjectId==0)
						sb.append("    Outport[" + p.portID + "] of Software " + p.softwareID + " is unbound"); 	
					else {
						String sBoundToInfo = (sm == null) ? Integer.toString(p.boundToObjectId) :
								Integer.toString(p.boundToObjectId) + " (" + sm.getTitleForSoftware(p.boundToObjectId) + ")";
						sb.append("    Outport[" + p.portID + "] of Software " + p.softwareID + " is bound to software " + sBoundToInfo  + 
							", Inport[" + p.boundToSoftwarePortArrayIndex + "] via data format " + p.getBoundViaDataFormatId());					
					}
				}
				sb.append("   ");
				nodeString.add(sb.toString());
			} 
			Collections.sort(nodeString);

			StringBuilder sb2 = new StringBuilder();
			for (String ns : nodeString) {
				//sb2.append("+++");
				sb2.append(ns);
				//sb2.append("+++   ");
			}
			return sb2.toString();
		}



		public static ArrayList<MutableValueGraph<Node, Integer>> getGraphList() {
			return gList;
		}

		public static SoftwareNode makeSoftwareNode(Integer softwareID) {
			SoftwareNode sn  = new SoftwareNode(softwareID, softwareID);
			if (sm != null) {
				sn.title = sm.getTitleForSoftware(softwareID);
			}
			//get the input ports for the software and add them to the SoftwareNode
			Iterator<ArrayList<Integer>> iterI = sddfr.getInputDataFormatsForSoftware(softwareID).iterator();
			Integer portID = 0;   // more accurate to say that input ports are numbered from 0 within each software.
			while (iterI.hasNext()) {
				SoftwarePort ip = new SoftwarePort(softwareID,PortType.INPUT,iterI.next()); //softwareID, type (0=output, 1=input), dataformatID  
				ip.setPortId(portID++);
				sn.inputPorts.add(ip);
			}
			//get the output ports for the software and add them to the SoftwareNode
			Iterator<ArrayList<Integer>> iterO = sddfr.getOutputDataFormatsForSoftware(softwareID).iterator();
			portID = 0;
			while (iterO.hasNext()) {
				SoftwarePort op = new SoftwarePort(softwareID,PortType.OUTPUT,iterO.next()); // softwareID, type (0=output, 1=input), dataformatID  
				op.setPortId(portID++);
				sn.outputPorts.add(op);
			}
			return sn;
		}

		public static MutableValueGraph<Node, Integer>  makeDagClone(MutableValueGraph<Node, Integer>  g) {
			MutableValueGraph<Node, Integer>  g1 = ValueGraphBuilder.directed().build();

			//make copies of all SoftwareNode and Ports in g
			SoftwareNode s, s1;
			SoftwarePort ip, ip1, op, op1;

			Iterator<Node> iter = g.nodes().iterator();
			while (iter.hasNext() ) {
				s = (SoftwareNode) iter.next();
				s1 = new SoftwareNode(s.nodeId, s.softwareId);  // constructor requires a uid and we want the graph to have the same software as the old!
				s1.title = s.title;

				Iterator<SoftwarePort> iterIP = s.inputPorts.iterator();
				while(iterIP.hasNext()){
					ip = iterIP.next();
					ip1 = new SoftwarePort(ip.softwareID, ip.type, ip.getDataFormats());

					ip1.setPortId(ip.getPortId());
					ip1.setBoundToObjectId(ip.getBoundToObjectId());				
					ip1.setBoundToSoftwarePortArrayIndex(ip.getBoundToSoftwarePortArrayIndex());
					ip1.setBoundViaDataFormatId(ip.getBoundViaDataFormatId());
					
					s1.inputPorts.add(ip1);
				}
				// int portCtr = 0;
				Iterator<SoftwarePort> iterOP = s.outputPorts.iterator();
				while(iterOP.hasNext()){
					op = iterOP.next();
					op1 = new SoftwarePort(op.softwareID, op.type, op.getDataFormats());

					op1.setPortId(op.getPortId());
					op1.setBoundToObjectId(op.getBoundToObjectId());				
					op1.setBoundToSoftwarePortArrayIndex(op.getBoundToSoftwarePortArrayIndex());
					op1.setBoundViaDataFormatId(op.getBoundViaDataFormatId());

					s1.outputPorts.add(op1);
				}
				g1.addNode(s1);
			}
			return g1;
		}

	}


