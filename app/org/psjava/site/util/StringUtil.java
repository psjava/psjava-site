package org.psjava.site.util;

import java.util.Scanner;

import org.psjava.ds.array.Array;
import org.psjava.ds.array.DynamicArray;

public class StringUtil {

	public static Array<String> toLines(String text) {
		DynamicArray<String> r = DynamicArray.create();
		Scanner scan = new Scanner(text);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			r.addToLast(line);
		}
		scan.close();
		return r;
	}


}
