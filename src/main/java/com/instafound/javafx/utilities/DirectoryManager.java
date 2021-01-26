package com.instafound.javafx.utilities;

import javax.swing.*;

public class DirectoryManager extends JFrame {

    public static String chooseFile() {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);

            //i could set a start path but it's not portable

            String file_path = null;
            if(result == JFileChooser.APPROVE_OPTION)
                file_path = fileChooser.getSelectedFile().getAbsolutePath();

            return file_path;

    }
}














