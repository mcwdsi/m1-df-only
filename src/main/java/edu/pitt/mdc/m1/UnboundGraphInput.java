package edu.pitt.mdc.m1;


// Together, the softwareID and arrayIndexOfPort within the software and the data format exactly define the unbound input port.
// toBindTo doesn't exactly define what the input port is supposed to bind to if the object is a multi-output software.
public class UnboundGraphInput {
Integer softwareId;    // the UID of the software in the graph with the unbound input port
int arrayIndexOfPort;  // the array index of the unbound input port in the software
Integer dataFormatId;  // the data format UID for the unbound input port
DigitalResearchObject objectToBindTo;  // an object containing essential info about the dataset or software that M1 has selected for binding to the unbound graph input
Integer arrayIndexOfPortInObjectToBindTo;  // TODO: no longer needed.  objectToBindTo has the info
int arrayIndexOfSoftware;   // TODO: Not used: local array index in the Graph type (doesn't work as planned with the Guava class because the list is of type <Node>)


public UnboundGraphInput (int arrayIndexOfSoftware, int arrayIndexOfPort, Integer dataFormatId, Integer softwareId) {
	this.arrayIndexOfSoftware = arrayIndexOfSoftware;   // TODO: not used
	this.softwareId = softwareId;
	this.arrayIndexOfPort = arrayIndexOfPort;	
	this.dataFormatId = dataFormatId;
}

public void setObjectToBindTo (DigitalResearchObject objectToBindTo) {
	this.objectToBindTo = objectToBindTo;
}

public DigitalResearchObject getObjectToBindTo () {
	return this.objectToBindTo;
}

public void setPortInObjectToBindTo (int arrayIndexOfPortInObjectToBindTo) {
	this.arrayIndexOfPortInObjectToBindTo = arrayIndexOfPortInObjectToBindTo;
}
public int getPortInObjectToBindTo () {
	return this.arrayIndexOfPortInObjectToBindTo;
}
}