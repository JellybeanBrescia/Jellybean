import java.util.Scanner;

public class Main {
    public static void main(String args[]) {
    	Scanner in = new Scanner(System.in);
    	System.out.println("Inserisci il file da leggere:");
    	String nomeFile = in.next();
    	FromXML2Nodes lettore;
    	try {
			lettore = new FromXML2Nodes(nomeFile);
			System.out.println(lettore.toString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} 
    	
    /*	
        Graph g1 = new Graph(5);
        g1.addEdge(1, 0);
        g1.addEdge(0, 2);
        g1.addEdge(2, 1);
        g1.addEdge(0, 3);
        g1.addEdge(3, 4);

        if (g1.isCyclic())
            System.out.println("Graph contains cycle");
        else
            System.out.println("Graph doesn't contains cycle");

        //Not cyclic
        Graph g2 = new Graph(3);
        g2.addEdge(0, 1);
        g2.addEdge(1, 2);

        if (g2.isCyclic())
            System.out.println("Graph contains cycle");
        else
            System.out.println("Graph doesn't contains cycle");
            */
    }

}
