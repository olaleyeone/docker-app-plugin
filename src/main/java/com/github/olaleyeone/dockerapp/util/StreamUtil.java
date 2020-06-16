package com.github.olaleyeone.dockerapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class StreamUtil {

    public static long transferTo(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0L;

        int read;
        for (byte[] buffer = new byte[8192]; (read = in.read(buffer, 0, 8192)) >= 0; transferred += read) {
            out.write(buffer, 0, read);
        }

        return transferred;
    }
}
