package lt.auciunas.tadas.testCaseCreator.phpFile.parser;

import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedOriginalFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedTestFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TestFileParser {

    private static final String FIXME_NO_VAR_TYPE = "//FIXME I don't know my type";
    private static final String FIXME_NO_VAR_VALUE = "//FIXME I don't know my value";
    private static final String FOUR_SPACE_TAB = "    ";

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
        this.parseTestClassDefinition();
        this.parseOriginalClassDefinition();
        this.parseOriginalClassInitialization();

        return this.parsedTestFile;
    }

    private void parseTestClassDefinition() {
        String testFileClassDefinition = "class " +
                this.parsedOriginalFile.getOriginalClassName() +
                "Test extends TestCase\n{\n";

        this.parsedTestFile.setTestFileClassDefinition(testFileClassDefinition);
    }

    private void parseDependencies() {
        String annotation, definition, init;

        for (Map.Entry<String, List<String>> entries : this.parsedOriginalFile.getDependencies().entrySet()) {
            String key = entries.getKey();
            for (String entryValue : entries.getValue()) {
                String value = decapitalize(entryValue);

                if (key == null) {
                    annotation = "/** @var " + FIXME_NO_VAR_TYPE + " */\n";
                } else {
                    annotation = "/** @var " + key + "|\\PHPUnit_Framework_MockObject_MockObject */\n";
                }

                definition = FOUR_SPACE_TAB + "private " + value + ";\n\n";
                this.parsedTestFile.addDependencyDefinition(annotation + definition);

                value = value.substring(1, value.length());
                if (key == null) {
                    init = "$this->" + value + " = null; " + FIXME_NO_VAR_VALUE + "\n";
                } else {
                    init = "$this->" + value + " = $this->getSimpleMock(" + key + "::class);\n";
                }

                this.parsedTestFile.addDependencyInitialization(init);
            }
        }
    }

    private void parseOriginalClassInitialization() {
        String originalClassInitialization, originalClassName = parsedOriginalFile.getOriginalClassName();

        originalClassInitialization = "\n" + FOUR_SPACE_TAB + FOUR_SPACE_TAB + "$this->" +
                decapitalize(originalClassName) +
                " = new " + originalClassName + "(";

        for (Map.Entry<String, List<String>> entries : this.parsedOriginalFile.getDependencies().entrySet()) {
            for (String entryValue : entries.getValue()) {

                String value = decapitalize(entryValue).substring(1, entryValue.length());
                value = "$this->" + value;
                originalClassInitialization += value + ", ";
            }
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

        originalClassDefinition = "/** @var " + originalClassName + " */\n";
        originalClassDefinition += "    private $" + decapitalize(originalClassName) + ";\n\n";

        this.parsedTestFile.setOriginalClassDefinition(originalClassDefinition);
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
        contents[0] = contents[0] + "Test";

        return String.join("\\", contents);
    }

    private String decapitalize(String input) {
        char c[] = input.toCharArray();
        c[0] = Character.toLowerCase(c[0]);

        return new String(c);
    }
}
