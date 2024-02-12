package edu.pitt.mdc.m1;

import java.util.ArrayList;

public class UnboundGraphOutput {
	Integer softwareId;  // the ID of the software in the graph with the unbound graph output port
	int arrayIndexOfPort; 		// array index of the unbound output port (in the software identified by softwareId.  Works fine.
	ArrayList<Integer> dataFormatIds;		// data formats of the unbound outport
	Integer softwareToBindTo;	//  Value is actually the software id
	int arrayIndexOfSoftwareInputPortToBindTo;  // left blank until bound

//TODO:  think about whether it is better to initialize the last two class elements to null or zero
public UnboundGraphOutput (int arrayIndexOfPort, ArrayList<Integer> dataFormatIds, Integer softwareId) {
	this.arrayIndexOfPort = arrayIndexOfPort;	
	this.dataFormatIds = dataFormatIds;
	this.softwareId = softwareId;
}

public void setSoftwareToBindTo (Integer softwareToBindTo) { 
	this.softwareToBindTo = softwareToBindTo;
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