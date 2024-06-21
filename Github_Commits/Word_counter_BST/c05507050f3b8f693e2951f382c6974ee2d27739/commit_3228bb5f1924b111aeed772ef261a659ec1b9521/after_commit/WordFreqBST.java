import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Objects;
import java.util.Scanner;

public class WordFreqBST implements WordCounter {
    public static class WordFreqTreeNode implements Comparator<WordFreq> {
        WordFreq wordFreqObj;
        WordFreqTreeNode left, right, parent;
        int subtreeSize;
        WordFreqTreeNode(WordFreq t) {
            wordFreqObj = new WordFreq(t);
        }
        WordFreqTreeNode(String item) {
            this.wordFreqObj = new WordFreq(item);
        }

        public WordFreq getWordFreqObj() {
            return wordFreqObj;
        }

        public WordFreqTreeNode getLeft() {
            return left;
        }

        public WordFreqTreeNode getRight() {
            return right;
        }

        public WordFreqTreeNode getParent() {return parent;}

        public int getSubtreeSize() {
            return subtreeSize;
        }

        public void subtreeIncrease() {
            WordFreqTreeNode current = this;
            while (current.parent != null) {
                current = current.parent;
                current.subtreeSize += 1;
            }
        }
        public void setItem(WordFreq item) {
            this.wordFreqObj = item;
        }

        public void setLeft(WordFreqTreeNode left) {
            this.left = left;
        }

        public void setRight(WordFreqTreeNode right) {
            this.right = right;
        }

        public void setParent(WordFreqTreeNode parent) {
            this.parent = parent;
        }

        public void setSubtreeSize(int subtreeSize) {
            this.subtreeSize = subtreeSize;
        }

        /** @return 0 means same value, 1 means bigger, 2 means same object, -1 means smaller */
        public String toString() {
            return wordFreqObj.toString();
        }
        @Override
        public int compare(WordFreq o1, WordFreq o2) {
            return o1.compareToIgnoreCaseWithoutDiacritics(o2);
        }
        /** Ignores case and diacritics */
        public int compareTo(WordFreqTreeNode newWordNode) {
            return this.getWordFreqObj().compareToIgnoreCaseWithoutDiacritics(newWordNode.getWordFreqObj());
        }
    }
    private static WordFreqTreeNode head;
    private static MyLinkedList<String> stopWords;
    private static MyLinkedList<WordFreq> wordFreqMyLinkedList;
    private static int totalWords = 0;

    public static void main(String[] args) {
        WordFreqBST a = new WordFreqBST();
        a.addStopWord("να", "και", "ειναι", "με", "το", "τα", "της", "την", "του", "τον", "δεν", "που",
                        "η", "ο", "στο", "στην", "στον", "σε", "θα", "από", "οι", "των", "τη", "τις", "of");
        a.load("D:\\Projects\\domes-dedomenon-2021\\3rd-assignment\\text1.txt");

        traverseR5(head);
        a.printTreeAlphabetically(System.out);
        //System.out.println(WordFreq.rootWords.toString());
        //a.printTreeByFrequency(System.out);
    }


    @Override
    public void load(String filename) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want to count words ending with a different postfix but same root as different words? Answer with yes or no");
        boolean sameOrigin = scanner.nextLine().equals("yes");
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line, la[];
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("[^α-ωά-ώΑ-ΩΆ-Ώa-zA-ZäöüÄÖÜß\\s']", "").replace("\t", " ").toLowerCase();
                la = line.split(" ");
                for (String s : la)
                    if (!(s.isBlank() || (s.startsWith("'") || s.endsWith("'"))) && !stopWords.contains(s))
                        insert(s,sameOrigin);
            }
        } catch (IOException ex) {
            System.out.println("file name is: " + filename); ex.printStackTrace();
        }
    }

    @Override
    public void insert(String string, boolean origin) {
        WordFreqTreeNode newWordNode = new WordFreqTreeNode(string);
        String newRoot = newWordNode.getWordFreqObj().getRoot();

        if (head == null) {
            head = newWordNode;
            if (newRoot != null)
                WordFreq.rootWords.insertAtBack(newRoot);
            return ;
        }

        WordFreqTreeNode nodeIter = head;
        while (true) {
            boolean equalsIgnorePostfix = false;
            if (!origin && nodeIter.getWordFreqObj().getType() != null && newWordNode.getWordFreqObj().getType() != null) {
                String IterRootSubstring, newRootSubstring;
                boolean condition = false;
//                try {
//                    String nodeIterRoot = nodeIter.getWordFreqObj().getRoot();
//                    IterRootSubstring = nodeIterRoot.substring(0, nodeIterRoot.length() - 2);
//                    newRootSubstring = newRoot.substring(0, newRoot.length() - 2);
//                    condition = (IterRootSubstring.equals(newRootSubstring) && Math.abs(nodeIter.getWordFreqObj().getRoot().length() - newRoot.length()) <= 1 && nodeIter.getWordFreqObj().getRoot().length() > 3);
//                    System.out.println(" IterRootSubstring equals newRootSubstring?   " + nodeIter.getWordFreqObj().getWord() + " " + string + " condition is: " + condition);
//                } catch (RuntimeException e) { }
                if (WordFreq.rootWords.containsString(newRoot) && (nodeIter.getWordFreqObj().getWord().equals(string) || (Objects.equals(nodeIter.getWordFreqObj().getRoot(), newRoot) && string.length() > 3) || condition )
                        && nodeIter.getWordFreqObj().containsType(newWordNode.getWordFreqObj().getType())) { //nouns and adjectives are being mixed
                            equalsIgnorePostfix = true;  // the root exist, so we need to increase the frequency
                }
            }

            if (nodeIter.compareTo(newWordNode) == 0 || equalsIgnorePostfix) {
                nodeIter.getWordFreqObj().increaseFrequency();
                ++totalWords;
                // if (!nodeIter.getWordFreqObj().getWord().equals(string)) //todo next patch: check for wrong equivalents
                    //System.out.println(nodeIter.getWordFreqObj().getWord() + "  " + string);
                return ;
            } else {
                WordFreqTreeNode childNode = (nodeIter.compareTo(newWordNode) < 0) ? nodeIter.getRight() : nodeIter.getLeft();
                if (childNode == null) {
                    ++totalWords;
                    if (nodeIter.compareTo(newWordNode) < 0) {
                        nodeIter.setRight(newWordNode);
                    } else {
                        nodeIter.setLeft(newWordNode);
                    }
                    newWordNode.setParent(nodeIter);
                    newWordNode.subtreeIncrease();
                    if (newRoot != null)
                        WordFreq.rootWords.insertAtBack(newRoot);   //if the stringRoot has been found but does not exist it must be added to the list
                    return;
                } else {
                    nodeIter = childNode;
                }
            }
        }
    }

    @Override
    public void printTreeAlphabetically(PrintStream stream) {
        wordFreqMyLinkedList.sort(new Alphabetically());
        stream.println(wordFreqMyLinkedList.toString());
    }

    @Override
    public void printTreeByFrequency(PrintStream stream) {
        wordFreqMyLinkedList.sort(new WordFreq());
        stream.println(wordFreqMyLinkedList.toString());
    }

    static void traverseR5(WordFreqTreeNode n) {
        if (n == null)
            return ;
        if (wordFreqMyLinkedList == null) {
            wordFreqMyLinkedList = new MyLinkedList<>();
        }
        wordFreqMyLinkedList.insertAtFront(n.getWordFreqObj());
        traverseR5(n.getRight());
        traverseR5(n.getLeft());
    }

    public void addStopWord(String... words){
        if (stopWords == null)
            stopWords = new MyLinkedList<>();
        for (String word : words){
            stopWords.insertAtBack(word);
        }
    }

    @Override
    public WordFreq search(String item) {
        WordFreqTreeNode current = head, temp = new WordFreqTreeNode(item);

        while (true) {
            if (current == null) return null;
            else if (current.compareTo(temp) == 0){

                if (current.getWordFreqObj().getFrequency() > getMeanFrequency()) {
                    remove(current.getWordFreqObj().key());
                    rootInsert(current.getWordFreqObj());
                }
                return current.getWordFreqObj();
            }
            else if (current.compareTo(temp) < 0)
                current = current.getRight();
            else
                current = current.getLeft();
        }
    }
    public void rootInsert(WordFreq element){
        head = rootInsert(head, element, null);
    }
    private WordFreqTreeNode rootInsert(WordFreqTreeNode head, WordFreq element, WordFreqTreeNode parent) {

        if (head == null) {
            //the WordFrequencyBST specified by root is empty (it has 0 elements) -
            //initialize node with element and insert it at the WordFrequencyBST
            //(do not make any rotations)
            WordFreqTreeNode node = new WordFreqTreeNode(element);
            node.setParent(parent);
            node.subtreeIncrease();
            return node;
        }

        //WordFrequencyBST not empty
        //find subtree where we're going to insert element
        int result = element.key().compareToIgnoreCase(head.getWordFreqObj().key());

        if (result == 0) {
            //element equal to root
            //do not insert element in the WordFrequencyBST
            return head;
        }

        if (result < 0) {
            //element smaller than root
            //(1) insert element at the left subtree of root (recursively)
            WordFreqTreeNode leftSubtreeRoot = this.rootInsert(head.getLeft(), element, head);
            //(2) update root's left subtree
            head.setLeft(leftSubtreeRoot);
            //(3) perform a rotation at the opposite (right)
            head = this.rotateRight(head);

        }
        else {
            //element bigger than root
            //(1) insert element at the right subtree of root (recursively)
            WordFreqTreeNode rightSubtreeRoot = this.rootInsert(head.getRight(), element, head);
            //(2) update root's right subtree
            head.setRight(rightSubtreeRoot);
            //(3) perform a rotation at the opposite (left)
            head = this.rotateLeft(head);
        }

        //after each rotation, return the updated WordFrequencyBST
        return head;
    }

    public WordFreqTreeNode rotateLeft(WordFreqTreeNode pivot) {
        WordFreqTreeNode parent = pivot.getParent();
        WordFreqTreeNode child = pivot.getRight();

        //update pivot's parent's child with pivot's right child
        if (parent == null) head = child;

        else if (parent.getLeft() == pivot) {
            parent.setLeft(child);
        }
        else parent.setRight(child);

        child.setParent(pivot.getParent());
        //update pivot's parent with child
        pivot.setParent(child);
        //during update, child has 3 children (1 right, 1 initial left + 1 left (pivot))
        //WordFrequencyBST spec violation
        //pivot takes child's initial left child as its right child
        pivot.setRight(child.getLeft());
        //if child's left child exists, update it with its new parent (pivot)
        if (child.getLeft() != null) {
            child.getLeft().setParent(pivot);
        }
        //update child's new left child (pivot)
        child.setLeft(pivot);
        //return the new WordFrequencyBST root after rotation
        return child;
    }

    private WordFreqTreeNode rotateRight(WordFreqTreeNode pivot) {

        WordFreqTreeNode parent = pivot.getParent();
        WordFreqTreeNode child = pivot.getLeft();

        if (parent == null) {
            head = child;
        }
        else if (parent.getLeft() == pivot) {
            parent.setLeft(child);
        }
        else {
            parent.setRight(child);
        }

        child.setParent(pivot.getParent());
        pivot.setParent(child);
        pivot.setLeft(child.getRight());
        if (child.getRight() != null) { child.getRight().setParent(pivot);
        }
        child.setRight(pivot);
        return child;
    }


    @Override
    public void remove(String w) {
        // find node to delete and its parent
        WordFreqTreeNode current = head, parent = null, deleteItem = new WordFreqTreeNode(w);

        while (true) {
            if (current == null)
                return;

            if (current.compareTo(deleteItem) == 0)
                break;

            parent = current;

            if (current.compareTo(deleteItem) < 0)
                current = current.getRight();
            else
                current = current.getLeft();
        }

        WordFreqTreeNode replace;

        if (current.getLeft() == null)
            replace = current.getRight();
        else if (current.getRight() == null)
            replace = current.getLeft();
        else {
            WordFreqTreeNode findCurrent = current.getRight();

            while (true) {
                if (findCurrent.getLeft() != null)
                    findCurrent = findCurrent.getLeft();
                else
                    break;
            }

            // only has zero or one child (there is no left child!!!)
            remove(findCurrent.getWordFreqObj().key());

            findCurrent.setLeft(current.getLeft());
            findCurrent.setRight(current.getRight());

            replace = findCurrent;
        }

        if (parent == null) {
            head = replace;
        } else {
            if (parent.getLeft() == current)
                parent.setLeft(replace);

            if (parent.getRight() == current)
                parent.setRight(replace);
        }
    }


    @Override
    public int getTotalWords() {
        return traverseR(head);
    }   //todo change?

    private int traverseR(WordFreqTreeNode h) {
        if (h == null || stopWords.contains(h.getWordFreqObj().key()))
            return 0;
        return h.getWordFreqObj().getFrequency()
                + traverseR(h.getLeft()) + traverseR(h.getRight());
    }


    @Override
    public int getDistinctWords() {
        return head.subtreeSize + 1;
    }

    /** returns 0 if it wasn't found
     * WordFreqNode need head as an argument at the first call */
    @Override
    public int getFrequency(String w, WordFreqTreeNode h) {
        if (h == null)
            return 0;
        else if (h.getWordFreqObj().key().equals(w))
            return h.getWordFreqObj().getFrequency();
        return getFrequency(w, h.getRight()) + getFrequency(w, h.getLeft());
    }


    @Override
    public WordFreq getMaximumFrequency() {
        return search(TraverseR3(head));
    }
    int max = -1;
    String TraverseR3(WordFreqTreeNode h) {
        if (h == null)
            return null;
        else if (h.getWordFreqObj().getFrequency() > max)
            max = h.getWordFreqObj().getFrequency();
        return TraverseR3(h.getRight()) + TraverseR3(h.getLeft());
    }

    @Override
    public double getMeanFrequency() {
        return TraverseR4(head);
    }

    double sum = 0.0;
    double TraverseR4(WordFreqTreeNode h) {
        if (h == null)
            return 0.0;
        sum += h.getWordFreqObj().getFrequency();
        TraverseR4(h.getLeft()) ;
        TraverseR4(h.getRight());
        return sum/getDistinctWords();
    }

    @Override
    public void removeStopWord(String w) {
        if (stopWords.remove(w) == -1)
            System.out.println("Stopword does not exist");
    }

    public void printPreorder(){
        preorder(head);
        System.out.println();
    }
    public void preorder(WordFreqTreeNode n) {
        if (n == null)
            return ;
        if(!stopWords.contains(n.getWordFreqObj().key()))
            System.out.println(n.getWordFreqObj().key() + " ");
        preorder(n.getRight());
        preorder(n.getLeft());
    }

    public static MyLinkedList<String> getStopWords() {
        return stopWords;
    }
    public static MyLinkedList<WordFreq> getWordFreqList() {
        return wordFreqMyLinkedList;
    }
    public static int getTotalWordsWritten() { return totalWords; }
}
