package com.echobound.story;

import java.util.ArrayList;
import java.util.List;

public class StoryChapter {
    public final int chapterNumber; // 1 to 20
    public final String title;
    public final String region;
    public final String climaxEncounter;
    public final List<SubChapter> subChapters = new ArrayList<>();

    public StoryChapter(int chapterNumber, String title, String region, String climaxEncounter) {
        this.chapterNumber = chapterNumber;
        this.title = title;
        this.region = region;
        this.climaxEncounter = climaxEncounter;
    }

    public void addSubChapter(SubChapter subChapter) {
        this.subChapters.add(subChapter);
    }

    public boolean isAllCompleted() {
        if (subChapters.isEmpty()) return false;
        for (SubChapter sc : subChapters) {
            if (!sc.completed) return false;
        }
        return true;
    }

    public int getCompletedCount() {
        int count = 0;
        for (SubChapter sc : subChapters) {
            if (sc.completed) count++;
        }
        return count;
    }
}
