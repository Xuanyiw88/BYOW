package byow.Core;

public class Pair<K, V> {

    private K key;
    private V value;

    public Pair(K k, V v) {
        key = k;
        value = v;
    }

    public K getFirst() {
        return key;
    }

    public V getSecond() {
        return value;
    }
}
