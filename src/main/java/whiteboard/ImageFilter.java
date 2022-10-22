package whiteboard;

import java.io.File;
import javax.swing.filechooser.*;

/*
* Code taken from the example in Java docs:
* https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
*  */
public class ImageFilter extends FileFilter {

    //Accept all directories and all jpg, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f.getName());
        if (extension != null) {
            return extension.equals("jpg") || extension.equals("png") || extension.equals("bmp");
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Images";
    }

    public static String getExtension(String s) {
        String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}
