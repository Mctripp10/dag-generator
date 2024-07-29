import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.println("------------------------------\nDirected Acyclic Graph Burning\n------------------------------\n");
        System.out.print("Choose a mode (enter number):\n   1. Graph generation - generate various orientations of DAGs with specified properties\n   2. Graph burning - simulate graph burning and find the burning number on specified graph files\n>> ");
        int chosenMode = in.nextInt();

        if (chosenMode == 1) {
            GraphGenerator g = new GraphGenerator();
            PrintWriter writer = null;
            String path, file = "";

            System.out.print("\nChoose method of generation:\n   1. Generate m random DAGs with k nodes\n   2. Generate all DAGs with k nodes given an adjacency property\n>> ");
            int chosenMethod = in.nextInt();
            int k = 0;
            int n;

            if (chosenMethod == 1) {
                System.out.print("\nInput m\n>> ");
                n = in.nextInt();

                System.out.print("\nInput k\n>> ");
                k = in.nextInt();

                g.generateNKDAGs(n, k);

                path = "./dags_random/";
                file = path + "" + n + "_" + k + "dag_r";

                if (g.CHECK_UNIQUENESS) 
                    file += "_u";
                if (g.CHECK_CONNECTEDNESS)
                    file += "_c";

                file += ".txt";

                try {
                    writer = new PrintWriter(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                System.out.println("\nGenerated " + n + " " + k + "-node-dags");
            } else if (chosenMethod == 2) {
                System.out.print("Input k\n>> ");
                k = in.nextInt();

                g.generateKDAGs(k);

                path = "./dags/";
                file = path + "" + k + "dag.txt";

                try {
                    writer = new PrintWriter(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                System.out.println("\nGenerated " + g.dags.size() + " " + k + "-node-dags");
            }

            for (int i = 0; i < g.dags.size(); i++) {
                writer.println(i+1);
                writer.println(k + " " + g.IS_DIRECTED);
                for (int j = 0; j < g.dags.get(i).length; j++) {
                    writer.print(j + " ");
                    for (int p = 0; p < g.dags.get(i)[j].size(); p++)
                        writer.print(g.dags.get(i)[j].get(p) + " ");
                    writer.println();
                }
                if (i != g.dags.size()-1)
                    writer.println();
            }
            writer.close();

            System.out.println("File written to successfully ---> " + file + "\n");
        } 
        else if (chosenMode == 2) {
            FileReader fr = null;

            double BNSum = 0.0;
            double numBNs = 0.0;

            while (true) {
                System.out.print("\nEnter file\n>> ");
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
                writer.println();
                writer.close();
            }
        }
    }
}
