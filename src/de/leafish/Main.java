package de.leafish;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class Main {

    // https://bugs.mojang.com/browse/MCL-23639

    private static final String BOOTSTRAP_PATH = "versions/Leafish/bootstrap";
    private static final String UPDATE_PATH = "versions/Leafish/bootstrap_new";
    private static final String BOOTSTRAP_HOME_DIR = "./versions/Leafish/";

    public static void main(String[] args) {
        try {
            // try starting the bootstrap twice as it might have downloaded an update the first time it was started,
            // so we are always running the latest bootstrap available
            startBootstrap(args);
            startBootstrap(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void startBootstrap(String[] command) throws Exception {
        File out = new File("./" + BOOTSTRAP_PATH);
        File updated = new File("./" + UPDATE_PATH);
        if (!out.exists()) {
            InputStream stream = Main.class.getResourceAsStream("/bootstrap");
            if (stream == null) {
                throw new RuntimeException("Failed extracting bootstrap binary from wrapper jar");
            }
            java.nio.file.Files.copy(
                    stream,
                    out.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            adjustPerms();
        } else if (updated.exists()) {
            Files.copy(updated.toPath(), out.toPath());
            adjustPerms(); // FIXME: is this needed?
        }

        File bootstrapHome = new File(BOOTSTRAP_HOME_DIR);

        Process proc = new ProcessBuilder(command).directory(bootstrapHome).redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT).start();
        proc.waitFor();
    }

    private static void adjustPerms() throws Exception {
        // FIXME: does MAC also need perms?
        OperatingSystem os = detectOperatingSystem();
        if (os == OperatingSystem.LINUX) {
            // try giving us execute perms
            Runtime.getRuntime().exec("chmod 777 " + BOOTSTRAP_PATH).waitFor();
        }
    }

    public enum OperatingSystem {
        WINDOWS, LINUX, MAC, UNKNOWN
    }

    public static OperatingSystem detectOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        // Return the corresponding enum based on the operating system
        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("nux")) {
            return OperatingSystem.LINUX;
        } else if (osName.contains("mac")) {
            return OperatingSystem.MAC;
        } else {
            return OperatingSystem.UNKNOWN;
        }
    }

}
