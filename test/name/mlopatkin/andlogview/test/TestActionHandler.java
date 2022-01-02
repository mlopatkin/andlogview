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

import com.google.common.base.MoreObjects;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Predicate;

public class TestActionHandler<T> {
    private final T nullAction;
    @Nullable
    private T action;

    public TestActionHandler(T nullAction) {
        this.nullAction = nullAction;
    }

    public T action() {
        return MoreObjects.firstNonNull(action, nullAction);
    }

    public void setAction(@Nullable T action) {
        this.action = action;
    }


    public static TestActionHandler<Runnable> runnableAction() {
        return new TestActionHandler<>(() -> {
        });
    }

    public static TestActionHandler<Predicate<String>> predicateAction(boolean defaultResult) {
        return new TestActionHandler<>(t -> defaultResult);
    }
}
