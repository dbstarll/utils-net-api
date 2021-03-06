package io.github.dbstarll.utils.net.api;

import io.github.dbstarll.utils.http.client.request.AbsoluteUriResolver;
import io.github.dbstarll.utils.http.client.request.UriResolver;
import io.github.dbstarll.utils.http.client.response.BasicResponseHandlerFactory;
import io.github.dbstarll.utils.http.client.response.ResponseHandlerFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import static org.apache.commons.lang3.Validate.notNull;

public abstract class ApiClient {
    private final HttpClient httpClient;

    private ResponseHandlerFactory responseHandlerFactory = new BasicResponseHandlerFactory();
    private UriResolver uriResolver = new AbsoluteUriResolver();
    private Charset charset = Charset.forName("UTF-8");

    protected ApiClient(final HttpClient httpClient) {
        this.httpClient = notNull(httpClient, "httpClient is null");
    }

    protected final void setResponseHandlerFactory(final ResponseHandlerFactory responseHandlerFactory) {
        this.responseHandlerFactory = notNull(responseHandlerFactory, "responseHandlerFactory is null");
    }

    protected final void setUriResolver(final UriResolver uriResolver) {
        this.uriResolver = notNull(uriResolver, "uriResolver is null");
    }

    protected final void setCharset(final Charset charset) {
        this.charset = notNull(charset, "charset is null");
    }

    protected final RequestBuilder get(final URI uri) throws ApiException {
        return preProcessing(RequestBuilder.get(notNull(uri, "uri is null")));
    }

    protected final RequestBuilder get(final String path) throws ApiException {
        return get(uriResolver.resolve(path));
    }

    protected final RequestBuilder post(final URI uri) throws ApiException {
        return preProcessing(RequestBuilder.post(notNull(uri, "uri is null")));
    }

    protected final RequestBuilder post(final String path) throws ApiException {
        return post(uriResolver.resolve(path));
    }

    protected final RequestBuilder delete(final URI uri) throws ApiException {
        return preProcessing(RequestBuilder.delete(notNull(uri, "uri is null")));
    }

    protected final RequestBuilder delete(final String path) throws ApiException {
        return delete(uriResolver.resolve(path));
    }

    /**
     * ?????????????????????????????????????????????.
     *
     * @param builder RequestBuilder
     * @return RequestBuilder
     * @throws ApiException api????????????
     */
    protected RequestBuilder preProcessing(final RequestBuilder builder) throws ApiException {
        return builder.addHeader("Connection", "close").setCharset(charset);
    }

    /**
     * ???????????????????????????????????????????????????.
     *
     * @param executeResult ????????????
     * @param <T>           ??????????????????
     * @return ????????????
     * @throws ApiException api????????????
     */
    protected <T> T postProcessing(final T executeResult) throws ApiException {
        return executeResult;
    }

    protected final <T> T execute(final HttpUriRequest request, final ResponseHandler<T> responseHandler)
            throws IOException, ApiException {
        try {
            return postProcessing(
                    httpClient.execute(notNull(request, "request is null"),
                            notNull(responseHandler, "responseHandler is null")));
        } catch (HttpResponseException ex) {
            throw new ApiResponseException(ex);
        } catch (ClientProtocolException ex) {
            throw new ApiProtocolException(ex);
        } catch (NullPointerException ex) {
            throw new ApiParameterException(ex);
        } catch (IOException ex) {
            throw ex;
        } catch (ApiException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new ApiException(ex);
        }
    }

    protected final <T> T execute(final HttpUriRequest request, final Class<T> responseClass)
            throws IOException, ApiException {
        return execute(request, responseHandlerFactory.getResponseHandler(responseClass));
    }
}
