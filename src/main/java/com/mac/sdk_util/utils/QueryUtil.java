package com.mac.sdk_util.utils;

import java.util.ArrayList;
import java.util.List;

public class QueryUtil {

    protected static List<String> FIELD_OPERATOR_LIST = new ArrayList<>();

    protected static List<String> GROUP_OPERATOR_LIST = new ArrayList<>();

    static {
        FIELD_OPERATOR_LIST.add(FieldOperator.EQUAL);
        FIELD_OPERATOR_LIST.add(FieldOperator.NOT_EQUAL);
        FIELD_OPERATOR_LIST.add(FieldOperator.GREATER_THAN);
        FIELD_OPERATOR_LIST.add(FieldOperator.GREATER_THAN_EQUAL);
        FIELD_OPERATOR_LIST.add(FieldOperator.LESS_THAN);
        FIELD_OPERATOR_LIST.add(FieldOperator.LESS_THAN_EQUAL);
        FIELD_OPERATOR_LIST.add(FieldOperator.INCLUDES);
        FIELD_OPERATOR_LIST.add(FieldOperator.NOT_INCLUDES);
        FIELD_OPERATOR_LIST.add(FieldOperator.BETWEEN);

        GROUP_OPERATOR_LIST.add(GroupOperator.AND);
        GROUP_OPERATOR_LIST.add(GroupOperator.OR);
    }

    public static class FieldOperator {
        
        public static final String EQUAL = "=";
        public static final String NOT_EQUAL = "!=";
        public static final String GREATER_THAN = ">";
        public static final String GREATER_THAN_EQUAL = ">=";
        public static final String LESS_THAN = "<";
        public static final String LESS_THAN_EQUAL = "<=";
        public static final String INCLUDES = "IN";
        public static final String NOT_INCLUDES = "NOT IN";
        public static final String BETWEEN = "BETWEEN";
        public static final String ILIKE = "ILIKE";
        public static final String NOT_ILIKE = "NOT ILIKE";
    }

    public static class GroupOperator {

        public static final String AND = "AND";
        public static final String OR = "OR";
    }

    public static class DataType {

        public static final String TEXT = "TEXT";
        public static final String NUMBER = "NUMERIC";
        public static final String BOOLEAN = "BOOLEAN";
        public static final String DATETIME = "TIMESTAMP";
    }

    public static class ModifierOperator {

        public static final String DEFAULT = "DEFAULT";
        public static final String COUNT = "COUNT";
        public static final String COUNT_UNIQUE = "COUNT_UNIQUE";
        public static final String SUM = "SUM";
        public static final String AVERAGE = "AVERAGE";
    }

}
