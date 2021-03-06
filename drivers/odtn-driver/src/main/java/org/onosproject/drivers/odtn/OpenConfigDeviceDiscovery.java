/*
 * Copyright 2018-present Open Networking Foundation
 *
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
 */
package org.onosproject.drivers.odtn;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port.Type;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DefaultPortDescription.Builder;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceDescriptionDiscovery;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfDevice;
import org.onosproject.netconf.NetconfRpcParserUtil;
import org.onosproject.netconf.NetconfRpcReply;
import org.onosproject.netconf.NetconfSession;
import org.onosproject.odtn.behaviour.OdtnDeviceDescriptionDiscovery;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;

/**
 * OpenConfig based device and port discovery.
 */
public class OpenConfigDeviceDiscovery
    extends AbstractHandlerBehaviour
    implements OdtnDeviceDescriptionDiscovery, DeviceDescriptionDiscovery {

    private static final Logger log = getLogger(OpenConfigDeviceDiscovery.class);

    @Override
    public DeviceDescription discoverDeviceDetails() {
        // TODO Auto-generated method stub
        // Not really used right now
        return null;
    }

    @Override
    public List<PortDescription> discoverPortDetails() {
        try {
            return discoverPorts();
        } catch (Exception e) {
            log.error("Error discovering port details on {}", data().deviceId(), e);
            return ImmutableList.of();
        }
    }

    private List<PortDescription> discoverPorts() throws ConfigurationException, IOException {
        DeviceId did = data().deviceId();
        NetconfSession ns = Optional.ofNullable(handler().get(NetconfController.class))
                    .map(c -> c.getNetconfDevice(did))
                    .map(NetconfDevice::getSession)
                    .orElseThrow(() -> new IllegalStateException("No NetconfSession found for " + did));

        // TODO convert this method into non-blocking form?

        NetconfRpcReply reply = ns.asyncGet()
            .thenApply(NetconfRpcParserUtil::parseRpcReply)
            .join();

        if (reply.hasError()) {
            log.error("Netconf error replies from {}:\n{}", did, reply.errors());
            return ImmutableList.of();
        }

        String data = reply.responses().stream()
                .filter(s -> s.startsWith("<data"))
                .findFirst()
                .orElse(null);

        if (data == null) {
            log.error("No valid response found from {}:\n{}", did, reply);
            return ImmutableList.of();
        }

        XMLConfiguration cfg = new XMLConfiguration();
        cfg.load(CharSource.wrap(data).openStream());

        return discoverPorts(cfg);
    }

    /**
     * Parses port information from OpenConfig XML configuration.
     *
     * @param cfg tree where the root node is {@literal <data>}
     * @return List of ports
     */
    @VisibleForTesting
    protected List<PortDescription> discoverPorts(XMLConfiguration cfg) {
        // If we want to use XPath
        cfg.setExpressionEngine(new XPathExpressionEngine());

        // converting components into PortDescription.
        List<HierarchicalConfiguration> components = cfg.configurationsAt("components/component");
        return components.stream()
            .map(this::toPortDescription)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    // wrapper to make parsing exception safe
    private PortDescription toPortDescription(HierarchicalConfiguration component) {
        try {
            return toPortDescriptionInternal(component);
        } catch (Exception e) {
            log.error("Unexpected exception parsing component {} on {}",
                      component.getString("name"),
                      data().deviceId(), e);
            return null;
        }
    }

    /**
     * Converts Component subtree to PortDescription.
     *
     * @param component subtree to parse
     * @return PortDescription or null if component is not an ONOS Port
     */
    private PortDescription toPortDescriptionInternal(HierarchicalConfiguration component) {

        // to access other part of <data> tree:
        //log.warn("parent data Node: {}",
        //       ((SubnodeConfiguration) component).getParent().getRootNode().getName());

        Map<String, String> props = new HashMap<>();

        String name = component.getString("name");
        String type = component.getString("state/type");
        checkNotNull(name);
        checkNotNull(type);
        props.put(OdtnDeviceDescriptionDiscovery.OC_NAME, name);
        props.put(OdtnDeviceDescriptionDiscovery.OC_TYPE, type);

        component.configurationsAt("properties/property").forEach(prop -> {
            String pName = prop.getString("name");
            String pValue = prop.getString("config/value");
            props.put(pName, pValue);
        });

        if (!props.containsKey("onos-index")) {
            log.info("DEBUG: Component {} does not include onos-index, skipping", name);
            // ODTN: port must have onos-index property
            return null;
        }

        Builder builder = DefaultPortDescription.builder();
        builder.withPortNumber(PortNumber.portNumber(Long.parseLong(props.get("onos-index")), name));

        switch (type) {
        case "oc-platform-types:PORT":
        case "oc-platform-types:TRANSCEIVER":
        case "oc-opt-types:OPTICAL_CHANNEL":
            // TODO assign appropriate port type & annotations at some point
            // for now we just need a Port with annotations
            builder.type(Type.PACKET);
            break;

        default:
            log.info("DEBUG: Unknown component type {}", type);
            return null;
        }

        builder.annotations(DefaultAnnotations.builder().putAll(props).build());

        return builder.build();

    }

}
