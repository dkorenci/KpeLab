package hr.irb.zel.kpelab.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cache for mapping of objects of type K to objects of type V.
 * When adding (key,value) to the map, if the cache capacity is reached, 
 * least recently added entry will be removed from the map.
 */
public class MapCache<K extends Comparable<K>, V> implements Serializable {
    
    private static final long serialVersionUID = -2004084683993684623L;

    private Map<K, Integer> keyToIndex; // maps key to index of value
    private List<V> values; // list of all values in the cache
    private List<K> keys; // list of all keyes in cache
    
    private int mostRecentKey; // index of the most recently added key    
    private int capacity;
    
    public MapCache(int cap) { 
        mostRecentKey = -1;
        keyToIndex = new TreeMap<K, Integer>();
        values = new ArrayList<V>();
        keys = new ArrayList<K>();           
        capacity = cap; 
    }
    
    public V get(K key) {         
        if (keyToIndex.containsKey(key)) {
            return values.get(keyToIndex.get(key));
        }
        else return null;        
    };
    
    public V put(K key, V value) {
        int valueIndex;                
        if (containsKey(key)) { // word is in the map, replace vector
            valueIndex = keyToIndex.get(key);
            V oldKey = values.get(valueIndex);
            values.set(valueIndex, value);
            return oldKey;
        }        
        if (keys.size() == capacity) { // add replacing least recently added key           
            // get last key, ie key after most recently added 
            int leastRecentKey = mostRecentKey+1; 
            if (leastRecentKey == capacity) leastRecentKey = 0;   
            // remove last key
            K removed = keys.get(leastRecentKey);
            valueIndex = keyToIndex.get(removed);            
            keyToIndex.remove(removed);
            // add new key in place of last key
            keys.set(leastRecentKey, key);
            values.set(valueIndex, value);
            keyToIndex.put(key, valueIndex);
            // update most recent key
            mostRecentKey = leastRecentKey;
            return null;
        }        
        else { // add key & value at the end of the array           
            mostRecentKey++; 
            keys.add(key);
            values.add(value);
            keyToIndex.put(key, mostRecentKey);
            return null;
        }        
    };
    
    public boolean containsKey(K key) {
        return keyToIndex.containsKey(key);
    }
    
}
