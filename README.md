# paokk-hl7-parser

A lightweight and easy-to-use HL7 parser for Java that simplifies the process of parsing and mapping HL7 messages to Java objects.

## Features

- **Simple API**: Easy to use with a straightforward interface
- **Annotation-based mapping**: Map HL7 fields to Java objects using annotations
- **Path-based field access**: Retrieve specific fields using a simple path syntax
- **Support for complex HL7 structures**: Handles segments, fields, components, and subcomponents
- **Collection support**: Automatically handles repeated segments as collections (List/Set)
- **Indexed path syntax**: Use `ORC(i)-2-1` syntax for List/Set elements where `i` is replaced with the collection index
- **Direct index access**: Access specific segment instances using `ORC(0)-2-1` syntax
- **Type conversion**: Supports basic types, dates, and custom types
- **Date format support**: Specify custom date formats using the `dateFormat` attribute
- **Customizable escape handling**: Provides `Hl7DefaultEscape` with option to customize

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven for dependency management

### Installation

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>org.paokk</groupId>
    <artifactId>paokk-hl7-parser</artifactId>
    <version>1.0</version>
</dependency>
```

### Basic Usage

#### 1. Parse HL7 text and access fields directly

```java
String hl7Text = "MSH|^~\\&|HIS||ESB||20210918140521||ORM^O01^ORM_O01|169bb7ac-6180-4e04-b42b-07713334966b|P|2.6^^&&&&D56D2C42401AAC6F36&&V1.0|||NE|AL||UTF-8\rPID||201532012|652101198612221332^^^^01||马志明^^^MA ZHI MING||19861222000000|1|||&华容文化路 179 号^华容^岳阳^湖南||18907804328^^^^^^^^^^^|||^^2|||652101198612221332|||^汉族^1\r";

HL7Parser parser = new HL7Parser(hl7Text);

// Access fields using path syntax
String patientId = parser.get("PID-3-1");
String patientName = parser.get("PID-5-1");
String messageType = parser.get("MSH-9-2");
```

#### 2. Map HL7 to Java objects using annotations

```java
// Define a model class with HL7Field annotations
public class Patient {
    // Basic type field
    @HL7Field(path = "PID-3-1")
    private String patientId;
    
    @HL7Field(path = "PID-5-1")
    private String firstName;
    
    @HL7Field(path = "PID-5-4")
    private String lastName;
    
    // Date field with custom format
    @HL7Field(path = "PID-7", dateFormat = "yyyyMMddHHmmss")
    private Date birthDate;
    
    // Getters and setters
    // ...
}

// Parse HL7 to object
HL7Parser parser = new HL7Parser(hl7Text);
Patient patient = parser.parse(Patient.class);

// Access mapped fields
System.out.println("Patient ID: " + patient.getPatientId());
System.out.println("Name: " + patient.getFirstName() + " " + patient.getLastName());
System.out.println("Birth Date: " + patient.getBirthDate());
```

## Path Syntax

The parser uses a simple path syntax to access fields:

```
[Segment]-[Field]-[Component]-[Subcomponent]
```

Examples:
- `MSH-9-2` - Access the 2nd component of the 9th field in the MSH segment
- `PID-3-1` - Access the 1st component of the 3rd field in the PID segment
- `OBR-4-2` - Access the 2nd component of the 4th field in the OBR segment

### Indexed Path Syntax

#### For Collections (List/Set)

For List and Set collections, use the `(i)` syntax where `i` will be replaced with the collection index:

```
[Segment](i)-[Field]-[Component]-[Subcomponent]
```

Example:
- `ORC(i)-2-1` - Access the 1st component of the 2nd field of the i-th ORC segment in a collection

#### For Direct Index Access

To access specific segment instances directly without using collections, use the `(n)` syntax where `n` is the zero-based index:

```
[Segment](n)-[Field]-[Component]-[Subcomponent]
```

Examples:
- `ORC(0)-2-1` - Access the 1st component of the 2nd field of the first ORC segment
- `ORC(1)-2-1` - Access the 1st component of the 2nd field of the second ORC segment

## Advanced Usage

### Handling Repeated Segments as Collections

```java
public class Order {
    @HL7Field(path = "OBR-1")
    private String orderId;
    
    // List collection of ORC segments
    @HL7Field(path = "ORC")
    private List<OrcSegment> orcSegments;
    
    // Getters and setters
    // ...
}

public class OrcSegment {
    // Using (i) syntax for collection elements
    @HL7Field(path = "ORC(i)-2-1")
    private String orderControl;
    
    @HL7Field(path = "ORC(i)-3-1")
    private String placerOrderNumber;
    
    // Getters and setters
    // ...
}

// Parse with repeated segments
HL7Parser parser = new HL7Parser(hl7Text);
Order order = parser.parse(Order.class);

// Access ORC segments
for (OrcSegment orc : order.getOrcSegments()) {
    System.out.println("Order Control: " + orc.getOrderControl());
    System.out.println("Placer Order Number: " + orc.getPlacerOrderNumber());
}
```

### Accessing Specific Segments Directly

```java
public class Order {
    @HL7Field(path = "OBR-1")
    private String orderId;
    
    // Access first ORC segment directly
    @HL7Field(path = "ORC(0)-2-1")
    private String firstOrderControl;
    
    // Access second ORC segment directly
    @HL7Field(path = "ORC(1)-2-1")
    private String secondOrderControl;
    
    // Getters and setters
    // ...
}
```

### Custom Date Formats

```java
public class Patient {
    @HL7Field(path = "PID-7", dateFormat = "yyyyMMdd")
    private Date birthDate;
    
    @HL7Field(path = "MSH-7", dateFormat = "yyyyMMddHHmmss")
    private Date messageDate;
    
    // Getters and setters
    // ...
}
```

### Custom Escape Handling

```java
// Use default escape handler
HL7Parser parser = new HL7Parser(hl7Text);
// Default escape handler is already set to Hl7DefaultEscape

// Create custom escape handler
class CustomEscape implements Hl7Escape {
    @Override
    public String escape(String hl7String) {
        // Custom escape logic
        return hl7String;
    }
}

// Set custom escape handler
parser.setHl7Escape(new CustomEscape());
```

## Supported Types

The parser supports the following types:

- **Basic types**: String, Integer, Float, Double, Boolean, Long, etc.
- **Date**: With customizable date format using `dateFormat` attribute
- **Collections**: List and Set of objects
- **Basic objects**: Custom classes with nested fields

## Project Structure

```
src/
├── main/java/org/paokk/hl7/parser/
│   ├── annotation/       # Annotations for field mapping
│   │   └── HL7Field.java # HL7 field annotation
│   ├── escape/           # Escape handling
│   │   ├── Hl7DefaultEscape.java # Default escape implementation
│   │   └── Hl7Escape.java        # Escape interface
│   ├── path/             # Path parsing
│   │   └── HL7Path.java  # HL7 path parser
│   ├── type/             # Type conversion
│   │   └── FieldSet.java # Field type handling
│   └── HL7Parser.java    # Main parser class
└── test/                 # Test files
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
