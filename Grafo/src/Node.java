import java.util.Vector;

/* La classe NODE è così definita
Attributi
  String ID;
	String label;
	boolean start=false,end=false;
	Vector<Node> linked_nodes;
  _analogo per i pesi dei rami_
Il metodo addNode, collega il nodo dato in input a quello attuale.
*/
public class Node {
	//Attributi 
	private String ID;
	private String label;
	private boolean start=false, end = false;
	private Vector<Node> linked_nodes;
	private Vector<Integer> weight_nodes;
	
	public String getID() {
		return this.ID;
	}

	public String getWeightEdge(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getEdge() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasEdge() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEnd() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStart() {
		// TODO Auto-generated method stub
		return false;
	}

	public void addNode(Node nodeByID, String weight) {
		// TODO Auto-generated method stub
		
	}

	public void setLabel(String data) {
		// TODO Auto-generated method stub
		
	}

	public void setID(String data) {
		// TODO Auto-generated method stub
		
	}

	public void setEnd() {
		// TODO Auto-generated method stub
		
	}

	public void setStart() {
		// TODO Auto-generated method stub
		
	}

}
