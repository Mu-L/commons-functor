/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.functor.range;

import java.util.Iterator;

import org.apache.commons.functor.BinaryFunction;
import org.apache.commons.lang3.Validate;

/**
 * A generator for a range of characters.
 *
 * @since 1.0
 * @version $Revision$ $Date$
 */
public final class CharacterRange extends AbstractRange<Character, Integer> {

    // attributes
    // ---------------------------------------------------------------
    /**
     * Default left bound type.
     */
    public static final BoundType DEFAULT_LEFT_BOUND_TYPE = BoundType.CLOSED;

    /**
     * Default right bound type.
     */
    public static final BoundType DEFAULT_RIGHT_BOUND_TYPE = BoundType.CLOSED;

    /**
     * Current value.
     */
    private char currentValue;

    /**
     * Calculate default step.
     */
    public static final BinaryFunction<Character, Character, Integer> DEFAULT_STEP
        = new BinaryFunction<Character, Character, Integer>() {

        public Integer evaluate(Character left, Character right) {
            return left > right ? -1 : 1;
        }
    };

    // constructors
    // ---------------------------------------------------------------
    /**
     * Create a new CharacterRange.
     *
     * @param from start
     * @param to end
     */
    public CharacterRange(char from, char to) {
        this(from, to, DEFAULT_STEP.evaluate(from, to).intValue());
    }

    /**
     * Create a new CharacterRange.
     *
     * @param from start
     * @param to end
     * @param step increment
     */
    public CharacterRange(char from, char to, int step) {
        this(from, BoundType.CLOSED, to, BoundType.CLOSED, step);
    }

    /**
     * Create a new CharacterRange.
     *
     * @param from start
     * @param to end
     * @throws NullPointerException if either {@link Endpoint} is {@code null}
     */
    public CharacterRange(Endpoint<Character> from, Endpoint<Character> to) {
        this(from, to, DEFAULT_STEP.evaluate(from.getValue(), to.getValue()));
    }

    /**
     * Create a new CharacterRange.
     *
     * @param from start
     * @param to end
     * @param step increment
     * @throws NullPointerException if either {@link Endpoint} is {@code null}
     */
    public CharacterRange(Endpoint<Character> from, Endpoint<Character> to, int step) {
        super(from, to, Integer.valueOf(step));
        final char f = from.getValue();
        final char t = to.getValue();

        Validate.isTrue(f == t || Integer.signum(step) == Integer.signum(t - f),
            "Will never reach '%s' from '%s' using step %s", t, f, step);

        if (from.getBoundType() == BoundType.CLOSED) {
            this.currentValue = f;
        } else {
            this.currentValue = (char) (f + step);
        }
    }

    /**
     * Create a new CharacterRange.
     *
     * @param from start
     * @param leftBoundType type of left bound
     * @param to end
     * @param rightBoundType type of right bound
     * @throws NullPointerException if either bound type is {@code null}
     */
    public CharacterRange(char from, BoundType leftBoundType, char to, BoundType rightBoundType) {
        this(from, leftBoundType, to, rightBoundType, DEFAULT_STEP.evaluate(from, to));
    }

    /**
     * Create a new CharacterRange.
     *
     * @param from start
     * @param leftBoundType type of left bound
     * @param to end
     * @param rightBoundType type of right bound
     * @param step increment
     * @throws NullPointerException if either bound type is {@code null}
     */
    public CharacterRange(char from, BoundType leftBoundType, char to, BoundType rightBoundType, int step) {
        this(new Endpoint<Character>(from, leftBoundType), new Endpoint<Character>(to, rightBoundType), step);
    }

    // range methods
    // ---------------------------------------------------------------

    // iterable, iterator methods
    // ---------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        final int to = this.rightEndpoint.getValue();
        if (step < 0) {
            if (this.rightEndpoint.getBoundType() == BoundType.CLOSED) {
                return this.currentValue >= to;
            } else {
                return this.currentValue > to;
            }
        } else {
            if (this.rightEndpoint.getBoundType() == BoundType.CLOSED) {
                return this.currentValue <= to;
            } else {
                return this.currentValue < to;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Character next() {
        final int step = this.getStep();
        final char r = this.currentValue;
        this.currentValue += step;
        return Character.valueOf(r);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Character> iterator() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        double leftValue = this.getLeftEndpoint().getValue().charValue();
        double rightValue = this.getRightEndpoint().getValue().charValue();
        boolean closedLeft = this.getLeftEndpoint().getBoundType() == BoundType.CLOSED;
        boolean closedRight = this.getRightEndpoint().getBoundType() == BoundType.CLOSED;
        if (!closedLeft && !closedRight
                && this.getLeftEndpoint().equals(this.getRightEndpoint())) {
            return Boolean.TRUE;
        }
        double step = this.getStep().intValue();
        if (step > 0.0) {
            double firstValue = closedLeft ? leftValue : leftValue + step;
            return closedRight ? firstValue > rightValue
                              : firstValue >= rightValue;
        } else {
            double firstValue = closedLeft ? leftValue : leftValue + step;
            return closedRight ? firstValue < rightValue
                              : firstValue <= rightValue;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(Character obj) {
        if (obj == null) {
            return Boolean.FALSE;
        }
        char leftValue = this.getLeftEndpoint().getValue().charValue();
        char rightValue = this.getRightEndpoint().getValue().charValue();
        boolean includeLeft = this.getLeftEndpoint().getBoundType() == BoundType.CLOSED;
        boolean includeRight = this.getRightEndpoint().getBoundType() == BoundType.CLOSED;
        int step = this.getStep().intValue();
        int value = (int) obj.charValue();

        int firstValue = 0;
        int lastValue = 0;

        if (step < 0.0) {
            firstValue = includeLeft ? leftValue : leftValue + step;
            lastValue = includeRight ? rightValue : rightValue + 1;
            if (value > firstValue || value < lastValue) {
                return Boolean.FALSE;
            }
        } else {
            firstValue = includeLeft ? leftValue : leftValue + step;
            lastValue = includeRight ? rightValue : rightValue - 1;
            if (value < firstValue || value > lastValue) {
                return Boolean.FALSE;
            }
        }
        return ((double) (value - firstValue) / step + 1) % 1.0 == 0.0;
    }

}