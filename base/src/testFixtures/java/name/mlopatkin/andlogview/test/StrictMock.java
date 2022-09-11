/*
 * Copyright 2022 the Andlogview authors
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

import org.mockito.Mockito;

import java.util.Objects;

/**
 * Provides stricter mocking classes. Mockito's strict mocks are not always the right tool for the job as these are
 * targeting rather specific use cases.
 */
public class StrictMock {
    private StrictMock() {}

    /**
     * Creates a stricter mock of the given class. The notion of strictness is different to the Mockito's one. The
     * returned mock will throw {@link UnsupportedOperationException} from all non-stubbed methods.
     *
     * @param clazz the class to mock
     * @param <T> the resulting type of the returned mock
     * @return the strict mock of type {@code clazz}
     */
    public static <T> T strictMock(Class<? extends T> clazz) {
        return Mockito.mock(clazz, invocation -> {
            if (!Objects.equals(void.class, invocation.getMethod().getReturnType())) {
                throw new UnsupportedOperationException(invocation.toString());
            }
            return null;
        });
    }
}
