package org.psjava.site;

import org.junit.Assert;
import org.junit.Test;

public class PsjavaSiteControllerTest {
	@Test
	public void test() {
		Assert.assertEquals("Hi I Am Dora", PsjavaSiteController.getCamelResolved("HiIAmDora"));
	}
}
