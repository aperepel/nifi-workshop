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

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.hortonworks.iot.MyProcessor.PROP_BUFFER_SIZE;
import static com.hortonworks.iot.MyProcessor.PROP_TO_UPPERCASE;
import static com.hortonworks.iot.MyProcessor.REL_FAILURE;
import static com.hortonworks.iot.MyProcessor.REL_SUCCESS;


public class MyProcessorTest {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(MyProcessor.class);
    }

    @Test
    public void testStringTooBig() {
        testRunner.setProperty(MyProcessor.PROP_BUFFER_SIZE, "10 B");
        byte[] testData = "this string is too long".getBytes();
        testRunner.enqueue(testData);
        testRunner.run();

        testRunner.assertAllFlowFilesTransferred(REL_FAILURE);

        testRunner.setProperty(PROP_TO_UPPERCASE, "true");
        testRunner.enqueue(testData);
        testRunner.run();
        testRunner.assertAllFlowFilesTransferred(REL_FAILURE);
    }

    @Test
    public void testUppercase() throws Exception {
        byte[] testData = "test string".getBytes();
        testRunner.enqueue(testData);
        testRunner.run();

        testRunner.assertAllFlowFilesTransferred(REL_SUCCESS);
        List<MockFlowFile> ff = testRunner.getFlowFilesForRelationship(REL_SUCCESS);
        Assert.assertNotNull(ff);
        Assert.assertEquals("wrong number of messages", 1, ff.size());
        ff.get(0).assertContentEquals(testData);


        testRunner.clearTransferState();
        testRunner.setProperty(PROP_BUFFER_SIZE, "10 B");
        testRunner.enqueue(testData);
        testRunner.run();
        testRunner.assertAllFlowFilesTransferred(REL_FAILURE);

    }
}
