import java.util.Iterator;
import java.util.Vector;

/**
 * A program to detect if a given undirected, unweighted graph contains loops
 * http://www.geeksforgeeks.org/detect-cycle-undirected-graph/
 */

public class Graph {
    private int n_vertices;
    private Vector<Integer> adjacency[]; // Adjacency Matrix
    //

    // Constructor of the class Graph
    public Graph(int n_vertices) {
        this.n_vertices = n_vertices;
        this.adjacency = new Vector[n_vertices];

        //Initialization of adjacency matrix
        for (int i = 0; i < n_vertices; ++i) {
            adjacency[i] = new Vector<Integer>();
        }
    }

    // Function to add an edge into the graph
    public void addEdge(int v1, int v2) {
        adjacency[v1].add(v2);
        adjacency[v2].add(v1);
    }

    // A recursive function that uses visited[] and parent to detect
    // cycle in subgraph reachable from vertex v
    public boolean isCyclicUtil(int current_vertex, boolean visited[], int parent_vertex) {
        // Mark the current node as visited
        visited[current_vertex] = true;
        int adj_vertex;

        // Recur for all the vertices adjacent to this vertex
        Iterator<Integer> it = adjacency[current_vertex].iterator();
        while (it.hasNext()) {
            adj_vertex = it.next();

            // If an adjacent is not visited, then recur for that adjacent
            if (!visited[adj_vertex])
                if (isCyclicUtil(adj_vertex, visited, current_vertex))
                    return true;

            // If an adjacent is visited and not parent of current
            // vertex, then there is a cycle. This control excludes the case in which
            // we visit the parent of the node we are analyzing.
            if (adj_vertex != parent_vertex)
                return true;
        }
        return false;
    }

    // Returns true if the graph contains a cycle, else false.
    boolean isCyclic() {
        // Mark all the vertices as not visited and not part of
        // recursion stack
        boolean visited[] = new boolean[n_vertices];
        for (int i = 0; i < n_vertices; i++)
            visited[i] = false;

        // Call the recursive helper function to detect cycle in
        // different DFS trees
        for (int u = 0; u < n_vertices; u++)
            if (!visited[u]) // Don't recur for u if already visited
                if (isCyclicUtil(u, visited, -1))
                    return true;
        return false;
    }

   
}
