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

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;

/**
 *
 * <p>
 * This class represents the following YANG schema fragment defined in module <b>data-provider</b>
 *
 * <pre>
 * container userdata {
 *   leaf id {
 *     type string;
 *   }
 *   leaf value {
 *     type string;
 *   }
 * }
 * </pre>
 *
 * The schema path to identify an instance is <i>data-provider/userdata</i>
 *
 * <p>
 * To create instances of this class use {@link UserdataBuilder}.
 *
 * @see UserdataBuilder
 *
 */
@Generated("mdsal-binding-generator")
public interface Userdata extends Augmentable<Userdata>, DataObject {

    /**
     * Default implementation of {@link Object#hashCode()} contract for this interface. Implementations of this
     * interface are encouraged to defer to this method to get consistent hashing results across all implementations.
     *
     * @param obj Object for which to generate hashCode() result.
     * @return Hash code value of data modeled by this interface.
     * @throws NullPointerException if {@code obj} is null
     */
    static int bindingHashCode(final Userdata obj) {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(obj.getId());
        result = prime * result + Objects.hashCode(obj.getValue());
        result = prime * result + obj.augmentations().hashCode();
        return result;
    }

    /**
     * Default implementation of {@link Object#equals(Object)} contract for this interface. Implementations of this
     * interface are encouraged to defer to this method to get consistent equality results across all implementations.
     *
     * @param thisObj Object acting as the receiver of equals invocation
     * @param obj Object acting as argument to equals invocation
     * @return True if thisObj and obj are considered equal
     * @throws NullPointerException if {@code thisObj} is null
     */
    static boolean bindingEquals(final Userdata thisObj, final Object obj) {
        if (thisObj == obj) {
            return true;
        }
        final Userdata other = CodeHelpers.checkCast(Userdata.class, obj);
        if (other == null) {
            return false;
        }
        if (!Objects.equals(thisObj.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(thisObj.getValue(), other.getValue())) {
            return false;
        }
        return thisObj.augmentations().equals(other.augmentations());
    }

    /**
     * Default implementation of {@link Object#toString()} contract for this interface. Implementations of this
     * interface are encouraged to defer to this method to get consistent string representations across all
     * implementations.
     *
     * @param obj Object for which to generate toString() result.
     * @return {@link String} value of data modeled by this interface.
     * @throws NullPointerException if {@code obj} is null
     */
    static String bindingToString(final Userdata obj) {
        final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Userdata");
        CodeHelpers.appendValue(helper, "id", obj.getId());
        CodeHelpers.appendValue(helper, "value", obj.getValue());
        CodeHelpers.appendValue(helper, "augmentation", obj.augmentations().values());
        return helper.toString();
    }

    /**
     * Return id, or {@code null} if it is not present.
     *
     * @return {@code java.lang.String} id, or {@code null} if it is not present.
     *
     */
    String getId();

    /**
     * Return value, or {@code null} if it is not present.
     *
     * @return {@code java.lang.String} value, or {@code null} if it is not present.
     *
     */
    String getValue();

}

