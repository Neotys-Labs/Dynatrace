package com.neotys.dynatrace.common.tag;

import com.google.common.base.Optional;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DynatraceTaggingUtils {
    private final static String TAGGING_CONTEXT="\\[(\\w{2,})\\](\\w{1,}):(\\w{1,})";
    private final static String TAGGING_NonCONTEXT="(\\w{1,}):(\\w{1,})";
    private static Pattern taggingContextPatern=Pattern.compile(TAGGING_CONTEXT);
    private static Pattern taggingNonContextPatern=Pattern.compile(TAGGING_NonCONTEXT);
    private static String NLTAGSUFFIX="NL";
    private static String CONTEXTLESS="CONTEXTLESS";

    public static String convertIntoDynatraceContextTag(Optional<String> dynatraceTag)
    {
        if(dynatraceTag.isPresent())
        {
            List<String> listofTags= Arrays.asList(dynatraceTag.get().split("\\s*,\\s*"));
            if(listofTags.size()>0)
            {
                List<String> listofConcertedTags = listofTags.stream().map(tag -> {
                    return convertApplicationContext(tag);
                }).filter(Objects::nonNull).collect(Collectors.toList());

                return String.join(",", listofConcertedTags);
            }
            else
            {
                return convertApplicationContext(dynatraceTag.get());
            }
        }
        else
            return null;
    }

    public static String convertforUpdateTags(Optional<String> dynatraceTag)
    {
        if(dynatraceTag.isPresent())
        {
            List<String> listofTags= Arrays.asList(dynatraceTag.get().split("\\s*,\\s*"));
            if(listofTags.size()>0)
            {
                List<String> listofConcertedTags = listofTags.stream().map(tag -> {
                    tag=tag.replaceAll(":",":"+NLTAGSUFFIX);
                    if(!tag.contains(":"))
                        tag=NLTAGSUFFIX+tag;
                    return "\""+tag+"\"";
                }).filter(Objects::nonNull).collect(Collectors.toList());

                return String.join(",", listofConcertedTags);
            }
            else
            {
                return "\""+dynatraceTag.get()+"\"";
            }
        }
        else
            return null;
    }
    
    public static String convertIntoDynatraceTag(Optional<String> dynatraceTag)
    {
        if(dynatraceTag.isPresent())
        {
            List<String> listofTags= Arrays.asList(dynatraceTag.get().split("\\s*,\\s*"));
            if(listofTags.size()>0)
            {
                List<String> listofConcertedTags = listofTags.stream().map(tag -> {
                    return convertApplicationTag(tag);
                }).filter(Objects::nonNull).collect(Collectors.toList());

                return String.join(",", listofConcertedTags);
            }
            else
            {
                return convertApplicationTag(dynatraceTag.get());
            }
        }
        else
            return null;
    }

    private static String convertApplicationContext(String tag)
    {
        String key;
        String context;
        String value;

        Matcher matcher=taggingContextPatern.matcher(tag);
        if (matcher.matches()) {
            context = matcher.group(1).toUpperCase();
            key = matcher.group(2);
            value = matcher.group(3);
        }else {
            matcher=taggingNonContextPatern.matcher(tag);
            if (matcher.matches()) {
                context = CONTEXTLESS;
                key = matcher.group(1);
                value = matcher.group(2);
            } else {
                List<String> listofkeys = Arrays.asList(tag.split(":"));
                if (listofkeys.size() > 1) {
                    context = CONTEXTLESS;
                    key = listofkeys.get(0);
                    value = listofkeys.get(1);
                } else {
                    context = CONTEXTLESS;
                    key = listofkeys.get(0);
                    value = null;
                }
            }

        }
        if (value != null)
            return "{\n" +
                    " \"context\": \"" + context + "\",\n" +
                    "\"key\": \"" + key + "\",\n" +
                    " \"value\" : \"" + value + "\"\n" +
                    "}";
        else
            return "{\n" +
                    " \"context\": \"" + context + "\",\n" +
                    "\"key\": \"" + key + "\""+
                    "}";

    }

    private static String convertApplicationTag(String tag)
    {
        String key;
        String context;
        String value;

        Matcher matcher=taggingContextPatern.matcher(tag);
        if (matcher.matches()) {
            context = matcher.group(1).toUpperCase();
            key = matcher.group(2);
            value = matcher.group(3);
        }else {
            matcher=taggingNonContextPatern.matcher(tag);
            if (matcher.matches())
            {
                context = CONTEXTLESS;
                key = matcher.group(1);
                value = matcher.group(2);
            } else {
                List<String> listofkeys = Arrays.asList(tag.split(":"));
                if (listofkeys.size() > 1)
                {
                    context = CONTEXTLESS;
                    key = listofkeys.get(0);
                    value = listofkeys.get(1);
                } else {
                    context = null;
                    key = listofkeys.get(0);
                    value = null;
                }
            }

        }
        if (value != null)
            return "{\n" +
                    "\"context\": \"" + context + "\",\n" +
                    "\"key\": \"" + key + "\",\n" +
                    "\"value\" : \"" + value + "\"\n" +
                    "}";
        else
        {
            if(context!=null)
                return "{\n" +
                        "\"context\": \"" + context + "\",\n" +
                        "\"key\": \"" + key + "\""+
                        "}";
            else
                return key;
        }


    }
}
