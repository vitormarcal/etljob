package br.com.vitormarcal.eltjob.core;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ResultSetHelper {

    private ResultSetHelper() {
        throw new UnsupportedOperationException("Only helper class");
    }

    public static String getOrBlankString(ResultSet rs, String label) throws SQLException {
        String valor = rs.getString(label);
        if (rs.wasNull()) valor = "";
        return valor.trim();
    }

    public static Long getLongOrZeroValue(ResultSet rs, String label) throws SQLException {
        Long value = rs.getLong(label);
        if (rs.wasNull()) value = 0L;
        return value;
    }

    public static Long getLongOrNullValue(ResultSet rs, String label) throws SQLException {
        Long value = rs.getLong(label);
        if (!rs.wasNull() && value > 0) return value;
        return null;
    }

    public static Long getBigIntAsLongOrNullValue(ResultSet rs, String label) throws SQLException {
        String value = rs.getString(label);
        if (!rs.wasNull() && !value.isEmpty()) return Long.valueOf(value);
        return null;
    }

    public static String getDoubleAsStringOrNullValue(ResultSet rs, String label) throws SQLException {
        BigDecimal value = rs.getBigDecimal(label);
        if (!rs.wasNull() && value != null ) return value.toString();
        return null;
    }

    public static String getDoubleAsStringDecimalCommaOrNullValue(ResultSet rs, String label) throws SQLException {
        String value = getDoubleAsStringOrNullValue(rs, label);
        if (value != null) return value.replaceAll("\\.", ",");
        return null;
    }

    public static String getDateAndHour(ResultSet rs, String label) throws SQLException {
        Timestamp valor = rs.getTimestamp(label);
        if (rs.wasNull()) return "";
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(valor.toLocalDateTime()
                .withNano(0)); //sem milisegundos
    }

    public static String getDate(ResultSet rs, String label) throws SQLException {
        Timestamp valor = rs.getTimestamp(label);
        if (rs.wasNull()) return "";
        return valor.toLocalDateTime().toLocalDate().toString();
    }

    public static LocalDateTime getLocalDateTime(ResultSet rs, String label) throws SQLException {
        Timestamp valor = rs.getTimestamp(label);
        if (rs.wasNull()) return null;
        return valor.toLocalDateTime();
    }

    public static String montarURI(ResultSet rs, String uri, String label) throws SQLException {
        String number = getOrBlankString(rs, label);
        if (number.isEmpty() || Long.valueOf(number) == 0) return "";
        return uri + number;
    }

    public static String getYearOfDate(ResultSet rs, String label) throws SQLException {
        Timestamp valor = rs.getTimestamp(label);
        if (rs.wasNull()) return null;
        LocalDateTime localDateTime = valor.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return localDateTime.format(formatter);
    }

    public static String getMonthOfDate(ResultSet rs, String label) throws SQLException {
        Timestamp valor = rs.getTimestamp(label);
        if (rs.wasNull()) return null;
        LocalDateTime localDateTime = valor.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM");
        return localDateTime.format(formatter);
    }
}