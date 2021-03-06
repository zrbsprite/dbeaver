/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2011-2012 Eugene Fradkin (eugene.fradkin@gmail.com)
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
package org.jkiss.dbeaver.ext.oracle.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.dbeaver.ext.oracle.OracleMessages;
import org.jkiss.dbeaver.ext.oracle.model.session.OracleServerSessionManager;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSession;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManager;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIIcon;
import org.jkiss.dbeaver.ui.dialogs.ConfirmationDialog;
import org.jkiss.dbeaver.ui.views.session.AbstractSessionEditor;
import org.jkiss.dbeaver.ui.views.session.SessionManagerViewer;

import java.util.HashMap;
import java.util.Map;

/**
 * OracleSessionEditor
 */
public class OracleSessionEditor extends AbstractSessionEditor
{
    private DisconnectSessionAction killSessionAction;
    private DisconnectSessionAction disconnectSessionAction;

    @Override
    public void createEditorControl(Composite parent) {
        killSessionAction = new DisconnectSessionAction(true);
        disconnectSessionAction = new DisconnectSessionAction(false);
        super.createEditorControl(parent);
    }

    @Override
    protected SessionManagerViewer createSessionViewer(DBCExecutionContext executionContext, Composite parent) {
        return new SessionManagerViewer(this, parent, new OracleServerSessionManager(getExecutionContext())) {
            @Override
            protected void contributeToToolbar(DBAServerSessionManager sessionManager, IContributionManager contributionManager)
            {
                contributionManager.add(killSessionAction);
                contributionManager.add(disconnectSessionAction);
                contributionManager.add(new Separator());
            }

            @Override
            protected void onSessionSelect(DBAServerSession session)
            {
                super.onSessionSelect(session);
                killSessionAction.setEnabled(session != null);
                disconnectSessionAction.setEnabled(session != null);
            }
        };
    }

    private class DisconnectSessionAction extends Action {
        private final boolean kill;
        public DisconnectSessionAction(boolean kill)
        {
            super(
                kill ? OracleMessages.editors_oracle_session_editor_title_kill_session : OracleMessages.editors_oracle_session_editor_title_disconnect_session,
                DBeaverIcons.getImageDescriptor(kill ? UIIcon.REJECT : UIIcon.SQL_DISCONNECT));
            this.kill = kill;
        }

        @Override
        public void run()
        {
            final DBAServerSession session = getSessionsViewer().getSelectedSession();
            final String action = (kill ? OracleMessages.editors_oracle_session_editor_action_kill : OracleMessages.editors_oracle_session_editor_action_disconnect) + OracleMessages.editors_oracle_session_editor_action__session;
            ConfirmationDialog dialog = new ConfirmationDialog(
                getSite().getShell(),
                action,
                null,
                NLS.bind(OracleMessages.editors_oracle_session_editor_confirm_action, action.toLowerCase(), session),
                MessageDialog.CONFIRM,
                new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
                0,
                OracleMessages.editors_oracle_session_editor_confirm_title,
                false);
            if (dialog.open() == IDialogConstants.YES_ID) {
                Map<String, Object> options = new HashMap<>();
                if (kill) {
                    options.put(OracleServerSessionManager.PROP_KILL_SESSION, kill);
                }
                if (dialog.getToggleState()) {
                    options.put(OracleServerSessionManager.PROP_IMMEDIATE, true);
                }
                getSessionsViewer().alterSession(session, options);
            }
        }
    }

}