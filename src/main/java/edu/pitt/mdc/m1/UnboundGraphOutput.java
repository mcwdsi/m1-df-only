package edu.pitt.mdc.m1;

import java.util.ArrayList;

public class UnboundGraphOutput {
	
	int arrayIndexOfPort; 		// array index of the unbound output port (in the software identified by softwareId.  Works fine.
	ArrayList<Integer> dataFormatIds;		// data formats of the unbound outport
	Integer softwareToBindTo;	//  Value is actually the software id
	int arrayIndexOfSoftwareInputPortToBindTo;  // left blank until bound
	SoftwareNode node; // the software node in the graph with the unbound graph output port

public UnboundGraphOutput (int arrayIndexOfPort, ArrayList<Integer> dataFormatIds, SoftwareNode node) {
	this.arrayIndexOfPort = arrayIndexOfPort;	
	this.dataFormatIds = dataFormatIds;
	this.node = node;
}

public void setSoftwareToBindTo (Integer softwareToBindTo) { 
	this.softwareToBindTo = softwareToBindTo;
}

public SoftwareNode getSoftwareNode() {
	return this.node;
}

public Integer getSoftwareToBindTo () {
	return this.softwareToBindTo;
}

public void setArrayIndexOfsoftwareInputPortToBindTo (int arrayIndexOfSoftwareInputPortToBindTo) { 
	this.arrayIndexOfSoftwareInputPortToBindTo = arrayIndexOfSoftwareInputPortToBindTo;
}
public int getArrayIndexOfsoftwareInputPortToBindTo() {
	return this.arrayIndexOfSoftwareInputPortToBindTo;
}
}