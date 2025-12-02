package com.tem2.karirku;

import java.util.ArrayList;
import java.util.List;

public class CVKeywordManager {
    private static CVKeywordManager instance;
    private List<String> detectedKeywords = new ArrayList<>();
    private boolean hasScannedCV = false;

    private CVKeywordManager() {}

    public static CVKeywordManager getInstance() {
        if (instance == null) {
            instance = new CVKeywordManager();
        }
        return instance;
    }

    public void setKeywords(List<String> keywords) {
        this.detectedKeywords = keywords;
        this.hasScannedCV = true;
    }

    public List<String> getKeywords() {
        return detectedKeywords;
    }

    public boolean hasScannedCV() {
        return hasScannedCV;
    }

    public void clearKeywords() {
        detectedKeywords.clear();
        hasScannedCV = false;
    }
}