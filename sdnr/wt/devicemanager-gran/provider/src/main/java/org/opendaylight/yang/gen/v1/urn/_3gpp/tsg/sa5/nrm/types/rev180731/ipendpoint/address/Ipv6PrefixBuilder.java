package org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.ipendpoint.address;
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
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Class that builds {@link Ipv6PrefixBuilder} instances.
 *
 * @see Ipv6PrefixBuilder
 *
 */
public class Ipv6PrefixBuilder implements Builder<Ipv6Prefix> {

    private org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix _ipv6Prefix;


    Map<Class<? extends Augmentation<Ipv6Prefix>>, Augmentation<Ipv6Prefix>> augmentation = Collections.emptyMap();

    public Ipv6PrefixBuilder() {
    }

    public Ipv6PrefixBuilder(Ipv6Prefix base) {
        this._ipv6Prefix = base.getIpv6Prefix();
        if (base instanceof Ipv6PrefixImpl) {
            Ipv6PrefixImpl impl = (Ipv6PrefixImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            Map<Class<? extends Augmentation<Ipv6Prefix>>, Augmentation<Ipv6Prefix>> aug =((AugmentationHolder<Ipv6Prefix>) base).augmentations();
            if (!aug.isEmpty()) {
                this.augmentation = new HashMap<>(aug);
            }
        }
    }


    public org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix getIpv6Prefix() {
        return _ipv6Prefix;
    }
    
    @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
    public <E$$ extends Augmentation<Ipv6Prefix>> E$$ augmentation(Class<E$$> augmentationType) {
        return (E$$) augmentation.get(CodeHelpers.nonNullValue(augmentationType, "augmentationType"));
    }

    
    public Ipv6PrefixBuilder setIpv6Prefix(final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix value) {
        this._ipv6Prefix = value;
        return this;
    }
    
    public Ipv6PrefixBuilder addAugmentation(Class<? extends Augmentation<Ipv6Prefix>> augmentationType, Augmentation<Ipv6Prefix> augmentationValue) {
        if (augmentationValue == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentationValue);
        return this;
    }
    
    public Ipv6PrefixBuilder removeAugmentation(Class<? extends Augmentation<Ipv6Prefix>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public Ipv6Prefix build() {
        return new Ipv6PrefixImpl(this);
    }

    private static final class Ipv6PrefixImpl implements Ipv6Prefix {
    
        private final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix _ipv6Prefix;
    
        private Map<Class<? extends Augmentation<Ipv6Prefix>>, Augmentation<Ipv6Prefix>> augmentation = Collections.emptyMap();
    
        Ipv6PrefixImpl(Ipv6PrefixBuilder base) {
            this._ipv6Prefix = base.getIpv6Prefix();
            this.augmentation = ImmutableMap.copyOf(base.augmentation);
        }
    
        @Override
        public Class<Ipv6Prefix> getImplementedInterface() {
            return Ipv6Prefix.class;
        }
    
        @Override
        public org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix getIpv6Prefix() {
            return _ipv6Prefix;
        }
        
        @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
        @Override
        public <E$$ extends Augmentation<Ipv6Prefix>> E$$ augmentation(Class<E$$> augmentationType) {
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
            result = prime * result + Objects.hashCode(_ipv6Prefix);
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
            if (!Ipv6Prefix.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            Ipv6Prefix other = (Ipv6Prefix)obj;
            if (!Objects.equals(_ipv6Prefix, other.getIpv6Prefix())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                Ipv6PrefixImpl otherImpl = (Ipv6PrefixImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<Class<? extends Augmentation<Ipv6Prefix>>, Augmentation<Ipv6Prefix>> e : augmentation.entrySet()) {
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
            final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Ipv6Prefix");
            CodeHelpers.appendValue(helper, "_ipv6Prefix", _ipv6Prefix);
            CodeHelpers.appendValue(helper, "augmentation", augmentation.values());
            return helper.toString();
        }
    }
}
