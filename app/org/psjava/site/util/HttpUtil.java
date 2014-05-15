package org.psjava.site.util;

import java.util.Map;
import java.util.TreeMap;

import org.psjava.ds.array.AddToLastAll;
import org.psjava.ds.array.DynamicArray;
import org.psjava.ds.set.MutableSet;
import org.psjava.goods.GoodMutableSetFactory;

import play.cache.Cache;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.Response;
import play.libs.WS.WSRequestHolder;

public class HttpUtil {

	private static MutableSet<String> cachedSet = GoodMutableSetFactory.getInstance().create();

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
					synchronized (cachedSet) {
						cachedSet.insert(key);
					}
					return res;
				}
			});
		} else {
			return Promise.pure(cached);
		}
	}

	public static void clearCached() {
		DynamicArray<String> keys = DynamicArray.create();
		synchronized (cachedSet) {
			AddToLastAll.add(keys, cachedSet);
			cachedSet.clear();
		}
		for (String k : keys)
			Cache.remove(k);
	}

}
