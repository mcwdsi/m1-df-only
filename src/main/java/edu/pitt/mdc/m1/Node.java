package edu.pitt.mdc.m1;

// Node class.  I had SoftwareNode as a subclass, but in a debugging frenzy put that 'feature' back onto the todo list
public class Node implements Comparable<Node> {
	 Integer nodeId;

	 public Node(int nodeId) {
	 	this.nodeId = nodeId;
	 }

	 public int getId() {
	 	return this.nodeId;
	 }

	 @Override
	 public boolean equals(Object o) {
	 	return (o instanceof Node) && ((Node)o).nodeId == this.nodeId;
	 }

	 public int compareTo(Node n) {
	 	int retVal;
	 	if (this.nodeId < n.nodeId) {
	 		retVal = -1;
	 	} else if (this.nodeId > n.nodeId) {
	 		retVal = 1;
	 	} else {
	 		retVal = 0;
	 	}
	 	return retVal;
	 }
}
