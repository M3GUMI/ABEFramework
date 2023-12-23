package utils.containers;

public class Quaternion<K, V, M, L> implements Container{
    public K first;
    public V second;
    public M third;
    public L fourth;

    public Quaternion(K k, V v, M m, L l) {
        first = k;
        second = v;
        third = m;
        fourth = l;
    }

    @Override
    public <T> T getType() {
        return (T) this;
    }
}
