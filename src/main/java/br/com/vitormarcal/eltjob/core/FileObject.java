package br.com.vitormarcal.eltjob.core;

import org.apache.commons.beanutils.PropertyUtilsBean;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public interface FileObject extends Serializable {

    default Map<String, String> mapeiaFields() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        if (mapeiaFiledsHookMethod()) {
            Map<String, String> mapaTemporario = new LinkedHashMap<>();
            for (Field field : this.getClass().getDeclaredFields()) {
                if (isFieldSerializable(field)) {
                    Optional property = Optional.ofNullable(propertyUtilsBean.getProperty(this, field.getName()));
                    if (property.isPresent() && FileObject.class.isAssignableFrom((property.get().getClass()))) {
                        mapaTemporario.putAll(((FileObject) property.get()).mapeiaFields());
                        continue;
                    }
                    mapaTemporario.put(getNameField(field), property.isPresent() ? trataSeCollection(property.get().toString()) : "");
                }
            }
            getFields().putAll(mapaTemporario);
        }
        return getFields();
    }

    default String trataSeCollection(String string) {
        if (string != null && string.startsWith("[") && string.endsWith("]")) return string.subSequence(1, string.length() - 1).toString();
        return string;
    }

    boolean isFieldSerializable(Field field);

    String getNameField(Field field);

    Map<String, String> getFields();

    boolean mapeiaFiledsHookMethod();
}
