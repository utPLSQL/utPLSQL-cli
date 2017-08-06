package io.github.utplsql.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vinicius on 18/06/2017.
 */
public class FileWalker {

    public List<String> getFileList(File baseDir, String inspectPath) {
        return getFileList(baseDir, inspectPath, true);
    }

    public List<String> getFileList(File baseDir, String inspectPath, boolean relative) {
        File inspectDir = new File(baseDir, inspectPath);

        if (!inspectDir.isDirectory())
            throw new IllegalArgumentException(inspectPath + " is not a directory.");

        List<String> fileList = new ArrayList<>();
        listDirFiles(baseDir, inspectDir, fileList, relative);

        return fileList;
    }

    private void listDirFiles(File baseDir, File directory, List<String> fileList, boolean relative) {
        File[] directoryFiles = directory.listFiles();

        if (directoryFiles == null)
            return;

        for (File file : directoryFiles) {
            if (file.isFile()) {
                String absolutePath = file.getAbsolutePath();

                if (relative)
                    absolutePath = absolutePath.substring(baseDir.getAbsolutePath().length() + 1);

                fileList.add(absolutePath);
            } else {
                listDirFiles(baseDir, file, fileList, relative);
            }
        }
    }

}
