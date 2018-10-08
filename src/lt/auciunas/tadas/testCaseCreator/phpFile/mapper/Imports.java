package lt.auciunas.tadas.testCaseCreator.phpFile.mapper;

import java.util.ArrayList;
import java.util.List;

public class Imports {
    private List<String> imports = new ArrayList<>();

    public List<String> getImports() {
        return imports;
    }

    public void addImport(String anImport) {
        this.imports.add(anImport);
    }

    public void setImports(List<String> testClassImports) {
        this.imports = testClassImports;
    }
}
