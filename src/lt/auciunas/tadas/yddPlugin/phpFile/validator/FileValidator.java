package lt.auciunas.tadas.yddPlugin.phpFile.validator;

import com.intellij.openapi.vfs.VirtualFile;
import lt.auciunas.tadas.yddPlugin.exceptions.FileNotSupportedException;

import java.io.FileNotFoundException;

public class FileValidator {

    private VirtualFile file;

    public FileValidator(VirtualFile file) {
        this.file = file;
    }

    public void validateFile() throws FileNotSupportedException, FileNotFoundException {
        if (this.file == null) {
            throw new FileNotFoundException("File is not found");
        }

        if (!this.file.getFileType().getName().equals("PHP")) {
            throw new FileNotSupportedException("Only PHP files are supported");
        }

        if (!isFilePhpClass()) {
            throw new FileNotSupportedException("Only PHP classes are supported");
        }

        if (!isFileInSrcDir()) {
            throw new FileNotSupportedException("File must belong to a src/ directory");
        }
    }

    private boolean isFileInSrcDir() {
        return this.file.getPath().contains("src/");
    }

    private boolean isFilePhpClass() {
        return true; //todo
    }
}
