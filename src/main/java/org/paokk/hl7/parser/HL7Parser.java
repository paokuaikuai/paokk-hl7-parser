package org.paokk.hl7.parser;

import org.paokk.hl7.parser.annotation.HL7Field;
import org.paokk.hl7.parser.path.HL7Path;
import org.paokk.hl7.parser.escape.Hl7DefaultEscape;
import org.paokk.hl7.parser.escape.Hl7Escape;
import org.paokk.hl7.parser.type.FieldSet;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HL7Parser {

    private String hl7Text;

    private Hl7Escape hl7Escape = new Hl7DefaultEscape();

    private FieldSet fieldSet = new FieldSet();

    private Map<String, List<List<String[][]>>> data;

    private String[] orderSegments;

    public HL7Parser(String hl7Text) {
        this.hl7Text = hl7Text;
        this.data = this.transform();
    }

    public HL7Parser(String hl7Text, String[] orderSegments) {
        this.hl7Text = hl7Text;
        this.data = this.transform();
        this.orderSegments = orderSegments;
        this.setOrderSegment();
    }

    private Map<String, List<List<String[][]>>> transform() {
        Map<String, List<List<String[][]>>> data = new LinkedHashMap<>();
        String[] segments = this.hl7Text.split("\r");

        for (String segment : segments) {
            String[] fields = segment.split("\\|", -1);
            String segmentName = fields[0];
            data.computeIfAbsent(segmentName, k -> new ArrayList<>());

            List<String[][]> parsedFields = new ArrayList<>();
            for (int i = 1; i < fields.length; i++) {
                String[] components = fields[i].split("\\^", -1);
                String[][] cs = new String[components.length][];
                for (int j = 0; j < components.length; j++) {
                    cs[j] = components[j].split("&", -1);
                }
                parsedFields.add(cs);
            }
            data.get(segmentName).add(parsedFields);
        }
        return data;
    }

    private void setOrderSegment() {
        for (String seg : this.orderSegments) {
            List<List<String[][]>> segment = getSegment(seg);
            segment.sort((o1, o2) -> {
                String key1 = getSortKey(o1);
                String key2 = getSortKey(o2);
                try {
                    int num1 = Integer.parseInt(key1);
                    int num2 = Integer.parseInt(key2);
                    return Integer.compare(num1, num2);
                } catch (NumberFormatException e) {
                    return key1.compareTo(key2);
                }
            });

        }
    }

    private static String getSortKey(List<String[][]> segment) {
        if (segment == null || segment.isEmpty()) {
            return "";
        }
        String[][] firstArray = segment.get(0);
        if (firstArray == null || firstArray.length == 0 || firstArray[0] == null || firstArray[0].length == 0) {
            return "";
        }
        return firstArray[0][0];
    }

    public String get(String path) {
        try {
            HL7Path hl7Path = parsePath(path);
            return escape(this.data.get(hl7Path.getKey()).get(hl7Path.getPos()[0]).get(hl7Path.getPos()[1] - 1)[hl7Path.getPos()[2] - 1][hl7Path.getPos()[3] - 1]);
        } catch (Exception e) {
            return "";
        }
    }

    public List<List<String[][]>> getSegment(String segment) {
        return this.data.get(segment);
    }


    private HL7Path parsePath(String path) {
        if (path == null || path.isEmpty()) {
            return new HL7Path("", new int[]{0, 1, 1, 1});
        }

        Pattern pattern = Pattern.compile("^([A-Z]+\\d*)(?:\\((\\d+)\\))?(?:-(\\d+))?(?:-(\\d+))?(?:-(\\d+))?$");
        Matcher matcher = pattern.matcher(path);

        if (!matcher.find()) {
            throw new IllegalArgumentException("invalid hl7 path: " + path);
        }

        String segment = matcher.group(1);
        int[] pos = new int[4];

        pos[0] = (matcher.group(2) != null) ? Integer.parseInt(matcher.group(2)) : 0;

        for (int i = 1; i <= 3; i++) {
            pos[i] = (matcher.group(i + 2) != null) ? Integer.parseInt(matcher.group(i + 2)) : 1;
        }

        if ("MSH".equals(segment)) {
            pos[1] = pos[1] - 1;
        }

        return new HL7Path(segment, pos);
    }

    public <T> T parse(Class<T> clazz) {
        return parse(clazz, 0);
    }

    private <T> T parse(Class<T> clazz, Integer index) {
        if (hl7Text == null || clazz == null) {
            return null;
        }
        try {
            T target = clazz.getDeclaredConstructor().newInstance();

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(HL7Field.class)) {
                    HL7Field annotation = field.getAnnotation(HL7Field.class);
                    String path = annotation.path();
                    try {
                        field.setAccessible(true);

                        if (fieldSet.isCollectionType(field)) {
                            Collection<Object> collection = new ArrayList<>();

                            if (field.getType() == List.class) {
                                collection = new ArrayList<>();
                            } else if (field.getType() == Set.class) {
                                collection = new HashSet<>();
                            }

                            field.set(target, collection);

                            ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                            Type[] actualTypeArguments = paramType.getActualTypeArguments();
                            Class<?> genericClass = (Class<?>) actualTypeArguments[0];

                            List<List<String[][]>> segment = getSegment(path);

                            for (int i = 0; segment != null && i < segment.size(); i++) {
                                Object obj = parse(genericClass, i);
                                collection.add(obj);
                            }
                        } else if (fieldSet.isBasicType(field)) {
                            if (index != null) {
                                path = path.replace("i", index + "");
                            }
                            fieldSet.setBasicTypeValue(target, field, get(path));
                        } else if (fieldSet.isDateType(field)) {
                            fieldSet.setDateTypeValue(target, field, get(path), annotation.dateFormat());
                        } else {
                            field.set(target, parse(field.getType(), 0));
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("set value failed:" + field.getName(), e);
                    }
                }
            }
            return target;
        } catch (Exception e) {
            throw new RuntimeException("parse hl7 text failed", e);
        }
    }

    public String escape(String hl7String) {
        return hl7Escape.escape(hl7String);
    }

    public void setHl7Escape(Hl7Escape hl7Escape) {
        this.hl7Escape = hl7Escape;
    }
}