/*
 * Copyright 2020 the Justify authors.
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
 */
package org.leadpony.justify.internal.problem;

import java.util.ArrayList;
import java.util.List;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;

/**
 * @author leadpony
 */
public interface DeferredProblemDispatcher extends ProblemDispatcher, List<Problem> {

    static DeferredProblemDispatcher empty() {

        @SuppressWarnings("serial")
        class ProblemDispatcherImpl extends ArrayList<Problem> implements DeferredProblemDispatcher {

            @Override
            public void dispatchProblem(Problem problem) {
                add(problem);
            }
        }

        return new ProblemDispatcherImpl();
    }

}
