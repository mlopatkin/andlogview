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

package name.mlopatkin.andlogview.parsers.dumpstate;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProcessWaitChannelsParserTest {
    @ParameterizedTest
    @CsvSource(value = {
            "1       init                             SyS_epoll_wait,1,init",
            "12      init                             SyS_epoll_wait,12,init",
            "123     init                             SyS_epoll_wait,123,init",
            "1234    init                             SyS_epoll_wait,1234,init",
            "12345   init                             SyS_epoll_wait,12345,init",
            "123456  init                             SyS_epoll_wait,123456,init",
            "1234567 init                             SyS_epoll_wait,1234567,init",
            "12345678 init                            SyS_epoll_wait,12345678,init",
            "1       init                             0,1,init",
            "12      init                             0,12,init",
            "123     init                             0,123,init",
            "1234    init                             0,1234,init",
            "12345   init                             0,12345,init",
            "123456  init                             0,123456,init",
            "1234567 init                             0,1234567,init",
            "12345678 init                            0,12345678,init",
            "1       init                             ,1,init",
            "12      init                             ,12,init",
            "123     init                             ,123,init",
            "1234    init                             ,1234,init",
            "12345   init                             ,12345,init",
            "123456  init                             ,123456,init",
            "1234567 init                             ,1234567,init",
            "12345678 init                            ,12345678,init",
            "1       com.google.android.apps.maps:FriendService SyS_epoll_wait"
                    + ",1,com.google.android.apps.maps:FriendService",
            "12      com.google.android.apps.maps:FriendService SyS_epoll_wait"
                    + ",12,com.google.android.apps.maps:FriendService",
            "123     com.google.android.apps.maps:FriendService SyS_epoll_wait"
                    + ",123,com.google.android.apps.maps:FriendService",
            "1234    com.google.android.apps.maps:FriendService SyS_epoll_wait"
                    + ",1234,com.google.android.apps.maps:FriendService",
            "12345   com.google.android.apps.maps:FriendService SyS_epoll_wait"
                    + ",12345,com.google.android.apps.maps:FriendService",
            "123456  com.google.android.apps.maps:FriendService SyS_epoll_wait"
                    + ",123456,com.google.android.apps.maps:FriendService",
            "1234567 com.google.android.apps.maps:FriendService SyS_epoll_wait"
                    + ",1234567,com.google.android.apps.maps:FriendService",
            "12345678 com.google.android.apps.maps:FriendService SyS_epoll_wait"
                    + ",12345678,com.google.android.apps.maps:FriendService",
            "1       com.google.android.apps.maps:FriendService 0"
                    + ",1,com.google.android.apps.maps:FriendService",
            "12      com.google.android.apps.maps:FriendService 0"
                    + ",12,com.google.android.apps.maps:FriendService",
            "123     com.google.android.apps.maps:FriendService 0"
                    + ",123,com.google.android.apps.maps:FriendService",
            "1234    com.google.android.apps.maps:FriendService 0"
                    + ",1234,com.google.android.apps.maps:FriendService",
            "12345   com.google.android.apps.maps:FriendService 0"
                    + ",12345,com.google.android.apps.maps:FriendService",
            "123456  com.google.android.apps.maps:FriendService 0"
                    + ",123456,com.google.android.apps.maps:FriendService",
            "1234567 com.google.android.apps.maps:FriendService 0"
                    + ",1234567,com.google.android.apps.maps:FriendService",
            "12345678 com.google.android.apps.maps:FriendService 0"
                    + ",12345678,com.google.android.apps.maps:FriendService",
            "1       com.google.android.apps.maps:FriendService "
                    + ",1,com.google.android.apps.maps:FriendService",
            "12      com.google.android.apps.maps:FriendService "
                    + ",12,com.google.android.apps.maps:FriendService",
            "123     com.google.android.apps.maps:FriendService "
                    + ",123,com.google.android.apps.maps:FriendService",
            "1234    com.google.android.apps.maps:FriendService "
                    + ",1234,com.google.android.apps.maps:FriendService",
            "12345   com.google.android.apps.maps:FriendService "
                    + ",12345,com.google.android.apps.maps:FriendService",
            "123456  com.google.android.apps.maps:FriendService "
                    + ",123456,com.google.android.apps.maps:FriendService",
            "1234567 com.google.android.apps.maps:FriendService "
                    + ",1234567,com.google.android.apps.maps:FriendService",
            "12345678 com.google.android.apps.maps:FriendService "
                    + ",12345678,com.google.android.apps.maps:FriendService",
            "1       shell srvc 4257               poll_schedule_timeout,1,shell srvc 4257",
            "12      shell srvc 4257               poll_schedule_timeout,12,shell srvc 4257",
            "123     shell srvc 4257               poll_schedule_timeout,123,shell srvc 4257",
            "1234    shell srvc 4257               poll_schedule_timeout,1234,shell srvc 4257",
            "12345   shell srvc 4257               poll_schedule_timeout,12345,shell srvc 4257",
            "123456  shell srvc 4257               poll_schedule_timeout,123456,shell srvc 4257",
            "1234567 shell srvc 4257               poll_schedule_timeout,1234567,shell srvc 4257",
            "12345678 shell srvc 4257               poll_schedule_timeout,12345678,shell srvc 4257",
    }, ignoreLeadingAndTrailingWhitespace = false)
    void canParseProcessLine(String line, int pid, String name) {
        ProcessEventsHandler handler = mock();
        parseLine(line, handler);

        verify(handler).process(pid, name);
        verify(handler).documentEnded();
        verifyNoMoreInteractions(handler);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1          init                          SyS_epoll_wait,1,init",
            "12         init                          SyS_epoll_wait,12,init",
            "123        init                          SyS_epoll_wait,123,init",
            "1234       init                          SyS_epoll_wait,1234,init",
            "12345      init                          SyS_epoll_wait,12345,init",
            "123456     init                          SyS_epoll_wait,123456,init",
            "1234567    init                          SyS_epoll_wait,1234567,init",
            "12345678    init                         SyS_epoll_wait,12345678,init",
            "1          init                          0,1,init",
            "12         init                          0,12,init",
            "123        init                          0,123,init",
            "1234       init                          0,1234,init",
            "12345      init                          0,12345,init",
            "123456     init                          0,123456,init",
            "1234567    init                          0,1234567,init",
            "12345678    init                         0,12345678,init",
            "1          init                          ,1,init",
            "12         init                          ,12,init",
            "123        init                          ,123,init",
            "1234       init                          ,1234,init",
            "12345      init                          ,12345,init",
            "123456     init                          ,123456,init",
            "1234567    init                          ,1234567,init",
            "12345678    init                         ,12345678,init",
            "1          shell srvc 4257               poll_schedule_timeout,1,shell srvc 4257",
            "12         shell srvc 4257               poll_schedule_timeout,12,shell srvc 4257",
            "123        shell srvc 4257               poll_schedule_timeout,123,shell srvc 4257",
            "1234       shell srvc 4257               poll_schedule_timeout,1234,shell srvc 4257",
            "12345      shell srvc 4257               poll_schedule_timeout,12345,shell srvc 4257",
            "123456     shell srvc 4257               poll_schedule_timeout,123456,shell srvc 4257",
            "1234567    shell srvc 4257               poll_schedule_timeout,1234567,shell srvc 4257",
            "12345678    shell srvc 4257               poll_schedule_timeout,12345678,shell srvc 4257",
    }, ignoreLeadingAndTrailingWhitespace = false)
    void canParseThreadLine(String line, int tid, String name) {
        ProcessEventsHandler handler = mock();
        parseLine(line, handler);

        verify(handler).thread(tid, name);
        verify(handler).documentEnded();
        verifyNoMoreInteractions(handler);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1                                       SyS_epoll_wait,1",
            "12                                      SyS_epoll_wait,12",
            "123                                     SyS_epoll_wait,123",
            "1234                                    SyS_epoll_wait,1234",
            "12345                                   SyS_epoll_wait,12345",
            "123456                                  SyS_epoll_wait,123456",
            "1234567                                 SyS_epoll_wait,1234567",
            "12345678                                SyS_epoll_wait,12345678",
            "1                                       0,1",
            "12                                      0,12",
            "123                                     0,123",
            "1234                                    0,1234",
            "12345                                   0,12345",
            "123456                                  0,123456",
            "1234567                                 0,1234567",
            "12345678                                0,12345678",
            "1                                       ,1",
            "12                                      ,12",
            "123                                     ,123",
            "1234                                    ,1234",
            "12345                                   ,12345",
            "123456                                  ,123456",
            "1234567                                 ,1234567",
            "12345678                                ,12345678",
    }, ignoreLeadingAndTrailingWhitespace = false)
    void canParseUnnamedKernelThread(String line, int tid) {
        ProcessEventsHandler handler = mock();
        parseLine(line, handler);

        verify(handler).unknownKernelThread(tid);
        verify(handler).documentEnded();
        verifyNoMoreInteractions(handler);
    }

    @Test
    void skipsUnparseableLine() {
        var line = "------ 0.020s was the duration of 'for_each_tid(BLOCKED PROCESS WAIT-CHANNELS)' ------";
        ProcessEventsHandler handler = mock();
        parseLine(line, handler);

        verify(handler).unparseableLine(line);
        verify(handler).documentEnded();
        verifyNoMoreInteractions(handler);
    }

    private void parseLine(String line, ProcessEventsHandler handler) {
        try (var parser = createParser(handler)) {
            if (!parser.nextLine(line)) {
                fail("Expected parser to parse line");
            }
        }
    }

    private ProcessWaitChannelsParser<?> createParser(ProcessEventsHandler handler) {
        return new ProcessWaitChannelsParser<>(handler);
    }
}
