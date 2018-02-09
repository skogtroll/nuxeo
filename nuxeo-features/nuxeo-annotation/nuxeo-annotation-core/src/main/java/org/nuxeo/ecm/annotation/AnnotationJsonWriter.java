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

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

import java.io.IOException;

import org.nuxeo.ecm.core.io.marshallers.json.ExtensibleEntityJsonWriter;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @since 10.1
 */
@Setup(mode = SINGLETON, priority = REFERENCE)
public class AnnotationJsonWriter extends ExtensibleEntityJsonWriter<Annotation> {

    public static final String ENTITY_TYPE = "annotation";

    public AnnotationJsonWriter() {
        super(ENTITY_TYPE, Annotation.class);
    }

    @Override
    protected void writeEntityBody(Annotation entity, JsonGenerator jg) throws IOException {
        jg.writeStringField("id", entity.getId());
        jg.writeStringField("color", entity.getColor());

        String date = null;
        if (entity.getDate() != null) {
            date = entity.getDate().toInstant().toString();
        }
        jg.writeStringField("date", date);

        jg.writeStringField("flags", entity.getFlags());
        jg.writeStringField("name", entity.getName());
        jg.writeStringField("documentId", entity.getDocumentId());
        jg.writeStringField("xpath", entity.getXpath());
        jg.writeStringField("lastModifier", entity.getLastModifier());
        jg.writeNumberField("page", entity.getPage());
        jg.writeStringField("position", entity.getPosition());

        String creationDate = null;
        if (entity.getCreationDate() != null) {
            creationDate = entity.getCreationDate().toInstant().toString();
        }
        jg.writeStringField("creationDate", creationDate);

        jg.writeNumberField("opacity", entity.getOpacity());
        jg.writeStringField("subject", entity.getSubject());
        jg.writeStringField("security", entity.getSecurity());
    }
}
