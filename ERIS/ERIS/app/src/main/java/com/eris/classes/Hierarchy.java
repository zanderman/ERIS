package com.eris.classes;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * A command hierarchy tree that can add and remove responders dynamically
 */

public class Hierarchy {

    private HashMap<String, Node> map;
    private Node root;

    public Hierarchy() {
        map = new HashMap<String, Node>();
        root = null;
    }

    /**
     * Adds a responder to the hierarchy based on certain rules regarding rank and branch
     * @param responder the responder being added to the hierarchy
     */
    public void addResponder(Responder responder) {
        Node node = new Node(responder,
                determineIntRank(responder.getRank(), responder.getBranch()), responder.getBranch());
        map.put(responder.getID(), node);
        addExistingNode(node);
    }

    /**
     * Gets the command chain starting with this responder and working up to root
     * @param r The responder a the bottom of the command chain
     * @return An ArrayList of Responders in increasing order of superiority, starting with r and
     * ending with root
     */
    public ArrayList<Responder> getCommandChain(Responder r) {
        Node node = map.get(r.getID());
        ArrayList<Responder> chain = new ArrayList<Responder>();
        while (node != null) {
            chain.add(node.responder);
            node = node.parent;
        }
        return chain;
    }

    public Responder getSuperior(Responder r) {
        Node node = map.get(r.getID());
        return node.getParent().responder;
    }

    public ArrayList<Responder> getSubordinates(Responder r) {
        Node node = map.get(r.getID());
        ArrayList<Responder> subs = new ArrayList<Responder>();
        for (Node child : node.getChildren()) {
            subs.add(child.responder);
        }
        return subs;
    }

    /**
     * A method to add an existing node to the tree. This node is assumed to already exist in the
     * HashMap and may already have children
     * @param node
     */
    private void addExistingNode(Node node) {
        Responder responder = node.responder;
        // if there are no responders present, the new responder becomes the root
        if (root == null) {
            root = node;
            return;
        }
        // if this responder's direct superior is present, the responder is automatically added as
        // the superior's direct subordinate
        if (map.containsKey(responder.getSuperior())) {
            Node parent = map.get(responder.getSuperior());
            addChildToParent(node, parent);
            return;
        }
        // if this responder outranks the current ranking responder, he becomes root
        if (node.rank > root.rank) {
            addChildToParent(root, node);
            root = node;
            return;
        }
        // if none of the above conditions are true, descend through the tree until a suitable
        // location for the new node is found
        Node top = root;
        boolean added = false;
        while (!added) {
            if (top.numChildren() > 0) {
                ArrayList<Node> children = top.getChildren();
                Node bestMatch = null; // the best child to place our new node underneath
                for (Node child : children) {
                    // for a child to be a superior to the new node, the child MUST outrank the new
                    // responder
                    if (child.rank > node.rank && bestMatch != null) {
                        // if there isn't a best match yet, the only criterion is that the child must
                        // outrank the new node
                        if (bestMatch == null) {
                            bestMatch = child;
                        }
                        // if another match has already been found, a shared responder branch trumps
                        // all other criteria
                        else if (bestMatch.branch != node.branch
                                && child.branch == node.branch) {
                            bestMatch = child;
                        }
                        // these criteria are used iff no match with the new node's branch has been
                        // found or this child also shares the new node's branch
                        else if (bestMatch.branch != node.branch || child.branch == node.branch) {
                            // a higher rank trumps a lower one
                            if (bestMatch.rank < child.rank) {
                                bestMatch = child;
                            }
                            // as a tiebreaker in the event that children share a branch and rank,
                            // count the total subordinates under each child and select the lesser
                            // value
                            else if (bestMatch.rank == child.rank
                                    && child.totalBeneath() < bestMatch.totalBeneath()) {
                                bestMatch = child;
                            }
                        }
                    }
                }
                // if there are no suitable children of top to insert the new node under, add it as
                // a child of top
                if (bestMatch == null) {
                    addChildToParent(node, top);
                }
                top = bestMatch;
            }
            // if top has no children, add the new responder as a child of top
            else {
                addChildToParent(node, top);
                added = true;
            }
        }
    }


    // TODO better logic for this
    private static int determineIntRank(String rank, Branch branch) {
        return Integer.parseInt(rank);
    }

    private static void addChildToParent(Node child, Node parent) {
        child.setParent(parent);
        parent.addChild(child);
    }

    private class Node {
        public final Responder responder;
        public final int rank;
        public final Branch branch;
        private Node parent;
        private ArrayList<Node> children;

        public Node(Responder responder, int rank, Branch branch) {
            this.responder = responder;
            this.rank = rank;
            this.branch = branch;
            children = new ArrayList<Node>();
            parent = null;
        }

        public void addChild(Node child) {
            children.add(child);
        }

        public ArrayList<Node> getChildren() {
            return children;
        }

        public int numChildren() {
            return children.size();
        }

        public int totalBeneath() {
            if (children.size() > 0) {
                int total = 0;
                for (Node child : children) {
                    total += child.totalBeneath();
                }
                return total;
            }
            else {
                return 0;
            }
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Node getParent() {
            return parent;
        }
    }
}
