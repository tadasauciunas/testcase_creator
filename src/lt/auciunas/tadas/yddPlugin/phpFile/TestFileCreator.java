package lt.auciunas.tadas.yddPlugin.phpFile;

import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.yddPlugin.exceptions.FileNotSupportedException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class TestFileCreator {

    private VirtualFile file;
    private String testNameSpace;

    public TestFileCreator(VirtualFile file) {
        this.file = file;
    }

    public VirtualFile createTestFile() throws FileNotSupportedException, IOException {
        return getTestFile();
    }

    private VirtualFile getTestFile() throws IOException, FileNotSupportedException {
        ArrayList<String> directories = getDirectoriesToTestFile(this.file);

        VirtualFile testFileParent = createTestDirectories(directories);
        VirtualFile testFile = testFileParent.findChild(getTestFileName());

        return testFile == null ?
                testFileParent.createChildData(this, getTestFileName()) :
                testFile;
    }

    @NotNull
    private ArrayList<String> getDirectoriesToTestFile(VirtualFile file) {
        ArrayList<String> directories = new ArrayList<>();

        VirtualFile parent = file.getParent();

        while (!isDirectorySrc(parent)) {
            directories.add(parent.getName());
            parent = parent.getParent();
        }

//        directories.remove(directories.size() - 1); //todo this was used earlier. is it ok?

        String defaultTestDirName = getDefaultTestDirName(parent);
        directories.add(defaultTestDirName);

        Collections.reverse(directories);

        return directories;
    }

    private String getDefaultTestDirName(VirtualFile parent) {
        if (parent.getParent().findChild("test") != null) {
            return "test";
        }

        return "tests";
    }

    private boolean isDirectorySrc(VirtualFile parent) {
        return parent.getPath().substring(parent.getPath().length() - 4, parent.getPath().length()).equals("/src");
    }

    private VirtualFile createTestDirectories(ArrayList<String> directories)
            throws IOException, FileNotSupportedException {

        VirtualFile parent = this.file;
        for (int i = 0; i <= directories.size(); i++) {
            parent = parent.getParent();
        }

        return createDirectoriesToTestFile(directories, parent);
    }

    @NotNull
    private String getTestFileName() {
        return this.file.getNameWithoutExtension() + "Test.php";
    }

    private VirtualFile createDirectoriesToTestFile(ArrayList<String> directories, VirtualFile parent)
            throws IOException {
        int key = 0;
        for (String item : directories) {
            if (key == 1) {
                item = item + "Test";
//                this.testNameSpace = item;
            }

            if (parent.findChild(item) == null) {
                parent = parent.createChildDirectory(this, item);
            } else {
                parent = parent.findChild(item);
            }

            key++;
        }

        return parent;
    }
}
