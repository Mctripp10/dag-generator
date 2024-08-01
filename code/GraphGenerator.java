import java.util.ArrayList;
import java.util.Random;   

public class GraphGenerator {

    ArrayList<ArrayList<Integer>[]> dags;
    ArrayList<ArrayList<Integer>[]> duplicates;
    public final int IS_DIRECTED = 1;
    public boolean CHECK_UNIQUENESS = false;
    public boolean CHECK_CONNECTEDNESS = true;

    public GraphGenerator () 
    {
        dags = new ArrayList<ArrayList<Integer>[]>();
        duplicates = new ArrayList<ArrayList<Integer>[]>();
    }

    // Generates DAGs (directed acyclic graphs) with k nodes, but each node has an adjacency attribute to it
    public void generateKDAGs (int k)
    {
        ArrayList<Integer>[] edges = new ArrayList[k];
        for (int i = 0; i < k; i++) {
            edges[i] = new ArrayList<Integer>();
        }
        helperFunc(0, edges);
    } 

    private void helperFunc (int r, ArrayList<Integer>[] edges)
    {
        if (r == edges.length-1) {
            ArrayList<Integer>[] tmp = new ArrayList[edges.length];
            for (int k = 0; k < edges.length; k++) {                    // Initialize edges by filling it with ArrayLists
                tmp[k] = new ArrayList<Integer>();
            }

            dags.add(copyArray(edges, tmp));
        } else {
            for (int i = r+1; i < edges.length; i++) {
                edges[r].add(i);                                // Add edge (i, j)

                ArrayList<Integer>[] tmp = new ArrayList[edges.length];
                for (int k = 0; k < edges.length; k++) {                    // Initialize edges by filling it with ArrayLists
                    tmp[k] = new ArrayList<Integer>();
                }
                helperFunc(r+1, copyArray(edges, tmp));
            }
        }
    }

    // Alternate method to generate n random dags with k nodes
    public void generateNKDAGs (int n, int k)
    {
        ArrayList<Integer>[] edges = new ArrayList[k];
        Random random = new Random();
        int rnd;
        int m = 0;

        while (m < n)
        {
            // Initialize/reset edges at start of each new dag by filling it with ArrayLists 
            for (int i = 0; i < k; i++) {
                edges[i] = new ArrayList<Integer>();        
            }

            // Generate a dag with k nodes with randomly generated edges between each node
            for (int i = 0; i < k-1; i++) {
                for (int j = i+1; j < k; j++) {
                    rnd = random.nextInt(2);        // Random int between 0 and 1 which decides if there is an edge or not
                    if (rnd == 1) {
                        edges[i].add(j);
                    }
                }
            }

            // Add this randomly generated dag to current array of dags, given specified checks of uniqueness and connectedness
            if (CHECK_UNIQUENESS && CHECK_CONNECTEDNESS) {

                if (isUnique(edges) && isWeaklyConnected(edges)) {
                    ArrayList<Integer>[] tmp = new ArrayList[edges.length];
                    for (int q = 0; q < edges.length; q++) {                    // Initialize tmp edges array by filling it with ArrayLists
                        tmp[q] = new ArrayList<Integer>();
                    }
                    dags.add(copyArray(edges, tmp));
                    m++;        // Increment m, the number of dags we have currently generated
                }

            } else if (CHECK_UNIQUENESS && !CHECK_CONNECTEDNESS) {

                if (isUnique(edges)) {
                    ArrayList<Integer>[] tmp = new ArrayList[edges.length];
                    for (int q = 0; q < edges.length; q++) {                    // Initialize tmp edges array by filling it with ArrayLists
                        tmp[q] = new ArrayList<Integer>();
                    }
                    dags.add(copyArray(edges, tmp));
                    m++;        // Increment m, the number of dags we have currently generated
                }

            } else if (!CHECK_UNIQUENESS && CHECK_CONNECTEDNESS) {

                if (isWeaklyConnected(edges)) {
                    ArrayList<Integer>[] tmp = new ArrayList[edges.length];
                    for (int q = 0; q < edges.length; q++) {                    // Initialize tmp edges array by filling it with ArrayLists
                        tmp[q] = new ArrayList<Integer>();
                    }
                    dags.add(copyArray(edges, tmp));
                    m++;        // Increment m, the number of dags we have currently generated
                }
                
            } else {

                ArrayList<Integer>[] tmp = new ArrayList[edges.length];
                for (int q = 0; q < edges.length; q++) {                    // Initialize tmp edges array by filling it with ArrayLists
                    tmp[q] = new ArrayList<Integer>();
                }
                dags.add(copyArray(edges, tmp));
                m++;        // Increment m, the number of dags we have currently generated
                
            }
        }
    }

    // Returns true if graph is weakly connected, that is, every node has an indegree or outdegree of at least 1
    public boolean isWeaklyConnected (ArrayList<Integer>[] edges)
    {
        boolean[] degreeGreaterThan1 = new boolean[edges.length];

        for (int i = 0; i < edges.length; i++) {
            if (edges[i].size() > 0) 
                degreeGreaterThan1[i] = true;                       // => there is an edge in node i's adjacency list, so outdegree of i >= 1
            for (int j = 0; j < edges[i].size(); j++) {
                degreeGreaterThan1[edges[i].get(j)] = true;         // => there is an edge pointing to node j, so indegree of j >= 1
            }
        }

        for (int i = 0; i < degreeGreaterThan1.length; i++) {
            if (!degreeGreaterThan1[i])
                return false;
        }
        return true;
    }

    public boolean isUnique (ArrayList<Integer>[] edges)
    {
        for (int i = 0; i < dags.size(); i++) 
            if (equalTo(dags.get(i), edges))
                return false;
        return true;
    }

    public boolean equalTo (ArrayList<Integer>[] a1, ArrayList<Integer>[] a2)
    {
        for (int i = 0; i < a1.length; i++) {
            if (a1[i].size() != a2[i].size())
                return false;
            for (int j = 0; j < a1[i].size(); j++) {
                if (a1[i].get(j) != a2[i].get(j))
                    return false;
            }
        }
        return true;
    }

    public ArrayList<Integer>[] copyArray (ArrayList<Integer>[] a1, ArrayList<Integer>[] a2)
    {
        for (int i = 0; i < a1.length; i++) 
            for (int j = 0; j < a1[i].size(); j++)
                a2[i].add(a1[i].get(j));
        return a2;
    }
}