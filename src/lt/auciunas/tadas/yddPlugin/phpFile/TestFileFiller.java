package lt.auciunas.tadas.yddPlugin.phpFile;

import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.yddPlugin.phpFile.entity.ParsedTestFile;

import java.io.IOException;

public class TestFileFiller {

    private VirtualFile createdFile;
    private ParsedTestFile parsedTestFile;

    public TestFileFiller(VirtualFile createdFile, ParsedTestFile parsedTestFile) {
        this.createdFile = createdFile;
        this.parsedTestFile = parsedTestFile;
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
            content.append("    " + item);
        }
        content.append("    " + this.parsedTestFile.getOriginalClassDefinition());

        content.append("    public function setUp()\n    {\n");
        for (String item : this.parsedTestFile.getDependencyInitializations()) {
            content.append("        " + item);
        }
        content.append("        " + this.parsedTestFile.getOriginalClassInitialization());
        content.append("    }\n");

        content.append("}\n?>");

        this.createdFile.setBinaryContent(content.toString().getBytes());

        //todo add ifs for each of these added items, to check if they actually need to be added.
        //todo or just replace the stuff that exists now.
        //todo how about finding the end of setUp() and removing everything until then?
    }
}
