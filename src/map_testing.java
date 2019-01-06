import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

import com.google.common.collect.Multimap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.collections4.multiset.HashMultiSet;
import org.apache.commons.compress.utils.Sets;
import org.jetbrains.annotations.NotNull;


import java.util.*;
class map_testing {
    public static void main(String[] args) {

        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            data.add(Integer.toString(i)); }
        {
            long start = System.currentTimeMillis();
            MultiMap<Integer, String> mmap = new MultiValueMap<>();

            for (int i = 0; i < data.size(); i++) {
                mmap.put(i, data.get(i));
            }
            long end = System.currentTimeMillis();
            System.out.println("MultiValueMap took " + (end - start) + " ms");
        }

        {
            long start = System.currentTimeMillis();
            Multimap<String, String> mmap;
            mmap = HashMultimap.create();
            for (String key : data) {
                mmap.put(key, key);
            }
            long end = System.currentTimeMillis();
            System.out.println("HashMultiMap took " + (end - start) + " ms");
        }

        {
            long start = System.currentTimeMillis();
            Multimap<String, String> mmap;
            mmap = ArrayListMultimap.create();
            for (String key : data) {
                mmap.put(key, key);
            }
            long end = System.currentTimeMillis();
            System.out.println("ArrayListMultiMap took " + (end - start) + " " +
                    "ms");
        }

        {
            long start = System.currentTimeMillis();
            HashMap<String, String> mmap =
                    new HashMap<>();
            for (String key : data) {
                 mmap.put(key, key);
            }
            long end = System.currentTimeMillis();
            System.out.println("HashMap took " + (end - start) + " ms");
        }
    }
}
