package lt.auciunas.tadas.testCaseCreator.phpFile.parser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedSourceFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedTestFile;
import org.jetbrains.annotations.NotNull;

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
        String testFileClassDefinition = "class " + this.parsedSrcFile.getSourceClassName() + "Test extends TestCase\n{\n";
        this.parsedTestFile.setTestFileClassDefinition(testFileClassDefinition);
    }

    private void parseDependencies() {
        String[] nonMockableTypes = new String[]{"string", "int", "float", "array"};

        for (Map<String, String> entries : this.parsedSrcFile.getDependencies()) {
            String key = entries.keySet().iterator().next();
            String value = decapitalize(entries.values().iterator().next());

            String annotation;
            if (key == null) {
                annotation = "/** @var " + FIXME_NO_VAR_TYPE + " */\n";
            } else {
                annotation = "/** @var " + key;
                if (Arrays.asList(nonMockableTypes).contains(key)) {
                    annotation = annotation + " */\n";
                } else {
                    annotation = annotation + "|MockObject */\n";
                }
            }

            String definition = FOUR_SPACE_TAB + "private " + value + ";\n\n";
            this.parsedTestFile.addDependencyDefinition(annotation + definition);

            String init;
            value = value.substring(1);
            if (key == null) {
                init = "$this->" + value + " = null; " + FIXME_NO_VAR_VALUE + "\n";
            } else if (key.equals("string")) {
                init = "$this->" + value + " = '';\n";
            } else if (key.equals("array")) {
                init = "$this->" + value + " = [];\n";
            } else if (!key.equals("int") && !key.equals("float")) {
                init = "$this->" + value + " = $this->createMock(" + key + "::class);\n";
            } else {
                init = "$this->" + value + " = 0;\n";
            }

            this.parsedTestFile.addDependencyInitialization(init);
        }
    }

    private void parseOriginalClassInitialization() {
        String originalClassName = this.parsedSrcFile.getSourceClassName();
        String originalClassInitialization = "\n        $this->" + this.decapitalize(originalClassName) + " = new " + originalClassName + "(";

        String value;
        for(Iterator iterator = this.parsedSrcFile.getDependencies().iterator(); iterator.hasNext(); originalClassInitialization = originalClassInitialization + value + ", ") {
            Map<String, String> entries = (Map)iterator.next();
            String entryValue = (String)entries.values().iterator().next();
            value = this.decapitalize(entryValue).substring(1, entryValue.length());
            value = "$this->" + value;
        }

        if (this.parsedSrcFile.getDependencies().size() > 0) {
            originalClassInitialization = originalClassInitialization
                    .substring(0, originalClassInitialization.length() - 2);
        }

        originalClassInitialization = originalClassInitialization + ");\n";
        if (originalClassInitialization.length() > MAX_LINE_LENGTH) {
            String two_tabs = "        ";
            String three_tabs = two_tabs + "    ";
            originalClassInitialization = originalClassInitialization.replaceAll(", ", ",\n" + three_tabs);
            originalClassInitialization = originalClassInitialization.replaceAll("\\(", "(\n" + three_tabs);
            originalClassInitialization = originalClassInitialization.replaceAll("\\)", "\n" + two_tabs + ")");
        }

        this.parsedTestFile.setOriginalClassInitialization(originalClassInitialization);
    }

    private void parseOriginalClassDefinition() {
        String originalClassName = this.parsedSrcFile.getSourceClassName();
        String originalClassDefinition = "/** @var " + originalClassName + " */\n";
        originalClassDefinition = originalClassDefinition + "    private $" + this.decapitalize(originalClassName) + ";\n\n";
        this.parsedTestFile.setOriginalClassDefinition(originalClassDefinition);
    }

    private void parseUsages() {
        this.parsedTestFile.getImports().setImports(this.parsedSrcFile.getImports().getImports());

        if (this.parsedSrcFile.getDependencies().size() > 0) { //if there's anything to mock
            this.parsedTestFile.getImports().addImport(this.getMockObjectUsage());
        }
    }


    private void parseTestNameSpace() {
        this.parsedTestFile.setOriginalNamespace(this.createTestNamespace());
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
