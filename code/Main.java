import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {

    public static void findBurningNumber (BurningGraph g)
    {
        boolean[] burned = new boolean[g.numV];
        int[] roundNum = new int[g.numV];

        for (int i = 0; i < g.numV; i++) {
            roundNum[i] = -1;
        }

        g.calcBurningNumber(0, burned, roundNum);
    
        if (g.OUTPUT_EXTRA_DATA) {
            System.out.println("\nIndex = " + g.graphIndex);
            System.out.println("Burning number = " + g.burningNumber);
            System.out.println(g.burningSequences.size() + " burning sequence(s)");
        }

        // Print all burning sequences for the given graph
        if (g.OUTPUT_BURNING_SEQUENCES) {
            for (int i = 0; i < g.burningSequences.size(); i++) {
                System.out.print("  ( ");
                for (int j = 0; j < g.burningSequences.get(i).size(); j++)
                    System.out.print(g.burningSequences.get(i).get(j) + " ");
                System.out.print(")\n");
            }
        }
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.println("------------------------------\nDirected Acyclic Graph Burning\n------------------------------\n");
        System.out.print("Choose a mode (enter number):\n   1. Graph generation - generate various orientations of DAGs with specified properties\n   2. Graph burning - simulate graph burning and find the burning number on specified graph files\n>> ");
        int chosenMode = in.nextInt();

        /*
         * Graph generation mode
         */
        if (chosenMode == 1) {
            GraphGenerator gen = new GraphGenerator();
            PrintWriter writer = null;
            String path, file = "";

            System.out.print("\nChoose method of generation:\n   1. Generate m random DAGs with k nodes (helpful when generating ALL DAGs with k nodes takes too long)\n   2. Generate all DAGs with k nodes given an adjacency property\n>> ");
            int chosenMethod = in.nextInt();
            int k = 0;
            int n;

            // Method to generate m random DAGs with k nodes
            if (chosenMethod == 1) {

                // Collect user input
                System.out.print("\nInput m\n>> ");
                n = in.nextInt();

                System.out.print("\nInput k\n>> ");
                k = in.nextInt();

                // Run method to generate DAGs
                gen.generateNKDAGs(n, k);

                // Label file according selected method and parameters
                path = "./dags_random/";
                file = path + "" + n + "_" + k + "dag_r";

                if (gen.CHECK_UNIQUENESS) 
                    file += "_u";
                if (gen.CHECK_CONNECTEDNESS)
                    file += "_c";

                file += ".txt";

                try {
                    writer = new PrintWriter(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                System.out.println("\nGenerated " + n + " " + k + "-node-dags");
            
            // Generate all DAGs with k nodes given an adjacency property
            } else if (chosenMethod == 2) {

                // Collect user input
                System.out.print("Input k\n>> ");
                k = in.nextInt();

                // Run method to generate DAGs
                gen.generateKDAGs(k);

                // Label file according to selected method and parameters
                path = "./dags/";
                file = path + "" + k + "dag.txt";

                try {
                    writer = new PrintWriter(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                System.out.println("\nGenerated " + gen.dags.size() + " " + k + "-node-dags");
            }

            // Write generated graph data to file
            for (int i = 0; i < gen.dags.size(); i++) {
                writer.println(i+1);
                writer.println(k + " " + gen.IS_DIRECTED);
                for (int j = 0; j < gen.dags.get(i).length; j++) {
                    writer.print(j + " ");
                    for (int p = 0; p < gen.dags.get(i)[j].size(); p++)
                        writer.print(gen.dags.get(i)[j].get(p) + " ");
                    writer.println();
                }
                if (i != gen.dags.size()-1)
                    writer.println();
            }
            writer.close();

            System.out.println("File written to successfully ---> " + file + "\n");
        } 

        /*
         * Graph burning mode
         */
        else if (chosenMode == 2) {
            FileReader fr = null;

            // Loop to allow user to enter as many graph files to burn as desired
            while (true) {
                System.out.print("\nEnter file (type \"exit\" to exit)\n>> ");
                String fname = in.next();

                // Type exit to exit loop & program
                if (fname.toLowerCase().equals("exit"))
                    System.exit(-1);

                try {
                    File file = new File(fname);
                    fr = new FileReader(fname);
                } catch (FileNotFoundException e){
                    System.out.println("File not found");
                    System.exit(-1);
                }
                
                // Initialize variables needed for graph burning process
                BufferedReader lineReader = new BufferedReader(fr);
                String line;
                boolean setInitial = false;
                boolean setIndex = false;
                int src;
                int directed = -1;
                int numV = -1;
                BurningGraph g = null;
                double avgBN;
                int maxBN = -1;
                int minBN = Integer.MAX_VALUE;
                double BNSum = 0.0;
                double numBNs = 0.0;
                
                // Read in graph data from file
                try {
                    while ((line = lineReader.readLine()) != null) {

                        // Just finished reading in graph, so find burning number of that graph
                        if (line.equals("")) {
                            findBurningNumber(g);
                            BNSum += g.burningNumber;
                            numBNs++;
                            System.out.println("incremented numBNs to " + numBNs);

                            if (g.burningNumber > maxBN)
                                maxBN = g.burningNumber;
                            if (g.burningNumber < minBN)
                                minBN = g.burningNumber;
                    
                            // Reset variables for next graph
                            setInitial = false;
                            setIndex = false;
                            directed = -1;
                            numV = -1;
                            g = null;

                        // Otherwise, we read in the graph g
                        } else {                                 
                            Scanner sc = new Scanner(line); 

                            // If first line of the graph, store the value in graph index var
                            if (!setIndex) {
                                g = new BurningGraph();
                                setIndex = true;
                                g.graphIndex = sc.nextInt();

                            // If second line of graph, store number of vertices in graph and if it is directed
                            } else if (!setInitial) {
                                setInitial = true;

                                numV = sc.nextInt();
                                directed = sc.nextInt();

                                g.initializeVertices(numV);
                                g.isDirected = (directed == 0) ? false : true;

                            // Otherwise, set first number in line to the source vertex and store the remaining numbers as it's edge list
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
                System.out.println("incremented numBNs to " + numBNs);

                if (g.burningNumber > maxBN)
                    maxBN = g.burningNumber;
                if (g.burningNumber < minBN)
                    minBN = g.burningNumber;

                System.out.println(numBNs);
                avgBN = BNSum/numBNs;
                
                System.out.println("\nAverage burning number = " + avgBN);
                System.out.println("Maximum burning number = " + maxBN);
                System.out.println("Minimum burning number = " + minBN);

                // Print result to results to log file
                PrintWriter writer = null;
                FileWriter fileWriter = null;
                String resultsFile = "results.txt";

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
                writer.println("   Min burning number     = " + minBN);
                writer.println();
                writer.close();
            }
        }
    }
}
