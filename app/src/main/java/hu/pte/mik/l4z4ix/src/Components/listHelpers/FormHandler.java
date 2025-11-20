package hu.pte.mik.l4z4ix.src.Components.listHelpers;

public interface FormHandler<T> {
    void onEditButtonClick(T item);

    void onRemoveButtonClick(T item);
}
