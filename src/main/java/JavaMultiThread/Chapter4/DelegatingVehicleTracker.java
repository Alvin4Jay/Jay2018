package JavaMultiThread.Chapter4;

import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于委托机制的线程安全类
 */
@ThreadSafe
public class DelegatingVehicleTracker {
    private final ConcurrentMap<String, Point> locations;
    private final Map<String, Point> unmodifiableMap;

    public DelegatingVehicleTracker(Map<String, Point> points){
        locations = new ConcurrentHashMap<>(points);
        unmodifiableMap = Collections.unmodifiableMap(locations);
    }

    public Map<String, Point> getLocations(){
        return unmodifiableMap;
    }

    public Point getLocation(String id){
        return locations.get(id);
    }

    public void setLocation(String id ,int x, int y){
        if(locations.replace(id, new Point(x, y)) == null){
            throw new IllegalArgumentException("invalid vehicle name: " + id);
        }
    }

}
