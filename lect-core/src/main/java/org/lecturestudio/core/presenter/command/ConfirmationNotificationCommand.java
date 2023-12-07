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

package org.lecturestudio.core.presenter.command;

import org.lecturestudio.core.presenter.ConfirmationNotificationPresenter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;

public class ConfirmationNotificationCommand extends ShowPresenterCommand<ConfirmationNotificationPresenter> {
	private final NotificationType type;
	private final String title;
	private final String message;
	private final Action confirmAction;
	private final String confirmButtonText;
	private final String closeButtonText;
	private final Action discardAction;


	public ConfirmationNotificationCommand(NotificationType type, String title,
			String message, Action confirmAction, Action discardAction,
			String confirmButtonText, String closeButtonText) {
		super(ConfirmationNotificationPresenter.class);

		this.type = type;
		this.title = title;
		this.message = message;
		this.confirmAction = confirmAction;
		this.discardAction = discardAction;
		this.confirmButtonText = confirmButtonText;
		this.closeButtonText = closeButtonText;
	}

	@Override
	public void execute(ConfirmationNotificationPresenter presenter) {
		presenter.initialize();
		presenter.setNotificationType(type);
		presenter.setTitle(title);
		presenter.setMessage(message);
		presenter.setConfirmationAction(confirmAction);
		presenter.setDiscardAction(discardAction);
		presenter.setConfirmButtonText(confirmButtonText);
		presenter.setDiscardButtonText(closeButtonText);
	}
}
