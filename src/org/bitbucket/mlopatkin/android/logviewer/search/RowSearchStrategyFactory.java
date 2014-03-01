
package org.bitbucket.mlopatkin.android.logviewer.search;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Grammar:
 *
 * <pre>
 * valid_symbol := [^:/]|'\:'|'\/'
 * nonspace := (valid_symbol) that is not a space
 * pattern := (nonspace)(valid_symbol)*(nonspace)
 * regex := /(valid_symbol)* /
 * prefix := app|tag|msg
 * single_simple_query := ((pattern)|(regex))
 * single_qualified_query := (prefix):(single_simple_query)
 * single_query := single_simple_query | single_qualified_query
 * </pre>
 */
public final class RowSearchStrategyFactory {

    private RowSearchStrategyFactory() {
    }

    private static final String PREFIX_APP = "app:";
    private static final String PREFIX_TAG = "tag:";
    private static final String PREFIX_MSG = "msg:";

    private static final List<String> PREFIXES = Arrays.asList(PREFIX_APP, PREFIX_MSG, PREFIX_TAG);

    public static RowSearchStrategy compile(String pattern) throws RequestCompilationException {
        if (CharMatcher.WHITESPACE.matchesAllOf(Strings.nullToEmpty(pattern))) {
            return null;
        }
        pattern = pattern.trim();
        // check for prefix
        for (String prefix : PREFIXES) {
            if (pattern.startsWith(prefix)) {
                return prepareWithPrefix(prefix, pattern.substring(prefix.length()));
            }
        }
        return prepareWithoutPrefix(pattern);
    }

    private static RowSearchStrategy prepareWithPrefix(String prefix, String rest)
            throws RequestCompilationException {
        rest = rest.trim();
        HighlightStrategy strategy = SearchStrategyFactory.createHighlightStrategy(rest);
        if (strategy == null) {
            return null;
        }
        if (PREFIX_APP.equals(prefix)) {
            return new AppNameSearcher(strategy);
        } else if (PREFIX_MSG.equals(prefix)) {
            return new MessageSearcher(strategy);
        } else if (PREFIX_TAG.equals(prefix)) {
            return new TagSearcher(strategy);
        } else {
            throw new AssertionError("Unrecognized prefix");
        }
    }

    private static RowSearchStrategy prepareWithoutPrefix(String pattern)
            throws RequestCompilationException {
        HighlightStrategy strategy = SearchStrategyFactory.createHighlightStrategy(pattern);
        if (strategy != null) {
            return new OrSearcher(new AppNameSearcher(strategy), new TagSearcher(strategy),
                    new MessageSearcher(strategy));
        } else {
            return null;
        }
    }
}
