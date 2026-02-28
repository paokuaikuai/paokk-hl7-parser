package org.paokk.hl7.parser.escape;

public class Hl7DefaultEscape implements Hl7Escape {
    @Override
    public String escape(String hl7String) {
        if (hl7String == null) {
            return "";
        }
        return hl7String.replace("\\F\\", "|")
                .replace("\\S\\", "^")
                .replace("\\T\\", "&")
                .replace("\\R\\", "~")
                .replace("\\E\\", "\\")
                .replace("\\.br\\", "\n")
                .replace("\\H\\", "^")
                .replace("\\N\\", " ");
    }
}
