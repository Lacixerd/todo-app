package todoapp.datastructure;

import java.util.ArrayList;
import java.util.List;

public class AVLTree<T extends Comparable<T>> {

    private class Node {
        T data;
        Node left, right;
        int height;

        Node(T data) {
            this.data = data;
            this.height = 1;
        }
    }

    private Node root;
    private int size;

    public AVLTree() {
        this.root = null;
        this.size = 0;
    }

    private int height(Node node) {
        return node == null ? 0 : node.height;
    }

    private int balanceFactor(Node node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private void updateHeight(Node node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private Node rotateRight(Node y) {
        Node x = y.left;
        Node t = x.right;
        x.right = y;
        y.left = t;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node t = y.left;
        y.left = x;
        x.right = t;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    private Node balance(Node node) {
        updateHeight(node);
        int bf = balanceFactor(node);

        if (bf > 1) {
            if (balanceFactor(node.left) < 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if (bf < -1) {
            if (balanceFactor(node.right) > 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    public void insert(T data) {
        root = insertRec(root, data);
        size++;
    }

    private Node insertRec(Node node, T data) {
        if (node == null) {
            return new Node(data);
        }
        int cmp = data.compareTo(node.data);
        if (cmp < 0) {
            node.left = insertRec(node.left, data);
        } else if (cmp > 0) {
            node.right = insertRec(node.right, data);
        } else {
            size--;
            return node;
        }
        return balance(node);
    }

    public void remove(T data) {
        root = removeRec(root, data);
    }

    private Node removeRec(Node node, T data) {
        if (node == null) return null;

        int cmp = data.compareTo(node.data);
        if (cmp < 0) {
            node.left = removeRec(node.left, data);
        } else if (cmp > 0) {
            node.right = removeRec(node.right, data);
        } else {
            size--;
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            Node minNode = findMin(node.right);
            node.data = minNode.data;
            node.right = removeRec(node.right, minNode.data);
        }
        return balance(node);
    }

    private Node findMin(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    public boolean search(T data) {
        return searchRec(root, data);
    }

    private boolean searchRec(Node node, T data) {
        if (node == null) return false;
        int cmp = data.compareTo(node.data);
        if (cmp == 0) return true;
        if (cmp < 0) return searchRec(node.left, data);
        return searchRec(node.right, data);
    }

    public List<T> inOrder() {
        List<T> result = new ArrayList<>();
        inOrderRec(root, result);
        return result;
    }

    private void inOrderRec(Node node, List<T> result) {
        if (node == null) return;
        inOrderRec(node.left, result);
        result.add(node.data);
        inOrderRec(node.right, result);
    }

    public List<T> prefixSearch(T prefix, java.util.function.BiFunction<T, T, Boolean> matcher) {
        List<T> result = new ArrayList<>();
        prefixSearchRec(root, prefix, matcher, result);
        return result;
    }

    private void prefixSearchRec(Node node, T prefix, java.util.function.BiFunction<T, T, Boolean> matcher, List<T> result) {
        if (node == null) return;
        prefixSearchRec(node.left, prefix, matcher, result);
        if (matcher.apply(node.data, prefix)) {
            result.add(node.data);
        }
        prefixSearchRec(node.right, prefix, matcher, result);
    }

    public void clear() {
        root = null;
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
