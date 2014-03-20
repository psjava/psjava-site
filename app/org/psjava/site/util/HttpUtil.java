package org.psjava.site.util;

import java.util.Map;
import java.util.TreeMap;

import play.cache.Cache;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.Response;
import play.libs.WS.WSRequestHolder;

public class HttpUtil {

	public static Promise<Response> createCacheableUrlFetchPromise(String getUrl, Map<String, String> getParams) {
		final String key = "http-cache-" + getUrl + ":" + new TreeMap<String, String>(getParams).toString();
		final Response cached = (Response) Cache.get(key);
		if (cached == null) {
			WSRequestHolder holder = WS.url(getUrl);
			for (String k : getParams.keySet())
				holder.setQueryParameter(k, getParams.get(k));
			Promise<Response> promise = holder.get();
			return promise.map(new Function<Response, Response>() {
				@Override
				public Response apply(Response res) throws Throwable {
					Cache.set(key, res);
					return res;
				}
			});
		} else {
			return Promise.promise(new Function0<Response>() {
				@Override
				public Response apply() throws Throwable {
					return cached;
				}
			});
		}
	}

}
