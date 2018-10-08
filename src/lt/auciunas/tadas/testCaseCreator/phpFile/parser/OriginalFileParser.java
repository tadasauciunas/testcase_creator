package lt.auciunas.tadas.testCaseCreator.phpFile.parser;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedSourceFile;

public class OriginalFileParser {
    private VirtualFile originalFile;
    private String[] originalFileRows;
    private ParsedSourceFile parsedSourceFile;

    public OriginalFileParser(VirtualFile sourceFile) {
        this.originalFile = sourceFile;
    }

    public ParsedSourceFile parseFile() {
        parsedSourceFile = new ParsedSourceFile();
        parseOriginalFileRows();

        for (String row : this.originalFileRows) {
            boolean x = this.parseNamespace(row) ||
                    this.parseUsages(row) ||
                    this.parseClassDefinition(row) ||
                    this.parseDependencies(row);
        }

        this.parsedSourceFile.getImports().addImport(
                getUsageOfClassInSameNamespace(this.parsedSourceFile.getSourceClassName())
        );

        return this.parsedSourceFile;
    }

    private void parseOriginalFileRows() {
        this.originalFileRows = LoadTextUtil.loadText(this.originalFile).toString().split("\n");
        this.originalFileRows[0] = "";

        int rowNumber = 0, constructorRow = 0;
        boolean constructFound = false, multiLineConstructor = false;

        for (String item : this.originalFileRows) {
            if (!constructFound && item.contains("__construct")) {
                constructFound = true;
                constructorRow = rowNumber;
            }
            if (constructFound) {
                if (multiLineConstructor) {
                    this.originalFileRows[constructorRow] += item; //add current line to the original constructor line
                }

                if (item.charAt(item.length() - 1) == ')') //if this line is the end of the constructor
                {
                    return;
                }

                multiLineConstructor = true;
            }
            rowNumber++;
        }
    }

    private boolean parseNamespace(String row) {
        if (row.indexOf("namespace") == 0) {
            this.parsedSourceFile.setSourceNamespace(row);

            return true;
        }

        return false;
    }

    private boolean parseUsages(String row) {
        if (row.indexOf("use ") == 0 && testFileConstructHasNotBeenDefined()) {
            this.parsedSourceFile.getImports().addImport(row);

            return true;
        }

        return false;
    }

    private boolean testFileConstructHasNotBeenDefined() {
        return this.parsedSourceFile.getSourceClassName() == null ||
                this.parsedSourceFile.getSourceClassName().length() == 0;
    }

    private boolean parseClassDefinition(String row) {
        if (row.indexOf("class ") == 0 && this.testFileConstructHasNotBeenDefined()) {
            String[] classDefinition = row.split("class ");
            String className;
            if (classDefinition[1].contains(" ")) { //class name contains spaces (implements interface, extends parent etc.)
                className = (classDefinition[1].split(" "))[0];
            } else {
                className = classDefinition[1];
            }
            this.parsedSourceFile.setSourceClassName(className);

            return true;
        }

        return false;
    }

    private boolean parseDependencies(String row) {
        if (row.contains("public function __construct")) {
            row = row.split("\\(")[1];
            if (row.equals(")")) { //constructor is empty
                return true;
            }
            row = row.split("\\)")[0];
            String[] items = row.split(",");

            for (String value : items) {
                String[] array = value.trim().split(" ");
                if (array[0].equals("array")) {
                    array = new String[]{array[1]};
                }
                if (array.length > 1) { //Dependency had a type
                    this.parsedSourceFile.addDependency(array[0], array[1]);

                    if (!this.dependencyIsImported(array[0])) {
                        //Add import of class if it's in the same namespace as src class and original import does not exist
                        this.parsedSourceFile.getImports().addImport(this.getUsageOfClassInSameNamespace(array[0]));
                    }
                } else { //Dependency had no type (i.e. scalars with no strict typing)
                    this.parsedSourceFile.addDependency(null, array[0]);
                }
            }

            return true;
        }

        return false;
    }

    private boolean dependencyIsImported(String s) {
        for (String usage : this.parsedSourceFile.getImports().getImports()) {
            if (usage.contains(s + ";")) {
                return true;
            }
        }
        return false;
    }

    private String getUsageOfClassInSameNamespace(String className) {
        String namespace = this.parsedSourceFile.getSourceNamespace();
        namespace = namespace.split("namespace ")[1];
        namespace = namespace.substring(0, namespace.length() - 1);

        return "use " + namespace + "\\" + className + ";";
    }
}
