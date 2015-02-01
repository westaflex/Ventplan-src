package com.ventplan.desktop

import com.ezware.dialog.task.CommandLink
import com.ezware.dialog.task.TaskDialog
import com.ezware.dialog.task.TaskDialogs

import javax.swing.*
import java.awt.*

/**
 * Manage and create dialogs.
 */
class DialogController {

    /**
     * Show an error message w/ or w/o stacktrace.
     * @param throwable Exception w/ stacktrace.
     * @return {@link com.ezware.dialog.task.TaskDialog.Command}
     */
    TaskDialog.Command showError(String title, String message, Throwable throwable) {
        TaskDialog dlg = new TaskDialog((Window) app.windowManager.windows.find { it.focused }, 'Fehler');
        dlg.setTitle(title ?: 'Uups...');
        dlg.setInstruction(title ?: 'Uups...');
        dlg.setIcon(TaskDialog.StandardIcon.ERROR);
        dlg.setText(message ?: 'Leider ist ein Fehler aufgetreten. Diese Aktion wurde an den Hersteller berichtet.');
        if (null != throwable) {
            StringBuilder builder = new StringBuilder();
            builder.append(String.format('%s: %s<br/>%n', throwable.getClass().getName(), throwable.getLocalizedMessage()));
            int i = 0;
            for (StackTraceElement ste : throwable.getStackTrace()) {
                if (i++ > 5 && !ste.className.startsWith('org.codehaus.') && !ste.className.startsWith('groovy.') && !ste.className.startsWith('sun.') && !ste.className.startsWith('java')) {
                    builder.append(String.format('  at %s.%s(%s:%d)<br/>%n', ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber()));
                }
            }
            dlg.getDetails().setExpandableComponent(new JScrollPane(new JLabel(String.format('<html>%s</html>', builder.toString()))));
        }
        dlg.setCommands(TaskDialog.StandardCommand.OK.derive("Ja, ich habe das gelesen!"));
        return dlg.show();
    }

    /**
     * Show an informational dialog.
     * @param title Title.
     * @param message Information.
     * @return {@link com.ezware.dialog.task.TaskDialog.Command}
     */
    TaskDialog.Command showInformation(String title, String message) {
        TaskDialog dlg = new TaskDialog((Window) app.windowManager.windows.find { it.focused }, 'Fehler');
        dlg.setIcon(TaskDialog.StandardIcon.INFO);
        dlg.setTitle(title);
        dlg.setInstruction(title);
        dlg.setText(message);
        return dlg.show();
    }

    /**
     * Dialog anzeigen, wenn die Applikation geschlossen werden soll, obwohl
     * noch nicht gespeicherte Projekte vorhanden sind.
     */
    DialogAnswer showApplicationSaveAndCloseDialog() {
        int choice = TaskDialogs.choice((Window) app.windowManager.windows.find { it.focused },
                'Anwendung schliessen?',
                'Die Anwendung enthält ein nicht gespeichertes Projekt.\n\nBitte wählen Sie.',
                1,
                [
                        new CommandLink('Speichern', 'Das Projekt wird gespeichert und Ventplan beendet.'),
                        new CommandLink('Abbrechen', 'Ventplan bleibt geöffnet.'),
                        new CommandLink('Nicht speichern', 'Das Projekt wird nicht gespeichert und Ventplan beendet.')
                ]);
        DialogAnswer answer = null;
        switch (choice) {
            case 0:
                answer = DialogAnswer.SAVE;
                break;
            case 1:
                answer = DialogAnswer.CANCEL;
                break;
            case 2:
                answer = DialogAnswer.DONT_SAVE;
                break;
        }
        return answer;
    }

    /**
     * Dialog anzeigen, wenn die Applikation geschlossen werden soll.
     * Dialog nur für Schliessen bzw. Abbrechen nutzen.
     */
    DialogAnswer showApplicationOnlyCloseDialog() {
        int choice = TaskDialogs.choice((Window) app.windowManager.windows.find { it.focused },
                'Ventplan beenden?',
                'Soll Ventplan beendet werden?',
                1,
                [
                        new CommandLink('Ja, klar!', 'Ventplan wird beendet.'),
                        new CommandLink('Nein, lieber doch nicht.', 'Ventplan bleibt geöffnet.')
                ]);
        DialogAnswer answer = null;
        switch (choice) {
            case 0:
                answer = DialogAnswer.YES;
                break;
            case 1:
                answer = DialogAnswer.NO;
                break;
        }
        return answer;
    }

    /**
     * Dialog anzeigen, wenn ein nicht gespeichertes Projekt geschlossen werden soll.
     * WAC-185: Schliessen in Ok ändern.
     */
    DialogAnswer showCloseProjectDialog() {
        int choice = TaskDialogs.choice((Window) app.windowManager.windows.find { it.focused },
                'Projekt schliessen?',
                'Das Projekt enthält <b>nicht</b> gespeicherte Werte.',
                1,
                [
                        new CommandLink('Speichern', 'Das Projekt wird gespeichert und geschlossen.'),
                        new CommandLink('Abbrechen', 'Das Projekt bleibt geöffnet.'),
                        new CommandLink("Schliessen", 'Das Projekt wird nicht gespeichert und geschlossen.')
                ]);
        DialogAnswer answer = null;
        switch (choice) {
            case 0:
                answer = DialogAnswer.SAVE;
                break;
            case 1:
                answer = DialogAnswer.CANCEL;
                break;
            case 2:
                answer = DialogAnswer.DONT_SAVE;
                break;
        }
        return answer;
    }

}
