package br.com.vitormarcal.eltjob.core;

import lombok.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractFileObject implements FileObject {
    private transient Map<String, String> fields;

    @Override
    public boolean isFieldSerializable(@NonNull Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers());
    }

    @Override
    public String getNameField(@NonNull Field field) {
        if (field.isAnnotationPresent(Coluna.class)) {
            Coluna annotation = field.getAnnotation(Coluna.class);
            return annotation.tabular();
        }
        return field.getName();
    }

    @Override
    public final Map<String, String> getFields() {
        if (fields == null) fields = new LinkedHashMap<>();
        return fields;
    }

    @Override
    public boolean mapeiaFiledsHookMethod() {
        return fields == null;
    }
}
