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

package name.mlopatkin.andlogview.parsers.dumpstate;

import name.mlopatkin.andlogview.parsers.ParserControl;
import name.mlopatkin.andlogview.parsers.PushParser;
import name.mlopatkin.andlogview.utils.LineParser;
import name.mlopatkin.andlogview.utils.LineParser.State;

import com.google.common.base.CharMatcher;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base parser understands only some aspects of the dumpstate structure and serves as a foundation for the more complex
 * parsers. Despite the name, it isn't intended to be a base class.
 *
 * @param <H> type of a handler
 */
class BaseDumpstatePushParser<H extends BaseDumpstateParseEventsHandler> implements PushParser<H> {
    // Implementation note: functions named "stateXxx" are actual states of the Line Parser. The rest are helpers.

    private final H eventsHandler;
    private final LineParser lineParser;

    private boolean shouldStop;
    private @Nullable String currentSection;
    private @Nullable SectionHandler sectionHandler;

    public BaseDumpstatePushParser(H eventsHandler) {
        this.eventsHandler = eventsHandler;
        State nextState = this::stateSeekDumpstateHeaderTitle;
        this.lineParser = new LineParser(line -> stateSeekDumpstateHeaderBorder(false, nextState, line));
    }

    @Override
    public H getHandler() {
        return eventsHandler;
    }

    @Override
    public boolean nextLine(CharSequence line) {
        if (shouldStop) {
            return false;
        }

        lineParser.nextLine(CharMatcher.whitespace().trimFrom(line));
        return !shouldStop;
    }

    @Override
    public void close() {
        if (shouldStop) {
            return;
        }
        if (currentSection != null) {
            endSection();
        }
        eventsHandler.documentEnded();
    }

    private State stateSeekDumpstateHeaderBorder(boolean endBorder, State nextState, CharSequence line) {
        if (DumpstateElements.isDumpstateHeaderBorder(line)) {
            if (endBorder) {
                handleControl(eventsHandler.header());
            }
            return nextOrStop(nextState);
        }
        return handleUnparseableLine(line);
    }

    private State stateSeekDumpstateHeaderTitle(CharSequence line) {
        if (DumpstateElements.isDumpstateHeaderTitle(line)) {
            State nextState = this::stateSeekSectionStart;
            return l -> stateSeekDumpstateHeaderBorder(true, nextState, l);
        }
        return handleUnparseableLine(line);
    }

    private State stateSeekSectionStart(CharSequence line) {
        assert currentSection == null;
        @Nullable String nextSectionName = DumpstateElements.tryGetSectionName(line);
        if (nextSectionName != null) {
            return handleSectionStart(nextSectionName);
        }
        return handleUnparseableLine(line);
    }

    private State handleSectionStart(String sessionName) {
        assert currentSection == null;
        currentSection = sessionName;
        DumpstateParserControl next = eventsHandler.sectionStarted(currentSection);
        if (next.shouldStop()) {
            shouldStop = true;
            return LineParser.sinkState();
        }
        if (next.shouldSkip()) {
            return this::stateSkipCurrentSection;
        }
        sectionHandler = next.shouldHandleWith();
        return this::stateParseSectionWithHandler;
    }

    private State stateParseSectionWithHandler(CharSequence line) {
        assert sectionHandler != null;
        return handleSectionContents(line);
    }

    private State stateSkipCurrentSection(CharSequence line) {
        assert sectionHandler == null;
        return handleSectionContents(line);
    }

    private State handleSectionContents(CharSequence line) {
        // This is a universal body for both stateParseSectionWithHandler and stateSkipCurrentSection. The former
        // has sectionHandler != null.
        assert currentSection != null;

        if (DumpstateElements.isSectionEnd(line)) {
            return endSection();
        }

        @Nullable String maybeNewSection = DumpstateElements.tryGetSectionName(line);
        if (maybeNewSection != null) {
            endSection();
            if (!shouldStop) {
                // Manually replay the current line into the next state.
                return handleSectionStart(maybeNewSection);
            }
            return LineParser.sinkState();
        }
        if (sectionHandler != null && !handleControl(sectionHandler.nextLine(line))) {
            // The section parsing and maybe the whole parsing is aborted.
            popSectionHandler();
            return nextOrStop(this::stateSkipCurrentSection);
        }
        // Either the sectionHandler consumed the line, and we continue to stay in the section or there is no handler,
        // and we ignore the section content.
        return LineParser.currentState();
    }

    private State handleUnparseableLine(CharSequence line) {
        handleControl(eventsHandler.unparseableLine(line));
        return currentOrStop();
    }

    private void popSectionHandler() {
        if (sectionHandler != null) {
            handleControl(sectionHandler.end());
            sectionHandler = null;
        }
    }

    private State endSection() {
        assert currentSection != null;
        popSectionHandler();
        handleControl(eventsHandler.sectionEnded(currentSection));
        currentSection = null;
        return nextOrStop(this::stateSeekSectionStart);
    }

    private State nextOrStop(State nextState) {
        return shouldStop ? LineParser.sinkState() : nextState;
    }

    private State currentOrStop() {
        return nextOrStop(LineParser.currentState());
    }

    private void handleControl(ParserControl parserControl) {
        shouldStop |= !parserControl.shouldProceed();
    }

    private boolean handleControl(SectionParserControl parserControl) {
        if (parserControl.shouldProceed()) {
            return true;
        }
        shouldStop |= parserControl.shouldStop();
        return false;
    }

}
