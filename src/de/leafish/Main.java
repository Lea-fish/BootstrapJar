package de.leafish;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class Main {

    // https://bugs.mojang.com/browse/MCL-23639

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }

        OperatingSystem os = detectOperatingSystem();

        // FIXME: for now just include the Leafish binary in the jar and run it (as a sub process)
        // FIXME: but in the future we would want to check the most recent version on github and
        // FIXME: download it if necessary

        File out = new File("./versions/Leafish/leafish");

        if (!out.exists()) {
            try {
                InputStream stream = Main.class.getResourceAsStream("/leafish");
                if (stream == null) {
                    throw new RuntimeException("Failed extracting leafish binary from wrapper jar");
                }
                java.nio.file.Files.copy(
                        stream,
                        out.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // FIXME: does MAC also need perms?
            if (os == OperatingSystem.LINUX) {
                // try giving us execute perms
                try {
                    Runtime.getRuntime().exec("chmod 777 versions/Leafish/leafish").waitFor();
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        String path = out.getAbsolutePath();

        // FIXME: pass all the (relevant) arguments once that's supported in leafish!
        String[] command = new String[/*args.length + */1];
        command[0] = path;
        // System.arraycopy(args, 0, command, 1, args.length);

        try {
            Process proc = new ProcessBuilder(command)/*.redirectOutput(ProcessBuilder.Redirect.INHERIT)*/.start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        // while(true) {}
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
