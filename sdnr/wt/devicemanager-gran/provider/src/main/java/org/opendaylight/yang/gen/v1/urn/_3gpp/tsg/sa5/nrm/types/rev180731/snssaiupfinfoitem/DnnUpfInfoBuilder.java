package org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.snssaiupfinfoitem;
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
import org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.TDnn;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Class that builds {@link DnnUpfInfoBuilder} instances.
 *
 * @see DnnUpfInfoBuilder
 *
 */
public class DnnUpfInfoBuilder implements Builder<DnnUpfInfo> {

    private TDnn _dnn;
    private DnnUpfInfoKey key;


    Map<Class<? extends Augmentation<DnnUpfInfo>>, Augmentation<DnnUpfInfo>> augmentation = Collections.emptyMap();

    public DnnUpfInfoBuilder() {
    }
    public DnnUpfInfoBuilder(org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.DnnUpfInfoItem arg) {
        this._dnn = arg.getDnn();
    }

    public DnnUpfInfoBuilder(DnnUpfInfo base) {
        this.key = base.key();
        this._dnn = base.getDnn();
        if (base instanceof DnnUpfInfoImpl) {
            DnnUpfInfoImpl impl = (DnnUpfInfoImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            Map<Class<? extends Augmentation<DnnUpfInfo>>, Augmentation<DnnUpfInfo>> aug =((AugmentationHolder<DnnUpfInfo>) base).augmentations();
            if (!aug.isEmpty()) {
                this.augmentation = new HashMap<>(aug);
            }
        }
    }

    /**
     * Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.DnnUpfInfoItem</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(DataObject arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.DnnUpfInfoItem) {
            this._dnn = ((org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.DnnUpfInfoItem)arg).getDnn();
            isValidArg = true;
        }
        CodeHelpers.validValue(isValidArg, arg, "[org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.DnnUpfInfoItem]");
    }

    public DnnUpfInfoKey key() {
        return key;
    }
    
    public TDnn getDnn() {
        return _dnn;
    }
    
    @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
    public <E$$ extends Augmentation<DnnUpfInfo>> E$$ augmentation(Class<E$$> augmentationType) {
        return (E$$) augmentation.get(CodeHelpers.nonNullValue(augmentationType, "augmentationType"));
    }

    public DnnUpfInfoBuilder withKey(final DnnUpfInfoKey key) {
        this.key = key;
        return this;
    }
    
    public DnnUpfInfoBuilder setDnn(final TDnn value) {
        this._dnn = value;
        return this;
    }
    
    public DnnUpfInfoBuilder addAugmentation(Class<? extends Augmentation<DnnUpfInfo>> augmentationType, Augmentation<DnnUpfInfo> augmentationValue) {
        if (augmentationValue == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentationValue);
        return this;
    }
    
    public DnnUpfInfoBuilder removeAugmentation(Class<? extends Augmentation<DnnUpfInfo>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public DnnUpfInfo build() {
        return new DnnUpfInfoImpl(this);
    }

    private static final class DnnUpfInfoImpl implements DnnUpfInfo {
    
        private final TDnn _dnn;
        private final DnnUpfInfoKey key;
    
        private Map<Class<? extends Augmentation<DnnUpfInfo>>, Augmentation<DnnUpfInfo>> augmentation = Collections.emptyMap();
    
        DnnUpfInfoImpl(DnnUpfInfoBuilder base) {
            if (base.key() != null) {
                this.key = base.key();
            } else {
                this.key = new DnnUpfInfoKey(base.getDnn());
            }
            this._dnn = key.getDnn();
            this.augmentation = ImmutableMap.copyOf(base.augmentation);
        }
    
        @Override
        public Class<DnnUpfInfo> getImplementedInterface() {
            return DnnUpfInfo.class;
        }
    
        @Override
        public DnnUpfInfoKey key() {
            return key;
        }
        
        @Override
        public TDnn getDnn() {
            return _dnn;
        }
        
        @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
        @Override
        public <E$$ extends Augmentation<DnnUpfInfo>> E$$ augmentation(Class<E$$> augmentationType) {
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
            result = prime * result + Objects.hashCode(_dnn);
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
            if (!DnnUpfInfo.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            DnnUpfInfo other = (DnnUpfInfo)obj;
            if (!Objects.equals(_dnn, other.getDnn())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                DnnUpfInfoImpl otherImpl = (DnnUpfInfoImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<Class<? extends Augmentation<DnnUpfInfo>>, Augmentation<DnnUpfInfo>> e : augmentation.entrySet()) {
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
            final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("DnnUpfInfo");
            CodeHelpers.appendValue(helper, "_dnn", _dnn);
            CodeHelpers.appendValue(helper, "augmentation", augmentation.values());
            return helper.toString();
        }
    }
}
