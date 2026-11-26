package com.harsh.pdfstudio.utils;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    public interface ProgressListener {
        void onProgress(long bytesWritten, long totalBytes);
    }

    private final RequestBody requestBody;
    private final ProgressListener listener;

    public ProgressRequestBody(
            RequestBody requestBody,
            ProgressListener listener
    ) {
        this.requestBody = requestBody;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {

        ProgressSink progressSink =
                new ProgressSink(sink, contentLength(), listener);

        BufferedSink bufferedSink = okio.Okio.buffer(progressSink);

        requestBody.writeTo(bufferedSink);

        bufferedSink.flush();
    }

    private static class ProgressSink extends okio.ForwardingSink {

        private final long totalBytes;
        private final ProgressListener listener;

        private long bytesWritten = 0;

        ProgressSink(
                okio.Sink delegate,
                long totalBytes,
                ProgressListener listener
        ) {
            super(delegate);
            this.totalBytes = totalBytes;
            this.listener = listener;
        }

        @Override
        public void write(
                okio.Buffer source,
                long byteCount
        ) throws IOException {

            super.write(source, byteCount);

            bytesWritten += byteCount;

            if (listener != null) {
                listener.onProgress(bytesWritten, totalBytes);
            }
        }
    }
}