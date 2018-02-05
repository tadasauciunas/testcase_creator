package lt.auciunas.tadas.testCaseCreator.phpFile.parser;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedOriginalFile;

public class OriginalFileParser {
    private VirtualFile originalFile;
    private String[] originalFileRows;
    private ParsedOriginalFile parsedFile;

    public OriginalFileParser(VirtualFile originalFile) {
        this.originalFile = originalFile;
    }

    public ParsedOriginalFile parseFile() {
        parsedFile = new ParsedOriginalFile();
        Boolean x;
        parseOriginalFileRows();

        for (String item : this.originalFileRows) {
            x = this.parseNamespace(item) ||
                    this.parseUsages(item) ||
                    this.parseClassDefinition(item) ||
                    this.parseDependencies(item);
        }

        this.parsedFile.addUsage(getOriginalClassUsage());

        return this.parsedFile;
    }

    private void parseOriginalFileRows() {
        this.originalFileRows = LoadTextUtil.loadText(this.originalFile).toString().split("\n");
        this.originalFileRows[0] = "";

        int rowNumber = 0, constructorRow = 0;
        Boolean constructFound = false, multiLineConstructor = false;

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

                if (!multiLineConstructor) {
                    multiLineConstructor = true;
                }
            }
            rowNumber++;
        }
    }

    private boolean parseNamespace(String item) {
        if (item.contains("namespace")) {
            this.parsedFile.setOriginalNamespace(item);

            return true;
        }

        return false;
    }

    private boolean parseUsages(String item) {
        if (item.contains("use ") && testFileConstructHasNotBeenDefined()) {
            this.parsedFile.addUsage(item);

            return true;
        }

        return false;
    }

    private boolean testFileConstructHasNotBeenDefined() {
        return this.parsedFile.getOriginalClassName() == null ||
                this.parsedFile.getOriginalClassName().length() == 0;
    }

    private boolean parseClassDefinition(String item) {
        if (item.indexOf("class ") == 0 && this.testFileConstructHasNotBeenDefined()) {
            String[] array = item.split("class ");
            String className;
            if (array[1].contains(" ")) { //class name contains spaces (implements interface, extends parent etc.)
                array = array[1].split(" ");
                className = array[0];
            } else {
                className = array[1];
            }
            this.parsedFile.setOriginalClassName(className);

            return true;
        }

        return false;
    }

    private boolean parseDependencies(String item) {
        if (item.contains("__construct")) {
            item = item.split("\\(")[1];
            if (item.equals(")")) { //constructor is empty
                return true;
            }
            item = item.split("\\)")[0];
            String[] items = item.split(",");

            for (String value : items) {
                String[] array = value.trim().split(" ");
                if (array.length > 1) { //Dependency had a type
                    this.parsedFile.addDependency(array[0], array[1]);
                } else { //Dependency had no type (i.e. scalars with no strict typing)
                    this.parsedFile.addDependency(null, array[0]);
                }
            }

            return true;
        }

        return false;
    }

    private String getOriginalClassUsage() {
        String namespace = this.parsedFile.getOriginalNamespace();
        namespace = namespace.split("namespace ")[1];
        namespace = namespace.substring(0, namespace.length() - 1);

        return "use " + namespace + "\\" + this.parsedFile.getOriginalClassName() + ";";
    }
}
