package ru.shestakov_a.parser;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Методом getFields возвращается разбиение переданного объекта на составные
 * поля примитивов и поля объектов базовых классов (из пакета java.*).
 * Метод getFieldNames возвращает сокращенные названия данных полей.
 * Если переданный объект не имеет массивов, коллекций, а также полей
 * с null-значением, то класс методом getBaseObjectList, возвращает
 * значения полей объекта, методом getStringValueFromObjects
 * возвращает их строковое представление.
 *
 * @author shestakov_a
 */
public class FieldParser {
    private static final String ANY_BASE_JAVA_OBJECT = "java";
    private static final String REGEX_FIELD_ARRAY = "\\[\\]";
    private static final String EMPTY = "";
    private static final int INITIAL_COUNTER_VALUE = 0;

    private final Object inputObject;
    private final Stack<Field> fieldStack = new Stack<>();
    private final Stack<Object> objectStack = new Stack<>();
    private final HashMap<Object, Integer> actualCounters = new HashMap<>();
    private final HashMap<Object, Integer> explainCounters = new HashMap<>();
    private List<Object> resultListObject = null;
    private List<Field> resultListField = null;


    public FieldParser(Object inputObject) {
        if (inputObject == null) {
            throw new IllegalArgumentException();
        }
        this.inputObject = inputObject;
        init();
    }


    public List<Object> getBaseObjectList() throws ClassNotFoundException, IllegalAccessException {
        if (resultListObject == null) {
            resultListObject = getObjectList();
        }
        return resultListObject;
    }


    public List<Field> getFields() throws ClassNotFoundException {
        if (resultListField == null) {
            resultListField = getFieldList();
        }
        return resultListField;
    }


    public List<String> getFieldNames() throws ClassNotFoundException {
        return getFields()
                .stream()
                .filter(FieldParser::isBaseObject)
                .map(Field::getName)
                .collect(Collectors.toList());
    }


    public List<String> getStringValueFromObjects() throws ClassNotFoundException, IllegalAccessException {
        return getBaseObjectList()
                .stream()
                .sequential()
                .map(Object::toString)
                .collect(Collectors.toList());
    }


    private void init() {
        objectStack.push(inputObject);
        actualCounters.put(inputObject, INITIAL_COUNTER_VALUE);
        explainCounters.put(inputObject, inputObject.getClass().getDeclaredFields().length);
        Arrays.stream(inputObject.getClass().getDeclaredFields())
                .sequential()
                .forEach(fieldStack::push);
    }


    private List<Object> getObjectList() throws ClassNotFoundException, IllegalAccessException {
        List<Field> fieldList = getFields();
        List<Object> resultList = new ArrayList<>();

        for (Field field : fieldList) {
            field.setAccessible(true);
            Object object = objectStack.peek();

            initObject(object);
            object = changeObject(object);

            if (isBaseObject(field)) {
                resultList.add(field.get(object));
            } else {
                Object objectToStack = field.get(object);
                objectStack.push(objectToStack);
            }

            actualCounters.put(object, actualCounters.get(object) + 1);
        }
        return resultList;
    }


    private List<Field> getFieldList() throws ClassNotFoundException {
        List<Field> fieldList = new ArrayList<>();

        while (!fieldStack.empty()) {
            Field field = fieldStack.pop();

            if (!isBaseObject(field)) {
                String className = field.getGenericType().getTypeName();
                if (field.getType().isArray()) {
                    className = className.replaceAll(REGEX_FIELD_ARRAY, EMPTY);
                }
                Class<?> clazz = Class.forName(className);
                Arrays.stream(clazz.getDeclaredFields())
                        .sequential()
                        .forEach(fieldStack::push);
            }

            fieldList.add(field);
        }
        return fieldList;
    }


    private Object changeObject(Object object) {
        if (isNeedChangeObject(object)) {
            objectStack.pop();
            return changeObject(objectStack.peek());
        } else {
            return object;
        }
    }


    private void initObject(Object object) {
        if (!actualCounters.containsKey(object)) {
            actualCounters.put(object, INITIAL_COUNTER_VALUE);
            explainCounters.put(object, object.getClass().getDeclaredFields().length);
        }
    }


    private boolean isNeedChangeObject(Object object) {
        return actualCounters.get(object).equals(explainCounters.get(object));
    }


    private static boolean isBaseObject(Field field) {
        return field.getType().isPrimitive() || field.getType().getName().startsWith(ANY_BASE_JAVA_OBJECT);
    }
}