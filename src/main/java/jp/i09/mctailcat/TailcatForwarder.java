package jp.i09.mctailcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class TailcatForwarder {

    private static Process process;
    private static int localPort = -1;

    private TailcatForwarder() {}

    public static synchronized int start(String address)
        throws IOException {

        stop();

        if (address == null || !address.startsWith("tc")) {
            throw new IllegalArgumentException(
                "Not a tailcat address"
            );
        }

        File executable = TailcatBinary.extract();

        localPort = findFreePort();

        ProcessBuilder builder = new ProcessBuilder(
            executable.getAbsolutePath(),
            "forward",
            address,
            localPort + ":25565"
        );

        builder.redirectErrorStream(true);

        process = builder.start();

        startOutputReader(process);

        waitForListener(process, localPort);

        return localPort;
    }

    public static synchronized void stop() {
        if (process != null) {
            process.destroy();
            process = null;
        }

        localPort = -1;
    }

    public static synchronized boolean isRunning() {
        return process != null;
    }

    public static synchronized int getLocalPort() {
        return localPort;
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket =
            new ServerSocket(0, 0, null)) {

            return socket.getLocalPort();
        }
    }

    private static void waitForListener(
        Process tailcat,
        int port
    ) throws IOException {

        long deadline =
            System.currentTimeMillis() + 10000L;

        while (System.currentTimeMillis() < deadline) {

            if (!isAlive(tailcat)) {
                throw new IOException(
                    "tailcat exited before opening local port"
                );
            }

            try (Socket socket = new Socket()) {
                socket.connect(
                    new InetSocketAddress(
                        "127.0.0.1",
                        port
                    ),
                    100
                );

                return;

            } catch (IOException ignored) {
                // まだlistenしていない
            }

            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                throw new IOException(
                    "Interrupted while waiting for tailcat",
                    e
                );
            }
        }

        stop();

        throw new IOException(
            "Timed out waiting for tailcat local listener"
        );
    }

    private static boolean isAlive(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    private static void startOutputReader(
        final Process process
    ) {
        Thread thread = new Thread(
            new Runnable() {

                @Override
                public void run() {
                    try (BufferedReader reader =
                        new BufferedReader(
                            new InputStreamReader(
                                process.getInputStream(),
                                StandardCharsets.UTF_8
                            )
                        )) {

                        while (reader.readLine() != null) {
                            // 今は読み捨てる。
                            // 本番ではtc addressをログに出さない。
                        }

                    } catch (IOException ignored) {
                    }
                }
            },
            "MCtailcat-output"
        );

        thread.setDaemon(true);
        thread.start();
    }
}
