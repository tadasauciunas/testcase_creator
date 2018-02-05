package lt.auciunas.tadas.testCaseCreator.phpFile;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ClearedTestFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedTestFile;

import java.io.IOException;
import java.util.*;

public class TestFileFiller {

    private static final String FOUR_SPACE_TAB = "    ";
    private static final String PHP_FILE_OPEN_TAG = "<?php";

    private VirtualFile createdFile;
    private ParsedTestFile parsedTestFile;
    private ClearedTestFile clearedTestFile = new ClearedTestFile();
    private String testFileContentsAfterSetUp = "";

    public TestFileFiller(VirtualFile createdFile, ParsedTestFile parsedTestFile) {
        this.createdFile = createdFile;
        this.parsedTestFile = parsedTestFile;
    }

    public void clearTestCaseSetUp() {
        Integer i;
        if (LoadTextUtil.loadText(this.createdFile).toString().equals("")) {
            return; //file was empty and there was nothing to clear
        }

        String[] rows = LoadTextUtil.loadText(this.createdFile).toString().split("\n");

        for (i = 0; i < rows.length; i++) {
            if (rows[i].indexOf("use ") == 0) {
                this.clearedTestFile.addUsage(rows[i]);
            }

            if (rows[i].contains(FOUR_SPACE_TAB + "}")) {
                break; //end of setUp() was found
            }
        }

        this.testFileContentsAfterSetUp = String.join("\n", Arrays.copyOfRange(rows, ++i, rows.length));
    }

    public void fillFile() throws IOException {
        StringBuilder content = new StringBuilder();
        content.append(PHP_FILE_OPEN_TAG + "\n\n");

        content.append(this.parsedTestFile.getOriginalNamespace() + "\n\n");

        for (String item : this.getOriginalDependenciesMerged()) {
            content.append(item + "\n");
        }
        content.append("\n");

        content.append(this.parsedTestFile.getTestFileClassDefinition());

        for (String item : this.parsedTestFile.getDependencyDefinitions()) {
            content.append(FOUR_SPACE_TAB + item);
        }
        content.append(FOUR_SPACE_TAB + this.parsedTestFile.getOriginalClassDefinition());

        content.append(FOUR_SPACE_TAB + "protected function setUp()\n" + FOUR_SPACE_TAB + "{\n");
        for (String item : this.parsedTestFile.getDependencyInitializations()) {
            content.append(FOUR_SPACE_TAB + FOUR_SPACE_TAB + item);
        }
        content.append(FOUR_SPACE_TAB + FOUR_SPACE_TAB + this.parsedTestFile.getOriginalClassInitialization());
        content.append(FOUR_SPACE_TAB + "}\n");

        String testFileContents = content.toString() + this.testFileContentsAfterSetUp;
        if (this.testFileContentsAfterSetUp.length() == 0) {
            testFileContents += "}\n";
        }

        this.createdFile.setBinaryContent(testFileContents.getBytes());
    }

    private List<String> getOriginalDependenciesMerged() {
        List<String> combined = new ArrayList<>();
        combined.addAll(this.parsedTestFile.getUsages());
        combined.addAll(this.clearedTestFile.getUsages());

        Set<String> hs = new HashSet<>(combined);
        combined.clear();
        combined.addAll(hs);

        return combined;
    }
}
