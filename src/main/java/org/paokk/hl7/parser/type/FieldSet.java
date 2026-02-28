package org.paokk.hl7.parser.type;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FieldSet {

    private static final Set<Class<?>> BASIC_TYPE_SET = new HashSet<>();

    static {
        BASIC_TYPE_SET.add(byte.class);
        BASIC_TYPE_SET.add(short.class);
        BASIC_TYPE_SET.add(int.class);
        BASIC_TYPE_SET.add(long.class);
        BASIC_TYPE_SET.add(float.class);
        BASIC_TYPE_SET.add(double.class);
        BASIC_TYPE_SET.add(boolean.class);
        BASIC_TYPE_SET.add(char.class);

        BASIC_TYPE_SET.add(Byte.class);
        BASIC_TYPE_SET.add(Short.class);
        BASIC_TYPE_SET.add(Integer.class);
        BASIC_TYPE_SET.add(Long.class);
        BASIC_TYPE_SET.add(Float.class);
        BASIC_TYPE_SET.add(Double.class);
        BASIC_TYPE_SET.add(Boolean.class);
        BASIC_TYPE_SET.add(Character.class);

        BASIC_TYPE_SET.add(String.class);
        BASIC_TYPE_SET.add(BigDecimal.class);
        BASIC_TYPE_SET.add(BigInteger.class);
    }

    public boolean isBasicType(Field field) {
        return field != null && BASIC_TYPE_SET.contains(field.getType());
    }

    public void setBasicTypeValue(Object target, Field field, String val) throws IllegalAccessException {
        Object o = convertStringToBasicType(field, val);
        if (o != null) {
            field.set(target, o);
        }
    }

    public Object convertStringToBasicType(Field field, String val) {
        Class<?> targetType = field.getType();
        if (targetType == String.class) {
            return val;
        }
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(val);
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.valueOf(val);
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.valueOf(val);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.valueOf(val);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.valueOf(val);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.valueOf(val);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.valueOf(val);
        } else if (targetType == char.class || targetType == Character.class) {
            return val.trim().charAt(0);
        } else if (targetType == BigDecimal.class) {
            return new BigDecimal(val);
        } else if (targetType == BigInteger.class) {
            return new BigInteger(val);
        }
        return null;
    }

    public boolean isCollectionType(Field field) {
        if (field == null) {
            return false;
        }
        Class<?> fieldType = field.getType();
        boolean isCollection = Collection.class.isAssignableFrom(fieldType);
        return isCollection && field.getGenericType() instanceof ParameterizedType;
    }

    public boolean isDateType(Field field) {
        return field != null && Date.class.isAssignableFrom(field.getType());
    }

    public void setDateTypeValue(Object target, Field field, String val, String format) throws ParseException, IllegalAccessException {
        if (format == null || format.trim().isEmpty()) {
            throw new RuntimeException("field " + field.getName() + " annotation dateFormat is empty!");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        Date date = sdf.parse(val);

        field.set(target, date);
    }
}
