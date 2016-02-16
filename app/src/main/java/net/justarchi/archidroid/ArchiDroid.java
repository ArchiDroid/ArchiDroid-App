package net.justarchi.archidroid;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import eu.chainfire.libsuperuser.Debug;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by justa on 16.02.2016.
 */
public final class ArchiDroid {
    private static final String ARCHIDROID_INIT = "/system/xbin/ARCHIDROID_INIT";

    private static String archidroidDir = null;
    private static String archidroidEventsDir = null;
    private static String archidroidTempDir = null;

    private static File lastEventFile = null;

    private static Boolean isShellUsable = false;
    private static Shell.Interactive shell = null;

    public static final void onEvent(final String event) {
        if (TextUtils.isEmpty(event)) {
            return;
        }

        if (lastEventFile != null && lastEventFile.isFile()) {
            Logging.logGenericError(lastEventFile.getAbsolutePath() + " already exists and nobody cares!");
            return;
        }

        if (archidroidEventsDir == null || archidroidTempDir == null) {
            return;
        }

        final File mArchidroidEventsDir = new File(archidroidEventsDir);
        if (!mArchidroidEventsDir.isDirectory()) {
            Logging.logGenericError(archidroidEventsDir + " doesn't exist!");
            return;
        }

        final File mArchidroidTempDir = new File(archidroidTempDir);
        if (!mArchidroidTempDir.isDirectory()) {
            Logging.logGenericError(archidroidTempDir + " doesn't exist!");
            return;
        }

        final File tempFile = createTempFile(mArchidroidTempDir, ".EVENT", event);
        if (tempFile == null) {
            Logging.logNullError("tempFile");
            return;
        }

        final File eventFile = new File(archidroidEventsDir + "/" + tempFile.getName());
        if (!tempFile.renameTo(eventFile)) {
            Logging.logGenericError("couldn't rename " + tempFile.getAbsolutePath() + " to " + eventFile.getAbsolutePath() + "!");
            tempFile.delete();
            return;
        }

        lastEventFile = eventFile;
    }


    public static final void init(Context context) {
        if (context == null) {
            Logging.logNullError("context");
            return;
        }

        if (archidroidDir != null) {
            return;
        }

        archidroidDir = context.getExternalFilesDir(null).getAbsolutePath();
        new File(archidroidDir).mkdirs();

        archidroidEventsDir = archidroidDir + File.separator + "Events";
        final File mArchidroidEventsDir = new File(archidroidEventsDir);
        mArchidroidEventsDir.mkdirs();
        for (final File file : mArchidroidEventsDir.listFiles()) {
            file.delete();
        }


        archidroidTempDir = archidroidDir + File.separator + "Temp";
        new File(archidroidTempDir).mkdirs();

        Debug.setDebug(true); // Remove me later, enabled for now to catch issues
    }

    public static final void initBackend() {
        try {
            new InitShell().execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (!isShellUsable) {
            Logging.logGenericError("No SU shell usable");
            return;
        }

        File archidroidInit = new File(ARCHIDROID_INIT);
        if (!archidroidInit.isFile()) {
            Logging.logGenericError(ARCHIDROID_INIT + " doesn't exist!");
            return;
        }

        shell.addCommand(ARCHIDROID_INIT + " --background --su-shell &");
    }

    private static final void initShell() {
        if (isShellUsable && shell != null) {
            return;
        }

        shell = new Shell.Builder().useSU().open(new Shell.OnCommandResultListener() {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                isShellUsable = exitCode == 0;
            }});

        shell.waitForIdle();
        if (!isShellUsable) {
            Logging.logGenericError("Failed to spawn SU shell");
            shell.close();
            shell = null;
        }
    }

    private static final File createTempFile(final File directory, final String extension, final String content) {
        if (directory == null || !directory.isDirectory() || TextUtils.isEmpty(extension) || TextUtils.isEmpty(content)) {
            return null;
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile("ArchiDroid", extension, directory);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (!writeFile(tempFile, content)) {
            tempFile.delete();
            return null;
        }

        return tempFile;
    }

    private static final boolean writeFile(final File file, final String content) {
        if (file == null || !file.isFile() || TextUtils.isEmpty(content)) {
            return false;
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(content);
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static final class InitShell extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (isShellUsable && shell != null) {
                return null;
            }

            shell = new Shell.Builder().useSU().open(new Shell.OnCommandResultListener() {
                @Override
                public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                    isShellUsable = exitCode == 0;
                }});

            shell.waitForIdle();
            if (!isShellUsable) {
                Logging.logGenericError("Failed to spawn SU shell");
                shell.close();
                shell = null;
            }
            return null;
        }
    }
}
