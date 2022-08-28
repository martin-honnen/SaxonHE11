////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2018-2022 Saxonica Limited
// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
// If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
// This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.saxon.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

/**
 * The class provides a static method for loading resources from a URL.
 * This method follows HTTP 301 and 302 redirects.
 */

public class ResourceLoader {
    /**
     * The maximum number of redirects to follow before throwing an IOException.
     * If you allow the underlying Java URL class to follow redirects, it gives
     * up after 20 hops.
     */
    public static int MAX_REDIRECTS = 20;

    /**
     * Open a URLConnection to the resource identified by the URI. For HTTP URIs, this
     * method will follow up to MAX_REDIRECTS redirects or until it detects a loop;
     * the connection returned in this case is to the first resource that did not
     * return a 301 or 302 response code.
     *
     * @param url The URL to retrieve.
     * @return An InputStream for the resource content.
     * @throws IOException If more than MAX_REDIRECTS are occur or if a loop is detected.
     */

    public static URLConnection urlConnection(URL url) throws IOException {
        if ("http".equals(url.getProtocol()) || "https".equals(url.getProtocol())) {
            HashSet<String> visited = new HashSet<>();
            String cookies = null;
            int count = MAX_REDIRECTS;
            for (;;) {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("Accept-Encoding", "gzip");
                if (cookies != null) {
                    conn.setRequestProperty("Cookie", cookies);
                }

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String location = conn.getHeaderField("Location");
                    url = new URL(location);
                    cookies = conn.getHeaderField("Set-Cookie");

                    if (visited.contains(location)) {
                        throw new IOException("HTTP redirect loop through " + location);
                    }
                    visited.add(location);

                    count -= 1;
                    if (count < 0) {
                        throw new IOException("HTTP redirects more than " + MAX_REDIRECTS + " times");
                    }
                } else {
                    return conn;
                }
            }
        } else {
            return url.openConnection();
        }
    }

    /**
     * Open a stream to retrieve the content identified by the URI. For HTTP URIs, this
     * method will follow up to MAX_REDIRECTS redirects or until it detects a loop.
     * This method automatically accepts and decompresses gzip encoded responses.
     *
     * @param url The URL to retrieve.
     * @return An InputStream for the resource content.
     * @throws IOException If more than MAX_REDIRECTS are occur or if a loop is detected.
     */
    public static InputStream urlStream(URL url) throws IOException  {
        URLConnection conn = ResourceLoader.urlConnection(url);
        InputStream inputStream =  conn.getInputStream();
        String contentEncoding = conn.getContentEncoding();
        if ("gzip".equals(contentEncoding)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        return inputStream;
    }
}
