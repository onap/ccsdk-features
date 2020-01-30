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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.DomainName;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Class that builds {@link EndpointFqdnBuilder} instances.
 *
 * @see EndpointFqdnBuilder
 *
 */
public class EndpointFqdnBuilder implements Builder<EndpointFqdn> {

    private DomainName _endpointFqdn;


    Map<Class<? extends Augmentation<EndpointFqdn>>, Augmentation<EndpointFqdn>> augmentation = Collections.emptyMap();

    public EndpointFqdnBuilder() {
    }

    public EndpointFqdnBuilder(EndpointFqdn base) {
        this._endpointFqdn = base.getEndpointFqdn();
        if (base instanceof EndpointFqdnImpl) {
            EndpointFqdnImpl impl = (EndpointFqdnImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            Map<Class<? extends Augmentation<EndpointFqdn>>, Augmentation<EndpointFqdn>> aug =((AugmentationHolder<EndpointFqdn>) base).augmentations();
            if (!aug.isEmpty()) {
                this.augmentation = new HashMap<>(aug);
            }
        }
    }


    public DomainName getEndpointFqdn() {
        return _endpointFqdn;
    }
    
    @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
    public <E$$ extends Augmentation<EndpointFqdn>> E$$ augmentation(Class<E$$> augmentationType) {
        return (E$$) augmentation.get(CodeHelpers.nonNullValue(augmentationType, "augmentationType"));
    }

    
    public EndpointFqdnBuilder setEndpointFqdn(final DomainName value) {
        this._endpointFqdn = value;
        return this;
    }
    
    public EndpointFqdnBuilder addAugmentation(Class<? extends Augmentation<EndpointFqdn>> augmentationType, Augmentation<EndpointFqdn> augmentationValue) {
        if (augmentationValue == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentationValue);
        return this;
    }
    
    public EndpointFqdnBuilder removeAugmentation(Class<? extends Augmentation<EndpointFqdn>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public EndpointFqdn build() {
        return new EndpointFqdnImpl(this);
    }

    private static final class EndpointFqdnImpl implements EndpointFqdn {
    
        private final DomainName _endpointFqdn;
    
        private Map<Class<? extends Augmentation<EndpointFqdn>>, Augmentation<EndpointFqdn>> augmentation = Collections.emptyMap();
    
        EndpointFqdnImpl(EndpointFqdnBuilder base) {
            this._endpointFqdn = base.getEndpointFqdn();
            this.augmentation = ImmutableMap.copyOf(base.augmentation);
        }
    
        @Override
        public Class<EndpointFqdn> getImplementedInterface() {
            return EndpointFqdn.class;
        }
    
        @Override
        public DomainName getEndpointFqdn() {
            return _endpointFqdn;
        }
        
        @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
        @Override
        public <E$$ extends Augmentation<EndpointFqdn>> E$$ augmentation(Class<E$$> augmentationType) {
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
            result = prime * result + Objects.hashCode(_endpointFqdn);
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
            if (!EndpointFqdn.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            EndpointFqdn other = (EndpointFqdn)obj;
            if (!Objects.equals(_endpointFqdn, other.getEndpointFqdn())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                EndpointFqdnImpl otherImpl = (EndpointFqdnImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<Class<? extends Augmentation<EndpointFqdn>>, Augmentation<EndpointFqdn>> e : augmentation.entrySet()) {
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
            final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper("EndpointFqdn");
            CodeHelpers.appendValue(helper, "_endpointFqdn", _endpointFqdn);
            CodeHelpers.appendValue(helper, "augmentation", augmentation.values());
            return helper.toString();
        }
    }
}
