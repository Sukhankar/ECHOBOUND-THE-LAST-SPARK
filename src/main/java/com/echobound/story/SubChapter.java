package com.echobound.story;

public class SubChapter {
    public final int globalIndex; // 1 to 500
    public final int majorChapter; // 1 to 20
    public final int localIndex; // 1 to 25
    public final String title;
    public final String narrativeSummary;
    public boolean completed = false;

    public SubChapter(int globalIndex, int majorChapter, int localIndex, String title, String narrativeSummary) {
        this.globalIndex = globalIndex;
        this.majorChapter = majorChapter;
        this.localIndex = localIndex;
        this.title = title;
        this.narrativeSummary = narrativeSummary;
    }
}
