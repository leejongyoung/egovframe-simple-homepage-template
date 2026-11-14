package egovframework.let.utl.fcc.service;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

class EgovFileUploadUtilSizeTest {
    @TempDir Path temp;

    // A request fixture, not a servlet container; production upload and disk I/O are real.
    private MultipartHttpServletRequest request(CountingFile... files) {
        Map<String, MultipartFile> map = new LinkedHashMap<>();
        for (int i = 0; i < files.length; i++) map.put("file" + i, files[i]);
        return (MultipartHttpServletRequest) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{MultipartHttpServletRequest.class},
            (proxy, method, args) -> {
                if (method.getName().equals("getFileNames")) return map.keySet().iterator();
                if (method.getName().equals("getFile")) return map.get(args[0]);
                throw new UnsupportedOperationException(method.getName());
            });
    }

    private void upload(boolean ext, Path dir, long limit, CountingFile... files) {
        MultipartHttpServletRequest request = request(files);
        if (ext) EgovFileUploadUtil.uploadFilesExt(request, dir.toString(), limit, ".txt");
        else EgovFileUploadUtil.uploadFiles(request, dir.toString(), limit);
    }

    private long writtenFiles(Path dir) throws IOException {
        if (!Files.exists(dir)) return 0;
        try (Stream<Path> paths = Files.walk(dir)) {
            return paths.filter(Files::isRegularFile).count();
        }
    }

    @Test void rejectsOversizeBeforeOpeningAnyStream() throws Exception {
        for (boolean ext : new boolean[]{false, true}) {
            CountingFile file = new CountingFile(4);
            Path dir = temp.resolve("single-" + ext);
            assertThrows(SecurityException.class, () -> upload(ext, dir, 3, file));
            assertEquals(0, file.opens);
            assertEquals(0, writtenFiles(dir));
        }
    }

    @Test void rejectsEntireMixedBatchBeforeSavingFirstFile() throws Exception {
        for (boolean ext : new boolean[]{false, true}) {
            CountingFile first = new CountingFile(1), later = new CountingFile(4);
            Path dir = temp.resolve("mixed-" + ext);
            assertThrows(SecurityException.class, () -> upload(ext, dir, 3, first, later));
            assertEquals(0, first.opens);
            assertEquals(0, later.opens);
            assertEquals(0, writtenFiles(dir));
        }
    }

    @Test void acceptsBelowLimitAndEqualLimitWithActualDiskWrites() throws Exception {
        for (boolean ext : new boolean[]{false, true}) {
            CountingFile below = new CountingFile(1), equal = new CountingFile(3);
            Path dir = temp.resolve("boundary-" + ext);
            upload(ext, dir, 3, below, equal);
            assertEquals(1, below.opens);
            assertEquals(1, equal.opens);
            assertEquals(2, writtenFiles(dir));
            try (Stream<Path> paths = Files.walk(dir)) {
                assertEquals(4, paths.filter(Files::isRegularFile)
                    .mapToLong(p -> p.toFile().length()).sum());
            }
        }
    }

    @Test void zeroAndNegativeLimitsRemainUnlimited() throws Exception {
        for (boolean ext : new boolean[]{false, true}) {
            for (long limit : new long[]{0, -1}) {
                CountingFile file = new CountingFile(4);
                Path dir = temp.resolve("unlimited-" + ext + "-" + limit);
                upload(ext, dir, limit, file);
                assertEquals(1, file.opens);
                assertEquals(1, writtenFiles(dir));
            }
        }
    }

    private static class CountingFile implements MultipartFile {
        private final byte[] data;
        int opens;
        CountingFile(int size) { data = new byte[size]; }
        public String getName() { return "file"; }
        public String getOriginalFilename() { return "file.txt"; }
        public String getContentType() { return "text/plain"; }
        public boolean isEmpty() { return data.length == 0; }
        public long getSize() { return data.length; }
        public byte[] getBytes() { return data.clone(); }
        public InputStream getInputStream() { opens++; return new ByteArrayInputStream(data); }
        public void transferTo(File dest) throws IOException { Files.write(dest.toPath(), data); }
    }
}
