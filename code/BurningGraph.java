import java.util.*;

import javax.sound.midi.SysexMessage;

import java.io.*;

public class BurningGraph {

    int numV;                                            // Total number of vertices in graph
    ArrayList<ArrayList<Integer>> burningSequences;      // Array to store each burning sequence of a graph, where each burning sequence is an int array of sources chosen
    boolean isDirected;                                  // Boolean for if graph is directed or not
    int burningNumber;
    int graphIndex;

    private static ArrayList<Integer>[] vertices;        // Array of arraylists where each arraylist is that node's edge list
    private boolean foundNewBN;
    private final static int INF = Integer.MAX_VALUE;   
    private static ArrayList<Integer> sources;

    final boolean OUTPUT_EXTRA_DATA = true;
    final boolean OUTPUT_BURNING_SEQUENCES = true;

    // Constructor for a BurningGraph object given graph index
    public BurningGraph () {
        isDirected = true;
        foundNewBN = false;
        burningSequences = new ArrayList<ArrayList<Integer>>();
        sources = new ArrayList<Integer>();
        burningNumber = INF;
    }

    // Initialize BurningGraph object vertex array list given n number of vertices
    public void initializeVertices (int n) {
        numV = n;
        vertices = new ArrayList[numV];
        
        // Initialize each edge list to empty list
        for (int i = 0; i < numV; i++)  
            vertices[i] = new ArrayList<Integer>();
    }

    // Adds an edge between vertices v and d
    public void addEdge (int v, int d)
    {
        if (vertices[v].size() == 0)
            vertices[v] = new ArrayList<Integer>();
        vertices[v].add(d);

        // Add edge in the other direction as well if it isn't directed
        if (!isDirected) {
            if (vertices[d].size() == 0)
                vertices[d] = new ArrayList<Integer>();
            vertices[d].add(v);
        }
    }

    // Burns all unburned vertices that share an edge with a burned vertex and returns the resulting burned list and round num in an ArrayList
    public ArrayList<Object> spread (boolean[] burned, int[] roundNum, int currRound)
    {
        int adj;
        ArrayList<Object> a = new ArrayList<Object>();

        for (int i = 0; i < numV; i++) {
            if (burned[i] && roundNum[i] == currRound) {
                for (int j = 0; j < vertices[i].size(); j++) {
                    adj = vertices[i].get(j);
                    if (!burned[adj]) {
                        burned[adj] = true;
                        roundNum[adj] = currRound + 1;
                    }
                }
            }
        }
        a.add(burned);
        a.add(roundNum);
        return a;
    }

    // Returns true if graph is fully burned and false otherwise based on the given list of burned vertices
    public boolean isFullyBurned (boolean[] burned)
    {
        for (int i = 0; i < numV; i++)
            if (!burned[i])
                return false;
        return true;
    }

    // Calculates the burning number of a given graph by recursively checking all source combinations (potential sequences)
    public int calcBurningNumber (int currRound, boolean[] burned, int[] roundNum)
    {
        // Once graph is fully burned, add current sequence to array of burning sequences
        if (isFullyBurned(burned)) {
            if (foundNewBN) {                // Clear previous (worse) sequences if a better burning number has been found before adding this new sequence
                burningSequences.clear();
                foundNewBN = false;
            }
            ArrayList<Integer> tmp = new ArrayList<Integer>();
            burningSequences.add(copyArray(sources, tmp));
            return currRound;

        // Quit  early and return -1 if the current round is already worse than the current burning number
        } else if (currRound >= burningNumber) {
            return -1;
        } else {
            
            boolean[] tmpBurned = new boolean[burned.length];
            int[] tmpRoundNum = new int[roundNum.length];
            ArrayList<Object> a;
            int num;

            for (int i = 0; i < numV; i++) {
                if (!burned[i]) {
                    
                    tmpBurned = copyArray(burned, tmpBurned);
                    tmpRoundNum = copyArray(roundNum, tmpRoundNum);
                    tmpBurned[i] = true;
                    tmpRoundNum[i] = currRound;
                    sources.add(i);

                    a = spread(tmpBurned, tmpRoundNum, currRound);
                    num = calcBurningNumber(currRound+1, (boolean[]) a.get(0), (int[]) a.get(1));
                    sources.remove(sources.size()-1);
                    if (num != -1 && num < burningNumber) {
                        burningNumber = num;
                        foundNewBN = true;
                    }
                }
            }
            return burningNumber;
        }
    }

    // Deep copy of two Integer array lists
    public ArrayList<Integer> copyArray (ArrayList<Integer> a1, ArrayList<Integer> a2)
    {
        for (int i = 0; i < a1.size(); i++) 
            a2.add(a1.get(i));
        return a2;
    }

    // Deep copy of two boolean arrays
    public boolean[] copyArray (boolean[] a1, boolean[] a2)
    {
        for (int i = 0; i < a1.length; i++) 
            a2[i] = a1[i];
        return a2;
    }

    // Deep copy of two int arrays
    public int[] copyArray (int[] a1, int[] a2)
    {
        for (int i = 0; i < a1.length; i++) 
            a2[i] = a1[i];
        return a2;
    }

    // Prints the burning graph object to screen
    public void printGraph ()
    {
        for (int i = 0; i < numV; i++) {
            System.out.print(i + " --> ");
            for (int j = 0; j < vertices[i].size(); j++)
                System.out.print(vertices[i].get(j) + " ");
            System.out.println();
        }
    }

    // Prints given array b of booleans
    public void printArray (boolean[] b)
    {
        for (int i = 0; i < b.length; i ++) {
            System.out.print(b[i] + " ");
        }
    }
}
