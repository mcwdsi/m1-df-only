package edu.pitt.mdc.m1;

import java.util.ArrayList;
import java.util.Collections;

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
        Collections.sort(this.dataFormatIds);
	}

	Integer portID;
	Integer softwareID;  // the software the port is a part of
	PortType type;  // INPUT, OUTPUT
	private ArrayList<Integer> dataFormatIds;  
	Integer boundToObjectId = 0;  // 0 denotes unbound. non-zero Integer is the softwareID or datasetID it is bound to. Perhaps better if it were of a new type DirectedEdge. Nil would mean unbound
	Integer boundToSoftwarePortArrayIndex = 0; // the array index of the port within the software it is bound to.
    Integer boundViaDataFormatId = 0;
	
    SoftwarePort(Integer softwareID, PortType type, ArrayList<Integer> dataFormatIds) { //type: INPUT, OUTPUT
        this.portID = 0;  // could be concatenation of software ID, input or output flag, and array index?
        this.softwareID=softwareID;
        this.type = type; 
        setDataFormats(dataFormatIds); 
        this.boundToObjectId = 0;          // 0 means unbound.
        this.boundToSoftwarePortArrayIndex = 0;    // In general, the bindings aren't set when the port is created.
        this.boundViaDataFormatId = 0;
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
        if (uid == null) throw new IllegalArgumentException("Cannot have boundToObjectId == null");
    	this.boundToObjectId = uid;   // since the binding is to a port, setBoundTo has to know exactly which port. Ports unfortunately have relative Ids.  Relative to softwareId and whether they are input or output ports
    }

    public void setBoundToSoftwarePortArrayIndex(int uid) {
    	this.boundToSoftwarePortArrayIndex = uid;   // since the binding is to a port, setBoundTo has to know exactly which port. Ports unfortunately have relative Ids.  Relative to softwareId and whether they are input or output ports
    }
    
    public Integer getBoundToSoftwarePortArrayIndex() {
    	return this.boundToSoftwarePortArrayIndex;
    }

    public void setBoundViaDataFormatId(Integer formatId) {
        if (formatId == null) throw new IllegalArgumentException("boundViaDataFormatId cannot be null");
        this.boundViaDataFormatId = formatId;
    }

    public Integer getBoundViaDataFormatId() {
        return this.boundViaDataFormatId;
    }

    @Override
    public boolean equals(Object o) {
        boolean eq = (o instanceof SoftwarePort);
        if (eq) {
            SoftwarePort sp = (SoftwarePort)o;
            eq = eq && this.softwareID.equals(sp.softwareID);
            eq = eq && this.type == sp.type;
            eq = eq && this.portID.equals(sp.portID);
            eq = eq && this.boundToObjectId.equals(sp.boundToObjectId);
            eq = eq && this.boundToSoftwarePortArrayIndex.equals(sp.boundToSoftwarePortArrayIndex);
            eq = eq && this.boundViaDataFormatId.equals(sp.boundViaDataFormatId);
            if (eq) {
                int cFormat = this.dataFormatIds.size();
                eq = eq && cFormat == sp.dataFormatIds.size();
                if (eq) {
                    ArrayList<Integer> spFormatIds = sp.getDataFormats();
                    for (int i=0; i<cFormat; i++) {
                        eq = eq && this.dataFormatIds.get(i).equals(spFormatIds.get(i));
                    }
                }
            }
        }
        return eq;
    }
}