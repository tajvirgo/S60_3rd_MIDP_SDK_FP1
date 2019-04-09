/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.mm.devicecap;

/**
 * Wrapper for queries to System.getProperty() to retrieve
 * Mobile Media support information.
 *
 * @version 1.1
 */
public class QueryProperty {
    /**
     * Returns a String indicating whether capture, recording and
     * snapshots of a given media format are supported.  The
     * media format should be one for which Mobile Media defines
     * a "supports. ... .capture" property.  As of Mobile Media
     * 1.0, this is "audio" or "video".
     *
     *@param media the format of interest, either "audio" or "video"
     */
    static String getCaptureSupport(String media) {
        StringBuffer result = new StringBuffer(media);
        if (isTrue("supports."+media+".capture")) {
            result.append(" capture supported");
            if (isTrue("supports.recording")) {
                String recordingEncodings = System.getProperty(media+".encodings");
                if ((recordingEncodings != null) && (! recordingEncodings.trim().equals(""))) {
                    result.append(", recording supported to ");
                    result.append(recordingEncodings);
                } else {
                    result.append(", recording not supported for this media format");
                }
            } else {
                result.append(", recording not supported");
            }
            String snapshotEncodings = System.getProperty(media+".snapshot.encodings");
            if (snapshotEncodings != null) {
                result.append(", snapshots supported to ");
                result.append(snapshotEncodings);
            }
        } else {
            result.append(" capture not supported");
        }
        return result.toString();
    }

    /**
     * Returns a String indicating whether polyphonic playback is supported.
     */
    static String getPolyphonySupport() {
        if (isTrue("supports.mixing")) {
            return "Polyphonic sound supported";
        } else {
            return "Polyphonic sound not supported";
        }
    }

    /**
     * Returns true if System.getProperty(property) returns a String "true".
     * Returns false otherwise.
     */
    private static boolean isTrue(String property) {
        String supported = System.getProperty(property);
        return ((supported != null) && (supported.equals("true")));
    }
}
