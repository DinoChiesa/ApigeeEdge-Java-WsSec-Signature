// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.google.apigee.callout.wssec.testng.tests;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
// import com.fasterxml.jackson.databind.DeserializationFeature;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.google.apigee.edgecallouts.EditXmlNode;

import com.google.apigee.callout.wssec.SOAPSignerCallout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import mockit.Mock;
import mockit.MockUp;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestSOAPSignerCallout {
    private final static String testDataDir = "src/test/resources/test-data";

    MessageContext msgCtxt;
    String messageContent;
    Message message;
    ExecutionContext exeCtxt;

    @BeforeMethod()
    public void testSetup1() {

        msgCtxt = new MockUp<MessageContext>() {
            private Map variables;
            public void $init() {
                variables = new HashMap();
            }

            @Mock()
            public <T> T getVariable(final String name){
                if (variables == null) {
                    variables = new HashMap();
                }
                return (T) variables.get(name);
            }

            @Mock()
            public boolean setVariable(final String name, final Object value) {
                if (variables == null) {
                    variables = new HashMap();
                }
                variables.put(name, value);
                return true;
            }

            @Mock()
            public boolean removeVariable(final String name) {
                if (variables == null) {
                    variables = new HashMap();
                }
                if (variables.containsKey(name)) {
                    variables.remove(name);
                }
                return true;
            }

            @Mock()
            public Message getMessage() {
                return message;
            }
        }.getMockInstance();

        exeCtxt = new MockUp<ExecutionContext>(){ }.getMockInstance();

        message = new MockUp<Message>(){
            @Mock()
            public InputStream getContentAsStream() {
                return new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
            }
            @Mock()
            public String getContent() {
                return messageContent;
            }
        }.getMockInstance();
    }

    // @DataProvider(name = "batch1")
    // public static Object[][] getDataForBatch1()
    //     throws IOException, IllegalStateException {
    // 
    //     // @DataProvider requires the output to be a Object[][]. The inner
    //     // Object[] is the set of params that get passed to the test method.
    //     // So, if you want to pass just one param to the constructor, then
    //     // each inner Object[] must have length 1.
    // 
    //     ObjectMapper om = new ObjectMapper();
    //     om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // 
    //     // Path currentRelativePath = Paths.get("");
    //     // String s = currentRelativePath.toAbsolutePath().toString();
    //     // System.out.println("Current relative path is: " + s);
    // 
    //     // read in all the *.json files in the test-data directory
    //     File testDir = new File(testDataDir);
    //     if (!testDir.exists()) {
    //         throw new IllegalStateException("no test directory.");
    //     }
    //     File[] files = testDir.listFiles();
    //     if (files.length == 0) {
    //         throw new IllegalStateException("no tests found.");
    //     }
    //     int c=0;
    //     ArrayList<TestCase> list = new ArrayList<TestCase>();
    //     for (File file : files) {
    //         String name = file.getName();
    //         if (name.endsWith(".json")) {
    //             TestCase tc = om.readValue(file, TestCase.class);
    //             tc.setTestName(name.substring(0,name.length()-5));
    //             list.add(tc);
    //         }
    //     }
    // 
    //     // OMG!!  Seriously? Is this the easiest way to generate a 2-d array?
    //     int n = list.size();
    //     Object[][] data = new Object[n][];
    //     for (int i = 0; i < data.length; i++) {
    //         data[i] = new Object[]{ list.get(i) };
    //     }
    //     return data;
    // }

    // @Test
    // public void testDataProviders() throws IOException {
    //     Assert.assertTrue(getDataForBatch1().length > 0);
    // }

    // private File getTestFile(String filename) {
    //     File testDir = new File(testDataDir);
    //        
    //     if (!testDir.exists()) {
    //         throw new IllegalStateException("no test directory.");
    //     }
    //     File fileInDirectory = new File(testDir, filename);
    //     if (!fileInDirectory.exists()) {
    //         throw new IllegalStateException("File does not exist.");
    //     }
    //     return fileInDirectory;
    // }
    
    private String readFile(String filename) throws java.io.IOException {
        return new String(Files.readAllBytes(Paths.get(testDataDir, filename)),
                          StandardCharsets.UTF_8);
    }
    
    @Test
    public void testBasicSigning() throws IOException {
        //File xmldoc = getTestFile();
        messageContent = readFile("sample-soap-message-1.xml");
        //String pemString = readFile("zionsbank.net.cert");
        
        Map props = new HashMap<String,String>();
        //props.put("certificate", pemString);
        props.put("debug", "true");
        props.put("alias", "soadev");
        props.put("password", "devteam");
        SOAPSignerCallout callout = new SOAPSignerCallout(props);

        // execute and retrieve output
        ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
        String actualContent = msgCtxt.getVariable("message.content");
        System.out.printf("content: %s\n", actualContent);
    }

    
    // @Test(dataProvider = "batch1")
    // public void test2_Configs(TestCase tc) {
    //     if (tc.getDescription()!= null)
    //         System.out.printf("  %10s - %s\n", tc.getTestName(), tc.getDescription() );
    //     else
    //         System.out.printf("  %10s\n", tc.getTestName() );
    // 
    //     messageContent = tc.getInput().get("message-content");
    // 
    //     EditXmlNode callout = new EditXmlNode(tc.getInput());  // properties
    // 
    //     // execute and retrieve output
    //     ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    //     String actualContent = msgCtxt.getVariable("message.content");
    // 
    //     String s = tc.getExpected().get("success");
    //     ExecutionResult expectedResult = (s!=null && s.toLowerCase().equals("true")) ?
    //                                        ExecutionResult.SUCCESS : ExecutionResult.ABORT;
    //     // check result and output
    //     if (expectedResult == actualResult) {
    //         if (expectedResult == ExecutionResult.SUCCESS) {
    //             String expectedContent = tc.getExpected().get("message-content");
    //             expectedContent = expectedContent.replace('\'','"');
    //             if (actualContent.equals(expectedContent)) {
    //             }
    //             else {
    //                 //System.out.printf("  FAIL - content\n");
    //                 System.err.printf("    got: %s\n", actualContent);
    //                 System.err.printf("    expected: %s\n", expectedContent);
    //                 // the following will throw
    //                 Assert.assertEquals(actualContent, expectedContent, "result not as expected");
    //             }
    //         }
    //         else {
    //             String expectedError = tc.getExpected().get("error");
    //             if (expectedError != null) {
    //                 String actualError = msgCtxt.getVariable("editxml_error");
    //                 Assert.assertEquals(actualError, expectedError, "error not as expected");
    //             }
    //         }
    //     }
    //     else {
    //         Assert.assertEquals(actualResult, expectedResult, "result not as expected");
    //     }
    //     System.out.println("=========================================================");
    // }

}
