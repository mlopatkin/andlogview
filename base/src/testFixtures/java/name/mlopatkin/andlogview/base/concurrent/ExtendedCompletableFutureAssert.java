/*
 * Copyright 2023 the Andlogview authors
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

package name.mlopatkin.andlogview.base.concurrent;

import name.mlopatkin.andlogview.base.MyThrowables;

import com.google.common.base.Preconditions;

import org.assertj.core.api.AbstractCompletableFutureAssert;
import org.assertj.core.api.CompletableFutureAssert;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Extended version of AssertJ's {@link CompletableFutureAssert}.
 *
 * @param <RESULT> the future's result type
 */
public class ExtendedCompletableFutureAssert<RESULT>
        extends AbstractCompletableFutureAssert<ExtendedCompletableFutureAssert<RESULT>, RESULT> {
    public ExtendedCompletableFutureAssert(CompletableFuture<RESULT> actual) {
        super(actual, ExtendedCompletableFutureAssert.class);
    }

    /**
     * Entry point to the completable future assertions (cannot use assertThat to avoid clashing with the existing
     * method).
     *
     * @param future the future to assert
     * @param <RESULT> the future's result type
     * @return the assertion
     */
    public static <RESULT> ExtendedCompletableFutureAssert<RESULT> assertThatCompletableFuture(
            CompletableFuture<RESULT> future) {
        return new ExtendedCompletableFutureAssert<>(future);
    }

    /**
     * Verifies that the {@link CompletableFuture} is cancelled or failed because one of its upstreams has been
     * cancelled.
     *
     * @return this assertion object
     */
    public ExtendedCompletableFutureAssert<RESULT> isCancelledUpstream() {
        super.isCompletedExceptionally();
        var failureReason = getFailureReason();
        if (!(failureReason instanceof CancellationException)) {
            failWithMessage("Expected failure reason to be CancellationException but got %s", failureReason);
        }
        return this;
    }


    private Throwable getFailureReason() {
        Preconditions.checkState(actual.isCompletedExceptionally(), "The future is not yet completed");
        try {
            actual.get();
            throw new AssertionError("Expecting failure to be present");
        } catch (ExecutionException | CancellationException | CompletionException e) {
            return MyThrowables.unwrapUninteresting(e);
        } catch (InterruptedException e) {
            throw new AssertionError("Completed future cannot be interrupted");
        }
    }
}
