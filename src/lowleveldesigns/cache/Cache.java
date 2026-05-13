package lowleveldesigns.cache;

/*
classes
* */
/*
EvictionPolicy (interface)
LRU implements EvictionPolicy
LFU implements EvictionPolicy
Cache
* */

import java.util.HashMap;
import java.util.Map;

/*
classes
* */
/*
EvictionPolicy
    knows:
        nothing
    does:
        get(K) -> V
        put(K, V)
* */
interface EvictionPolicy<K, V> {
    V get(K key);
    void put(K key, V value);
}

/*
LRU
    knows:
        ListNode<K, V> (inner class)
        Map<K, ListNode>
        capacity
        head
        tail
        size
    does:
        get(K) -> V
        put(K, V)
* */
class LRU<K, V> implements EvictionPolicy<K, V> {
    private static class ListNode<K, V> {
        K key;
        V value;
        ListNode<K, V> prev;
        ListNode<K, V> next;

        public ListNode () {
        }

        public ListNode(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public ListNode(K key, V value, ListNode<K, V> prev, ListNode<K, V> next) {
            this.key = key;
            this.value = value;
            this.prev = prev;
            this.next = next;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public ListNode<K, V> getPrev() {
            return this.prev;
        }

        public ListNode<K, V> getNext() {
            return this.next;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public void setPrev(ListNode<K, V> prev) {
            this.prev = prev;
        }

        public void setNext(ListNode<K, V> next) {
            this.next = next;
        }
    }

    private Map<K, ListNode<K, V>> map;
    private int capacity;
    private ListNode<K, V> head;
    private ListNode<K, V> tail;
    private int size;

    public LRU(int capacity) {
        this.map = new HashMap<>();
        this.capacity = capacity;
        this.head = new ListNode<>();   // for most recently used items
        this.tail = new ListNode<>();   // for least recently used items
        this.size = 0;

        this.head.setNext(this.tail);
        this.tail.setPrev(this.head);
    }

    public void incrementSize() {
        this.size++;
    }

    public void decrementSize() {
        this.size--;
    }

    public int getSize() {
        return this.size;
    }

    public int getCapacity() {
        return this.capacity;
    }

    private void removeNode(ListNode<K, V> node) {
        ListNode<K, V> prevNode = node.getPrev();
        ListNode<K, V> nextNode = node.getNext();

        prevNode.setNext(nextNode);
        nextNode.setPrev(prevNode);
    }

    private void addNodeToHead(ListNode<K, V> node) {
        ListNode<K, V> currMostRecent = head.getNext();

        head.setNext(node);
        currMostRecent.setPrev(node);

        node.setPrev(head);
        node.setNext(currMostRecent);
    }

    @Override
    public V get(K key) {
        if(!map.containsKey(key)) {
            return null;
        }

        // 1. Get the node
        // 2. Remove it from its position in the linked list
        // 3. Add the node next to the head
        // 4. Return the node's value at the end
        ListNode<K, V> node = map.get(key);
        removeNode(node);
        addNodeToHead(node);

        return node.getValue();
    }

    @Override
    public void put(K key, V value) {
        // case 1: If the node already exists
        // case 2: If the node does NOT exist
        if(map.containsKey(key)) {
            // 1. Get the node
            // 2. Remove it from its positon in the linked list
            // 3. Add the node next to the head
            // 4. Change its value to the new value
            ListNode<K, V> node = map.get(key);
            removeNode(node);
            addNodeToHead(node);
            node.setValue(value);
        }
        else {
            // 1. Create a new node
            // 2. Add it to the cache
            // 3. Increment size
            // 4. Add it next to the head
            // 5. Check the size of the cache
            // 6. If size exceeds cache capacity,
            //  a. Get the tail node
            //  b. Remove it from the cache and the linked list
            //  c. Decrement size
            ListNode<K, V> newNode = new ListNode<>(key, value);
            map.put(key, newNode);
            incrementSize();
            addNodeToHead(newNode);

            if(getSize() > getCapacity()) {
                ListNode<K, V> nodeToRemove = this.tail.getPrev();
                map.remove(nodeToRemove.getKey());
                removeNode(nodeToRemove);
                decrementSize();
            }

        }
    }
}

/*
LFU
    knows:
        ListNode<K, V> (inner class)
        Map<freq, ListNode>
        Map<int, LinkedList<ListNode>>
        minFreq
        size
    does:
        get(K) -> V
        put(K, V)
* */


/*
Cache
    knows:
        EvictionPolicy
    does:
        get(K) -> V
        put(K, V)
* */
public class Cache<K, V> {
    private EvictionPolicy<K, V> evictionPolicy;

    public Cache(EvictionPolicy<K, V> evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public V get(K key) {
        return this.evictionPolicy.get(key);
    }

    public void put(K key, V value) {
        this.evictionPolicy.put(key, value);
    }
}

/*

LRU has-a ListNode

LFU has-a ListNode

Cache has-a EvictionPolicy

* */
