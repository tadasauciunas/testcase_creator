package lt.auciunas.tadas.testCaseCreator.phpFile.parser;

import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedSourceFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedTestFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TestFileParser {

    private static final String FIXME_NO_VAR_TYPE = "//FIXME I don't know my type";
    private static final String FIXME_NO_VAR_VALUE = "//FIXME I don't know my value";
    private static final String FOUR_SPACE_TAB = "    ";
    private static final Integer MAX_LINE_LENGTH = 120;

    private ParsedTestFile parsedTestFile;
    private ParsedSourceFile parsedSrcFile;

    public TestFileParser(ParsedSourceFile parsedSrcFile) {
        this.parsedSrcFile = parsedSrcFile;
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
                this.parsedSrcFile.getSourceClassName() +
                "Test extends TestCase\n{\n";

        this.parsedTestFile.setTestFileClassDefinition(testFileClassDefinition);
    }

    private void parseDependencies() {
        String annotation, definition, init;

        for (Map<String, String> entries : this.parsedSrcFile.getDependencies()) {
            String key = entries.keySet().iterator().next();
            String value = decapitalize(entries.values().iterator().next());

            if (key == null) {
                annotation = "/** @var " + FIXME_NO_VAR_TYPE + " */\n";
            } else {
                annotation = "/** @var " + key + "|MockObject */\n";
            }

            definition = FOUR_SPACE_TAB + "private " + value + ";\n\n";
            this.parsedTestFile.addDependencyDefinition(annotation + definition);

            value = value.substring(1);
            if (key == null) {
                init = "$this->" + value + " = null; " + FIXME_NO_VAR_VALUE + "\n";
            } else {
                init = "$this->" + value + " = $this->createMock(" + key + "::class);\n";
            }

            this.parsedTestFile.addDependencyInitialization(init);
        }
    }

    private void parseOriginalClassInitialization() {
        String originalClassInitialization, originalClassName = parsedSrcFile.getSourceClassName();

        originalClassInitialization = "\n" + FOUR_SPACE_TAB + FOUR_SPACE_TAB + "$this->" +
                decapitalize(originalClassName) +
                " = new " + originalClassName + "(";

        for (Map<String, String> entries : this.parsedSrcFile.getDependencies()) {
            String entryValue = entries.values().iterator().next();
            String value = decapitalize(entryValue).substring(1, entryValue.length());
            value = "$this->" + value;
            if (isLineTooLong(originalClassInitialization, value)) {
                originalClassInitialization = originalClassInitialization.replaceFirst(".$","");
                value = "\n" + FOUR_SPACE_TAB + FOUR_SPACE_TAB + FOUR_SPACE_TAB + value;
            }
            originalClassInitialization += value + ", ";
        }

        if (this.parsedSrcFile.getDependencies().size() > 0) {
            originalClassInitialization = originalClassInitialization.
                    substring(0, originalClassInitialization.length() - 2);
        }
        originalClassInitialization += ");\n";

        this.parsedTestFile.setOriginalClassInitialization(originalClassInitialization);
    }

    private boolean isLineTooLong(String initial, String additional) {
        String[] initialLines = initial.split("\n");
        int lastLineLength = initialLines[initialLines.length - 1].length();

        return lastLineLength + additional.length() > MAX_LINE_LENGTH;
    }

    private void parseOriginalClassDefinition() {
        String originalClassDefinition, originalClassName = parsedSrcFile.getSourceClassName();

        originalClassDefinition = "/** @var " + originalClassName + " */\n";
        originalClassDefinition += "    private $" + decapitalize(originalClassName) + ";\n\n";

        this.parsedTestFile.setOriginalClassDefinition(originalClassDefinition);
    }

    private void parseUsages() {
        this.parsedTestFile.getImports().setImports(this.parsedSrcFile.getImports().getImports());

        if (this.parsedSrcFile.getDependencies().size() > 0) { //if there's anything to mock
            this.parsedTestFile.getImports().addImport(getMockObjectUsage());
        }
    }


    private void parseTestNameSpace() {
        this.parsedTestFile.setOriginalNamespace(createTestNamespace());
    }

    private String getMockObjectUsage() {
        return "use PHPUnit\\Framework\\MockObject\\MockObject;";
    }

    @NotNull
    private String createTestNamespace() {
        String originalNamespace = this.parsedSrcFile.getSourceNamespace();

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
