package utils.containers;

public class Hexadecimal <K, V, M, L, N, P> implements Container{
    public K first;
    public V second;
    public M third;
    public L fourth;
    public N fifth;
    public P sixth;

    public Hexadecimal(K first, V second, M third, L fourth, N fifth, P sixth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
        this.sixth = sixth;
    }

    @Override
    public <T> T getType() {
        return null;
    }
}
