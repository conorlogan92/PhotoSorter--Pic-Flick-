import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reads standard JPEG EXIF orientation without altering the source file. */
public final class PhotoOrientation {
    private PhotoOrientation() {}

    public static int rotationDegrees(Path path) {
        String name = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        if (!name.endsWith(".jpg") && !name.endsWith(".jpeg")) return 0;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            if (in.readUnsignedShort() != 0xffd8) return 0;
            while (true) {
                int mark;
                do { mark = in.readUnsignedByte(); } while (mark != 0xff);
                do { mark = in.readUnsignedByte(); } while (mark == 0xff);
                if (mark == 0xd9 || mark == 0xda) return 0;
                if (mark == 0x01 || (mark >= 0xd0 && mark <= 0xd7)) continue;
                int length = in.readUnsignedShort() - 2;
                if (length < 0) return 0;
                if (mark == 0xe1 && length >= 14 && length <= 65533) {
                    byte[] data = new byte[length];
                    in.readFully(data);
                    if (data[0] == 'E' && data[1] == 'x' && data[2] == 'i'
                            && data[3] == 'f' && data[4] == 0 && data[5] == 0) {
                        return fromExif(data);
                    }
                } else {
                    in.skipNBytes(length);
                }
            }
        } catch (IOException | RuntimeException ignored) {
            return 0;
        }
    }

    private static int fromExif(byte[] data) {
        int start = 6;
        if (data.length < 14) return 0;
        boolean little = data[start] == 'I' && data[start + 1] == 'I';
        if (!little && !(data[start] == 'M' && data[start + 1] == 'M')) return 0;
        if (u16(data, start + 2, little) != 42) return 0;
        long offset = u32(data, start + 4, little);
        if (offset > data.length - start - 2) return 0;
        int directory = start + (int) offset;
        int count = u16(data, directory, little);
        for (int i = 0; i < count && i < 256; i++) {
            int entry = directory + 2 + i * 12;
            if (entry < 0 || entry > data.length - 12) break;
            if (u16(data, entry, little) == 0x0112 && u16(data, entry + 2, little) == 3
                    && u32(data, entry + 4, little) >= 1) {
                int orientation = u16(data, entry + 8, little);
                // Mirrored orientations need additional flipping: use the
                // rotation component here; the manual controls remain available.
                switch (orientation) {
                    case 3: case 4: return 180;
                    case 5: case 6: return 90;
                    case 7: case 8: return 270;
                    default: return 0;
                }
            }
        }
        return 0;
    }

    private static int u16(byte[] b, int p, boolean little) {
        if (p < 0 || p > b.length - 2) return 0;
        return little ? (b[p] & 255) | ((b[p + 1] & 255) << 8)
                      : ((b[p] & 255) << 8) | (b[p + 1] & 255);
    }
    private static long u32(byte[] b, int p, boolean little) {
        if (p < 0 || p > b.length - 4) return 0;
        return little ? ((long)u16(b,p,little) | ((long)u16(b,p+2,little) << 16))
                      : (((long)u16(b,p,little) << 16) | u16(b,p+2,little));
    }
}
