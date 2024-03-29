package com.devgrapher.ocebook.server;

import android.util.Log;

import com.devgrapher.ocebook.readium.ScriptProcessor;
import com.devgrapher.ocebook.util.Laz;

import org.readium.sdk.android.ManifestItem;
import org.readium.sdk.android.Package;

/**
 * Created by Brent on 2/19/17.
 */

public class ReadimWebServer {
    private static final String TAG = ReadimWebServer.class.toString();
    private ScriptProcessor.ScriptInjector mScriptInjector = new ScriptProcessor.ScriptInjector();

    private final WebServer.DataPreProcessor dataPreProcessor =
            new WebServer.DataPreProcessor() {

                @Override
                public byte[] handle(byte[] data, String mime, String uriPath,
                                     ManifestItem item) {
                    if (mime == null
                            || (!mime.equals("text/html") && !mime.equals("application/xhtml+xml"))) {
                        return null;
                    }
                    Laz.y(()-> Log.d(TAG, "PRE-PROCESSED HTML: " + uriPath));

                    return mScriptInjector.injectEpubHtml(data);
                }
            };

    private WebServer mWebServer = new WebServer(WebServer.HTTP_HOST, WebServer.HTTP_PORT, dataPreProcessor);

    public void start(Package pckg) {
        if (mWebServer.getPackage() == pckg)
            return;

        mWebServer.startServer(pckg);
        Laz.yLog(TAG, "Start Server");
    }

    public void stop() {
        mWebServer.stop();
        Laz.yLog(TAG, "Stop Server");
    }
}
