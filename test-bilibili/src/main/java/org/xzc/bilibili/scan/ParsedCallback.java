package org.xzc.bilibili.scan;

import org.xzc.bilibili.model.Video;

public interface ParsedCallback {
	void onParsed(Video v);
}
