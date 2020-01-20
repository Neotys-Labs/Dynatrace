package com.neotys.dynatrace.events;

import com.neotys.dynatrace.common.tag.DynatraceTaggingUtils;
import org.junit.Test;


import com.google.common.base.Optional;

import java.util.regex.Pattern;

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
                "\"context\": \"KUBERNETES\",\n" +
                "\"key\": \"app\",\n" +
                "\"value\" : \"carts\"\n" +
                "}," +
                "{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"environement\",\n" +
                "\"value\" : \"dev\"\n" +
                "}";
        String dynatracesimplekubernetes="{\n" +
                "\"context\": \"KUBERNETES\",\n" +
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
        String dynatracesimplesingletag ="{\n" +
                "\"context\": \"CONTEXTLESS\",\n" +
                "\"key\": \"app\"" +
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
        generatedtag= DynatraceTaggingUtils.convertIntoDynatraceContextTag(tag);
       // assertEquals(dynatracesimplesingletag,generatedtag);

    }

    @Test
    public void isRequestNaming()
    {
        String name="{RequestAttribute:43c92a98-67c8-465f-b80b-b5ae38632367}_{RequestAttribute:0c8d4694-257b-4dcd-90c4-873392b699f0}:{URL:Path}";
        String requestNamingRuleRegexp="\\{RequestAttrib=ute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}_\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}:\\{URL:Path\\}";
        Pattern requestnamingPatern=Pattern.compile(requestNamingRuleRegexp);
        if (requestnamingPatern.matcher(name).matches())
            System.out.println("works");
        else
            System.out.println("Not working");
    }

    private Optional<String> getDynatracetag(Optional<String> tag)
    {
        Optional<String> result;
        if(tag.isPresent()) {
            result = Optional.of(tag.get().replaceAll(":", ":NL"));
            if(!result.get().contains(":"))
                result=Optional.of("NL"+tag.get());
            if(result.get().startsWith("["))
            {
                String[] tagcontext=result.get().split("]");
                if(tagcontext.length>1)
                {
                    result=Optional.of(tagcontext[1]);
                }
            }
        }
        else
            result=Optional.absent();

        return result;
    }
    @Test
    public void validateChangedtag() {
        Optional<String> tagkubernetes= Optional.of("[Kubernetes]app:carts,environement:dev");
        Optional<String> tagnonkubernetes= Optional.of("app:carts,environement:dev");
        Optional<String> tagsimplekubernetes= Optional.of("[Kubernetes]app:carts");
        Optional<String> tagsimplenonkubernetes= Optional.of("app:carts");
        Optional<String> tagsimple= Optional.of("app,dev");
        Optional<String> tag= Optional.of("app");

        Optional<String> change=getDynatracetag(tagsimple);
        System.out.println(change.get());
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
            "\"context\": \"KUBERNETES\",\n" +
            "\"key\": \"app\",\n" +
            "\"value\" : \"carts\"\n" +
            "}," +
            "{\n" +
            "\"context\": \"CONTEXTLESS\",\n" +
            "\"key\": \"environement\",\n" +
            "\"value\" : \"dev\"\n" +
            "}";
    String dynatracesimplekubernetes="{\n" +
                "\"context\": \"KUBERNETES\",\n" +
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
