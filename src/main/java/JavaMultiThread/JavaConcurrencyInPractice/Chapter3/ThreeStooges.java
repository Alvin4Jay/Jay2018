package JavaMultiThread.JavaConcurrencyInPractice.Chapter3;

import net.jcip.annotations.Immutable;

import java.util.HashSet;
import java.util.Set;

/**
 *  不可变对象
 */
@Immutable
public class ThreeStooges {
    private final Set<String> stooges = new HashSet<String>();

    public ThreeStooges(){
        stooges.add("Moe");
        stooges.add("Larry");
        stooges.add("Curly");
    }

    public boolean isStooge(String name){
        return stooges.contains(name);
    }

}
