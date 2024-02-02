package edu.pitt.mdc.m1;

public class SoftwarePort implements Cloneable
{
	Integer portID;
	Integer softwareID;  // the software it is part of
	PortType type;  //should use ENUM of input, output, but for now 0 denotes output and 1 denotes input. 
	Integer dataFormatID;  // its required data format
	Integer boundToObjectId = 0;  // 0 denotes unbound. non-zero Integer is the softwareID or datasetID it is bound to. Perhaps better if it were of a new type DirectedEdge. Nil would mean unbound
	int boundToSoftwarePortArrayIndex =0; // the array index of the port within the software it is bound to.
	
    SoftwarePort(Integer softwareID, PortType type, Integer dataFormatID) { //type: 0=input, 1=output
        this.portID = 0;  // could be concatenation of software ID, input or output flag, and array index?
        this.softwareID=softwareID;
        this.type = type; 
        this.dataFormatID = dataFormatID; 
        this.boundToObjectId = 0;          // 0 means unbound.
        this.boundToSoftwarePortArrayIndex = 0;    // In general, the bindings aren't set when the port is created.
    }
    
    // is this method needed?  If I have a variable SoftwarePort sp, I can just write sp.softwareID()
    public Integer getSoftwareId() {
    	return this.softwareID;   
    }

    public void setPortId(Integer portId) {
    	this.portID = portId;
    }  
    
    public Integer getPortId() {
    	return this.portID;
    }    
    
    
    public Integer getBoundToObjectId() {
    	return this.boundToObjectId;
    }
    	
    public void setBoundToObjectId(Integer uid) {
    	this.boundToObjectId = uid;   // since the binding is to a port, setBoundTo has to know exactly which port. Ports unfortunately have relative Ids.  Relative to softwareId and whether they are input or output ports
    }
    public void setBoundToSoftwarePortArrayIndex(int uid) {
    	this.boundToSoftwarePortArrayIndex = uid;   // since the binding is to a port, setBoundTo has to know exactly which port. Ports unfortunately have relative Ids.  Relative to softwareId and whether they are input or output ports
    }
    
    public Integer getBoundToSoftwarePortArrayIndex() {
    	return this.boundToSoftwarePortArrayIndex;
    }
}