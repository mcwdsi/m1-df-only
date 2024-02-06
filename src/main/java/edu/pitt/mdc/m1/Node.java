package edu.pitt.mdc.m1;

// Node class.  I had SoftwareNode as a subclass, but in a debugging frenzy put that 'feature' back onto the todo list
public class Node implements Comparable<Node> {
	 Integer uid;

	 public boolean equals(Object o) {
	 	return (o instanceof Node) && ((Node)o).uid == this.uid;
	 }

	 public int compareTo(Node n) {
	 	int retVal;
	 	if (this.uid < n.uid) {
	 		retVal = -1;
	 	} else if (this.uid > n.uid) {
	 		retVal = 1;
	 	} else {
	 		retVal = 0;
	 	}
	 	return retVal;
	 }
}
