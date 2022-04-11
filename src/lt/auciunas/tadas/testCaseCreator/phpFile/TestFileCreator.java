package lt.auciunas.tadas.testCaseCreator.phpFile;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class TestFileCreator {
    private VirtualFile file;
    private boolean isModuleFullyPsr4Compliant = true;

    public TestFileCreator(VirtualFile file) {
        this.file = file;
    }

    public VirtualFile createTestFile() throws IOException {
        return this.getTestFile();
    }

    private VirtualFile getTestFile() throws IOException {
        ArrayList<String> directories = this.getDirectoriesToTestFile(this.file);
        VirtualFile testFileParent = this.createTestDirectories(directories);
        VirtualFile testFile = testFileParent.findChild(this.getTestFileName());
        return testFile == null ? testFileParent.createChildData(this, this.getTestFileName()) : testFile;
    }

    @NotNull
    private ArrayList<String> getDirectoriesToTestFile(VirtualFile file) {
        ArrayList<String> directories = new ArrayList<>();

        VirtualFile parent;
        for(parent = file.getParent(); !this.isDirectorySrc(parent); parent = parent.getParent()) {
            directories.add(parent.getName());
        }

        String moduleName = parent.getParent().getName();
        if (directories.contains(moduleName)) {
            this.isModuleFullyPsr4Compliant = false;
        }

        String defaultTestDirName = this.getDefaultTestDirName(parent);
        directories.add(defaultTestDirName);

        Collections.reverse(directories);

        return directories;
    }

    private String getDefaultTestDirName(VirtualFile parent) {
        return parent.getParent().findChild("tests") != null ? "tests" : "test";
    }

    private boolean isDirectorySrc(VirtualFile parent) {
        return parent.getPath().substring(parent.getPath().length() - 4).equals("/src");
    }

    private VirtualFile createTestDirectories(ArrayList<String> directories) throws IOException {
        VirtualFile parent = this.file;

        for(int i = 0; i <= directories.size(); ++i) {
            parent = parent.getParent();
        }

        return this.createDirectoriesToTestFile(directories, parent);
    }

    @NotNull
    private String getTestFileName() {
        return this.file.getNameWithoutExtension() + "Test.php";
    }

    private VirtualFile createDirectoriesToTestFile(ArrayList<String> directories, VirtualFile parent)
            throws IOException {
        int key = 0;
        for (String item : directories) {
            ++key;
            if (key == 1 && !this.isModuleFullyPsr4Compliant) {
                item += "Test";
            }

            if (parent.findChild(item) == null) {
                parent = parent.createChildDirectory(this, item);
            } else {
                parent = parent.findChild(item);
            }
        }

        return parent;
    }
}
