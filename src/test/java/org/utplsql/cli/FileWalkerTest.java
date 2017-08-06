package org.utplsql.cli;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by Vinicius on 18/06/2017.
 */
public class FileWalkerTest {

    private final File BASE_DIR = new File(new File("").getAbsolutePath(), "assets/demo_project");

    @Test
    public void fileWalker_Relative() {
        List<String> fileList = new FileWalker().getFileList(BASE_DIR, "source");
        Collections.sort(fileList);
        Assert.assertArrayEquals(new Object[] {
                "source/packages/package.pkb".replace('/', File.separatorChar),
                "source/packages/package.pks".replace('/', File.separatorChar),
                "source/script.sql".replace('/', File.separatorChar),
                "source/triggers/trigger.trg".replace('/', File.separatorChar),
        }, fileList.toArray());
    }

    @Test
    public void fileWalker_Absolute() {
        List<String> fileList = new FileWalker().getFileList(BASE_DIR, "source", false);
        Collections.sort(fileList);
        Assert.assertArrayEquals(new Object[] {
                BASE_DIR.getAbsolutePath() + "/source/packages/package.pkb".replace('/', File.separatorChar),
                BASE_DIR.getAbsolutePath() + "/source/packages/package.pks".replace('/', File.separatorChar),
                BASE_DIR.getAbsolutePath() + "/source/script.sql".replace('/', File.separatorChar),
                BASE_DIR.getAbsolutePath() + "/source/triggers/trigger.trg".replace('/', File.separatorChar),
        }, fileList.toArray());
    }

}
