package edu.pitt.mdc.m1;

import java.util.ArrayList;

public class SoftwarePort implements Cloneable
{
	public ArrayList<Integer> getDataFormats() {
        ArrayList<Integer> formats = new ArrayList<Integer>();
        formats.addAll(this.dataFormatIds);
		return formats;
	}

	public void setDataFormats(ArrayList<Integer> dataFormats) {
		this.dataFormatIds = new ArrayList<Integer>();
        this.dataFormatIds.addAll(dataFormats);
	}

	Integer portID;
	Integer softwareID;  // the software the port is a part of
	PortType type;  // INPUT, OUTPUT
	ArrayList<Integer> dataFormatIds;  
	Integer boundToObjectId = 0;  // 0 denotes unbound. non-zero Integer is the softwareID or datasetID it is bound to. Perhaps better if it were of a new type DirectedEdge. Nil would mean unbound
	int boundToSoftwarePortArrayIndex =0; // the array index of the port within the software it is bound to.
	
    SoftwarePort(Integer softwareID, PortType type, ArrayList<Integer> dataFormatIds) { //type: INPUT, OUTPUT
        this.portID = 0;  // could be concatenation of software ID, input or output flag, and array index?
        this.softwareID=softwareID;
        this.type = type; 
        this.dataFormatIds = dataFormatIds; 
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