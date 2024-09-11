/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.presenter.api.presenter.state;

import static java.util.Objects.nonNull;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.presenter.RemindRecordingPresenter;
import org.lecturestudio.presenter.api.presenter.command.CloseablePresenterCommand;
import org.lecturestudio.presenter.api.view.ToolbarView;

public class RecordNotifyState {

	private final ToolbarView view;

	private final PresenterConfiguration config;

	private final EventBus eventBus;

	private ExecutableState recordingState;

	private Page page;

	private boolean pageChanged;

	private boolean shapeAdded;

	private boolean userDeclined;


	public RecordNotifyState(ApplicationContext context, ToolbarView toolbarView) {
		view = toolbarView;
		config = (PresenterConfiguration) context.getConfiguration();
		eventBus = context.getEventBus();
	}

	public void setShape() {
		shapeAdded = true;

		if (notifyState()) {
			view.showRecordNotification(notifyState());

			showRecordNotification();
		}
	}

	public void setPage(Page page) {
		if (nonNull(this.page) && this.page != page) {
			pageChanged = true;
		}

		this.page = page;

		if (notifyState()) {
			view.showRecordNotification(notifyState());

			showRecordNotification();
		}
	}

	public void setRecordingState(ExecutableState state) {
		this.recordingState = state;

		switch (recordingState) {
			case Suspended:
			case Stopped:
				resetState();
				break;
		}

		view.showRecordNotification(notifyState());
	}

	private boolean notifyState() {
		if (!config.getNotifyToRecord() || userDeclined) {
			return false;
		}
		return (shapeAdded || pageChanged)
				&& recordingState != ExecutableState.Started;
	}

	private void showRecordNotification() {
		eventBus.post(new CloseablePresenterCommand<>(
				RemindRecordingPresenter.class, () -> {
			// User declined, so do not ask again.
			userDeclined = true;
		}));
	}

	private void resetState() {
		pageChanged = false;
		shapeAdded = false;
		userDeclined = false;
	}
}
