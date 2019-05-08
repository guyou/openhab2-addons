package org.openhab.binding.freeboxv5.model;

public class UpDownValue<ValueType extends Number> {
    public final ValueType up;
    public final ValueType down;

    public UpDownValue(ValueType down, ValueType up) {
        this.up = up;
        this.down = down;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof UpDownValue<?>) {
            UpDownValue<?> otherValue = (UpDownValue<?>) other;
            return this.up.equals(otherValue.up) && this.down.equals(otherValue.down);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "{up=" + up.toString() + ",down=" + down.toString() + "}";
    }
}
