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
 * Class that builds {@link Ipv4AddressBuilder} instances.
 *
 * @see Ipv4AddressBuilder
 *
 */
public class Ipv4AddressBuilder implements Builder<Ipv4Address> {

    private org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address _ipv4Address;


    Map<Class<? extends Augmentation<Ipv4Address>>, Augmentation<Ipv4Address>> augmentation = Collections.emptyMap();

    public Ipv4AddressBuilder() {
    }

    public Ipv4AddressBuilder(Ipv4Address base) {
        this._ipv4Address = base.getIpv4Address();
        if (base instanceof Ipv4AddressImpl) {
            Ipv4AddressImpl impl = (Ipv4AddressImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            Map<Class<? extends Augmentation<Ipv4Address>>, Augmentation<Ipv4Address>> aug =((AugmentationHolder<Ipv4Address>) base).augmentations();
            if (!aug.isEmpty()) {
                this.augmentation = new HashMap<>(aug);
            }
        }
    }


    public org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address getIpv4Address() {
        return _ipv4Address;
    }
    
    @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
    public <E$$ extends Augmentation<Ipv4Address>> E$$ augmentation(Class<E$$> augmentationType) {
        return (E$$) augmentation.get(CodeHelpers.nonNullValue(augmentationType, "augmentationType"));
    }

    
    public Ipv4AddressBuilder setIpv4Address(final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address value) {
        this._ipv4Address = value;
        return this;
    }
    
    public Ipv4AddressBuilder addAugmentation(Class<? extends Augmentation<Ipv4Address>> augmentationType, Augmentation<Ipv4Address> augmentationValue) {
        if (augmentationValue == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentationValue);
        return this;
    }
    
    public Ipv4AddressBuilder removeAugmentation(Class<? extends Augmentation<Ipv4Address>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public Ipv4Address build() {
        return new Ipv4AddressImpl(this);
    }

    private static final class Ipv4AddressImpl implements Ipv4Address {
    
        private final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address _ipv4Address;
    
        private Map<Class<? extends Augmentation<Ipv4Address>>, Augmentation<Ipv4Address>> augmentation = Collections.emptyMap();
    
        Ipv4AddressImpl(Ipv4AddressBuilder base) {
            this._ipv4Address = base.getIpv4Address();
            this.augmentation = ImmutableMap.copyOf(base.augmentation);
        }
    
        @Override
        public Class<Ipv4Address> getImplementedInterface() {
            return Ipv4Address.class;
        }
    
        @Override
        public org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address getIpv4Address() {
            return _ipv4Address;
        }
        
        @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
        @Override
        public <E$$ extends Augmentation<Ipv4Address>> E$$ augmentation(Class<E$$> augmentationType) {
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
            result = prime * result + Objects.hashCode(_ipv4Address);
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
            if (!Ipv4Address.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            Ipv4Address other = (Ipv4Address)obj;
            if (!Objects.equals(_ipv4Address, other.getIpv4Address())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                Ipv4AddressImpl otherImpl = (Ipv4AddressImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<Class<? extends Augmentation<Ipv4Address>>, Augmentation<Ipv4Address>> e : augmentation.entrySet()) {
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
            final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("Ipv4Address");
            CodeHelpers.appendValue(helper, "_ipv4Address", _ipv4Address);
            CodeHelpers.appendValue(helper, "augmentation", augmentation.values());
            return helper.toString();
        }
    }
}
