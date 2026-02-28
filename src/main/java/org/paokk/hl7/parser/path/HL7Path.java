package org.paokk.hl7.parser.path;


public class HL7Path {
    private String key;
    private int[] pos;

    public HL7Path(){}

    public HL7Path(String key, int[] pos) {
        this.key = key;
        this.pos = pos;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int[] getPos() {
        return pos;
    }

    public void setPos(int[] pos) {
        this.pos = pos;
    }
}
