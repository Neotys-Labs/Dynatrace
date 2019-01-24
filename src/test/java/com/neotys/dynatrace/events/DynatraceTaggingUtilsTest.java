package com.neotys.dynatrace.events;

import com.neotys.dynatrace.common.tag.DynatraceTaggingUtils;
import org.junit.Test;


import com.google.common.base.Optional;

import static org.junit.Assert.assertEquals;

public class DynatraceTaggingUtilsTest {
    @Test
    public void validateApplicationtag() {
        Optional<String> tagkubernetes= Optional.of("[Kubernetes]app:carts,environement:dev");
        Optional<String> tagnonkubernetes= Optional.of("app:carts,environement:dev");
        Optional<String> tagsimplekubernetes= Optional.of("[Kubernetes]app:carts");
        Optional<String> tagsimplenonkubernetes= Optional.of("app:carts");

        Optional<String> tagsimple= Optional.of("app,dev");
        Optional<String> tag= Optional.of("app");

        String dynatracekubernetes="{\n" +
                "\"context\": \"Kubernetes\",\n" +
                "\"key\": \"app\",\n" +
                "\"value\" : \"carts\"\n" +
                "}," +
                "{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"environement\",\n" +
                "\"value\" : \"dev\"\n" +
                "}";
        String dynatracesimplekubernetes="{\n" +
                "\"context\": \"Kubernetes\",\n" +
                "\"key\": \"app\",\n" +
                "\"value\" : \"carts\"\n" +
                "}";

        String dynatracenonkubernetes="{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"app\",\n" +
                "\"value\" : \"carts\"\n" +
                "}," +
                "{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"environement\",\n" +
                "\"value\" : \"dev\"\n" +
                "}";
        String dynatracesimplenonkubernetes="{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"app\",\n" +
                "\"value\" : \"carts\"\n" +
                "}";
        String dynatracesimpltag="app,dev";
        String dynatracetag="app";

        String generatedtag=DynatraceTaggingUtils.convertIntoDynatraceTag(tagkubernetes);
        assertEquals(dynatracekubernetes, generatedtag);
        generatedtag=DynatraceTaggingUtils.convertIntoDynatraceTag(tagnonkubernetes) ;
        assertEquals(dynatracenonkubernetes,generatedtag);
        generatedtag=DynatraceTaggingUtils.convertIntoDynatraceTag(tagsimplekubernetes) ;
        assertEquals(dynatracesimplekubernetes,generatedtag );
        generatedtag=DynatraceTaggingUtils.convertIntoDynatraceTag(tagsimplenonkubernetes);
        assertEquals(dynatracesimplenonkubernetes,generatedtag );
        generatedtag=DynatraceTaggingUtils.convertIntoDynatraceTag(tagsimple);
        assertEquals(dynatracesimpltag, generatedtag);
        generatedtag= DynatraceTaggingUtils.convertIntoDynatraceTag(tag);
        assertEquals(dynatracetag,generatedtag);

    }


    @Test
    public void validatetag() {
    Optional<String> tagkubernetes= Optional.of("[Kubernetes]app:carts,environement:dev");
    Optional<String> tagnonkubernetes= Optional.of("app:carts,environement:dev");
    Optional<String> tagsimplekubernetes= Optional.of("[Kubernetes]app:carts");
    Optional<String> tagsimplenonkubernetes= Optional.of("app:carts");

    Optional<String> tagsimple= Optional.of("app,dev");
    Optional<String> tag= Optional.of("app");

    String dynatracekubernetes="{\n" +
            "\"context\": \"Kubernetes\",\n" +
            "\"key\": \"app\",\n" +
            "\"value\" : \"carts\"\n" +
            "}," +
            "{\n" +
            "\"context\": \"CONTEXTLESS\",\n" +
            "\"key\": \"environement\",\n" +
            "\"value\" : \"dev\"\n" +
            "}";
    String dynatracesimplekubernetes="{\n" +
                "\"context\": \"Kubernetes\",\n" +
                "\"key\": \"app\",\n" +
                "\"value\" : \"carts\"\n" +
                "}";

    String dynatracenonkubernetes="{\n" +
            "\"context\": \"CONTEXTLESS\",\n" +
            "\"key\": \"app\",\n" +
            "\"value\" : \"carts\"\n" +
            "}," +
            "{\n" +
            "\"context\": \"CONTEXTLESS\",\n" +
            "\"key\": \"environement\",\n" +
            "\"value\" : \"dev\"\n" +
            "}";
    String dynatracesimplenonkubernetes="{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"app\",\n" +
                "\"value\" : \"carts\"\n" +
                "}";
    String dynatracesimpltag="{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"app\"\n" +
            "}," +
            "{\n" +
            "\"context\": \"CONTEXTLESS\",\n" +
            "\"key\": \"environement\"\n" +
            "}";
    String dynatracetag="{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"app\"\n" +
                "}";

    String generatedtag=DynatraceTaggingUtils.convertIntoDynatraceContextTag(tagkubernetes);
    //assertEquals(dynatracekubernetes, generatedtag);
    generatedtag=DynatraceTaggingUtils.convertIntoDynatraceContextTag(tagnonkubernetes) ;
    //assertEquals(dynatracenonkubernetes,generatedtag);
    generatedtag=DynatraceTaggingUtils.convertIntoDynatraceContextTag(tagsimplekubernetes) ;
    //assertEquals(dynatracesimplekubernetes,generatedtag );
    generatedtag=DynatraceTaggingUtils.convertIntoDynatraceContextTag(tagsimplenonkubernetes);
    //assertEquals(dynatracesimplenonkubernetes,generatedtag );
    generatedtag=DynatraceTaggingUtils.convertIntoDynatraceContextTag(tagsimple);
    //assertEquals(dynatracesimpltag, generatedtag);
    generatedtag= DynatraceTaggingUtils.convertIntoDynatraceContextTag(tag);
    //assertEquals(dynatracetag,generatedtag);

    }

}
