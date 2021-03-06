/*
 * Copyright 2020 Reshift Security Intellij plugin contributors
 *
 * This file is part of Reshift Security Intellij plugin.
 *
 * Reshift Security Intellij plugin is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Reshift Security Intellij plugin is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Reshift Security Intellij plugin.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.reshiftsecurity.plugins.intellij.gui.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class DetectorGroupNode extends AbstractDetectorNode {

	DetectorGroupNode(@NotNull final String text) {
		super(text);
	}

	@Override
	boolean isGroup() {
		return true;
	}

	@Nullable
	@Override
	Boolean getEnabled() {
		boolean allEnabled = true;
		boolean allDisabled = true;
		for (int i = 0; i < getChildCount(); i++) {
			final AbstractDetectorNode child = (AbstractDetectorNode) getChildAt(i);
			final Boolean enabled = child.getEnabled();
			if (enabled == null) {
				return null;
			}
			if (enabled) {
				allDisabled = false;
			} else {
				allEnabled = false;
			}
		}
		if (allEnabled) {
			return true;
		} else if (allDisabled) {
			return false;
		}
		return null;
	}

	@Override
	void setEnabled(@Nullable final Boolean enabled) {
		for (int i = 0; i < getChildCount(); i++) {
			final AbstractDetectorNode child = (AbstractDetectorNode) getChildAt(i);
			child.setEnabled(enabled);
		}
	}
}
