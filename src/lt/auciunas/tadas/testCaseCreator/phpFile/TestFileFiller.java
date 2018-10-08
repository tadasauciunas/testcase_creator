package lt.auciunas.tadas.testCaseCreator.phpFile;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.Imports;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedTestFile;

import java.io.IOException;
import java.util.*;

public class TestFileFiller {

    private static final String FOUR_SPACE_TAB = "    ";
    private static final String PHP_FILE_OPEN_TAG = "<?php";

    private VirtualFile createdFile;
    private ParsedTestFile parsedTestFile;
    private Imports existingTestFileImports = new Imports();
    private String testFileContentsAfterSetUp = "";

    public TestFileFiller(VirtualFile createdFile, ParsedTestFile parsedTestFile) {
        this.createdFile = createdFile;
        this.parsedTestFile = parsedTestFile;
    }

    public void clearTestCaseSetUp() {
        int i;
        if (LoadTextUtil.loadText(this.createdFile).toString().equals("")) {
            return; //file was empty and there was nothing to clear
        }

        String[] rows = LoadTextUtil.loadText(this.createdFile).toString().split("\n");

        for (i = 0; i < rows.length; i++) {
            if (rows[i].indexOf("use ") == 0) {
                this.existingTestFileImports.addImport(rows[i]);
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

        for (String item : this.getMergedImports()) {
            content.append(item + "\n");
        }
        content.append("\n");

        content.append(this.parsedTestFile.getTestFileClassDefinition());

        for (String item : this.parsedTestFile.getDependencyDefinitions()) {
            content.append(FOUR_SPACE_TAB + item);
        }
        content.append(FOUR_SPACE_TAB + this.parsedTestFile.getOriginalClassDefinition());

        content.append(FOUR_SPACE_TAB + "protected function setUp(): void\n" + FOUR_SPACE_TAB + "{\n");
        for (String item : this.parsedTestFile.getDependencyInitializations()) {
            content.append(FOUR_SPACE_TAB + FOUR_SPACE_TAB + item);
        }
        content.append(FOUR_SPACE_TAB + FOUR_SPACE_TAB + this.parsedTestFile.getOriginalClassInitialization());
        content.append(FOUR_SPACE_TAB + "}\n");

        String testFileContents = content.toString() + this.testFileContentsAfterSetUp;
        if (this.testFileContentsAfterSetUp.length() == 0) {
            testFileContents += "}";
        }

        testFileContents += "\n";

        this.createdFile.setBinaryContent(testFileContents.getBytes());
    }

    private List<String> getMergedImports() {
        List<String> combined = new ArrayList<>(this.existingTestFileImports.getImports());

        for (String s : this.parsedTestFile.getImports().getImports()) {
            if (!combined.contains(s)) {
                combined.add(s);
            }
        }

        addTestCaseImport(combined);

        return combined;
    }

    private void addTestCaseImport(List<String> combined) {
        boolean testCaseImportExists = false;
        for (String s : combined) {
            if (s.contains("TestCase;")) {
                testCaseImportExists = true;
            }
        }

        if (!testCaseImportExists) {
            combined.add(getTestCaseUsage());
        }
    }

    private String getTestCaseUsage() {
        return "use PHPUnit\\Framework\\TestCase;";
    }
}
