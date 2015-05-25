package at.yawk.yarn.compiler.tree;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
class DependencySorter<T> {
    private final Node[] nodeArray;
    private Node lowest;

    @SuppressWarnings("unchecked")
    private DependencySorter(Set<T> input,
                             Function<T, Stream<T>> hardDependencies,
                             Function<T, Stream<T>> softDependencies) {
        this.nodeArray = new DependencySorter.Node[input.size()];

        // build nodes

        Map<T, Node> nodes = new HashMap<>();
        for (T t : input) {
            Node node = new Node(t);
            nodes.put(t, node);
        }
        int i = 0;
        for (Node n : nodes.values()) {
            n.hardDependencies = hardDependencies.apply(n.value)
                    .map(nodes::get).collect(Collectors.toList());
            n.dependencies = Stream.concat(
                    hardDependencies.apply(n.value),
                    softDependencies.apply(n.value)
            ).map(nodes::get).collect(Collectors.toList());

            for (Node dependency : n.dependencies) {
                dependency.dependents.add(n);
            }
            n.index = i;
            nodeArray[i++] = n;
        }
        lowest = nodeArray[0];
    }

    public static <T> List<T> sort(Set<T> input,
                                   Function<T, Stream<T>> hardDependencies,
                                   Function<T, Stream<T>> softDependencies) {
        DependencySorter<T> sorter = new DependencySorter<>(input, hardDependencies, softDependencies);
        sorter.passHard();
        sorter.passSoft();
        return sorter.getSorted();
    }

    // HARD PASS

    private void passHard() {
        Node end = nodeArray[0];
        Node current = nodeArray[nodeArray.length - 1];
        while (true) {
            Node next = nodeArray[trimIndex(current.index - 1)];
            Node highestHardDependency = highest(current.hardDependencies);
            if (highestHardDependency != null && highestHardDependency.value() > current.value()) {
                current.insertAfter(highestHardDependency);
            }
            if (current == end) {
                break;
            }
            current = next;
        }
    }

    // SOFT PASS

    private void passSoft() {
        Node end = nodeArray[trimIndex(lowest.index - 1)];
        Node current = lowest;
        while (true) {
            Node next = nodeArray[trimIndex(current.index + 1)];
            Node highestDependency = highest(current.dependencies);
            if (highestDependency == null) {
                current.pushToFront();
            } else if (highestDependency.value() < current.value()) {
                current.insertAfter(highestDependency);
            }
            if (current == end) {
                break;
            }
            current = next;
        }
    }

    // UTIL

    private Node highest(List<Node> nodes) {
        if (nodes.isEmpty()) { return null; }
        int i = nodes.size() - 1;
        Node best = nodes.get(i);
        while (i-- != 0) {
            Node here = nodes.get(i);
            if (here.value() > best.value()) { best = here; }
        }
        return best;
    }

    /**
     * Trim the given index a valid array index. Must be within +- nodeArray.length of accept range.
     */
    private int trimIndex(int i) {
        // only support one shift
        return (i + nodeArray.length) % nodeArray.length;
    }

    @RequiredArgsConstructor
    private final class Node {
        final T value;

        List<Node> hardDependencies;
        List<Node> dependencies;
        List<Node> dependents = new ArrayList<>(0);

        /**
         * Index in the array.
         */
        int index;

        boolean adding = false;
        boolean added = false;

        void pushToFront() {
            insertAfter(nodeArray[trimIndex(lowest.index - 1)]);
            lowest = this;
        }

        void placeAt(int index) {
            this.index = index;
            nodeArray[this.index] = this;
        }

        void insertAfter(Node node) {
            if (lowest == this) {
                lowest = nodeArray[trimIndex(index + 1)];
            }

            // distance to that item to the left.
            // always in [0; len[
            int n = trimIndex(this.index - node.index);
            if (n <= 1) { return; } // no change
            if (n > nodeArray.length / 2) {
                // distance to that item to the right
                int m = trimIndex(node.index - this.index);
                for (int i = this.index; i < this.index + m; i++) {
                    Node here = nodeArray[(i + 1) % nodeArray.length];
                    here.placeAt(i % nodeArray.length);
                }
            } else {
                for (int i = node.index + n - 1; i > node.index; i--) {
                    Node here = nodeArray[i % nodeArray.length];
                    here.placeAt((i + 1) % nodeArray.length);
                }
            }
            placeAt((node.index + 1) % nodeArray.length);
        }

        int value() {
            return trimIndex(index - lowest.index);
        }

        @Override
        public String toString() {
            return value() + ":" + value;
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> getSorted() {
        List<T> sorted = new ArrayList<>(nodeArray.length);
        for (int i = 0; i < nodeArray.length; i++) {
            Node node = nodeArray[(i + lowest.index) % nodeArray.length];
            sorted.add(node.value);
        }
        return sorted;
    }
}
