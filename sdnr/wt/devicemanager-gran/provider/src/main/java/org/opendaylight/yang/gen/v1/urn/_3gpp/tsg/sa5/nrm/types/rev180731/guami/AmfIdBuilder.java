package org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.guami;
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
import org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.TAMFPointer;
import org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.TAMFRegionId;
import org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.TAMFSetId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Class that builds {@link AmfIdBuilder} instances.
 *
 * @see AmfIdBuilder
 *
 */
public class AmfIdBuilder implements Builder<AmfId> {

    private TAMFPointer _aMFPointer;
    private TAMFRegionId _aMFRegionId;
    private TAMFSetId _aMFSetId;


    Map<Class<? extends Augmentation<AmfId>>, Augmentation<AmfId>> augmentation = Collections.emptyMap();

    public AmfIdBuilder() {
    }
    public AmfIdBuilder(org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.AMFIdentifier arg) {
        this._aMFRegionId = arg.getAMFRegionId();
        this._aMFSetId = arg.getAMFSetId();
        this._aMFPointer = arg.getAMFPointer();
    }

    public AmfIdBuilder(AmfId base) {
        this._aMFPointer = base.getAMFPointer();
        this._aMFRegionId = base.getAMFRegionId();
        this._aMFSetId = base.getAMFSetId();
        if (base instanceof AmfIdImpl) {
            AmfIdImpl impl = (AmfIdImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            Map<Class<? extends Augmentation<AmfId>>, Augmentation<AmfId>> aug =((AugmentationHolder<AmfId>) base).augmentations();
            if (!aug.isEmpty()) {
                this.augmentation = new HashMap<>(aug);
            }
        }
    }

    /**
     * Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.AMFIdentifier</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(DataObject arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.AMFIdentifier) {
            this._aMFRegionId = ((org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.AMFIdentifier)arg).getAMFRegionId();
            this._aMFSetId = ((org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.AMFIdentifier)arg).getAMFSetId();
            this._aMFPointer = ((org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.AMFIdentifier)arg).getAMFPointer();
            isValidArg = true;
        }
        CodeHelpers.validValue(isValidArg, arg, "[org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.AMFIdentifier]");
    }

    public TAMFPointer getAMFPointer() {
        return _aMFPointer;
    }
    
    public TAMFRegionId getAMFRegionId() {
        return _aMFRegionId;
    }
    
    public TAMFSetId getAMFSetId() {
        return _aMFSetId;
    }
    
    @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
    public <E$$ extends Augmentation<AmfId>> E$$ augmentation(Class<E$$> augmentationType) {
        return (E$$) augmentation.get(CodeHelpers.nonNullValue(augmentationType, "augmentationType"));
    }

    
    public AmfIdBuilder setAMFPointer(final TAMFPointer value) {
        this._aMFPointer = value;
        return this;
    }
    
    public AmfIdBuilder setAMFRegionId(final TAMFRegionId value) {
        this._aMFRegionId = value;
        return this;
    }
    
    public AmfIdBuilder setAMFSetId(final TAMFSetId value) {
        this._aMFSetId = value;
        return this;
    }
    
    public AmfIdBuilder addAugmentation(Class<? extends Augmentation<AmfId>> augmentationType, Augmentation<AmfId> augmentationValue) {
        if (augmentationValue == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentationValue);
        return this;
    }
    
    public AmfIdBuilder removeAugmentation(Class<? extends Augmentation<AmfId>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public AmfId build() {
        return new AmfIdImpl(this);
    }

    private static final class AmfIdImpl implements AmfId {
    
        private final TAMFPointer _aMFPointer;
        private final TAMFRegionId _aMFRegionId;
        private final TAMFSetId _aMFSetId;
    
        private Map<Class<? extends Augmentation<AmfId>>, Augmentation<AmfId>> augmentation = Collections.emptyMap();
    
        AmfIdImpl(AmfIdBuilder base) {
            this._aMFPointer = base.getAMFPointer();
            this._aMFRegionId = base.getAMFRegionId();
            this._aMFSetId = base.getAMFSetId();
            this.augmentation = ImmutableMap.copyOf(base.augmentation);
        }
    
        @Override
        public Class<AmfId> getImplementedInterface() {
            return AmfId.class;
        }
    
        @Override
        public TAMFPointer getAMFPointer() {
            return _aMFPointer;
        }
        
        @Override
        public TAMFRegionId getAMFRegionId() {
            return _aMFRegionId;
        }
        
        @Override
        public TAMFSetId getAMFSetId() {
            return _aMFSetId;
        }
        
        @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
        @Override
        public <E$$ extends Augmentation<AmfId>> E$$ augmentation(Class<E$$> augmentationType) {
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
            result = prime * result + Objects.hashCode(_aMFPointer);
            result = prime * result + Objects.hashCode(_aMFRegionId);
            result = prime * result + Objects.hashCode(_aMFSetId);
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
            if (!AmfId.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            AmfId other = (AmfId)obj;
            if (!Objects.equals(_aMFPointer, other.getAMFPointer())) {
                return false;
            }
            if (!Objects.equals(_aMFRegionId, other.getAMFRegionId())) {
                return false;
            }
            if (!Objects.equals(_aMFSetId, other.getAMFSetId())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                AmfIdImpl otherImpl = (AmfIdImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<Class<? extends Augmentation<AmfId>>, Augmentation<AmfId>> e : augmentation.entrySet()) {
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
            final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("AmfId");
            CodeHelpers.appendValue(helper, "_aMFPointer", _aMFPointer);
            CodeHelpers.appendValue(helper, "_aMFRegionId", _aMFRegionId);
            CodeHelpers.appendValue(helper, "_aMFSetId", _aMFSetId);
            CodeHelpers.appendValue(helper, "augmentation", augmentation.values());
            return helper.toString();
        }
    }
}
