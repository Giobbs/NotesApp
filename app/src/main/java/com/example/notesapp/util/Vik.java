package com.example.notesapp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vik {

    public static List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) return new ArrayList<>();
        return Arrays.asList(tags.split(","));
    }
}
