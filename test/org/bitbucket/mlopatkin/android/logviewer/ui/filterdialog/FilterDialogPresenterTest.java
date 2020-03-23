/*
 * Copyright 2020 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.config.Configuration;
import org.bitbucket.mlopatkin.android.logviewer.filters.FilteringMode;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static org.bitbucket.mlopatkin.utils.FutureMatchers.completedWithResult;
import static org.bitbucket.mlopatkin.utils.FutureMatchers.notCompleted;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public class FilterDialogPresenterTest {

    private static class FakeDialogView implements FilterDialogPresenter.FilterDialogView {
        boolean isShown = false;
        String tagsText = "";
        String messageText = "";
        String pidsAppsText = "";
        LogRecord.@Nullable Priority priority;
        FilteringMode mode = FilteringMode.getDefaultMode();
        @Nullable Color highlightColor;
        @MonotonicNonNull Runnable commitAction;
        @MonotonicNonNull Runnable discardAction;
        @Nullable String errorText;

        @Override
        public void setTagsText(String text) {
            tagsText = Objects.requireNonNull(text);
        }

        @Override
        public String getTagsText() {
            return Objects.requireNonNull(tagsText);
        }

        @Override
        public void setMessageText(String text) {
            messageText = Objects.requireNonNull(text);
        }

        @Override
        public String getMessageText() {
            return messageText;
        }

        @Override
        public void setPidsAppsText(String text) {
            pidsAppsText = Objects.requireNonNull(text);
        }

        @Override
        public String getPidsAppsText() {
            return pidsAppsText;
        }

        @Override
        public void setPriority(LogRecord.@Nullable Priority priority) {
            this.priority = priority;
        }

        @Override
        public Optional<LogRecord.Priority> getPriority() {
            return Optional.ofNullable(priority);
        }

        @Override
        public void setMode(FilteringMode mode) {
            this.mode = Objects.requireNonNull(mode);
        }

        @Override
        public FilteringMode getMode() {
            return mode;
        }

        @Override
        public void setHighlightColor(@Nullable Color color) {
            this.highlightColor = color;
        }

        @Override
        public Optional<Color> getHighlightColor() {
            return Optional.ofNullable(highlightColor);
        }

        @Override
        public void setCommitAction(Runnable commitAction) {
            this.commitAction = Objects.requireNonNull(commitAction);
        }

        @Override
        public void setDiscardAction(Runnable discardAction) {
            this.discardAction = Objects.requireNonNull(discardAction);
        }

        @Override
        public void show() {
            isShown = true;
        }

        @Override
        public void hide() {
            isShown = false;
        }

        @Override
        public void showError(String text) {
            errorText = text;
        }

        public void commit() {
            commitAction.run();
        }

        public void discard() {
            discardAction.run();
        }

        public void clearError() {
            errorText = null;
        }

        public boolean isShowingError() {
            return errorText != null;
        }
    }

    private final FakeDialogView fakeView = new FakeDialogView();

    @Test
    public void presenterSetsUpActions() {
        FilterDialogPresenter.create(fakeView);

        assertNotNull(fakeView.commitAction);
        assertNotNull(fakeView.discardAction);
    }

    @Test
    public void presenterSetsUpActionsIfFilterGiven() {
        FilterFromDialog showFilter = new FilterFromDialog()
                .setTags(Collections.singletonList("TAG"))
                .setMode(FilteringMode.SHOW);

        FilterDialogPresenter.create(fakeView, showFilter);

        assertNotNull(fakeView.commitAction);
        assertNotNull(fakeView.discardAction);
    }

    @Test
    public void presenterSetsUpViewIfShowFilterGiven() {
        FilterFromDialog showFilter = new FilterFromDialog()
                .setTags(Arrays.asList("TAG", "/[Oo]ther/"))
                .setMessagePattern("/[Ee]xception/")
                .setApps(Arrays.asList("com.example.app", "/org\\.example\\..+/"))
                .setPids(Arrays.asList(1, 2, 100500))
                .setPriority(LogRecord.Priority.ERROR)
                .setMode(FilteringMode.SHOW);

        FilterDialogPresenter.create(fakeView, showFilter);

        assertEquals("TAG, /[Oo]ther/", fakeView.getTagsText());
        assertEquals("/[Ee]xception/", fakeView.getMessageText());
        assertEquals("1, 2, 100500, com.example.app, /org\\.example\\..+/", fakeView.getPidsAppsText());
        assertEquals(LogRecord.Priority.ERROR, fakeView.priority);
        assertEquals(FilteringMode.SHOW, fakeView.getMode());
    }

    @Test
    public void presenterSetsUpViewIfHighlightFilterGiven() {
        Color highlightColor = Configuration.ui.highlightColors().get(0);
        FilterFromDialog highlightFilter = new FilterFromDialog()
                .setTags(Arrays.asList("TAG", "/[Oo]ther/"))
                .setHighlightColor(highlightColor)
                .setMode(FilteringMode.HIGHLIGHT);

        FilterDialogPresenter.create(fakeView, highlightFilter);

        assertEquals(highlightColor, fakeView.highlightColor);
        assertEquals(FilteringMode.HIGHLIGHT, fakeView.getMode());
    }

    @Test
    public void presenterShowsDialogWhenShowIsCalled() {
        FilterDialogPresenter.create(fakeView).show();

        assertTrue(fakeView.isShown);
    }

    @Test
    public void presenterHidesDialogWhenItCommits() {
        FilterDialogPresenter presenter = FilterDialogPresenter.create(fakeView);
        presenter.show();
        assumeTrue(fakeView.isShown);

        fakeView.commit();

        assertFalse(fakeView.isShown);
    }

    @Test
    public void presenterHidesDialogWhenItDiscards() {
        FilterDialogPresenter presenter = FilterDialogPresenter.create(fakeView);
        presenter.show();
        assumeTrue(fakeView.isShown);

        fakeView.discard();

        assertFalse(fakeView.isShown);
    }

    @Test
    public void presenterReturnsIncompletePromiseWhenShown() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        assertThat("Expected that promise completes only after commit/discard", promise, notCompleted());
    }

    @Test(expected = IllegalStateException.class)
    public void presenterThrowsIfDialogIsShownAlready() {
        FilterDialogPresenter presenter = FilterDialogPresenter.create(fakeView);

        presenter.show();
        presenter.show();
    }

    @Test
    public void presentersPromiseCompletesWithEmptyIfDiscarded() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.discard();

        assertThat(promise, completedWithResult(equalTo(Optional.empty())));
    }

    @Test
    public void presenterReturnsTagsFilter() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setTagsText("Hello");
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)
                .setTags(Collections.singletonList("Hello"))))));
    }

    @Test
    public void presenterReturnsMultipleTagsFilter() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.HIDE);
        fakeView.setTagsText("Hello,foo,  bar , BAZ,,");
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.HIDE)
                .setTags(Arrays.asList("Hello", "foo", "bar", "BAZ"))))));
    }

    @Test
    public void presenterReturnsMessageFilter() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.WINDOW);
        fakeView.setMessageText("Hello");
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.WINDOW)
                .setMessagePattern("Hello")))));
    }

    @Test
    public void presenterReturnsTrimmedMessageFilter() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.WINDOW);
        fakeView.setMessageText("  with   whitespace\t\t ");
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.WINDOW)
                .setMessagePattern("with   whitespace")))));
    }

    @Test
    public void presenterReturnsPid() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setPidsAppsText(" 12");
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)
                .setPids(Collections.singletonList(12))))));
    }

    @Test
    public void presenterReturnsAppName() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setPidsAppsText(" a12,  ");
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)
                .setApps(Collections.singletonList("a12"))))));
    }

    @Test
    public void presenterReturnsPidsAndAppNames() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setPidsAppsText("com.example, 12 , /[Ff]oo/  , 10");
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)
                .setPids(Arrays.asList(12, 10))
                .setApps(Arrays.asList("com.example", "/[Ff]oo/"))))));
    }

    @Test
    public void presenterReturnsPriority() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setPriority(LogRecord.Priority.ERROR);
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)
                .setPriority(LogRecord.Priority.ERROR)))));
    }

    @Test
    public void presenterReturnsHighlightColor() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        Color highlightColor = Color.CYAN;

        fakeView.setMode(FilteringMode.HIGHLIGHT);
        fakeView.setHighlightColor(highlightColor);
        fakeView.setPriority(LogRecord.Priority.ERROR);
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.HIGHLIGHT)
                .setHighlightColor(highlightColor)
                .setPriority(LogRecord.Priority.ERROR)))));
    }

    @Test
    public void presenterReturnsDefaultFilter() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)))));
    }

    @Test
    public void presenterShowsErrorIfTagsPatternFailsToCompile() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setTagsText("/[unclosed bracket/");
        fakeView.commit();

        assertTrue(fakeView.isShown);
        assertTrue(fakeView.isShowingError());
        assertThat(promise, notCompleted());
    }

    @Test
    public void presenterShowsErrorIfMessagePatternFailsToCompile() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setMessageText("/[unclosed bracket/");
        fakeView.commit();

        assertTrue(fakeView.isShown);
        assertTrue(fakeView.isShowingError());
        assertThat(promise, notCompleted());
    }

    @Test
    public void presenterShowsErrorIfAppPatternFailsToCompile() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setPidsAppsText("/[unclosed bracket/");
        fakeView.commit();

        assertTrue(fakeView.isShown);
        assertTrue(fakeView.isShowingError());
        assertThat(promise, notCompleted());
    }

    @Test
    public void presenterCompletesIfErrorIsFixed() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.setMessageText("/[unclosed bracket/");
        fakeView.commit();

        assumeThat(promise, notCompleted());
        fakeView.clearError();
        fakeView.setMessageText("/[closed bracket]/");

        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)
                .setMessagePattern("/[closed bracket]/")))));
    }

    @Test
    public void presenterHandlesDoubleCommit() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.commit();
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)))));
    }

    @Test
    public void presenterHandlesDoubleDiscard() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.discard();
        fakeView.discard();

        assertThat(promise, completedWithResult(equalTo(Optional.empty())));
    }

    @Test
    public void presenterHandlesCommitThenDiscard() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.commit();
        fakeView.discard();

        assertThat(promise, completedWithResult(equalTo(Optional.of(new FilterFromDialog()
                .setMode(FilteringMode.SHOW)))));
    }

    @Test
    public void presenterHandlesDiscardThenCommit() {
        CompletionStage<Optional<FilterFromDialog>> promise = FilterDialogPresenter.create(fakeView).show();

        fakeView.setMode(FilteringMode.SHOW);
        fakeView.discard();
        fakeView.commit();

        assertThat(promise, completedWithResult(equalTo(Optional.empty())));
    }
}
