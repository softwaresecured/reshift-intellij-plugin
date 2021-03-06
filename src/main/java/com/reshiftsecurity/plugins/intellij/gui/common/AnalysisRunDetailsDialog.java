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
package com.reshiftsecurity.plugins.intellij.gui.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.JBUI;
import com.reshiftsecurity.analytics.AnalyticsAction;
import com.reshiftsecurity.plugins.intellij.common.VersionManager;
import com.reshiftsecurity.plugins.intellij.common.util.New;
import com.reshiftsecurity.plugins.intellij.core.FindBugsProject;
import com.reshiftsecurity.plugins.intellij.core.FindBugsResult;
import com.reshiftsecurity.plugins.intellij.resources.GuiResources;
import com.reshiftsecurity.plugins.intellij.service.AnalyticsService;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;

@SuppressWarnings({"HardcodedFileSeparator"})
public class AnalysisRunDetailsDialog {

	private AnalysisRunDetailsDialog() {
	}

	public static DialogBuilder create(
			final Project project,
			final int bugCount,
			final int numClasses,
			final FindBugsResult result
	) {

		final StringBuilder html = new StringBuilder();
		html.append("<html><body>");
		html.append("<p><h2>").append(VersionManager.getName()).append(": <b>found ").append(bugCount).append(" Security Bugs in ").append(numClasses).append(numClasses > 1 ? " classes" : " class").append("</b>").append("</h2></p>");
		html.append("<p>").append("<font size='10px'>using ").append(VersionManager.getFullVersion()).append("</font>").append("</p>");

		for (final edu.umd.cs.findbugs.Project bugsProject : result.getProjects()) {

			html.append("<p><h2>").append(bugsProject.getProjectName()).append("</h3></p>");

			final List<String> fileList = bugsProject.getFileList();
			final List<String> auxClasspathEntries = bugsProject.getAuxClasspathEntryList();
			List<String> configuredOutputFiles = New.arrayList();
			if (bugsProject instanceof FindBugsProject) {
				configuredOutputFiles = ((FindBugsProject) bugsProject).getConfiguredOutputFiles();
			} // else project was imported from XML

			if (!configuredOutputFiles.isEmpty()) {
				html.append("<p><h3>Configured Output Files/Paths - the analysis entry point").append(" <font color='gray'>(").append(configuredOutputFiles.size()).append(")</h3></p>");
				html.append("<ul>");
				for (final String file : configuredOutputFiles) {
					html.append("<li>");
					html.append(file);
					html.append("</li>");
				}
				html.append("</ul>");
			}

			html.append("<p><h3>Compile Output Path").append(" <font size='9px' color='gray'>(").append("IDEA)</h3></p>");
			html.append("<ul>");
			for (final String resolved : bugsProject.getResolvedSourcePaths()) {
				html.append("<li>");
				html.append(resolved);
				html.append("</li>");
			}
			html.append("</ul>");


			html.append("<p><h3>Sources Dir List ").append(" <font size='9px' color='gray'>(").append(bugsProject.getNumSourceDirs()).append(")</h3></p>");
			html.append("<ul>");
			final List<String> sourceDirList = bugsProject.getSourceDirList();
			for (final String sourceDir : sourceDirList) {
				html.append("<li>");
				html.append(sourceDir);
				html.append("</li>");
			}
			html.append("</ul>");


			html.append("<p><h3>Analyzed Classes List ").append(" <font size='9px' color='gray'>(").append(bugsProject.getFileCount()).append(")</h3></p>");
			html.append("<ul>");
			for (final String file : fileList) {
				html.append("<li>");
				html.append(file);
				html.append("</li>");
			}
			html.append("</ul>");

			html.append("<p><h3>Aux Classpath Entries ").append(" <font size='9px' color='gray'>(").append(bugsProject.getNumAuxClasspathEntries()).append(")</h3></p>");
			html.append("<ul>");
			for (final String auxClasspathEntry : auxClasspathEntries) {
				html.append("<li>");
				html.append(auxClasspathEntry);
				html.append("</li>");
			}
			html.append("</ul>");
		}

		html.append("</html></body>");


		final DialogBuilder dialogBuilder = new DialogBuilder(project);
		dialogBuilder.addCloseButton();
		dialogBuilder.setTitle(StringUtil.capitalizeWords("Reshift analysis details", true));
		final JComponent panel = new JPanel(new BorderLayout());
		panel.setBorder(JBUI.Borders.empty(10));

		final HTMLEditorKit htmlEditorKit = GuiResources.createHtmlEditorKit();
		final JEditorPane jEditorPane = new JEditorPane() {

			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				final Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}

		};
		jEditorPane.setPreferredSize(new Dimension(550, 650));
		jEditorPane.setEditable(false);
		jEditorPane.setContentType("text/html");
		jEditorPane.setEditorKit(htmlEditorKit);


		jEditorPane.setText(html.toString());

		panel.add(ScrollPaneFacade.createScrollPane(jEditorPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		dialogBuilder.setCenterPanel(panel);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jEditorPane.scrollRectToVisible(new Rectangle(0, 0));
			}
		});

		AnalyticsService.getInstance().recordAction(AnalyticsAction.ISSUE_REPORT_MORE_SCAN_INFO);

		return dialogBuilder;
	}
}
