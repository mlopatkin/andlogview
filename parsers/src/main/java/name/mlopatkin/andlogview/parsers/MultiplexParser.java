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

package name.mlopatkin.andlogview.parsers;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A combining parser that feeds several child parsers with its input. The parser stops (return {@code false} from
 * {@link #nextLine(CharSequence)}) only if all children stop. However, if the child stops, it no longer receives the
 * input.
 *
 * @param <T> the common type of the child parsers
 */
public class MultiplexParser<T extends BasePushParser> extends AbstractBasePushParser {
    private final ImmutableList<T> children;
    private final List<T> activeChildren;

    @SafeVarargs
    public MultiplexParser(T... children) {
        this(ImmutableList.copyOf(children));
    }

    public MultiplexParser(List<T> children) {
        this.children = ImmutableList.copyOf(children);
        this.activeChildren = new ArrayList<>(this.children);
    }

    @Override
    protected void onNextLine(CharSequence line) {
        Iterator<T> activeIter = activeChildren.iterator();
        boolean result = false;
        while (activeIter.hasNext()) {
            T target = activeIter.next();
            if (!target.nextLine(line)) {
                // This parser has given up, do not feed it anymore.
                activeIter.remove();
            } else {
                result = true;
            }
        }
        stopUnless(result);
    }

    @Override
    public void close() {
        children.forEach(BasePushParser::close);
    }
}
