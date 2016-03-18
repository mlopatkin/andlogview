/*
 * Copyright 2014 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.utils;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;

public class FluentPredicateTest {

    @Test
    public void testFrom() throws Exception {
        assertAlwaysFalse(FluentPredicate.from(Predicates.<Integer>alwaysFalse()));
        assertAlwaysTrue(FluentPredicate.from(Predicates.<Integer>alwaysTrue()));
        assertLikeIsOdd(FluentPredicate.from(isOdd()));

        assertAlwaysTrue(FluentPredicate.<Integer>from(null));
    }

    @Test
    public void testFrom_null() throws Exception {
        assertAlwaysTrue(FluentPredicate.<Integer>from(null));
    }

    @Test
    public void testAnd() throws Exception {
        FluentPredicate<Integer> fluentIsOdd = FluentPredicate.from(isOdd());

        assertAlwaysFalse(fluentIsOdd.and(Predicates.<Integer>alwaysFalse()));
        assertLikeIsOdd(fluentIsOdd.and(Predicates.<Integer>alwaysTrue()));
    }

    @Test
    public void testOr() throws Exception {
        FluentPredicate<Integer> fluentIsOdd = FluentPredicate.from(isOdd());

        assertLikeIsOdd(fluentIsOdd.or(Predicates.<Integer>alwaysFalse()));
        assertAlwaysTrue(fluentIsOdd.or(Predicates.<Integer>alwaysTrue()));
    }

    @Test
    public void testNot() throws Exception {
        FluentPredicate<Integer> fluentAlwaysTrue = FluentPredicate
                .from(Predicates.<Integer>alwaysTrue());

        assertAlwaysFalse(fluentAlwaysTrue.not());
    }

    private void assertAlwaysFalse(FluentPredicate<Integer> p) {
        Assert.assertFalse(p.apply(0));
        Assert.assertFalse(p.apply(1));
        Assert.assertFalse(p.apply(null));
    }

    private void assertAlwaysTrue(FluentPredicate<Integer> p) {
        Assert.assertTrue(p.apply(0));
        Assert.assertTrue(p.apply(1));
        Assert.assertTrue(p.apply(null));
    }

    private void assertLikeIsOdd(FluentPredicate<Integer> p) {
        Assert.assertFalse(p.apply(0));
        Assert.assertTrue(p.apply(1));
        Assert.assertTrue(p.apply(null));
    }

    private Predicate<Integer> isOdd() {
        return new Predicate<Integer>() {
            @Override
            public boolean apply(@Nullable Integer integer) {
                return MoreObjects.firstNonNull(integer, 1) % 2 != 0;
            }
        };
    }
}
