/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.ui.logtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

@ExtendWith(MockitoExtension.class)
class HighlightCellRendererTest {
    private static final int TEXT_Y = 10;

    final Map<Integer, String> highlighted = new HashMap<>();
    final Map<Integer, String> plain = new HashMap<>();

    final HighlightCellRenderer.HighlightPainter painter = new HighlightCellRenderer.HighlightPainter() {
        @Override
        protected void paintHighlightedText(JLabel l, Graphics g, String s, int textX, int textY, int width) {
            assertThat(textY).isEqualTo(TEXT_Y);
            assertThat(width).isEqualTo(width(s));

            highlighted.put(textX, s);
        }

        @Override
        protected void paintText(JLabel l, Graphics g, String s, int textX, int textY) {
            assertThat(textY).isEqualTo(TEXT_Y);

            plain.put(textX, s);
        }
    };

    @Mock
    JLabel label;

    @BeforeEach
    void setUp() {
        final FontMetrics fontMetrics = mock();
        lenient().when(fontMetrics.stringWidth(anyString())).thenAnswer(
                invocation -> width(invocation.getArgument(0, String.class)));
        lenient().when(label.getFontMetrics(any())).thenReturn(fontMetrics);
    }

    @Test
    void canDisplayNonTruncatedWithoutHighlights() {
        paintText("value");

        assertHighlight("value");
    }

    @ParameterizedTest(name = "{0}{1}{2}")
    @CsvSource({
            "'',only highlight,''",
            "prefix_,highlight,''",
            "'',highlight,_suffix",
            "prefix_,highlight,_suffix",
    })
    void canDisplayNonTruncatedWithOneHighlight(String p, String h, String s) {
        paintText(p, h, s);

        assertHighlight(p, h, s);
    }

    @Test
    void canDisplayTwoConsecutiveHighlights() {
        paintText("p_", "h", "", "h", "s");

        assertHighlight("p_", "h", "", "h", "s");
    }

    @Test
    void canDisplayTruncatedWithoutHighlights() {
        paintTruncatedText("value...", "value truncated");

        assertHighlight("value...");
    }

    @Test
    void canDisplayTruncatedWithHighlightInVisiblePart() {
        paintTruncatedText("p_h_...", "p_", "h", "_s");

        assertHighlight("p_", "h", "_...");
    }

    @Test
    void canDisplayTruncatedWithHighlightInInvisiblePart() {
        paintTruncatedText("prefix...", "prefix_", "highlight");

        assertHighlight("prefix", "...");
    }

    @Test
    void canDisplayTruncatedWithHighlightsInVisibleAndInvisiblePart() {
        paintTruncatedText("p_h_...", "p_", "h", "_s", "other");

        assertHighlight("p_", "h", "_", "...");
    }

    @Test
    void canDisplayTruncatedWithConsecutiveHighlightsInVisibleAndInvisiblePart() {
        paintTruncatedText("p_h...", "p_", "h", "", "h");

        assertHighlight("p_", "h", "", "...");
    }

    @Test
    void canDisplayTruncatedWithHighlightSpanningTruncatedPart() {
        paintTruncatedText("p_h...", "p_", "highlight");

        assertHighlight("p_", "h...");
    }

    @Test
    void canDisplayTruncatedWithHighlightEndingAtTruncation() {
        paintTruncatedText("p_h...", "p_", "h", "_s");

        assertHighlight("p_", "h", "...");
    }


    private JLabel withLabelText(String displayText) {
        when(label.getText()).thenReturn(displayText);
        return label;
    }

    private void paintText(String text0, String... texts) {
        paintTextImpl(null, text0, texts);
    }

    private void paintTruncatedText(String truncated, String text0, String... texts) {
        paintTextImpl(truncated, text0, texts);
    }

    private void paintTextImpl(@Nullable String truncatedText, String text0, String... texts) {
        ImmutableList.Builder<Range<Integer>> highlights = ImmutableList.builder();
        var builder = new StringBuilder(text0);
        for (int i = 0; i < texts.length; i++) {
            if (i % 2 == 0) {
                Preconditions.checkArgument(!texts[i].isEmpty(), "Highlight cannot be empty");
                highlights.add(Range.closedOpen(builder.length(), builder.length() + texts[i].length()));
            }
            builder.append(texts[i]);
        }
        var text = builder.toString();


        Preconditions.checkArgument(truncatedText == null || isTruncation(truncatedText, text),
                "`%s` is not a possible truncation of `%s`", truncatedText, text);

        var displayText = truncatedText == null ? text : truncatedText;
        painter.paintText(withLabelText(text), mock(), displayText, 0, TEXT_Y, highlights.build());
    }

    private boolean isTruncation(String displayText, String text) {
        if (!displayText.endsWith("...")) {
            return false;
        }
        var prefix = displayText.substring(0, displayText.length() - "...".length());
        return text.startsWith(prefix) && text.length() > prefix.length();
    }

    private int width(String s) {
        return s.length() * 2;
    }

    private void assertHighlight(String... texts) {
        int width = 0;
        int highlightCount = 0;
        int plainCount = 0;
        for (int i = 0; i < texts.length; i++) {
            if (texts[i].isEmpty()) {
                continue;
            }
            if (i % 2 == 0) {
                ++plainCount;
            } else {
                ++highlightCount;
            }

            var checkMap = (i % 2 == 0) ? plain : highlighted;

            assertThat(checkMap).containsEntry(width, texts[i]);
            width += width(texts[i]);
        }

        assertThat(plain).hasSize(plainCount);
        assertThat(highlighted).hasSize(highlightCount);
    }
}
