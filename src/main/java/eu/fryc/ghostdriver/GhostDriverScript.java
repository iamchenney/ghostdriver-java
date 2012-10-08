package eu.fryc.ghostdriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GhostDriverScript {

    private static final String GHOSTDRIVER_PREFIX = "ghostdriver";
    private static final String MAIN_SCRIPT = "main.js";

    private static final File GHOSTDRIVER_TMP_DIR = initializeTempDirectory();

    private static volatile boolean tempWritten = false;

    File getGhostDriver() {
        if (!tempWritten) {
            InputStream inputStream = getResourceStream();
            String destination = GHOSTDRIVER_TMP_DIR.getAbsolutePath();
            unzip(inputStream, destination);
            deleteDirOnExit(GHOSTDRIVER_TMP_DIR);
        }
        return new File(GHOSTDRIVER_TMP_DIR, MAIN_SCRIPT);
    }

    InputStream getResourceStream() {
        return this.getClass().getResourceAsStream("ghostdriver.zip");
    }

    static File initializeTempDirectory() {
        try {
            File dir = Files.createTempDirectory(GHOSTDRIVER_PREFIX).toFile();
            dir.deleteOnExit();
            return dir;
        } catch (IOException e) {
            throw new IllegalStateException("Can't initialize PhantomJS executable", e);
        }
    }

    public static void unzip(InputStream inputStream, String destination) {
        try {
            byte[] buf = new byte[1024];
            ZipInputStream zipinputstream = null;
            ZipEntry zipentry;
            zipinputstream = new ZipInputStream(inputStream);

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                // for each entry to be extracted
                String entryName = destination + "/" + zipentry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);
                int n;
                FileOutputStream fileoutputstream;
                File newFile = new File(entryName);
                if (zipentry.isDirectory()) {
                    if (!newFile.mkdirs()) {
                        break;
                    }
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                fileoutputstream = new FileOutputStream(entryName);

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }

                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }

            zipinputstream.close();
        } catch (Exception e) {
            throw new IllegalStateException("Can't unzip input stream", e);
        }
    }

    private static void deleteDirOnExit(File dir) {
        // call deleteOnExit for the folder first, so it will get deleted last
        dir.deleteOnExit();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirOnExit(f);
                } else {
                    f.deleteOnExit();
                }
            }
        }
    }
}
