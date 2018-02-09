/*
 * (C) Copyright 2018 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Funsho David
 *
 */

package org.nuxeo.ecm.restapi.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.annotation.Annotation;
import org.nuxeo.ecm.annotation.AnnotationImpl;
import org.nuxeo.ecm.annotation.AnnotationJsonWriter;
import org.nuxeo.ecm.annotation.AnnotationService;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.io.registry.MarshallerHelper;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext.CtxBuilder;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.Jetty;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @since 10.1
 */
@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class })
@Jetty(port = 18090)
@Deploy({ "org.nuxeo.ecm.platform.restapi.server.annotation", "org.nuxeo.ecm.annotation.api",
        "org.nuxeo.ecm.annotation.core" })
public class AnnotationTest extends BaseTest {

    @Inject
    protected AnnotationService annotationService;

    @Test
    public void testCreateAnnotation() throws IOException {
        DocumentModel file = session.createDocumentModel("/", "testDoc", "File");
        file = session.createDocument(file);

        fetchInvalidations();
        String xpath = "file:content";

        Annotation annotation = new AnnotationImpl();
        annotation.setColor("000000");
        annotation.setLastModifier("bob");
        annotation.setPosition("0,0,0,0");

        String jsonAnnotation = MarshallerHelper.objectToJson(annotation, CtxBuilder.get());

        try (CloseableClientResponse response = getResponse(RequestType.POST,
                "id/" + file.getId() + "/@annotation/" + xpath, jsonAnnotation)) {
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            JsonNode node = mapper.readTree(response.getEntityInputStream());
            assertEquals(file.getId(), node.get("documentId").textValue());
            assertEquals(xpath, node.get("xpath").textValue());
            assertEquals("000000", node.get("color").textValue());
            assertEquals("bob", node.get("lastModifier").textValue());
            assertEquals("0,0,0,0", node.get("position").textValue());
        }
    }

    @Test
    public void testGetAnnotation() throws IOException {
        DocumentModel file = session.createDocumentModel("/", "testDoc", "File");
        file = session.createDocument(file);
        String xpath = "files:files/0/file";

        Annotation annotation = new AnnotationImpl();
        Calendar now = Calendar.getInstance();
        annotation.setCreationDate(now);
        annotation.setPage(42L);
        annotation = annotationService.createAnnotation(session, file.getId(), xpath, annotation);
        fetchInvalidations();

        JsonNode node = getResponseAsJson(RequestType.GET,
                "id/" + file.getId() + "/@annotation/" + xpath + "/" + annotation.getId());

        assertEquals(AnnotationJsonWriter.ENTITY_TYPE, node.get("entity-type").asText());
        assertEquals(annotation.getId(), node.get("id").textValue());
        assertEquals(42L, node.get("page").longValue());
        assertEquals(file.getId(), node.get("documentId").textValue());
        assertEquals(xpath, node.get("xpath").textValue());
        assertEquals(now.toInstant().toString(), node.get("creationDate").textValue());
    }

    @Test
    public void testUpdateAnnotation() throws IOException {
        DocumentModel file = session.createDocumentModel("/", "testDoc", "File");
        file = session.createDocument(file);

        fetchInvalidations();
        String xpath = "file:content";

        Annotation annotation = new AnnotationImpl();
        annotation.setOpacity(0.5);
        annotation.setName("testAnnotation");
        annotation = annotationService.createAnnotation(session, file.getId(), xpath, annotation);

        fetchInvalidations();

        annotation.setName("testUpdatedAnnotation");
        annotation.setSecurity("Private");

        String jsonAnnotation = MarshallerHelper.objectToJson(annotation, CtxBuilder.get());

        try (CloseableClientResponse response = getResponse(RequestType.PUT,
                "id/" + file.getId() + "/@annotation/" + xpath + "/" + annotation.getId(), jsonAnnotation)) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            fetchInvalidations();

            Annotation updatedAnnotation = annotationService.getAnnotation(session, file.getId(), xpath,
                    annotation.getId());
            assertEquals(0.5, updatedAnnotation.getOpacity(), 0.0);
            assertEquals("testUpdatedAnnotation", updatedAnnotation.getName());
            assertEquals("Private", updatedAnnotation.getSecurity());
        }
    }

    @Test
    public void testDeleteAnnotation() {
        DocumentModel file = session.createDocumentModel("/", "testDoc", "File");
        file = session.createDocument(file);
        String xpath = "files:files/0/file";

        Annotation annotation = new AnnotationImpl();
        annotation = annotationService.createAnnotation(session, file.getId(), xpath, annotation);

        fetchInvalidations();
        DocumentRef annotationRef = new IdRef(annotation.getId());

        assertTrue(session.exists(annotationRef));

        try (CloseableClientResponse response = getResponse(RequestType.DELETE,
                "id/" + file.getId() + "/@annotation/" + xpath + "/" + annotation.getId())) {
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            fetchInvalidations();
            assertFalse(session.exists(annotationRef));
        }
    }

    @Test
    public void testSearchAnnotations() throws IOException {
        DocumentModel file1 = session.createDocumentModel("/", "testDoc1", "File");
        file1 = session.createDocument(file1);
        String xpath1 = "files:files/0/file";
        String xpath2 = "files:files/1/file";

        DocumentModel file2 = session.createDocumentModel("/", "testDoc2", "File");
        file2 = session.createDocument(file2);
        String xpath3 = "file:content";

        Annotation annotation1 = new AnnotationImpl();
        annotation1.setName("annotation1");
        Annotation annotation2 = new AnnotationImpl();
        annotation2.setName("annotation2");
        Annotation annotation3 = new AnnotationImpl();
        annotation3.setName("annotation3");
        Annotation annotation4 = new AnnotationImpl();
        annotation4.setName("annotation4");
        Annotation annotation5 = new AnnotationImpl();
        annotation5.setName("annotation5");

        annotation1 = annotationService.createAnnotation(session, file1.getId(), xpath1, annotation1);
        annotation2 = annotationService.createAnnotation(session, file1.getId(), xpath2, annotation2);
        annotation3 = annotationService.createAnnotation(session, file1.getId(), xpath2, annotation3);
        annotation4 = annotationService.createAnnotation(session, file2.getId(), xpath3, annotation4);
        annotation5 = annotationService.createAnnotation(session, file2.getId(), xpath3, annotation5);

        fetchInvalidations();

        MultivaluedMapImpl params1 = new MultivaluedMapImpl();
        params1.putSingle("xpath", xpath1);
        MultivaluedMapImpl params2 = new MultivaluedMapImpl();
        params2.putSingle("xpath", xpath2);
        MultivaluedMapImpl params3 = new MultivaluedMapImpl();
        params3.putSingle("xpath", xpath3);

        JsonNode node1 = getResponseAsJson(RequestType.GET, "id/" + file1.getId() + "/@annotation", params1).get(
                "entries");
        JsonNode node2 = getResponseAsJson(RequestType.GET, "id/" + file1.getId() + "/@annotation", params2).get(
                "entries");
        JsonNode node3 = getResponseAsJson(RequestType.GET, "id/" + file2.getId() + "/@annotation", params3).get(
                "entries");

        assertEquals(1, node1.size());
        assertEquals(2, node2.size());
        assertEquals(2, node3.size());

        assertEquals(annotation1.getId(), node1.get(0).get("id").textValue());
        assertEquals(annotation2.getId(), node2.get(0).get("id").textValue());
        assertEquals(annotation3.getId(), node2.get(1).get("id").textValue());
        assertEquals(annotation4.getId(), node3.get(0).get("id").textValue());
        assertEquals(annotation5.getId(), node3.get(1).get("id").textValue());
    }

}
