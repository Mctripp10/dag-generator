import java.util.*;

import javax.sound.midi.SysexMessage;

import java.io.*;

public class BurningGraph {

    private static ArrayList<Integer>[] vertices;        // Array of arraylists where each arraylist is that node's edge list
    private static int numV;                             // Total number of vertices in graph
    private static boolean isDirected;                   // Boolean for if graph is directed or not
    private boolean foundNewBN;
    private final static int INF = Integer.MAX_VALUE;   
    private ArrayList<ArrayList<Integer>> burningSequences;     // Array to store each burning sequence of a graph, where each burning sequence is an int array of sources chosen
    private static ArrayList<Integer> sources;

    private int burningNumber;
    private static double avgBN;
    private static int maxBN;
    private static int graphIndex;

    private static final boolean OUTPUT_EXTRA_DATA = true;
    private static final boolean OUTPUT_BURNING_SEQUENCES = true;

    // Constructor for a BurningGraph object
    public BurningGraph (int n) {
        numV = n;
        vertices = new ArrayList[numV];
        isDirected = true;
        foundNewBN = false;
        burningSequences = new ArrayList<ArrayList<Integer>>();
        sources = new ArrayList<Integer>();
        burningNumber = INF;
        
        // Initialize each edge list to empty list
        for (int i = 0; i < numV; i++)  
            vertices[i] = new ArrayList<Integer>();
    }

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

    public boolean isFullyBurned (boolean[] burned)
    {
        for (int i = 0; i < numV; i++)
            if (!burned[i])
                return false;
        return true;
    }

    // Method that calculates the burning number of a given graph by recursively checking all source combinations (potential sequences)
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

    public ArrayList<Integer> copyArray (ArrayList<Integer> a1, ArrayList<Integer> a2)
    {
        for (int i = 0; i < a1.size(); i++) 
            a2.add(a1.get(i));
        return a2;
    }

    public boolean[] copyArray (boolean[] a1, boolean[] a2)
    {
        for (int i = 0; i < a1.length; i++) 
            a2[i] = a1[i];
        return a2;
    }

    public int[] copyArray (int[] a1, int[] a2)
    {
        for (int i = 0; i < a1.length; i++) 
            a2[i] = a1[i];
        return a2;
    }

    public void printGraph ()
    {
        for (int i = 0; i < numV; i++) {
            System.out.print(i + " --> ");
            for (int j = 0; j < vertices[i].size(); j++)
                System.out.print(vertices[i].get(j) + " ");
            System.out.println();
        }
    }

    public void printArray (boolean[] b)
    {
        for (int i = 0; i < b.length; i ++) {
            System.out.print(b[i] + " ");
        }
    }

    public static void main (String [] args)
    {
        Scanner in = new Scanner(System.in);
        FileReader fr = null;

        double BNSum = 0.0;
        double numBNs = 0.0;

        while (true) {
            System.out.print("\nEnter file --> ");
            String fname = in.nextLine();

            if (fname.toLowerCase().equals("exit"))
                System.exit(-1);

            try {
                File file = new File(fname);
                fr = new FileReader(fname);
            } catch (FileNotFoundException e){
                System.out.println("File not found");
                System.exit(-1);
            }

            BufferedReader lineReader = new BufferedReader(fr);
            String line;
            boolean setInitial = false;
            boolean setIndex = false;
            int src;
            int directed = -1;
            int numV = -1;
            BurningGraph g = null;
            
            try {
                while ((line = lineReader.readLine()) != null) {
                    if (line.equals("")) {          // Just finished reading in graph, so find burning number of that graph
                        findBurningNumber(g);
                        BNSum += g.burningNumber;
                        numBNs++;

                        if (g.burningNumber > maxBN)
                            maxBN = g.burningNumber;
                
                        // Reset variables for next graph
                        setInitial = false;
                        setIndex = false;
                        directed = -1;
                        numV = -1;
                        g = null;
                    } else {                                 // Otherwise, we read in the graph g
                        Scanner sc = new Scanner(line); 
                        if (!setIndex) {
                            setIndex = true;
                            graphIndex = sc.nextInt();
                        } else if (!setInitial) {
                            setInitial = true;

                            numV = sc.nextInt();
                            directed = sc.nextInt();

                            g = new BurningGraph(numV);
                            g.numV = numV;
                            g.isDirected = (directed == 0) ? false : true;
                        } else {
                            src = sc.nextInt();
                            while (sc.hasNextInt()) {
                                g.addEdge(src, sc.nextInt());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            findBurningNumber(g);
            BNSum += g.burningNumber;
            numBNs++;

            if (g.burningNumber > maxBN)
                maxBN = g.burningNumber;

            avgBN = BNSum/numBNs;
            
            System.out.println("\nAverage burning number = " + avgBN);
            System.out.println("Max burning number = " + maxBN);

            // Print result to results file
            PrintWriter writer = null;
            FileWriter fileWriter = null;
            String path = "C:/Users/Admin/Documents/College/Fall 2022/MTH601/";
            String resultsFile = path + "results.txt";

            try {
                fileWriter = new FileWriter(resultsFile, true);
                writer = new PrintWriter(fileWriter);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            writer.println(fname);
            writer.println("   Average burning number = " + avgBN);
            writer.println("   Max burning number     = " + maxBN);
            writer.println();
            writer.close();
        }
    }

    public static void findBurningNumber (BurningGraph g)
    {
        boolean[] burned = new boolean[numV];
        int[] roundNum = new int[numV];

        for (int i = 0; i < numV; i++) {
            roundNum[i] = -1;
        }

        g.calcBurningNumber(0, burned, roundNum);
    
        if (OUTPUT_EXTRA_DATA) {
            System.out.println("\nIndex = " + graphIndex);
            System.out.println("Burning number = " + g.burningNumber);
            System.out.println(g.burningSequences.size() + " burning sequence(s)");
        }

        // Print all burning sequences for the given graph
        if (OUTPUT_BURNING_SEQUENCES) {
            for (int i = 0; i < g.burningSequences.size(); i++) {
                System.out.print("  ( ");
                for (int j = 0; j < g.burningSequences.get(i).size(); j++)
                    System.out.print(g.burningSequences.get(i).get(j) + " ");
                System.out.print(")\n");
            }
        }
    }
}
