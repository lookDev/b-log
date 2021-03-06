/*
 * Copyright (c) 2016. Kaede
 */

package moe.kaede.log;

import android.text.TextUtils;

class LogCatImpl implements Log {

    private static final int CHUNK_SIZE = 4000;

    private final String mEmptyMessage;
    private final LogSetting mSetting;
    private final boolean mShowThreadInfo;

    public LogCatImpl(LogSetting setting) {
        mSetting = setting;
        mShowThreadInfo = setting.isShowThreadInfo();
        mEmptyMessage = setting.getLogFormatter().emptyMessage();
    }

    @Override
    public void log(int logLevel, String tag, String msg) {
        if (mSetting.getLogcatLevel() == LogLevel.NONE || mSetting.getLogcatLevel() > logLevel)
            return;

        // AndroidLogcat may abort long message, just in case.
        separateMessageIfNeed(logLevel, tag, msg);
    }

    private void separateMessageIfNeed(int logType, String tag, String msg) {
        if (TextUtils.isEmpty(msg)) {
            logMessage(logType, tag, mEmptyMessage);
            return;
        }

        byte[] bytes = msg.getBytes();
        int length = bytes.length;

        if (length <= CHUNK_SIZE) {
            logMessage(logType, tag, msg);
            return;
        }

        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            logMessage(logType, tag, new String(bytes, i, count));
        }
    }

    private void logMessage(int logType, String tag, String chunk) {
        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            if (mShowThreadInfo)
                line = "[" + Thread.currentThread().getName() + "]  " + line;

            logcat(logType, tag, line);
        }
    }

    private void logcat(int logType, String tag, String chunk) {
        switch (logType) {
            case LogLevel.VERBOSE:
                android.util.Log.v(tag, chunk);
                break;
            case LogLevel.DEBUG:
                android.util.Log.d(tag, chunk);
                break;
            case LogLevel.INFO:
                android.util.Log.i(tag, chunk);
                break;
            case LogLevel.WARN:
                android.util.Log.w(tag, chunk);
                break;
            case LogLevel.ERROR:
                android.util.Log.e(tag, chunk);
                break;
            case LogLevel.ASSERT:
                android.util.Log.wtf(tag, chunk);
                break;
            default:
                android.util.Log.d(tag, chunk);
                break;
        }
    }
}
