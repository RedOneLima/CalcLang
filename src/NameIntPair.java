/**
 * Created by Surface Pro 3 on 4/4/2017.
 */
public class NameIntPair {
    public String name;
    public int number;

    public NameIntPair( String s, int num ) {
        name = s;
        number = num;
    }

    public String toString() {
        return name + " " + number;
    }
}
