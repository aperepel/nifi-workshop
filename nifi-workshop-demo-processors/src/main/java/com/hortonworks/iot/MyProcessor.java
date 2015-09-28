/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hortonworks.iot;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.DataUnit;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Tags({"string"})
@CapabilityDescription("Awesome string manipulator")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})
public class MyProcessor extends AbstractProcessor {

    public static final PropertyDescriptor PROP_TO_UPPERCASE = new PropertyDescriptor
                                                                             .Builder().name("Convert to UPPERCASE")
                                                                         .description("All caps")
                                                                         .required(false)
                                                                         .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
                                                                         .defaultValue("false")
                                                                         .build();

    public static final PropertyDescriptor PROP_BUFFER_SIZE = new PropertyDescriptor
                                                                       .Builder().name("Buffer size")
                                                                       .description("Size of the working buffer. If an incoming string is bigger it will be routed to a failure path")
                                                                       .required(false)
                                                                       .addValidator(StandardValidators.DATA_SIZE_VALIDATOR)
                                                                       .defaultValue("8192 B")
                                                                       .build();


    public static final Relationship REL_SUCCESS = new Relationship.Builder()
                                                           .name("success")
                                                           .description("Success, all done")
                                                           .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
                                                           .name("failure")
                                                           .description("Where all bad people end up")
                                                           .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(PROP_TO_UPPERCASE);
        descriptors.add(PROP_BUFFER_SIZE);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final Boolean toUppercase = context.getProperty(PROP_TO_UPPERCASE).asBoolean();
        final Double bufSize = context.getProperty(PROP_BUFFER_SIZE).asDataSize(DataUnit.B);

        if (flowFile.getSize() > bufSize) {
            getLogger().warn("Incoming string too big: {} vs buffer: {}",
                                    new Object[]{flowFile.getSize(), bufSize});
            session.transfer(flowFile, REL_FAILURE);
            return;
        }

        try {

            if (toUppercase) {
                flowFile = session.write(flowFile, new StreamCallback() {
                    @Override
                    public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
                        String s = IOUtils.toString(inputStream);
                        IOUtils.write(s.toUpperCase(), outputStream);
                    }
                });
            }
            session.transfer(flowFile, REL_SUCCESS);
        } catch (Exception e) {
            getLogger().error("Failed while processing a string", e);
            session.transfer(flowFile, REL_FAILURE);
        }
    }

}
