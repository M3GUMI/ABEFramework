package utils.containers;

public class Tuple <K, V> implements Container{
    public K first;
    public V second;

    public Tuple(K k, V v) {
        first = k;
        second = v;
    }

    @Override
    public <T> T getType() {
        return (T) this;
    }
}

