package edu.pitt.mdc.m1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

// Software node class
public class SoftwareNode extends Node implements Cloneable
{
	ArrayList<SoftwarePort> inputPorts, outputPorts;  

	String title;

	SoftwareNode(Integer uid) { 
	      super(uid);
	      this.inputPorts = new ArrayList<SoftwarePort>();
	      this.outputPorts = new ArrayList<SoftwarePort>();
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
              if (o instanceof SoftwareNode) {
                     SoftwareNode sn = (SoftwareNode)o;
                     eq = this.uid.equals(sn.uid);
                     eq = eq && this.title == sn.title;
                     eq = eq && arePortsEqual(this.inputPorts, sn.inputPorts);
                     eq = eq && arePortsEqual(this.outputPorts, sn.outputPorts);
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