package org.utplsql.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractFileOutputTest {

    private Set<Path> tempPaths;

    protected void addTempPath(Path path) {
        tempPaths.add(path);
    }

    protected boolean tempPathExists( Path path ) { return tempPaths.contains(path); }

    @BeforeEach
    public void setupTest() {
        tempPaths = new HashSet<>();
    }

    @AfterEach
    public void deleteTempFiles() {
        tempPaths.forEach(p -> deleteDir(p.toFile()));
    }

    void deleteDir(File file) {
        if (file.exists()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteDir(f);
                }
            }
            file.delete();
        }
    }
}
