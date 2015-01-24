/*
 * Copyright 2015 Mikhail Lopatkin
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

package org.bitbucket.mlopatkin.android.logviewer.filters;

import com.google.common.base.Optional;

import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.logviewer.FilterChain;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.CreateFilterDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.EditFilterDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterdialog.FilterFromDialog;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterCreator;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.FilterPanelModel;
import org.bitbucket.mlopatkin.android.logviewer.ui.filterpanel.PanelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.logtable.LogModelFilter;
import org.bitbucket.mlopatkin.android.logviewer.ui.mainframe.DialogFactory;
import org.bitbucket.mlopatkin.utils.events.Observable;
import org.bitbucket.mlopatkin.utils.events.Subject;

import java.awt.Color;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The filter controller of the main window.
 */
@Singleton
public class MainFilterController implements LogModelFilter, FilterCreator {

    private final FilterPanelModel filterPanelModel;
    private final DialogFactory dialogFactory;
    private final Subject<LogModelFilter.Observer> observers = new Subject<>();
    private final FilterChain filterChain = new FilterChain();

    @Inject
    public MainFilterController(FilterPanelModel filterPanelModel, DialogFactory dialogFactory) {
        this.filterPanelModel = filterPanelModel;
        this.dialogFactory = dialogFactory;
    }


    @Override
    public boolean shouldShowRecord(LogRecord record) {
        return true;
    }

    @Nullable
    @Override
    public Color getHighlightColor(LogRecord record) {
        return null;
    }

    @Override
    public Observable<LogModelFilter.Observer> asObservable() {
        return observers.asObservable();
    }

    @Override
    public void createFilterWithDialog() {
        CreateFilterDialog.startCreateFilterDialog(
                dialogFactory.getOwner(), new CreateFilterDialog.DialogResultReceiver() {
                    @Override
                    public void onDialogResult(CreateFilterDialog result,
                                               boolean success) {
                        if (success) {
                            addNewDialogFilter(result.createFilter());
                        }
                    }
                });
    }

    private void addNewDialogFilter(final FilterFromDialog filter) {
        filterPanelModel.addFilter(convertDialogFilterToPanelFilter(filter));
    }

    private PanelFilter convertDialogFilterToPanelFilter(final FilterFromDialog filter) {
        return new PanelFilter() {
            @Override
            public void setEnabled(boolean enabled) {
            }

            @Override
            public void openFilterEditor() {
                final PanelFilter original = this;
                EditFilterDialog.startEditFilterDialog(
                        dialogFactory.getOwner(), filter,
                        new EditFilterDialog.DialogResultReceiver() {
                            @Override
                            public void onDialogResult(FilterFromDialog oldFilter,
                                                       Optional<FilterFromDialog>
                                                               newFilter,
                                                       boolean success) {
                                if (success) {
                                    filterPanelModel
                                            .replaceFilter(original, convertDialogFilterToPanelFilter(newFilter.get()));
                                }
                            }
                        });
            }

            @Override
            public void delete() {
            }

            @Override
            public String getTooltip() {
                return filter.getTooltip();
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
    }
}
