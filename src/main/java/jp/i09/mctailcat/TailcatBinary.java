package jp.i09.mctailcat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import net.minecraft.client.Minecraft;

public final class TailcatBinary {

    private static final String RESOURCE_PATH =
        "/assets/mctailcat/windows/tailcat.exe";

    private TailcatBinary() {}

    public static File extract() throws IOException {
        File gameDir = Minecraft.getMinecraft().mcDataDir;

        File binDir = new File(gameDir, "mctailcat/bin");

        if (!binDir.exists() && !binDir.mkdirs()) {
            throw new IOException(
                "Failed to create directory: " + binDir
            );
        }

        File target = new File(binDir, "tailcat.exe");

        try (InputStream in =
            TailcatBinary.class.getResourceAsStream(RESOURCE_PATH)) {

            if (in == null) {
                throw new IOException(
                    "tailcat.exe not found in resources: "
                        + RESOURCE_PATH
                );
            }

            Files.copy(
                in,
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            );
        }

        return target;
    }
}
