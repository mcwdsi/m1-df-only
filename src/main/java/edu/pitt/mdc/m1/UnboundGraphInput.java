package edu.pitt.mdc.m1;

import java.util.ArrayList;

// Together, the softwareID and arrayIndexOfPort within the software and the data format exactly define the unbound input port.
// toBindTo doesn't exactly define what the input port is supposed to bind to when the target is a multi-DF outport (i.e. one w/subports)
// TODO: to handle has to specify the subport
public class UnboundGraphInput {

	public ArrayList<Integer> getDataFormatIds() {
		return dataFormatIds;
	}

	public void setDataFormatIds(ArrayList<Integer> dataFormatIds) {
		this.dataFormatIds = dataFormatIds;
	}

Integer softwareId;    // the UID of the software in the graph with the unbound input port
int arrayIndexOfPort;  // the array index of the unbound input port in the software
ArrayList<Integer> dataFormatIds;  // the data format UIDs for the unbound input port
DigitalResearchObject objectToBindTo;  // an object containing essential info about the dataset or software that M1 has selected for binding to the unbound graph input

public UnboundGraphInput (int arrayIndexOfPort, ArrayList<Integer> dataFormatIds, Integer softwareId) {
	//this.arrayIndexOfSoftware = arrayIndexOfSoftware;   // TODO: not used
	this.softwareId = softwareId;
	this.arrayIndexOfPort = arrayIndexOfPort;	
	this.dataFormatIds = dataFormatIds;
}

public void setObjectToBindTo (DigitalResearchObject objectToBindTo) {
	this.objectToBindTo = objectToBindTo;
}

public DigitalResearchObject getObjectToBindTo () {
	return this.objectToBindTo;
}
}