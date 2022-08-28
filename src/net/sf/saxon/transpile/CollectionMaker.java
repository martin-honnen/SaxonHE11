package net.sf.saxon.transpile;

import java.util.Collections;
import java.util.Iterator;

/**
 * Provides alternatives to methods such as {@link Collections#emptyList} and {@link Collections#emptyIterator}
 * designed to facilitate transpilation to C#
 */

public class CollectionMaker {

    /**
     * Create an empty iterator over items of a specified type
     * @param over the class of items (not) delivered by the iterator
     * @param <E> the class of items (not) delivered by the iterator
     * @return an empty iterator
     */

    public static <E> Iterator<E> emptyIterator(Class<E> over) {
        return Collections.emptyIterator();
    }
}

