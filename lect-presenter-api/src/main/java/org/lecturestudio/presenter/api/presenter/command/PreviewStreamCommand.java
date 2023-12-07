/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.presenter.command;

import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.presenter.StreamPreviewPresenter;

public class PreviewStreamCommand extends ShowPresenterCommand<StreamPreviewPresenter> {

	private final Action closeAction;


	public PreviewStreamCommand(Action closeAction) {
		super(StreamPreviewPresenter.class);

		this.closeAction = closeAction;
	}

	@Override
	public void execute(StreamPreviewPresenter presenter) {
		presenter.setOnClose(closeAction);
	}
}
