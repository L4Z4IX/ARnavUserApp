package com.google.ar.core.examples.java.common.listHelpers;

public interface FormHandler<T> {
    void onEditButtonClick(T item);

    void onRemoveButtonClick(T item);
}
