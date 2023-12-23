package utils.containers;

public class Triad <K, V, M> implements Container{
    public K first;
    public V second;
    public M third;

    public Triad(K k, V v, M m) {
        first = k;
        second = v;
        third = m;
    }

    @Override
    public <T> T getType() {
        return (T) this;
    }
}
