package edu.pitt.mdc.m1;
import java.util.ArrayList;

// Software node class
public class SoftwareNode extends Node implements Cloneable
{
       ArrayList<SoftwarePort> inputPorts, outputPorts;  // Assuming exactly 1 output port for now

       String title;
	 
     SoftwareNode(Integer uid) { 
            this.uid = uid;
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
     
    }