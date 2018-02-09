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

package org.nuxeo.ecm.annotation;

import static org.nuxeo.ecm.annotation.AnnotationJsonWriter.ENTITY_TYPE;
import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;
import java.util.Calendar;

import org.joda.time.Instant;
import org.nuxeo.ecm.core.io.marshallers.json.EntityJsonReader;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @since 10.1
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class AnnotationJsonReader extends EntityJsonReader<Annotation> {

    public AnnotationJsonReader() {
        super(ENTITY_TYPE);
    }

    @Override
    protected Annotation readEntity(JsonNode jn) throws IOException {
        Annotation annotation = new AnnotationImpl();
        annotation.setId(jn.get("id").textValue());
        annotation.setColor(jn.get("color").textValue());

        String dateValue = jn.get("date").textValue();
        if (dateValue != null) {
            Calendar date = Calendar.getInstance();
            date.setTime(Instant.parse(dateValue).toDate());
            annotation.setDate(date);
        }

        annotation.setFlags(jn.get("flags").textValue());
        annotation.setName(jn.get("name").textValue());
        annotation.setDocumentId(jn.get("documentId").textValue());
        annotation.setXpath(jn.get("xpath").textValue());
        annotation.setLastModifier(jn.get("lastModifier").textValue());
        annotation.setPage(jn.get("page").longValue());
        annotation.setPosition(jn.get("position").textValue());

        String creationDateValue = jn.get("creationDate").textValue();
        if (creationDateValue != null) {
            Calendar creationDate = Calendar.getInstance();
            creationDate.setTime(Instant.parse(creationDateValue).toDate());
            annotation.setCreationDate(creationDate);
        }

        annotation.setOpacity(jn.get("opacity").doubleValue());
        annotation.setSubject(jn.get("subject").textValue());
        annotation.setSecurity(jn.get("security").textValue());

        return annotation;
    }

}
