import com.google.common.collect.HashMultimap;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.collections4.multiset.HashMultiSet;
import org.apache.commons.compress.utils.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class map_testing {

    void map_testing() {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            data.add(Integer.toString(i));
        }

        // pour the data into a linked list multimap
        {
            long start = System.currentTimeMillis();
            MultiMap<Integer, String> mmap = new MultiValueMap<>();

            for(int i = 0; i<data.size(); i++){
                mmap.put( i, data.get(i));
            }
            long end = System.currentTimeMillis();
            System.out.println("MultiKeyMap took " + (end - start) + " ms");
        }

        {
            // pour the data into a linked hash multimap
            long start = System.currentTimeMillis();
            HashMultimap<String, String> mmap = new HashMultimap<String, String>();
            for (String key : data) {
                mmap.put(key, key);
            }
            long end = System.currentTimeMillis();
            System.out.println("MultiMap took " + (end - start) + " ms");
        }

        {
            // pour the data into "hand made" multimap
            long start = System.currentTimeMillis();
            HashMap<String, ArrayList<String>> mmap =
                    new HashMultiSet<String, ArrayList<String>>();
            for (String key : data) {
                HashSet<String> set = Sets.newHashSet();
                set.add(key);
                mmap.put(key, set);
            }
            long end = System.currentTimeMillis();
            System.out.println("Manual multimap took " + (end - start) + " ms");
        }

    }
}
