package com.linute.linute.MainContent.Uploading;

/**
 * Created by QiFeng on 6/27/16.
 */


import com.linute.linute.API.API_Methods;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;

public class CountingRequestBody extends RequestBody {

    protected RequestBody delegate;
    protected Listener listener;

    private String json;

    public CountingRequestBody(String json, Listener listener) {
        //this.delegate = delegate;
        this.delegate = RequestBody.create(API_Methods.JSON, json);
        this.json = json;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        Buffer buffer = new Buffer().write(json.getBytes());
        long read;
        long total = 0;

        while (((read = buffer.read(sink.buffer(), 2048))) != -1){
            total += read;
            sink.flush();
            this.listener.onRequestProgress(total, contentLength());
        }
    }


    public interface Listener {
        void onRequestProgress(long bytesWritten, long contentLength);
    }
}