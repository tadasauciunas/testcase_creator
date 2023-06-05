package lt.auciunas.tadas.testCaseCreator.phpFile.parser;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.testCaseCreator.phpFile.mapper.ParsedSourceFile;

import java.util.Arrays;

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
                this.getUsageOfClassInSameNamespace(this.parsedSourceFile.getSourceClassName())
        );

        return this.parsedSourceFile;
    }

    private void parseOriginalFileRows() {
        this.originalFileRows = LoadTextUtil.loadText(this.originalFile).toString().split("\n");
        this.originalFileRows[0] = "";
        int rowNumber = 0;
        int constructorRow = 0;
        boolean constructFound = false;
        boolean multiLineConstructor = false;
        String[] var5 = this.originalFileRows;
        int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String item = var5[var7];
            if (!constructFound && item.contains("__construct")) {
                constructFound = true;
                constructorRow = rowNumber;
            }

            if (constructFound) {
                if (multiLineConstructor) {
                    StringBuilder var10000 = new StringBuilder();
                    String[] var10002 = this.originalFileRows;
                    var10002[constructorRow] = var10000.append(var10002[constructorRow]).append(item).toString();
                }

                if (item.charAt(item.length() - 1) == ')' || item.contains(") {")) {
                    return;
                }

                multiLineConstructor = true;
            }

            ++rowNumber;
        }

    }

    private boolean parseNamespace(String row) {
        if (row.indexOf("namespace") == 0) {
            this.parsedSourceFile.setSourceNamespace(row);
            return true;
        } else {
            return false;
        }
    }

    private boolean parseUsages(String row) {
        if (row.indexOf("use ") == 0 && this.testFileConstructHasNotBeenDefined()) {
            this.parsedSourceFile.getImports().addImport(row);
            return true;
        } else {
            return false;
        }
    }

    private boolean testFileConstructHasNotBeenDefined() {
        return this.parsedSourceFile.getSourceClassName() == null || this.parsedSourceFile.getSourceClassName().length() == 0;
    }

    private boolean parseClassDefinition(String row) {
        if (row.indexOf("class ") == 0 && this.testFileConstructHasNotBeenDefined()) {
            String[] classDefinition = row.split("class ");
            String className;
            if (classDefinition[1].contains(" ")) {
                className = classDefinition[1].split(" ")[0];
            } else {
                className = classDefinition[1];
            }

            this.parsedSourceFile.setSourceClassName(className);
            return true;
        } else {
            return false;
        }
    }

    private boolean parseDependencies(String row) {
        if (row.contains("public function __construct")) {
            row = row.split("\\(")[1];
            if (row.equals(")")) {
                return true;
            } else {
                row = row.split("\\)")[0];
                String[] items = row.split(",");
                String[] var3 = items;
                int var4 = items.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    String value = var3[var5];
                    String[] array = value.trim().split(" ");
                    if (array.length > 1) {
                        this.parsedSourceFile.addDependency(array[0], array[1]);
                        if (this.dependencyCanBeImported(array[0]) && !this.dependencyIsImported(array[0])) {
                            this.parsedSourceFile.getImports().addImport(this.getUsageOfClassInSameNamespace(array[0]));
                        }
                    } else {
                        this.parsedSourceFile.addDependency((String)null, array[0]);
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    private boolean dependencyIsImported(String s) {
        for (String usage : this.parsedSourceFile.getImports().getImports()) {
            if (usage.contains(s + ";")) {
                return true;
            }
        }
        return false;
    }

    private boolean dependencyCanBeImported(String s) {
        String[] nonImportableTypes = {"string", "int", "float", "array"};

        return !Arrays.asList(nonImportableTypes).contains(s);
    }

    private String getUsageOfClassInSameNamespace(String className) {
        String namespace = this.parsedSourceFile.getSourceNamespace();
        namespace = namespace.split("namespace ")[1];
        namespace = namespace.substring(0, namespace.length() - 1);

        return "use " + namespace + "\\" + className + ";";
    }
}
