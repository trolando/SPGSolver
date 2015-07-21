package com.JPGSolver;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;

/**
 * Created by umberto1 on 21/07/15.
 */
public class MyTrove {

    public static boolean addAllEx(TIntCollection A, TIntCollection collection){
        boolean changed = false;
        TIntIterator iter = collection.iterator();
        while ( iter.hasNext() ) {
            int element = iter.next();
            if ( !A.contains( element )) {
                if (A.add(element)) {
                    changed = true;
                }
            }
        }
        return changed;
    }

}
