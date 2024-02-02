package edu.pitt.mdc.m1;

public class UnboundGraphOutput {
	Integer softwareId;  // the ID of the software in the graph with the unbound graph output port
	int arrayIndexOfSoftware;   // local array index in the Graph type (doesn't work as planned with the Guava class because the list is of type <Node>)
	int arrayIndexOfPort; 		// array index of the unbound output port (in the software identified by softwareId.  Works fine.
	Integer dataFormatId;		// data format of the unbound output port
	Integer softwareToBindTo;	//  Slightly more like it is software id
	int arrayIndexOfSoftwareInputPortToBindTo;  // left blank until bound

//TODO:  think about whether it is better to initialize the last two class elements to null or zero
public UnboundGraphOutput (int arrayIndexOfSoftware, int arrayIndexOfPort, Integer dataFormatId, Integer softwareId) {
	this.arrayIndexOfSoftware = arrayIndexOfSoftware;
	this.arrayIndexOfPort = arrayIndexOfPort;	
	this.dataFormatId = dataFormatId;
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