import org.junit.*;

import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class IntegrationTest {

	@Test
	public void test() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
			public void invoke(TestBrowser browser) {
				browser.goTo("http://localhost:3333/spoj/PRIME1/prime-generator");
				assertThat(browser.pageSource()).contains("Prime Generator");
			}
		});
	}

}
