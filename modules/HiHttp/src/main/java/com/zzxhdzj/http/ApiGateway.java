package com.zzxhdzj.http;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: yangning.roy
 * Date: 10/27/13
 * Time: 6:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ApiGateway <T extends ApiResponse>{

    private static final int CUP_CORE_NUM = 1;
    private static final int DEFAULT_MAX_CONNECTIONS = 5*CUP_CORE_NUM;
    private final Http http = new Http();

    private ExecutorService threadPool;

    public ApiGateway() {
        threadPool = Executors.newFixedThreadPool(DEFAULT_MAX_CONNECTIONS);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void makeRequest(ApiRequest<T> apiRequest, final ApiResponseCallbacks<T> responseCallbacks) {
        responseCallbacks.onStart();
        new RemoteCallTask(responseCallbacks).execute(apiRequest);
    }

    protected void dispatch(T apiResponse, ApiResponseCallbacks<T> responseCallbacks) {
        if (apiResponse.isSuccess()) {
            try {
                responseCallbacks.onSuccess(apiResponse);
            } catch (Exception e) {
                Log.e(ApiGateway.class.getName(), "Error processing response", e);
                responseCallbacks.onProcessFailure(apiResponse);
            }
        } else {
            responseCallbacks.onRequestFailure(apiResponse);
        }
        responseCallbacks.onComplete();
    }

    private void closeStream(InputStream responseBody) {
        if (responseBody != null) {
            try {
                responseBody.close();
            } catch (IOException ignored) {
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private class RemoteCallTask{
        private final ApiResponseCallbacks<T> responseCallbacks;

        public RemoteCallTask(ApiResponseCallbacks<T> responseCallbacks) {
            this.responseCallbacks = responseCallbacks;
        }

        public void execute(final ApiRequest<T> apiRequest) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    InputStream responseBody = null;
                    Http.Response response;
                    T apiResponse;
                    try {
                        try {
                            Log.d("ApiGateway", "req info [url=" + apiRequest.getUrlString() + " ]");
                            if (HttpPost.METHOD_NAME.equals(apiRequest.getMethod())) {
                                response = http.post(apiRequest.getUrlString(), apiRequest.getHeaders(), apiRequest.getPostEntity(), apiRequest.allowRedirect);
                            } else if (HttpGet.METHOD_NAME.equals(apiRequest.getMethod())) {
                                response = http.get(apiRequest.getUrlString(), apiRequest.getHeaders(), apiRequest.allowRedirect);
                            } else {
                                throw new RuntimeException("Unsupported Http Method!");
                            }
                        } catch (Exception e) {
                            Log.e("ApiGateway", "request failed\n" + e.getStackTrace().toString());
                            apiResponse = apiRequest.createResponse(WrappedHttpError.REQUEST_ERROR.getCode(), null);
                            dispatch(apiResponse, responseCallbacks);
                            return;
                        }

                        try {
                            responseBody = response.getResponseBody();
                            apiResponse = apiRequest.createResponse(response.getStatusCode(), response.getHeaderFields());
                            Log.d("ApiGateway", "resp info [response code = " + apiResponse.getHttpResponseCode() + "]\n");
                            apiResponse.consumeResponse(responseBody);
                        } catch (Exception e) {
                            Log.d("ApiGateway", "resp process failed\n");
                            apiResponse = apiRequest.createResponse(WrappedHttpError.CONSUME_ERROR.getCode(), (response == null) ? null : response.getHeaderFields());
                        }
                        dispatch(apiResponse, responseCallbacks);
                    } finally {
                        closeStream(responseBody);
                    }
                }
            };
            threadPool.submit(runnable);
        }
    }
}
