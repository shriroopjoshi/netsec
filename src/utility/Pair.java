package utility;

/**
 *
 * @author shriroop
 * @param <U>
 * @param <V>
 */
public class Pair<U, V> {
    private U first;
    private V second;

    public U getFirst() {
        return first;
    }

    public void setFirst(U first) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond(V second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair{" + "first=" + first + ", second=" + second + '}';
    }
}
