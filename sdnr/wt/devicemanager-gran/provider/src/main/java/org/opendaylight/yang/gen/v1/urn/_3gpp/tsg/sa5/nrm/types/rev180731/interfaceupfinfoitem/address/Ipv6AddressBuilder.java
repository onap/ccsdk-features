package org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.interfaceupfinfoitem.address;
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
 * Class that builds {@link Ipv6AddressBuilder} instances.
 *
 * @see Ipv6AddressBuilder
 *
 */
public class Ipv6AddressBuilder implements Builder<Ipv6Address> {

    private org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address _ipv6Address;


    Map<Class<? extends Augmentation<Ipv6Address>>, Augmentation<Ipv6Address>> augmentation = Collections.emptyMap();

    public Ipv6AddressBuilder() {
    }

    public Ipv6AddressBuilder(Ipv6Address base) {
        this._ipv6Address = base.getIpv6Address();
        if (base instanceof Ipv6AddressImpl) {
            Ipv6AddressImpl impl = (Ipv6AddressImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            Map<Class<? extends Augmentation<Ipv6Address>>, Augmentation<Ipv6Address>> aug =((AugmentationHolder<Ipv6Address>) base).augmentations();
            if (!aug.isEmpty()) {
                this.augmentation = new HashMap<>(aug);
            }
        }
    }


    public org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address getIpv6Address() {
        return _ipv6Address;
    }
    
    @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
    public <E$$ extends Augmentation<Ipv6Address>> E$$ augmentation(Class<E$$> augmentationType) {
        return (E$$) augmentation.get(CodeHelpers.nonNullValue(augmentationType, "augmentationType"));
    }

    
    public Ipv6AddressBuilder setIpv6Address(final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address value) {
        this._ipv6Address = value;
        return this;
    }
    
    public Ipv6AddressBuilder addAugmentation(Class<? extends Augmentation<Ipv6Address>> augmentationType, Augmentation<Ipv6Address> augmentationValue) {
        if (augmentationValue == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentationValue);
        return this;
    }
    
    public Ipv6AddressBuilder removeAugmentation(Class<? extends Augmentation<Ipv6Address>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public Ipv6Address build() {
        return new Ipv6AddressImpl(this);
    }

    private static final class Ipv6AddressImpl implements Ipv6Address {
    
        private final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address _ipv6Address;
    
        private Map<Class<? extends Augmentation<Ipv6Address>>, Augmentation<Ipv6Address>> augmentation = Collections.emptyMap();
    
        Ipv6AddressImpl(Ipv6AddressBuilder base) {
            this._ipv6Address = base.getIpv6Address();
            this.augmentation = ImmutableMap.copyOf(base.augmentation);
        }
    
        @Override
        public Class<Ipv6Address> getImplementedInterface() {
            return Ipv6Address.class;
        }
    
        @Override
        public org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address getIpv6Address() {
            return _ipv6Address;
        }
        
        @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
        @Override
        public <E$$ extends Augmentation<Ipv6Address>> E$$ augmentation(Class<E$$> augmentationType) {
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
            result = prime * result + Objects.hashCode(_ipv6Address);
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
            if (!Ipv6Address.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            Ipv6Address other = (Ipv6Address)obj;
            if (!Objects.equals(_ipv6Address, other.getIpv6Address())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                Ipv6AddressImpl otherImpl = (Ipv6AddressImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<Class<? extends Augmentation<Ipv6Address>>, Augmentation<Ipv6Address>> e : augmentation.entrySet()) {
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
            final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Ipv6Address");
            CodeHelpers.appendValue(helper, "_ipv6Address", _ipv6Address);
            CodeHelpers.appendValue(helper, "augmentation", augmentation.values());
            return helper.toString();
        }
    }
}
