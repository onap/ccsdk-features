package org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.tai;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.lang.Class;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.TMcc;
import org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.TMnc;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Class that builds {@link PlmnIdBuilder} instances.
 *
 * @see PlmnIdBuilder
 *
 */
public class PlmnIdBuilder implements Builder<PlmnId> {

    private TMcc _mCC;
    private TMnc _mNC;


    Map<Class<? extends Augmentation<PlmnId>>, Augmentation<PlmnId>> augmentation = Collections.emptyMap();

    public PlmnIdBuilder() {
    }
    public PlmnIdBuilder(org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.PLMNId arg) {
        this._mCC = arg.getMCC();
        this._mNC = arg.getMNC();
    }

    public PlmnIdBuilder(PlmnId base) {
        this._mCC = base.getMCC();
        this._mNC = base.getMNC();
        if (base instanceof PlmnIdImpl) {
            PlmnIdImpl impl = (PlmnIdImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            Map<Class<? extends Augmentation<PlmnId>>, Augmentation<PlmnId>> aug =((AugmentationHolder<PlmnId>) base).augmentations();
            if (!aug.isEmpty()) {
                this.augmentation = new HashMap<>(aug);
            }
        }
    }

    /**
     * Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.PLMNId</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(DataObject arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.PLMNId) {
            this._mCC = ((org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.PLMNId)arg).getMCC();
            this._mNC = ((org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.PLMNId)arg).getMNC();
            isValidArg = true;
        }
        CodeHelpers.validValue(isValidArg, arg, "[org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.PLMNId]");
    }

    public TMcc getMCC() {
        return _mCC;
    }
    
    public TMnc getMNC() {
        return _mNC;
    }
    
    @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
    public <E$$ extends Augmentation<PlmnId>> E$$ augmentation(Class<E$$> augmentationType) {
        return (E$$) augmentation.get(CodeHelpers.nonNullValue(augmentationType, "augmentationType"));
    }

    
    public PlmnIdBuilder setMCC(final TMcc value) {
        this._mCC = value;
        return this;
    }
    
    public PlmnIdBuilder setMNC(final TMnc value) {
        this._mNC = value;
        return this;
    }
    
    public PlmnIdBuilder addAugmentation(Class<? extends Augmentation<PlmnId>> augmentationType, Augmentation<PlmnId> augmentationValue) {
        if (augmentationValue == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentationValue);
        return this;
    }
    
    public PlmnIdBuilder removeAugmentation(Class<? extends Augmentation<PlmnId>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public PlmnId build() {
        return new PlmnIdImpl(this);
    }

    private static final class PlmnIdImpl implements PlmnId {
    
        private final TMcc _mCC;
        private final TMnc _mNC;
    
        private Map<Class<? extends Augmentation<PlmnId>>, Augmentation<PlmnId>> augmentation = Collections.emptyMap();
    
        PlmnIdImpl(PlmnIdBuilder base) {
            this._mCC = base.getMCC();
            this._mNC = base.getMNC();
            this.augmentation = ImmutableMap.copyOf(base.augmentation);
        }
    
        @Override
        public Class<PlmnId> getImplementedInterface() {
            return PlmnId.class;
        }
    
        @Override
        public TMcc getMCC() {
            return _mCC;
        }
        
        @Override
        public TMnc getMNC() {
            return _mNC;
        }
        
        @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
        @Override
        public <E$$ extends Augmentation<PlmnId>> E$$ augmentation(Class<E$$> augmentationType) {
            return (E$$) augmentation.get(CodeHelpers.nonNullValue(augmentationType, "augmentationType"));
        }
    
        private int hash = 0;
        private volatile boolean hashValid = false;
        
        @Override
        public int hashCode() {
            if (hashValid) {
                return hash;
            }
        
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(_mCC);
            result = prime * result + Objects.hashCode(_mNC);
            result = prime * result + Objects.hashCode(augmentation);
        
            hash = result;
            hashValid = true;
            return result;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!PlmnId.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            PlmnId other = (PlmnId)obj;
            if (!Objects.equals(_mCC, other.getMCC())) {
                return false;
            }
            if (!Objects.equals(_mNC, other.getMNC())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                PlmnIdImpl otherImpl = (PlmnIdImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<Class<? extends Augmentation<PlmnId>>, Augmentation<PlmnId>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.augmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }
    
        @Override
        public String toString() {
            final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("PlmnId");
            CodeHelpers.appendValue(helper, "_mCC", _mCC);
            CodeHelpers.appendValue(helper, "_mNC", _mNC);
            CodeHelpers.appendValue(helper, "augmentation", augmentation.values());
            return helper.toString();
        }
    }
}
