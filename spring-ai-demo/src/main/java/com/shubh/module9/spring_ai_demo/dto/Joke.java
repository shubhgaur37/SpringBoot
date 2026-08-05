package com.shubh.module9.spring_ai_demo.dto;

public record Joke(String text,
                   String category,
                   String language,
                   Boolean isNSFW,
                   Integer laughRating
){

}
