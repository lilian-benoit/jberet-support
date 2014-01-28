/*
 * Copyright (c) 2012-2013 Red Hat, Inc. and/or its affiliates.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cheng Fang - Initial API and implementation
 */

package org.jberet.support.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;

import org.jberet.runtime.JobExecutionImpl;
import org.jberet.util.BatchUtil;
import org.junit.Assert;
import org.junit.Test;

public class ParseBoolTest {
    static final String jobName = "org.jberet.support.io.ParseBoolTest";
    private final JobOperator jobOperator = BatchRuntime.getJobOperator();
    static final int waitTimeoutMinutes = 0;
    static final String tmpdir = System.getProperty("java.io.tmpdir");
    public final String skipComments = "matches '\\(,.*,\\)'";

    @Test
    public void testParseBoolDefault() throws Exception {
        //use the default boolean true false values in org.supercsv.cellprocessor.ParseBool
        final String data =
                "boolTrueFalse,bool10,boolyn,booltf,boolYesNo,boolOnOff,description" + BatchUtil.NL +
                        "(, THIS IS A COMMENT ,)" + BatchUtil.NL +
                        "true,      1,     y,     t,,," + BatchUtil.NL +
                        "false,     0,     n,     f,,,";

        final String cellProcessors =
                        "NotNull, ParseBool; " +
                        "NotNull, Trim, ParseBool; " +
                        "NotNull, Trim, ParseBool; " +
                        "NotNull, Trim, ParseBool;" +
                        "Optional, ParseBool;" +
                        "Optional, ParseBool;" +
                        "ConvertNullTo('This row contains booleans parsed from strings, (e.g., \'true\', 1, y, t).')";
        testParseBool0("testParseBoolDefault", data, cellProcessors);
    }

    @Test
    public void testParseBoolSingleCustomValue() throws Exception {
        //use the SINGLE true or false string value to override the default boolean true false values in
        // org.supercsv.cellprocessor.ParseBool
        final String data =
                "boolTrueFalse,bool10,boolyn,booltf,boolYesNo,boolOnOff" + BatchUtil.NL +
                        "true,      1,     y,     t,      yes,       on" + BatchUtil.NL +
                        "false,     0,     n,     f,       no,      off";

        final String cellProcessors =
                        "Optional, ParseBool('true', 'false');" +
                        "Optional, Trim, ParseBool('1', '0'); " +
                        "Optional, Trim, ParseBool('y', 'n'); " +
                        "Optional, Trim, ParseBool('t', 'f'); " +
                        "Optional, Trim, ParseBool('yes', 'no');" +
                        "Optional, Trim, ParseBool('on', 'off')";
        testParseBool0("testParseBoolSingleCustomValue", data, cellProcessors);
    }

    @Test
    public void testParseBoolMultipleCustomValues() throws Exception {
        //use multiple true or false string values to override the default boolean true false values in
        // org.supercsv.cellprocessor.ParseBool
        final String data =
                "boolTrueFalse,bool10,boolyn,booltf,boolYesNo,boolOnOff" + BatchUtil.NL +
                        "true,      1,     y,     t,      yes,       on" + BatchUtil.NL +
                        "false,     0,     n,     f,       no,      off";

        final String cellProcessors =
                "Trim, ParseBool('true, 1, y, t, yes, on', 'false, 0, n, f, no, off');" +
                "Trim, ParseBool('true, 1, y, t, yes, on', 'false, 0, n, f, no, off'); " +
                "Trim, ParseBool('true, 1, y, t, yes, on', 'false, 0, n, f, no, off'); " +
                "Trim, ParseBool('true, 1, y, t, yes, on', 'false, 0, n, f, no, off'); " +
                "Trim, ParseBool('true, 1, y, t, yes, on', 'false, 0, n, f, no, off');" +
                "Trim, ParseBool('true, 1, y, t, yes, on', 'false, 0, n, f, no, off')";
        testParseBool0("testParseBoolMultipleCustomValues", data, cellProcessors);
    }

    private void testParseBool0(final String fileName, final String data, final String cellProcessors) throws Exception {
        final String resource = saveFileToTmpdir(fileName, data).getPath();
        final Properties params = CsvItemReaderTest.createParams(CsvProperties.BEAN_TYPE_KEY, BooleansBean.class.getName());
        params.setProperty(CsvProperties.RESOURCE_KEY, resource);
        params.setProperty(CsvProperties.CELL_PROCESSORS_KEY, cellProcessors);
        params.setProperty(CsvProperties.SKIP_COMMENTS_KEY, skipComments);
        System.out.printf("CSV resource to read: %s%n", resource);

        final long jobExecutionId = jobOperator.start(jobName, params);
        final JobExecutionImpl jobExecution = (JobExecutionImpl) jobOperator.getJobExecution(jobExecutionId);
        jobExecution.awaitTermination(waitTimeoutMinutes, TimeUnit.MINUTES);
        Assert.assertEquals(BatchStatus.COMPLETED, jobExecution.getBatchStatus());
    }

    static File saveFileToTmpdir(final String fileName, final String content) throws Exception {
        final File file = new File(tmpdir, fileName);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(content, 0, content.length());
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
        return file;
    }
}
