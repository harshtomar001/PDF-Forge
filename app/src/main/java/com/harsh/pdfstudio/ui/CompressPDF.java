package com.harsh.pdfstudio.ui;

import static com.harsh.pdfstudio.creation.PDF_Compression.CompressMode.EXTREME;
import static com.harsh.pdfstudio.creation.PDF_Compression.CompressMode.LARGE;
import static com.harsh.pdfstudio.creation.PDF_Compression.CompressMode.NORMAL;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.creation.PDF_Compression;
import com.harsh.pdfstudio.fragments.Compress_pdf_Inp_Frag;
import com.harsh.pdfstudio.fragments.Compress_pdf_Out_Frag;
import com.harsh.pdfstudio.fragments.serverDown;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.utils.ApiClient;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GenerateName;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.Page_Count;
import com.harsh.pdfstudio.utils.ProgressRequestBody;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;

public class CompressPDF extends AppCompatActivity {

    private static final String TAG = "CompressPDF";

    private static final String SERVER_URL =
            "https://pdf-compressor-backend-pjts.onrender.com";

    /** Files up to this size are compressed on-device, bigger ones go to the server. */
    private static final long LOCAL_COMPRESSION_LIMIT = 190L * 1024 * 1024;

    // ---------------------------------------------------------------------
    // One Session == one "Compress" click.
    // Every async callback captures its own Session, so a callback that
    // belongs to an old/cancelled run can never touch the UI of a new run.
    // ---------------------------------------------------------------------
    private static final class Session {
        volatile boolean cancelled;      // user left / activity destroyed / failed
        volatile boolean finished;       // server job is over (completed/failed) -> nothing to cancel remotely

        volatile String jobId;
        volatile String accessToken;

        volatile Call uploadCall;
        volatile Call downloadCall;
        volatile HubConnection hub;
        volatile Future<?> localTask;

        final AtomicBoolean serverCancelSent = new AtomicBoolean(false);

        boolean downloadStarted;         // main thread only (guards duplicate "completed" messages)
    }

    private FrameLayout progressbar;
    private TextView progressBarText;
    private WebView loaderWebView;

    private int qualityLevel;
    private long originalPdfSize;

    private final ExecutorService executors = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    private Session session;

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Registered BEFORE super.onCreate() so that fragments restored after a
        // configuration change also get their click listener re-attached.
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentAttached(@NonNull FragmentManager fm,
                                                   @NonNull Fragment f,
                                                   @NonNull Context context) {
                        if (f instanceof Compress_pdf_Inp_Frag) {
                            ((Compress_pdf_Inp_Frag) f).setonCompressClick(
                                    (uri, quality) -> startCompression(uri, quality));
                        }
                    }
                }, false);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.parent_page);

        progressbar = findViewById(R.id.frame_layout_progressBar);
        progressBarText = findViewById(R.id.loadingBar_textview);
        loaderWebView = findViewById(R.id.webViewLoadingBar);

        // swallow touches so nothing underneath the overlay can be tapped
        progressbar.setClickable(true);

        loaderWebView.getSettings().setJavaScriptEnabled(true);
        loaderWebView.setBackgroundColor(Color.TRANSPARENT);
        loaderWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript("setOperation('convert');", null);
            }
        });

        loaderWebView.loadUrl("file:///android_asset/pdf_loader.html");
        loaderWebView.setVerticalScrollBarEnabled(false);
        loaderWebView.setHorizontalScrollBarEnabled(false);
        loaderWebView.setOnTouchListener((v, event) -> true);
        loaderWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Back press: cancel everything that is running, then behave normally.
        getOnBackPressedDispatcher().addCallback(
                this, new OnBackPressedCallback(true
                ) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "Back pressed -> cancelling running work");
                cancelSession(session);
                hideProgress();

                // hand the event over to the default behaviour (pop fragment / finish)
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });

        if (savedInstanceState == null) {
            // NOT added to the back stack, otherwise Back pops it and leaves a blank screen.
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container, new Compress_pdf_Inp_Frag())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        // Covers: back press, finish(), swipe-away, rotation, process teardown.
        cancelSession(session);
        session = null;

        handler.removeCallbacksAndMessages(null);
        executors.shutdownNow();

        if (loaderWebView != null) {
            ViewGroup parent = (ViewGroup) loaderWebView.getParent();
            if (parent != null) parent.removeView(loaderWebView);
            loaderWebView.destroy();
            loaderWebView = null;
        }

        super.onDestroy();
    }

    // ---------------------------------------------------------------------
    // Start
    // ---------------------------------------------------------------------

    private void startCompression(Uri uri, int quality) {

        // a new run always replaces (and cancels) the previous one
        cancelSession(session);
        Session s = new Session();
        session = s;

        originalPdfSize = File_Size.getFileSize(this, uri);

        PDF_Compression.CompressMode mode = NORMAL;
        qualityLevel = 0;
        if (quality == 1) {
            mode = LARGE;
            qualityLevel = 1;
        } else if (quality == 2) {
            mode = EXTREME;
            qualityLevel = 2;
        }

        showProgress();

        String outName = GenerateName.generateUniqueName("comp");

        if (originalPdfSize <= LOCAL_COMPRESSION_LIMIT) {
            final PDF_Compression.CompressMode finalMode = mode;
            s.localTask = executors.submit(() -> runLocalCompression(s, uri, finalMode, outName));
        } else {
            progressBarText.setText(R.string.uploading_pdf_0);
            uploadToServer(s, uri, GetFileName.getFileName(this, uri));
        }
    }

    // ---------------------------------------------------------------------
    // Local compression (background thread)
    // ---------------------------------------------------------------------

    private void runLocalCompression(Session s, Uri uri,
                                     PDF_Compression.CompressMode mode, String outName) {
        File result = null;
        try {
            ArrayList<FileItem> input = new ArrayList<>();
            input.add(new FileItem(
                    GetFileName.getFileName(this, uri),
                    uri,
                    File_Size.getFileSize(this, uri),
                    Page_Count.getPdfPageCount(this, uri)));

            if (s.cancelled) return;

            result = PDF_Compression.compressPdf(this, input, outName, mode);

            if (s.cancelled) {
                deleteQuietly(result);
                return;
            }

            if (result == null) {
                ui(s, () -> failSession(s, "PDF compression failed", false));
                return;
            }

            Uri outUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", result);

            if (File_Size.getFileSize(this, outUri) >= originalPdfSize) {
                // TODO: decide what to do when compression did not help (keep original?)
                Log.w(TAG, "Compressed file is not smaller than the original");
            }

            final File finalResult = result;
            handler.post(() -> {
                if (s.cancelled || isFinishing() || isDestroyed()) {
                    deleteQuietly(finalResult);
                    return;
                }
                s.finished = true;

                loaderWebView.evaluateJavascript("completeAnim();", null);

                handler.postDelayed(() -> {
                    if (s.cancelled || isFinishing() || isDestroyed()) {
                        deleteQuietly(finalResult);
                        return;
                    }
                    hideProgress();
                    moveOn(outUri);
                }, 1500);
            });

        } catch (Exception e) {
            Log.e(TAG, "Local compression failed", e);
            deleteQuietly(result);
            if (!s.cancelled) {
                ui(s, () -> failSession(s, "Error compressing PDF", false));
            }
        }
    }

    // ---------------------------------------------------------------------
    // Server compression: 1) upload
    // ---------------------------------------------------------------------

    private void uploadToServer(Session s, Uri uri, String pdfName) {

        progressBarText.setText(R.string.uploading_file);

        // do not capture the Activity inside the long-lived RequestBody
        final ContentResolver resolver = getApplicationContext().getContentResolver();
        final long length = originalPdfSize;

        RequestBody fileBody = new RequestBody() {

            @Override
            public MediaType contentType() {
                return MediaType.parse("application/pdf");
            }

            @Override
            public long contentLength() {
                return length > 0 ? length : -1;
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                try (InputStream in = resolver.openInputStream(uri)) {

                    if (in == null) {
                        throw new IOException("Unable to open PDF file");
                    }

                    byte[] buffer = new byte[64 * 1024];
                    int read;

                    while ((read = in.read(buffer)) != -1) {

                        // stop pumping bytes as soon as the user cancelled
                        if (s.cancelled) {
                            throw new IOException("Upload cancelled");
                        }

                        sink.write(buffer, 0, read);
                    }
                }
            }
        };

        final int[] lastProgress = {-1};

        ProgressRequestBody progressBody = new ProgressRequestBody(
                fileBody,
                (bytesWritten, totalBytes) -> {

                    if (s.cancelled || totalBytes <= 0) return;

                    // never show 100% until the HTTP response has arrived
                    int progress = Math.min((int) ((bytesWritten * 100L) / totalBytes), 99);
                    if (progress == lastProgress[0]) return;
                    lastProgress[0] = progress;

                    final String text = String.format(
                            Locale.US,
                            "Uploading PDF...\n%.1f MB / %.1f MB  •  %d%%",
                            bytesWritten / (1024.0 * 1024.0),
                            totalBytes / (1024.0 * 1024.0),
                            progress);

                    ui(s, () -> progressBarText.setText(text));
                });

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("File", pdfName, progressBody)
                .addFormDataPart("Engine", "ghostscript")
                // NOTE: the selected quality level is not sent to the server yet
                .addFormDataPart("Profile", "balanced")
                .build();

        Request request = new Request.Builder()
                .url(SERVER_URL + "/PdfCompressor/compress_PDF")
                .post(requestBody)
                .build();

        Call call = ApiClient.client.newCall(request);
        s.uploadCall = call;

        call.enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (s.cancelled || call.isCanceled()) return;   // we cancelled it ourselves

                ui(s, () -> failSession(s, "Upload failed: " + e.getMessage(), true));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

                final String json;

                try (Response r = response) {

                    if (!r.isSuccessful()) {
                        final int code = r.code();
                        ui(s, () -> failSession(s, "Server error: " + code, false));
                        return;
                    }

                    ResponseBody body = r.body();
                    if (body == null) {
                        ui(s, () -> failSession(s, null, false));
                        return;
                    }

                    json = body.string();

                } catch (IOException e) {
                    if (!s.cancelled) {
                        ui(s, () -> failSession(s, "Server error: " + e.getMessage(), false));
                    }
                    return;
                }

                final String jobId;
                final String token;

                try {
                    JSONObject object = new JSONObject(json);
                    jobId = object.getString("jobId");
                    token = object.getString("accessToken");
                } catch (JSONException e) {
                    Log.e(TAG, "Bad server response", e);
                    ui(s, () -> failSession(s, "Invalid server response", false));
                    return;
                }

                onJobCreated(s, jobId, token);
            }
        });
    }

    // ---------------------------------------------------------------------
    // Server compression: 2) SignalR updates (called from OkHttp thread)
    // ---------------------------------------------------------------------

    private void onJobCreated(Session s, String jobId, String token) {

        // Store the job FIRST, then look at `cancelled`.
        // cancelSession() does the opposite (sets `cancelled`, then reads the job),
        // so whichever runs second is guaranteed to see the other's write and the
        // server job is always cancelled (sendServerCancel is de-duplicated).
        s.jobId = jobId;
        s.accessToken = token;

        if (s.cancelled) {
            // user pressed Back while the upload response was still in flight
            sendServerCancel(s);
            return;
        }

        HubConnection hub = HubConnectionBuilder.create(SERVER_URL + "/PdfHub").build();
        s.hub = hub;

        hub.onClosed(ex -> {
            if (ex != null) {
                Log.e(TAG, "SignalR closed with error", ex);
                // connection died while we still wait for the result
                ui(s, () -> {
                    if (!s.finished) {
                        failSession(s, "Lost connection to the server", true);
                    }
                });
            } else {
                Log.d(TAG, "SignalR closed normally");
            }
        });

        hub.on("JobUpdate",
                message -> ui(s, () -> handleJobUpdate(s, message)),
                String.class);

        hub.on("QueuePosition",
                pos -> ui(s, () -> progressBarText.setText(
                        "Added to queue...\nYou are #" + pos + " in Queue")),
                Integer.class);

        // cancelSession() may have run before s.hub was assigned
        if (s.cancelled) {
            stopHub(s);
            sendServerCancel(s);
            return;
        }

        ui(s, () -> progressBarText.setText(R.string.added_to_queue));

        connectSignalR(s, hub, jobId, token);
    }

    @SuppressLint("CheckResult")
    private void connectSignalR(Session s, HubConnection hub, String jobId, String token) {

        hub.start().subscribe(
                () -> hub.invoke("JoinJob", jobId, token).subscribe(
                        () -> Log.d(TAG, "JoinJob completed for " + jobId),
                        err -> {
                            Log.e(TAG, "JoinJob FAILED", err);
                            ui(s, () -> failSession(s, "Could not join the job", true));
                        }),
                err -> {
                    Log.e(TAG, "SignalR start FAILED", err);
                    ui(s, () -> failSession(s, "Could not connect to the server", true));
                });
    }

    private void handleJobUpdate(Session s, @Nullable String message) {

        if (message == null) return;

        switch (message.toLowerCase(Locale.ROOT)) {

            case "compression completed":
                if (s.downloadStarted) return;         // duplicate message
                s.downloadStarted = true;
                s.finished = true;                     // nothing left to cancel remotely
                progressBarText.setText(R.string.compression_completed);
                stopHub(s);
                downloadResult(s);
                break;

            case "compression started":
                progressBarText.setText(R.string.compressing_pdf);
                break;

            case "compression failed":
                s.finished = true;
                failSession(s, null, false);
                break;

            default:
                progressBarText.setText(message);
                break;
        }
    }

    // ---------------------------------------------------------------------
    // Server compression: 3) download
    // ---------------------------------------------------------------------

    private void downloadResult(Session s) {

        String downloadUrl = SERVER_URL
                + "/PdfCompressor/get_compress_PDF?jobId=" + Uri.encode(s.jobId)
                + "&accessToken=" + Uri.encode(s.accessToken);

        progressBarText.setText(R.string.fetching_file);

        Call call = ApiClient.client.newCall(new Request.Builder().url(downloadUrl).get().build());
        s.downloadCall = call;

        call.enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (s.cancelled || call.isCanceled()) return;
                ui(s, () -> failSession(s, e.getMessage(), false));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

                File out = null;

                try (Response r = response) {

                    if (!r.isSuccessful()) {
                        final int code = r.code();
                        ui(s, () -> failSession(s, "Server error: " + code, false));
                        return;
                    }

                    ResponseBody body = r.body();
                    if (body == null) {
                        ui(s, () -> {
                            Toast.makeText(CompressPDF.this,
                                    R.string.empty_file_from_server, Toast.LENGTH_LONG).show();
                            failSession(s, null, false);
                        });
                        return;
                    }

                    File dir = getExternalFilesDir(null);
                    if (dir == null) dir = getFilesDir();
                    out = new File(dir, "compressed_" + s.jobId + ".pdf");

                    long total = 0;

                    try (InputStream in = body.byteStream();
                         FileOutputStream os = new FileOutputStream(out)) {

                        byte[] buffer = new byte[16 * 1024];
                        int read;

                        while ((read = in.read(buffer)) != -1) {
                            if (s.cancelled || call.isCanceled()) {
                                throw new IOException("Download cancelled");
                            }
                            os.write(buffer, 0, read);
                            total += read;
                        }
                    }

                    if (total == 0) {
                        throw new IOException("Server returned an empty file");
                    }

                    final Uri pdfUri = FileProvider.getUriForFile(
                            CompressPDF.this, getPackageName() + ".provider", out);

                    final File finalOut = out;
                    handler.post(() -> {
                        if (s.cancelled || isFinishing() || isDestroyed()) {
                            deleteQuietly(finalOut);
                            return;
                        }
                        hideProgress();
                        moveOn(pdfUri);
                    });

                } catch (Exception e) {
                    // I/O error, cancelled download, FileProvider problem ...
                    deleteQuietly(out);                       // never keep a half-written PDF
                    if (!s.cancelled) {
                        Log.e(TAG, "Download failed", e);
                        ui(s, () -> failSession(s, e.getMessage(), false));
                    }
                }
            }
        });
    }

    // ---------------------------------------------------------------------
    // Cancellation
    // ---------------------------------------------------------------------

    /**
     * Stops EVERYTHING that belongs to the session: the upload (which also aborts
     * RequestBody.writeTo), the download, the local compression thread, the SignalR
     * connection and - if a job already exists on the server - the server job.
     * Safe to call multiple times. Call from the main thread.
     */
    private void cancelSession(@Nullable Session s) {

        if (s == null || s.cancelled) return;
        s.cancelled = true;

        Call upload = s.uploadCall;
        if (upload != null) upload.cancel();

        Call download = s.downloadCall;
        if (download != null) download.cancel();

        Future<?> local = s.localTask;
        if (local != null) local.cancel(true);

        stopHub(s);
        sendServerCancel(s);
    }

    private void stopHub(Session s) {

        HubConnection hub = s.hub;
        s.hub = null;
        if (hub == null) return;

        // stop() may block on network, so never run it on the main thread
        Thread t = new Thread(() -> {
            try {
                hub.stop().blockingAwait();
            } catch (Exception e) {
                Log.w(TAG, "Error while stopping SignalR", e);
            }
        }, "signalr-stop");
        t.setDaemon(true);
        t.start();
    }

    /** Tells the backend to drop the job. Uses the shared client, so it survives the activity. */
    private void sendServerCancel(Session s) {

        final String jobId = s.jobId;
        final String token = s.accessToken;

        if (jobId == null || token == null || s.finished) return;
        if (!s.serverCancelSent.compareAndSet(false, true)) return;

        Request request = new Request.Builder()
                .url(SERVER_URL + "/PdfCompressor/cancel/" + Uri.encode(jobId)
                        + "?accessToken=" + Uri.encode(token))
                .post(RequestBody.create(new byte[0], MediaType.parse("application/json")))
                .build();

        ApiClient.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Cancel request failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response r = response) {
                    Log.d(TAG, "Cancel response: " + r.code());
                }
            }
        });
    }

    // ---------------------------------------------------------------------
    // UI helpers (main thread)
    // ---------------------------------------------------------------------

    /** Runs on the main thread, but only if the session is still alive. */
    private void ui(Session s, Runnable action) {
        handler.post(() -> {
            if (s.cancelled || isFinishing() || isDestroyed()) return;
            action.run();
        });
    }

    private void failSession(Session s, @Nullable String message, boolean serverIsDown) {

        if (s.cancelled) return;
        cancelSession(s);                 // stop whatever is still running (+ cancel remote job if needed)

        if (isFinishing() || isDestroyed()) return;

        hideProgress();

        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        if (serverIsDown) showServerDown();
        else showConversionFailed();
    }

    private void showProgress() {
        progressbar.bringToFront();
        progressbar.setVisibility(View.VISIBLE);
        loaderWebView.evaluateJavascript("startAnim();", null);
    }

    private void hideProgress() {
        progressbar.setVisibility(View.GONE);
    }

    private void showConversionFailed() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container, new ConversionFailed("for_pdf_operation"))
                .addToBackStack(null)
                .commitAllowingStateLoss();   // result may arrive while the app is in background
    }

    private void showServerDown() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container, new serverDown())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    private void moveOn(Uri resultUri) {

        Compress_pdf_Out_Frag outFrag = new Compress_pdf_Out_Frag();

        Bundle bundle = new Bundle();
        bundle.putParcelable("uri", resultUri);
        bundle.putString("original_size", String.valueOf(originalPdfSize));
        bundle.putString("compressionLevel", String.valueOf(qualityLevel));
        outFrag.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container, outFrag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    private static void deleteQuietly(@Nullable File f) {
        if (f != null && f.exists() && !f.delete()) {
            Log.w(TAG, "Could not delete " + f);
        }
    }
}