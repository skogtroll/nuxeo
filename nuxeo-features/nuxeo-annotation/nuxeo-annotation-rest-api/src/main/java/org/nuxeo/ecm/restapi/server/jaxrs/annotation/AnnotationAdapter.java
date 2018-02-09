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

package org.nuxeo.ecm.restapi.server.jaxrs.annotation;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.nuxeo.ecm.annotation.Annotation;
import org.nuxeo.ecm.annotation.AnnotationService;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webengine.model.WebAdapter;
import org.nuxeo.ecm.webengine.model.impl.DefaultAdapter;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 10.1
 */
@WebAdapter(name = AnnotationAdapter.NAME, type = "annotationAdapter")
@Produces(MediaType.APPLICATION_JSON)
public class AnnotationAdapter extends DefaultAdapter {

    public static final String NAME = "annotation";

    @POST
    @Path("{xpath}")
    public Response createAnnotation(@PathParam("xpath") String xpath, Annotation annotation) {
        DocumentModel doc = getTarget().getAdapter(DocumentModel.class);
        AnnotationService annotationService = Framework.getService(AnnotationService.class);
        Annotation result = annotationService.createAnnotation(getContext().getCoreSession(), doc.getId(), xpath,
                annotation);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @GET
    public List<Annotation> getAnnotations(@QueryParam("xpath") String xpath) {
        DocumentModel doc = getTarget().getAdapter(DocumentModel.class);
        AnnotationService annotationService = Framework.getService(AnnotationService.class);
        return annotationService.getAnnotations(getContext().getCoreSession(), doc.getId(), xpath);
    }

    @GET
    @Path("{xpath:((?:(?!/@).)*)}/{annotationId}")
    public Annotation getAnnotation(@PathParam("xpath") String xpath, @PathParam("annotationId") String annotationId) {
        DocumentModel doc = getTarget().getAdapter(DocumentModel.class);
        AnnotationService annotationService = Framework.getService(AnnotationService.class);
        return annotationService.getAnnotation(getContext().getCoreSession(), doc.getId(), xpath, annotationId);
    }

    @PUT
    @Path("{xpath:((?:(?!/@).)*)}/{annotationId}")
    public Response updateAnnotation(@PathParam("xpath") String xpath, @PathParam("annotationId") String annotationId,
            Annotation annotation) {
        DocumentModel doc = getTarget().getAdapter(DocumentModel.class);
        AnnotationService annotationService = Framework.getService(AnnotationService.class);
        annotationService.updateAnnotation(getContext().getCoreSession(), doc.getId(), xpath, annotation);
        return Response.status(Response.Status.OK).entity(annotation).build();
    }

    @DELETE
    @Path("{xpath:((?:(?!/@).)*)}/{annotationId}")
    public Response deleteAnnotation(@PathParam("xpath") String xpath, @PathParam("annotationId") String annotationId) {
        DocumentModel doc = getTarget().getAdapter(DocumentModel.class);
        AnnotationService annotationService = Framework.getService(AnnotationService.class);
        annotationService.deleteAnnotation(getContext().getCoreSession(), doc.getId(), xpath, annotationId);
        return Response.status(Response.Status.OK).entity(annotationId).build();
    }

}
