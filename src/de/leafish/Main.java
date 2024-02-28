package de.leafish;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public final class Main {

    // https://bugs.mojang.com/browse/MCL-23639

    private static final String BOOTSTRAP_PATH = "versions/Leafish/bootstrap";
    private static final String UPDATE_PATH = "versions/Leafish/bootstrap_new";
    private static final String BOOTSTRAP_HOME_DIR = "./versions/Leafish/";

    public static void main(String[] args) {
        OperatingSystem os = detectOperatingSystem();
        ArrayList<String> command = new ArrayList<>(Arrays.asList(args));
        try {
            File out = new File("./" + BOOTSTRAP_PATH + getExecutableExtension(os));
            command.add(0, out.getAbsolutePath());
            // try starting the bootstrap twice as it might have downloaded an update the first time it was started,
            // so we are always running the latest bootstrap available
            startBootstrap(command);
            command.add("noupdate");
            startBootstrap(command);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void startBootstrap(ArrayList<String> command) throws Exception {
        OperatingSystem os = detectOperatingSystem();
        File out = new File("./" + BOOTSTRAP_PATH + getExecutableExtension(os));
        File updated = new File("./" + UPDATE_PATH + getExecutableExtension(os));
        if (!out.exists()) {
            String fileSuffix = getProcessorArchitecture().name().toLowerCase() + "_" + os.name().toLowerCase() + getExecutableExtension(os);
            InputStream stream = Main.class.getResourceAsStream("/bootstrap_" + fileSuffix);
            if (stream == null) {
                throw new RuntimeException("Failed extracting bootstrap binary from wrapper jar, is your architecture and operating system supported?");
            }
            java.nio.file.Files.copy(
                    stream,
                    out.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            adjustPerms();
        } else if (updated.exists()) {
            Files.copy(updated.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            updated.delete();
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
            Runtime.getRuntime().exec("chmod 777 " + BOOTSTRAP_PATH + getExecutableExtension(os)).waitFor();
        }
    }

    private enum OperatingSystem {
        WINDOWS, LINUX, MACOS, UNKNOWN
    }

    private static OperatingSystem detectOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        // Return the corresponding enum based on the operating system
        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("nux")) {
            return OperatingSystem.LINUX;
        } else if (osName.contains("mac")) {
            return OperatingSystem.MACOS;
        } else {
            return OperatingSystem.UNKNOWN;
        }
    }

    private enum Architecture {
        X86(32),
        X86_64(64),
        ARM(32),
        AARCH64(64),
        UNKNOWN(0);

        private final int bitSize;

        Architecture(int bitSize) {
            this.bitSize = bitSize;
        }

        public int getBitSize() {
            return bitSize;
        }
    }

    private static Architecture getProcessorArchitecture() {
        String osArch = System.getProperty("os.arch").toLowerCase();

        if (osArch.contains("x86_64") || osArch.contains("amd64")) {
            return Architecture.X86_64;
        } else if (osArch.contains("x86") || osArch.contains("i386") || osArch.contains("i486") || osArch.contains("i586") || osArch.contains("i686")) {
            return Architecture.X86;
        } else if (osArch.contains("aarch64")) {
            return Architecture.AARCH64;
        } else if (osArch.contains("arm")) {
            return Architecture.ARM;
        } else {
            return Architecture.UNKNOWN;
        }
    }

    private static String getExecutableExtension(OperatingSystem os) {
        if (os == OperatingSystem.WINDOWS) {
            return ".exe";
        } else {
            return "";
        }
    }

}
