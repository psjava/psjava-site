package org.psjava.site;

import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class PsjavaSiteController extends Controller {

	public static Promise<Result> index() {
		return Promise.promise(new Function0<Result>() {
			@Override
			public Result apply() throws Throwable {
				return ok(index.render());
			}
		});
	}

}
