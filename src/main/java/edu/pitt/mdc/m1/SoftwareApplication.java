package edu.pitt.mdc.m1; 

import java.util.ArrayList;
import java.util.Iterator;

public class SoftwareApplication {
	
	Integer id;
	ArrayList<SoftwarePort> inputPorts, outputPorts;  
	String title;


	public SoftwareApplication(Integer id) {
		this.id = id;
		this.title = null;
	    this.inputPorts = new ArrayList<SoftwarePort>();
		this.outputPorts = new ArrayList<SoftwarePort>();
	}

	public SoftwareApplication(Integer id, String title, 
		ArrayList<SoftwarePort> inputPorts, ArrayList<SoftwarePort> outputPorts) {
		this.id = id;
		this.title = title;
	    this.inputPorts = new ArrayList<SoftwarePort>();
	    this.inputPorts.addAll(inputPorts);
		this.outputPorts = new ArrayList<SoftwarePort>();
		this.outputPorts.addAll(outputPorts);
	}

	public int numInputs() {
	      return this.inputPorts.size();
	}

	public int numOutputs() {
	      return this.outputPorts.size();
	}

	public String getTitle() {
	      return this.title;
	}

       @Override
       public boolean equals(Object o) {
              boolean eq = false;
              if (o instanceof SoftwareApplication) {
                     SoftwareApplication sa = (SoftwareApplication)o;
                     eq = this.id.equals(sa.id);
                     eq = eq && this.title == sa.title;
                     eq = eq && arePortsEqual(this.inputPorts, sa.inputPorts);
                     eq = eq && arePortsEqual(this.outputPorts, sa.outputPorts);
              }
              return eq;
       }

       protected boolean arePortsEqual(ArrayList<SoftwarePort> thisPorts, 
              ArrayList<SoftwarePort> thatPorts) {
              boolean eq = (thisPorts.size() == thatPorts.size());
              if (eq) {
                     Iterator<SoftwarePort> thispIter = thisPorts.iterator();
                     Iterator<SoftwarePort> thatpIter = thatPorts.iterator();
                     while (thispIter.hasNext()) {
                            SoftwarePort thispNext = thispIter.next();
                            SoftwarePort thatpNext = thatpIter.next();
                            eq = eq && thispNext.equals(thatpNext);
                            // as soon as it's false we're done, no need to continue
                            if (!eq) break;
                     }
              }
              return eq;
       }
}