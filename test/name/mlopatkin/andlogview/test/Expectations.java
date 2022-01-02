/*
 * Copyright 2022 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

/**
 * Helper to write conditional tests.
 */
public class Expectations {
    private static class Impl implements Expectation, Condition {
        protected boolean fulfilled;
        private Impl(boolean initial) {
            fulfilled = initial;
        }

        @Override
        public void fulfill() {
            fulfilled = true;
        }

        @Override
        public void invalidate() {
            fulfilled = false;
        }
    }

    /**
     * The expectation must be fulfilled by the action block.
     */
    public interface Expectation {
        /**
         * Fulfills this expectation. Can be called multiple times.
         */
        void fulfill();
    }

    /**
     * The condition holds before entering the action block but can be invalidated by it.
     */
    public interface Condition {
        /**
         * Invalidates this condition. This condition no longer holds. Can be called multiple times.
         */
        void invalidate();
    }

    /**
     * Verifies that the expectation is fulfilled after the action is completed.
     *
     * @param description the description
     * @param action the action
     */
    public static void expect(String description, Consumer<Expectation> action) {
        Impl expectation = new Impl(false);
        action.accept(expectation);
        assertTrue(expectation.fulfilled, String.format("Failed to fulfill %s", description));
    }

    /**
     * Verifies that the condition still holds after the action is completed.
     *
     * @param description the description
     * @param action the action
     */
    public static void ensure(String description, Consumer<Condition> action) {
        Impl condition = new Impl(true);
        action.accept(condition);
        assertTrue(condition.fulfilled, String.format("Condition %s no longer holds", description));
    }
}
