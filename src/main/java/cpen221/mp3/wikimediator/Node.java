package cpen221.mp3.wikimediator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node<String> { //Source: https://www.baeldung.com/java-breadth-first-search
        private String value;
        public Set<Node<String>> neighbors;
        public Node<String> parent;

        public Node(String value) {
            this.value = value;
            this.neighbors = new HashSet<>();
        }


        public String getValue() {
            return value;
        }

        public Node<String> getParent() {
            return parent;
        }

}
