package ru.linachan.mysql;

import ru.linachan.yggdrasil.common.vector.Vector3;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class MySQLQuery<T extends MySQLTable> {

    private MySQLDBManager sqlDBManager;

    private List<Vector3<String, Boolean, Object>> filters = new ArrayList<>();

    private int limit = 0;
    private Boolean limitSet = false;

    private int offset = 0;
    private Boolean offsetSet = false;

    private String orderBy = "";
    private Boolean orderByAsc = false;
    private Boolean orderBySet = false;

    MySQLQuery(MySQLDBManager dbManager) {
        sqlDBManager = dbManager;
    }

    private Class<T> getGenericTypeClass() {
        return ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    private Boolean hasColumn(String column) {
        for (Field field : getGenericTypeClass().getFields()) {
            if (field.getName().equals(column)&&field.isAccessible()) {
                return true;
            }
        }
        return false;
    }

    public MySQLQuery<T> eq(String field, Object value) throws CloneNotSupportedException {
        if (hasColumn(field)) {
            filters.add(new Vector3<>(field, true, value));
        }
        return (MySQLQuery<T>) this.clone();
    }

    public MySQLQuery<T> like(String field, Object value) throws CloneNotSupportedException {
        if (hasColumn(field)) {
            filters.add(new Vector3<>(field, false, value));
        }
        return (MySQLQuery<T>) this.clone();
    }

    public MySQLQuery<T> orderBy(String field, Boolean asc) throws CloneNotSupportedException {
        if (hasColumn(field)) {
            this.orderBySet = true;
            this.orderBy = field;
            this.orderByAsc = asc;
        }
        return (MySQLQuery<T>) this.clone();
    }

    public MySQLQuery<T> limit(int limit) throws CloneNotSupportedException {
        this.limitSet = true;
        this.limit = limit;
        return (MySQLQuery<T>) this.clone();
    }

    public MySQLQuery<T> limit(int limit, int offset) throws CloneNotSupportedException {
        this.limitSet = true;
        this.offsetSet = true;
        this.limit = limit;
        this.offset = offset;
        return (MySQLQuery<T>) this.clone();
    }

    private ResultSet query(String expression) throws SQLException {
        String SQL_STATEMENT = String.format(
            "SELECT %s FROM `%s` WHERE ",
            expression, getGenericTypeClass().getAnnotation(Table.class).name()
        );

        for (Vector3<String, Boolean, Object> filter : filters) {
            String column = filter.getX();
            String value = (String) filter.getZ();

            if (filter.getY()) {
                SQL_STATEMENT += column + " = '" + value + "'";
            } else {
                SQL_STATEMENT += "(";
                SQL_STATEMENT += column + " LIKE '" + value + "'";
                SQL_STATEMENT += " OR " + column + " LIKE '%" + value + "'";
                SQL_STATEMENT += " OR " + column + " LIKE '" + value + "%'";
                SQL_STATEMENT += " OR " + column + " LIKE '%" + value + "%'";
                SQL_STATEMENT += ")";
            }
            SQL_STATEMENT += " AND ";
        }
        SQL_STATEMENT += "1";

        if (orderBySet) {
            SQL_STATEMENT += " ORDER BY " + orderBy + ((orderByAsc) ? " ASC" : " DESC");
        }

        if (limitSet) {
            if (offsetSet) {
                SQL_STATEMENT += " LIMIT " + String.valueOf(offset) + ", " + String.valueOf(limit);
            } else {
                SQL_STATEMENT += " LIMIT " + String.valueOf(limit);
            }
        }

        Statement statement = sqlDBManager.getStatement();
        return statement.executeQuery(SQL_STATEMENT);
    }

    public List<T> all() throws SQLException, IllegalAccessException, InstantiationException {
        ResultSet res = query("*");

        List<T> res_queue = new ArrayList<>();

        while (res.next()) {
            T result = getGenericTypeClass().newInstance();
            for (Field field: result.getClass().getFields()) {
                if(field.isAccessible()&&MySQLType.class.isAssignableFrom(field.getType())) {
                    Class<? extends MySQLType<?>> fieldType = (Class<? extends MySQLType<?>>) field.getType();

                    MySQLType<?> fieldValue = fieldType.newInstance();
                    fieldValue.setValue(res.getString(field.getName()));

                    field.set(result, fieldValue);
                }
            }
            res_queue.add(result);
        }

        return res_queue;
    }

    public int count() throws SQLException {
        ResultSet res = query("COUNT(*)");
        if(res.next()) {
            return res.getInt(1);
        } else {
            return 0;
        }
    }
}
