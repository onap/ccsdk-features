/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.lib.AbstractAugmentable;

/**
 * Class that builds {@link UserdataBuilder} instances. Overall design of the class is that of a
 * <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>, where method chaining is used.
 *
 * <p>
 * In general, this class is supposed to be used like this template:
 *
 * <pre>
 *   <code>
 *     UserdataBuilder createTarget(int fooXyzzy, int barBaz) {
 *         return new UserdataBuilderBuilder()
 *             .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())
 *             .setBar(new BarBuilder().setBaz(barBaz).build())
 *             .build();
 *     }
 *   </code>
 * </pre>
 *
 * <p>
 * This pattern is supported by the immutable nature of UserdataBuilder, as instances can be freely passed around
 * without worrying about synchronization issues.
 *
 * <p>
 * As a side note: method chaining results in:
 * <ul>
 * <li>very efficient Java bytecode, as the method invocation result, in this case the Builder reference, is on the
 * stack, so further method invocations just need to fill method arguments for the next method invocation, which is
 * terminated by {@link #build()}, which is then returned from the method</li>
 * <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a minimum and is very
 * localized</li>
 * <li>better optimization oportunities, as the object scope is minimized in terms of invocation (rather than method)
 * stack, making <a href="https://en.wikipedia.org/wiki/Escape_analysis">escape analysis</a> a lot easier. Given enough
 * compiler (JIT/AOT) prowess, the cost of th builder object can be completely eliminated</li>
 * </ul>
 *
 * @see UserdataBuilder
 * @see Builder
 *
 */
@Generated("mdsal-binding-generator")
public class UserdataBuilder {

    private String _id;
    private String _value;


    Map<Class<? extends Augmentation<Userdata>>, Augmentation<Userdata>> augmentation = Collections.emptyMap();

    public UserdataBuilder() {}



    public UserdataBuilder(Userdata base) {
        Map<Class<? extends Augmentation<Userdata>>, Augmentation<Userdata>> aug = base.augmentations();
        if (!aug.isEmpty()) {
            this.augmentation = new HashMap<>(aug);
        }
        this._id = base.getId();
        this._value = base.getValue();
    }


    public String getId() {
        return _id;
    }

    public String getValue() {
        return _value;
    }

    @SuppressWarnings({"unchecked", "checkstyle:methodTypeParameterName"})
    public <E$$ extends Augmentation<Userdata>> E$$ augmentation(Class<E$$> augmentationType) {
        return (E$$) augmentation.get(Objects.requireNonNull(augmentationType));
    }


    public UserdataBuilder setId(final String value) {
        this._id = value;
        return this;
    }

    public UserdataBuilder setValue(final String value) {
        this._value = value;
        return this;
    }

    /**
     * Add an augmentation to this builder's product.
     *
     * @param augmentation augmentation to be added
     * @return this builder
     * @throws NullPointerException if {@code augmentation} is null
     */
    public UserdataBuilder addAugmentation(Augmentation<Userdata> augmentation) {
        Class<? extends Augmentation<Userdata>> augmentationType = augmentation.implementedInterface();
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }

        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    /**
     * Remove an augmentation from this builder's product. If this builder does not track such an augmentation type,
     * this method does nothing.
     *
     * @param augmentationType augmentation type to be removed
     * @return this builder
     */
    public UserdataBuilder removeAugmentation(Class<? extends Augmentation<Userdata>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }


    public Userdata build() {
        return new UserdataImpl(this);
    }

    private static final class UserdataImpl extends AbstractAugmentable<Userdata> implements Userdata {

        private final String _id;
        private final String _value;

        UserdataImpl(UserdataBuilder base) {
            super(base.augmentation);
            this._id = base.getId();
            this._value = base.getValue();
        }

        @Override
        public String getId() {
            return _id;
        }

        @Override
        public String getValue() {
            return _value;
        }

        private int hash = 0;
        private volatile boolean hashValid = false;

        @Override
        public int hashCode() {
            if (hashValid) {
                return hash;
            }

            final int result = Userdata.bindingHashCode(this);
            hash = result;
            hashValid = true;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return Userdata.bindingEquals(this, obj);
        }

        @Override
        public String toString() {
            return Userdata.bindingToString(this);
        }

        @Override
        public Class<? extends DataObject> implementedInterface() {
            return Userdata.class;
        }
    }
}
