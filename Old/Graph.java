package Old;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.*;

public class Graph {

    private boolean isDirected;
    private ArrayList<Node> nodeList;
    private ArrayList<Node> unburnedNodes;
    private HashMap<String, Integer> nodeMap;
    private int numNodes;
    private int roundNum;
    private final static int INF = Integer.MAX_VALUE;

    public Graph () 
    {
        nodeList = new ArrayList<Node>();
        unburnedNodes = new ArrayList<Node>();
        nodeMap = new HashMap<String, Integer>();
        isDirected = false;
        numNodes = 0;
        roundNum = 0;
    }

    public Graph (Graph g)
    {
        nodeList = new ArrayList<Node>();
        copyNodeList(g.nodeList);
        unburnedNodes = new ArrayList<Node>();
        copyUnburnedNodes(g.unburnedNodes);
        nodeMap = new HashMap<>(g.nodeMap);
        isDirected = g.isDirected;
        numNodes = g.numNodes;
        roundNum = g.roundNum;
    }

    public void copyUnburnedNodes (ArrayList<Node> a)
    {
        for (int i = 0; i < a.size(); i++)
            if (hasNode(a, nodeList.get(i)))
                unburnedNodes.add(nodeList.get(i));
    }

    public boolean hasNode (ArrayList<Node> a, Node n)
    {
        for (int i = 0; i < a.size(); i++) 
            if (a.get(i).name.equals(n.name))
                return true;
        return false;
    }

    public void copyNodeList (ArrayList<Node> a)
    {
        for (int i = 0; i < a.size(); i++) 
            nodeList.add(new Node(a.get(i)));
    }
    
    private class Node
    {
        public String name;
        public LinkedList<Edge> adj;
        public boolean isBurned;
        public int srcNum;

        public Node (String n)
        {
            name = n;
            adj = new LinkedList<Edge>();
            isBurned = false;
            srcNum = -1;
        }

        public Node (Node n)
        {
            adj = new LinkedList<Edge>();
            name = n.name;
            copyEdges(n.adj);
            isBurned = n.isBurned;
            srcNum = n.srcNum;
        }

        private void copyEdges (LinkedList<Edge> l)
        {
            for (int i = 0; i < l.size(); i++)
            {
                adj.add(new Edge (l.get(i)));
            }
        }
    }

    private class Edge
    {
        public Node dest;
        public boolean isBurned; // Edge burning

        public Edge (Node d)
        {
            dest = d;
            isBurned = false;
        }

        public Edge (Edge e)
        {
            dest = e.dest;
            isBurned = e.isBurned;
        }
    } 

    public void addEdge (String srcName, String destName)
    {
        Node s = getNode(srcName);
        Node d = getNode(destName);

        s.adj.add(new Edge(d));
        if (!isDirected) d.adj.add(new Edge(s));        // If directed, only add edge from src to dest
    }

    public Node getNode (String name)
    {
        Node n;
        var i = nodeMap.get(name);
        n = (i == null) ? addNode(name) : nodeList.get(i);      // Add node if it hasn't been seen before
        return n;
    }

    private Node addNode (String name)
    {
        Node n = new Node(name);
        nodeList.add(n);
        unburnedNodes.add(n);
        nodeMap.put(name, numNodes++);
        return n;
    }

    public void setDirected (int b)
    {
        isDirected = (b == 0) ? false : true;
    }

    public boolean getDirected ()
    {
        return isDirected;
    }

    public boolean graphIsBurned ()
    {
        if (unburnedNodes.size() == 0)
            return true;
        else
            return false;
    }

    public void burn (Node n)
    {
        n.isBurned = true;
        System.out.println(unburnedNodes.remove(n));
        printUnburned();
    }

    public void setSource (String name)
    {
        Node n = getNode(name);
        n.srcNum = roundNum;
        System.out.println("Round " + roundNum + ", src " + n.name);
        burn(n);
    }

    public void spread ()
    {
        Node s;
        Node n;

        for (int i = 0; i < nodeList.size(); i++) {
            s = nodeList.get(i);
            if (s.isBurned && s.srcNum == roundNum) {
                System.out.println("??");
                for (int j = 0; j < s.adj.size(); j++) {
                    n = s.adj.get(j).dest;
                    if (!n.isBurned) {
                        n.srcNum = roundNum + 1;
                        burn(n);
                        System.out.println("        " + s.name + " burned " + n.name);
                    }
                }
            }
        }
        roundNum++;
    }

    public void printUnburned ()
    {
        for (int i = 0; i < unburnedNodes.size(); i++)
        {
            System.out.print(unburnedNodes.get(i).name + "(" + unburnedNodes.get(i).isBurned + ")" + " ");
        }
        System.out.println();
    }

    // Try backtracking!
    public static int calcBurningNumber (int bn, int rounds, Graph g)
    {
        if (g.graphIsBurned()) {
            System.out.println("base case");
            return rounds;
        } else {
            int r;
            Graph tmp;
            for (int i = 0; i < g.unburnedNodes.size(); i++) {
                tmp = new Graph(g);
                tmp.setSource(g.unburnedNodes.get(i).name);
                g.printUnburned();
                tmp.spread();
                r = calcBurningNumber(bn, ++rounds, tmp);
                if (r < bn) bn = r;
            }
            return bn;
        }
    }

    public static void main (String [] args)
    {
        final int ROUND_BY_ROUND = 0;
        final int LIST_SOURCES = 1; 
        final int CHECK_ALL_SOURCES = 2;

        Graph g = new Graph();

        Scanner in = new Scanner(System.in);
        Scanner fin = null;

        System.out.println("Enter file --> ");
        String fname = in.nextLine();

        try {
            File file = new File(fname);
            fin = new Scanner(file);
        } catch (FileNotFoundException e){
            System.out.println("File not found");
            System.exit(-1);
        }

        g.setDirected(fin.nextInt());

        while (fin.hasNextLine()) {
            g.addEdge(fin.next(), fin.next());
        }

        System.out.println("Select a mode: ");
        System.out.println("    0 --> Round by round");
        System.out.println("    1 --> List sources");
        System.out.println("    2 --> Find burning number");

        int mode = in.nextInt();
        in.nextLine();

        String src;
        int burningNumber = INF;

        if (mode == ROUND_BY_ROUND) {

            while (!g.graphIsBurned()) {
                System.out.println("Enter a new source: ");
                src = in.nextLine();
                g.setSource(src);
                g.spread();
            }

            System.out.println("Graph is burned");
            System.out.println("\"Burning number\" --> " + g.roundNum);

        } else if (mode == LIST_SOURCES) {

            System.out.println("Enter list of sources: ");

            while (!g.graphIsBurned()) {
                src = in.next();
                g.setSource(src);
                g.spread();
            }

            System.out.println("Graph is burned");
            System.out.println("\"Burning number\" --> " + g.roundNum);

        } else if (mode == CHECK_ALL_SOURCES) {

            System.out.println("Burning number --> " + calcBurningNumber(INF, 0, g));
            
        }
    }    
}

