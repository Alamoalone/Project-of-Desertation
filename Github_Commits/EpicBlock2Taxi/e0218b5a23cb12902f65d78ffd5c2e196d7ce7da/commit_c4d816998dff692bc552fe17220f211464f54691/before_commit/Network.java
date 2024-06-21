package network;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Network class! Will have edges and nodes, and will be used to keep track of the network. once the network is created, we will
 * find the central node and call that the "root" node. Though it will be cool to add an entity based on x,y or address?
 *
 * """If some edges connect nodes not yet in the graph, the nodes are added automatically.
 * There are no errors when adding nodes or edges that already exist""" - from networkx documentation
 * we will do the same thing, and just add the node or edge to the network if it doesn't exist.
 *
 */

public class Network {
    private Node[] nodes;
    private Edge[] edges;

    public Network() {
        this.nodes = new Node[0]; // This could start with a size of much more than 1, but just for testing purposes this is ok
        this.edges = new Edge[0];
    }

    private void addNodeToNodesArray(Node UserNode) {
//         add node to network
        Node[] temp_nodes = new Node[this.nodes.length + 1]; // TODO create new array with more space, maybe create dynamic array
//         https://www.geeksforgeeks.org/creating-a-dynamic-array-in-java/ SOMETHING LIKE THIS

        System.arraycopy(this.nodes, 0, temp_nodes, 0, this.nodes.length);

//        manual copy, same as above
//        for (int i = 0; i < this.nodes.length; i++) {
//            temp_nodes[i] = this.nodes[i];
//        }

        temp_nodes[this.nodes.length] = UserNode;
        this.nodes = temp_nodes;

    }
    private void addEdgeToEdgesArray(Edge UserEdge) {
        // add edge to network
        Edge[] temp_edges = new Edge[this.edges.length + 1]; // TODO create new array with more space, maybe create dynamic array

        System.arraycopy(this.edges, 0, temp_edges, 0, this.edges.length);

        temp_edges[this.edges.length] = UserEdge;
        this.edges = temp_edges;
        UserEdge.start.addEdge(UserEdge);
        UserEdge.end.addEdge(UserEdge);
    }

    private Node getNode(String nodeID) {
        // get node from network
        for (Node n : this.nodes) {
            if (n.id.equals(nodeID)) {
                return n;
            }
        }
        return null;
    }

//    SEARCHING FOR NODES AND EDGES IN NETWORK ---- TODO: implement searching algorithms
    private boolean checkNodeExists(Node UserNode) {
        // check if node exists in network
        for (Node n : this.nodes) {
            if (UserNode == n) { // == and not .equals because we are looking for the same object, not the same value
                return true;
            }
        }
        return false;
    }
    private boolean checkNodeExists(String nodeID) {
        // check if node exists in network
        for (Node n : this.nodes) {
            if (n.id.equals(nodeID)) {
                return true;
            }
        }
        return false;
    }
    private boolean checkEdgeExists(Edge UserEdge) {
        // check if edge exists in network
        for (Edge e : this.edges) {
            if (e.start.id.equals(UserEdge.start.id) && e.end.id.equals(UserEdge.end.id)) {
                return true;
            }
        }
        return false;
    }
    private boolean checkEdgeExists(String nodeID1, String nodeID2) {
        // check if edge exists in network
        for (Edge e : this.edges) {
            if (e.start.id.equals(nodeID1) && e.end.id.equals(nodeID2)) {
                return true;
            }
        }
        return false;
    }

// ----------------------------------------------------


    private void removeNode(Node UserNode) {
        // remove node from network
    }

    public void addEdge(Node node1, Node node2) {
        // add edge to network
        if (!checkNodeExists(node1)) {
            addNodeToNodesArray(node1);
        }
        if (!checkNodeExists(node2)) {
            addNodeToNodesArray(node2);
        }
        Edge UserEdge = new Edge();
        UserEdge.start = node1;
        UserEdge.end = node2;
        UserEdge.weight = 1; // TODO: make this be the distance between the two nodes (in ticks)
        addEdgeToEdgesArray(UserEdge);

    }

    public void addEdge(String nodeID1, String nodeID2) {
        // add edge to network
        if (!checkNodeExists(nodeID1)) {
            Node node1 = new Node(nodeID1, 0, 0);
            addNodeToNodesArray(node1);
        }
        if (!checkNodeExists(nodeID2)) {
            Node node2 = new Node(nodeID2, 0, 0);
            addNodeToNodesArray(node2);
        }
        Edge UserEdge = new Edge();
        UserEdge.start = getNode(nodeID1);
        UserEdge.end = getNode(nodeID2);
        UserEdge.weight = 1; // TODO: make this be the distance between the two nodes (in ticks)
        addEdgeToEdgesArray(UserEdge);

    }

    // with weight should be default, NOT other way round TODO invert this
    public void addEdge(Node node1, Node node2, int weight) {
        this.addEdge(node1, node2);
        this.getEdge(node1, node2).weight = weight;
    }

    public void addEdge(String nodeID1, String nodeID2, int weight) {
        this.addEdge(nodeID1, nodeID2);
        this.getEdge(nodeID1, nodeID2).weight = weight;

    }

    private Edge getEdge(String nodeID1, String nodeID2) {
        // get edge from network
        for (Edge e : this.edges) {
            if (e.start.id.equals(nodeID1) && e.end.id.equals(nodeID2)) {
                return e;
            }
        }
        return null;
    }

    private Edge getEdge(Node node1, Node node2) {
        for (Edge e : this.edges) {
            if (e.start == node1 && e.end == node2) {
                return e;
            }
        }
        return null;
    }


    public Node[] getNodes() {
        return nodes;
    }

    public Edge[] getEdges() {
        return edges;
    }

    public Node[] findPath(Node Nodestart, Node Nodeend) {
        // find path from start to end
        // A* would be cool, but would require a direct line of distance to target node geometrically
        // so we will use Dijkstra's algorithm
        //https://stackoverflow.com/a/10053969
        //"""From Introduction to Algorithms (CLRS) second edition, page 581 :
        //Find a shortest path from u to v for a given vertices u and v.
        // If we solve the single-source problem with source vertex u, we solve this problem also.
        // Moreover, no algorithms for this problem are known that run asymptotically faster than the best single-source algorithms in the worst case.
        //So, stick to Dijkstra's algorithm :)"""

         class DijkstraNode {
            Node node;
            int dist;
            DijkstraNode prev;

        }

        DijkstraNode[] dijkstraNodes = new DijkstraNode[this.nodes.length];
        for (int i = 0; i < this.nodes.length; i++) {
            dijkstraNodes[i] = new DijkstraNode();
            dijkstraNodes[i].node = this.nodes[i];
            dijkstraNodes[i].dist = Integer.MAX_VALUE;
            dijkstraNodes[i].prev = null;
        }

        // find start node TODO extract this to a function
        DijkstraNode startNode = null;
        for (DijkstraNode dn : dijkstraNodes) {
            if (dn.node == Nodestart) {
                startNode = dn;
                break;
            }
        }

        if (startNode == null) {
            throw new NullPointerException("Start node not found in network");
        }

        // find destination node
        DijkstraNode endNode = null;
        for (DijkstraNode dn : dijkstraNodes) {
            if (dn.node == Nodeend) {
                endNode = dn;
                break;
            }
        }
        if (endNode == null) {
            throw new NullPointerException("End node not found in network");
        }


        startNode.dist = 0;

        Arrays.sort(dijkstraNodes, (a, b) -> a.dist - b.dist);

        Node[] path = new Node[0];
        while (dijkstraNodes.length > 0) {
            DijkstraNode u = dijkstraNodes[0];
            for (DijkstraNode dn : dijkstraNodes) {
                if (dn.dist < u.dist) {
                    u = dn;
                }
            }
            if (u.node == Nodeend) {
                break;
            }
            DijkstraNode[] temp_Q = new DijkstraNode[dijkstraNodes.length - 1];
            int temp_Q_index = 0;
            for (DijkstraNode dn : dijkstraNodes) {
                if (dn != u) {
                    temp_Q[temp_Q_index] = dn;
                    temp_Q_index++;
                }
            }
            dijkstraNodes = temp_Q;

            for (Edge e : u.node.edges) {
                DijkstraNode v = null;
                for (DijkstraNode dn : dijkstraNodes) {
                    if (dn.node == e.end) {
                        v = dn;
                        break;
                    }
                }
                int alt = u.dist + e.weight;
                if (alt < v.dist) {
                    v.dist = alt;
                    v.prev = u;
                }
            }
        }


        List<Node> pathList = new ArrayList<>();    // TODO URGENT: make this a dynamic array
        for (DijkstraNode dn = endNode; dn != null; dn = dn.prev) {
            pathList.add(0, dn.node); // append node to the path
        }

        return pathList.toArray(new Node[0]);
    }

    public Node[] findPath(String nodeID1, String nodeID2) {
        return findPath(getNode(nodeID1), getNode(nodeID2));
    }


}
