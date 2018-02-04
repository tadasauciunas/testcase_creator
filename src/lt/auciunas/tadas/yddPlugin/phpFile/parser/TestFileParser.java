package lt.auciunas.tadas.yddPlugin.phpFile.parser;

import lt.auciunas.tadas.yddPlugin.phpFile.entity.ParsedOriginalFile;
import lt.auciunas.tadas.yddPlugin.phpFile.entity.ParsedTestFile;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Pattern;

public class TestFileParser {

    private ParsedTestFile parsedTestFile;
    private ParsedOriginalFile parsedOriginalFile;

    public TestFileParser(ParsedOriginalFile parsedOriginalFile) {
        this.parsedOriginalFile = parsedOriginalFile;
    }

    public ParsedTestFile parseTestFileContents() {
        this.parsedTestFile = new ParsedTestFile();

        this.parseTestNameSpace();
        this.parseUsages();
        this.parseDependencies();
        this.parseSetUp();
        this.parseTestClassDefinition();
        this.parseOriginalClassDefinition();
        this.parseOriginalClassInitialization();

        return this.parsedTestFile;
    }

    private void parseTestClassDefinition() {
        //todo actual parsing of test file class definition belongs in this parser
        this.parsedTestFile.setTestFileClassDefinition(this.parsedOriginalFile.getTestFileClassDefinition());
    }

    private void parseDependencies() {
        String annotation, definition, init;
        Integer currentDependencyIndex = 0;

        for (Map.Entry<String, String> entry : this.parsedOriginalFile.getDependencies().entrySet()) {
            currentDependencyIndex++;

            String key = entry.getKey();
            String value = decapitalize(entry.getValue());

            annotation = "/** @var " + key + "|\\PHPUnit_Framework_MockObject_MockObject */\n";
            definition = "\tprivate " + value + ";\n\n";
            this.parsedTestFile.addDependencyDefinition(annotation + definition);

            value = value.substring(1, value.length());
            init = "$this->" + value + " = $this->getSimpleMock(" + key + "::class);\n";
            if (this.parsedOriginalFile.getDependencies().size() == currentDependencyIndex) {
                init += "\n";
            }
            this.parsedTestFile.addDependencyInitialization(init);
        }
    }

    private void parseOriginalClassInitialization() {
        String originalClassInitialization, originalClassName = parsedOriginalFile.getOriginalClassName();

        originalClassInitialization = "$this->" +
                decapitalize(originalClassName) +
                " = new " + originalClassName + "(";

        for (Map.Entry<String, String> entry : this.parsedOriginalFile.getDependencies().entrySet()) {
            String value = decapitalize(entry.getValue()).substring(1, entry.getValue().length());
            value = "$this->" + value;
            originalClassInitialization += value + ", ";
        }

        if (this.parsedOriginalFile.getDependencies().size() > 0) {
            originalClassInitialization = originalClassInitialization.
                    substring(0, originalClassInitialization.length() - 2);
        }
        originalClassInitialization += ");\n";

        this.parsedTestFile.setOriginalClassInitialization(originalClassInitialization);
    }

    private void parseOriginalClassDefinition() {
        String originalClassDefinition, originalClassName = parsedOriginalFile.getOriginalClassName();

        originalClassDefinition = "/** @var " + originalClassName + "*/\n";
        originalClassDefinition += "\tprivate $" + decapitalize(originalClassName) + ";\n\n";

        this.parsedTestFile.setOriginalClassDefinition(originalClassDefinition);
    }

    private void parseSetUp() {
    }

    private void parseUsages() {
        this.parsedTestFile.setUsages(this.parsedOriginalFile.getUsages());
        this.parsedTestFile.addUsage(getTestCaseUsage());
    }

    private void parseTestNameSpace() {
        this.parsedTestFile.setOriginalNamespace(createTestNamespace());
    }

    private String getTestCaseUsage() {
        String testNamespace = createTestNamespace();

        String[] contents = testNamespace.split(Pattern.quote("\\"));
        testNamespace = contents[0].split(" ")[1];

        return "use " + testNamespace + "\\TestCase;";
    }

    @NotNull
    private String createTestNamespace() {
        String originalNamespace = this.parsedOriginalFile.getOriginalNamespace();

        String[] contents = originalNamespace.split(Pattern.quote("\\"));
//        contents[0] = contents[0].substring(0, contents[0].length() - 1);
        contents[0] = contents[0] + "Test";

        return String.join("\\", contents);
    }

    private String decapitalize(String input) {
        char c[] = input.toCharArray();
        c[0] = Character.toLowerCase(c[0]);

        return new String(c);
    }
}
