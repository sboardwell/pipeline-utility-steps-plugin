/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Nikolas Falco
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.utility.steps.json;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import hudson.FilePath;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

/**
 * Execution of {@link ReadJSONStep}.
 *
 * @author Nikolas Falco
 */
public class ReadJSONStepExecution extends AbstractSynchronousNonBlockingStepExecution<JSON> {

    private static final long serialVersionUID = 1L;

    @StepContextParameter
    private transient FilePath ws;

    @Inject
    private transient ReadJSONStep step;

    @Override
    protected JSON run() throws Exception {
        String fName = step.getDescriptor().getFunctionName();
        if (isNotBlank(step.getFile()) && isNotBlank(step.getText())) {
            throw new IllegalArgumentException(Messages.ReadJSONStepExecution_tooManyArguments(fName));
        }
        if (isBlank(step.getFile()) && isBlank(step.getText())) {
            throw new IllegalArgumentException(Messages.ReadJSONStepExecution_missingRequiredArgument(fName));
        }

        JSON json = null;
        if (!isBlank(step.getFile())) {
            FilePath f = ws.child(step.getFile());
            if (f.exists() && !f.isDirectory()) {
                try (InputStream is = f.read()) {
                    json = JSONSerializer.toJSON(IOUtils.toString(is));
                }
            } else if (f.isDirectory()) {
                throw new IllegalArgumentException(Messages.JSONStepExecution_fileIsDirectory(f.getRemote()));
            } else if (!f.exists()) {
                throw new FileNotFoundException(Messages.JSONStepExecution_fileNotFound(f.getRemote()));
            }
        }
        if (!isBlank(step.getText())) {
            json = JSONSerializer.toJSON(step.getText().trim());
        }

        return json;
    }
}