package lt.auciunas.tadas.yddPlugin.phpFile;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.yddPlugin.phpFile.entity.ParsedTestFile;

import java.io.IOException;
import java.util.Arrays;

public class TestFileFiller {

    private VirtualFile createdFile;
    private ParsedTestFile parsedTestFile;
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
            if (rows[i].contains("    }")) {
                break; //end of setUp() was found
            }
        }

        this.testFileContentsAfterSetUp = String.join("\n", Arrays.copyOfRange(rows, ++i, rows.length));
    }

    public void fillFile() throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("<?php\n\n");

        content.append(this.parsedTestFile.getOriginalNamespace() + "\n\n");

        for (String item : this.parsedTestFile.getUsages()) {
            content.append(item + "\n");
        }
        content.append("\n");

        content.append(this.parsedTestFile.getTestFileClassDefinition());

        for (String item : this.parsedTestFile.getDependencyDefinitions()) {
            content.append("\t" + item);
        }
        content.append("\t" + this.parsedTestFile.getOriginalClassDefinition());

        content.append("\tpublic function setUp()\n    {\n");
        for (String item : this.parsedTestFile.getDependencyInitializations()) {
            content.append("\t\t" + item);
        }
        content.append("\t\t" + this.parsedTestFile.getOriginalClassInitialization());
        content.append("\t}\n");

        String testFileContents = content.toString() + this.testFileContentsAfterSetUp;
        if (this.testFileContentsAfterSetUp.length() == 0) {
            testFileContents += "}\n?>";
        }

        this.createdFile.setBinaryContent(testFileContents.getBytes());
    }
}
