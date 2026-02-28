package org.paokk.hl7.parser.model;

import org.paokk.hl7.parser.annotation.HL7Field;

public class Test {

    @HL7Field(path = "NTE(i)-3")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
