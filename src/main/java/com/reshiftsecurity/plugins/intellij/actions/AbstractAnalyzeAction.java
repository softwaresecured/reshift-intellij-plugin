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
package com.reshiftsecurity.plugins.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.ui.UIUtil;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import com.reshiftsecurity.plugins.intellij.common.EventDispatchThreadHelper;
import com.reshiftsecurity.plugins.intellij.common.PluginConstants;
import com.reshiftsecurity.plugins.intellij.common.util.New;
import com.reshiftsecurity.plugins.intellij.core.AbstractSettings;
import com.reshiftsecurity.plugins.intellij.core.FindBugsState;
import com.reshiftsecurity.plugins.intellij.core.PluginSettings;
import com.reshiftsecurity.plugins.intellij.core.ProjectSettings;
import com.reshiftsecurity.plugins.intellij.gui.common.BalloonTipFactory;
import com.reshiftsecurity.plugins.intellij.gui.settings.ProjectConfigurableImpl;
import com.reshiftsecurity.plugins.intellij.plugins.PluginLoader;
import com.reshiftsecurity.plugins.intellij.resources.ResourcesLoader;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.Map;

abstract class AbstractAnalyzeAction extends AbstractAction {

	@Override
	final void actionPerformedImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	) {

		EventDispatchThreadHelper.checkEDT();

// TODO
//		if (areAllBugCategoriesDisabled(settings)) {
//			showSettingsWarning(project, "analysis.allBugCategoriesDisabled");
//			return;
//		}
//
//		if (areAllDetectorsDisabled(project, projectSettings)) {
//			showSettingsWarning(project, "analysis.allDetectorsDisabled");
//			return;
//		}

		analyze(
				e,
				project,
				toolWindow,
				state
		);
	}

	abstract void analyze(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state
	);

	private static boolean areAllBugCategoriesDisabled(@NotNull final AbstractSettings settings) {
		for (final String category : DetectorFactoryCollection.instance().getBugCategories()) {
			if (!settings.hiddenBugCategory.contains(category)) {
				return false;
			}
		}
		return true;
	}

	private static boolean areAllDetectorsDisabled(
			@NotNull final Project project,
			@NotNull final ProjectSettings projectSettings
	) {

		if (!PluginLoader.isLoaded(project)) {
			// PluginLoader.load is called outside EDT by FindBugsStarter which is nice because
			// loading plugins is not very fast. So we skip the detector check if
			// plugin state is invalid.
			return false;
		}
		final Map<String, Map<String, Boolean>> byPluginId = New.map();
		byPluginId.put(PluginConstants.FINDBUGS_CORE_PLUGIN_ID, projectSettings.detectors);
		for (final PluginSettings pluginSettings : projectSettings.plugins) {
			byPluginId.put(pluginSettings.id, pluginSettings.detectors);
		}
		for (final DetectorFactory detector : DetectorFactoryCollection.instance().getFactories()) {
			if (detector.isReportingDetector()) {
				boolean enabled = detector.isDefaultEnabled();
				final Map<String, Boolean> detectors = byPluginId.get(detector.getPlugin().getPluginId());
				if (detectors != null) {
					final Boolean userEnabled = detectors.get(detector.getShortName());
					if (userEnabled != null) {
						enabled = userEnabled;
					}
				}
				if (enabled) {
					return false;
				}
			}
		}
		return true;
	}

	private static void showSettingsWarning(
			@NotNull final Project project,
			@NotNull @PropertyKey(resourceBundle = ResourcesLoader.BUNDLE) String messageKey
	) {
		BalloonTipFactory.showToolWindowWarnNotifier(project,
				ResourcesLoader.getString(messageKey)
						+ " " + ResourcesLoader.getString("analysis.aborted")
						+ "<br>" + "<a href=edit>" + ResourcesLoader.getString("edit.settings") + "</a>",
				new HyperlinkListener() {
					@Override
					public void hyperlinkUpdate(@NotNull final HyperlinkEvent event) {
						if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
							ProjectConfigurableImpl.show(project);
						}
					}
				});
	}

	static int askIncludeTest(@NotNull final Project project) {
		return Messages.showYesNoCancelDialog(
				project,
				ResourcesLoader.getString("analysis.includeTests.text"),
				StringUtil.capitalizeWords(ResourcesLoader.getString("analysis.includeTests.title"), true),
				UIUtil.getQuestionIcon()
		);
	}
}
