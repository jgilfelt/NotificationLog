/*
 * Copyright (C) 2013 readyState Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readystatesoftware.notificationlog;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

public class LogEntry implements Parcelable {

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat FMT_LOG = new SimpleDateFormat("HH:mm:ss:SSS");
	private static final int TAG_LENGTH = 10;
	
	private int mLevel;
	private long mTime;
	private String mTag;
	private String mText;
	
	public LogEntry(int level, long time, String tag, String text) {
		mLevel = level;
		mTime = time;
		mTag = tag;
		mText = text;
	}
	
	protected LogEntry(Parcel in) {
        mLevel = in.readInt();
        mTime = in.readLong();
        mTag = in.readString();
        mText = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mLevel);
        dest.writeLong(mTime);
        dest.writeString(mTag);
        dest.writeString(mText);
    }

    public static final Parcelable.Creator<LogEntry> CREATOR = new Parcelable.Creator<LogEntry>() {
        public LogEntry createFromParcel(Parcel in) {
            return new LogEntry(in);
        }

        public LogEntry[] newArray(int size) {
            return new LogEntry[size];
        }
    };
    
    private String getColor(int level) {
    	switch (level) {
		case Log.VERBOSE:
			return "#000000";
		case Log.DEBUG:
			return "#0000FF";
		case Log.INFO:
			return "#367000";
		case Log.WARN:
			return "#F5B800";
		case Log.ERROR:
			return "#FF0000";
		case Log.ASSERT:
			return "#F500B8";
		default:
			return "#703A00";
		}
    }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Date d = new Date(mTime);
		
		sb.append("<span style='color:" + getColor(mLevel) + ";'>");
		sb.append(FMT_LOG.format(d));
		sb.append(" ");
		if (mTag.length() > TAG_LENGTH) {
			sb.append(mTag.substring(0, TAG_LENGTH-1) + "É");
		} else if (mTag.length() < TAG_LENGTH) {
			sb.append(mTag + "               ".substring(0, TAG_LENGTH - mTag.length()));
		} else {
			sb.append(mTag);
		}
		sb.append(" ");
		sb.append(mText);
		sb.append("</span><br/>");
		
		return sb.toString();
	}

	public int getLevel() {
		return mLevel;
	}

	public long getTime() {
		return mTime;
	}

	public String getTag() {
		return mTag;
	}

	public String getText() {
		return mText;
	}
	
}
