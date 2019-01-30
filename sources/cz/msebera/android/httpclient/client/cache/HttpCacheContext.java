package cz.msebera.android.httpclient.client.cache;

import cz.msebera.android.httpclient.annotation.NotThreadSafe;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;

@NotThreadSafe
public class HttpCacheContext extends HttpClientContext {
    public static final String CACHE_RESPONSE_STATUS = "http.cache.response.status";

    public static HttpCacheContext adapt(HttpContext context) {
        if (context instanceof HttpCacheContext) {
            return (HttpCacheContext) context;
        }
        return new HttpCacheContext(context);
    }

    public static HttpCacheContext create() {
        return new HttpCacheContext(new BasicHttpContext());
    }

    public HttpCacheContext(HttpContext context) {
        super(context);
    }

    public CacheResponseStatus getCacheResponseStatus() {
        return (CacheResponseStatus) getAttribute(CACHE_RESPONSE_STATUS, CacheResponseStatus.class);
    }
}
