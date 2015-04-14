package org.psjava.site;

import java.util.ArrayList;
import java.util.List;

import org.psjava.ds.array.Array;

public class Util {

	public static <T> List<T> toList(Array<T> a) {
		ArrayList<T> r = new ArrayList<T>();
		for (T s : a)
			r.add(s);
		return r;
	}

}
