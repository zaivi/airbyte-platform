/*
 * Copyright (c) 2023 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.config.init;

import com.google.common.io.Resources;
import io.airbyte.commons.json.Jsons;
import io.airbyte.commons.version.AirbyteProtocolVersion;
import io.airbyte.config.CatalogDefinitionsConfig;
import io.airbyte.config.ConnectorRegistry;
import io.airbyte.config.ConnectorRegistryDestinationDefinition;
import io.airbyte.config.ConnectorRegistrySourceDefinition;
import io.airbyte.config.persistence.ConfigNotFoundException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provider contains all definitions according to the local registry json files.
 */
public final class LocalDefinitionsProvider implements DefinitionsProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalDefinitionsProvider.class);

  private static final String LOCAL_CONNECTOR_REGISTRY_PATH = CatalogDefinitionsConfig.getLocalConnectorCatalogPath();

  /**
   * Get connector registry.
   *
   * @return connector registry
   */
  public ConnectorRegistry getLocalConnectorRegistry() {
    try {
      final URL url = Resources.getResource(LOCAL_CONNECTOR_REGISTRY_PATH);
      LOGGER.info("Loading {} definitions from local connector registry {}", LOCAL_CONNECTOR_REGISTRY_PATH, url);

      final String jsonString = Resources.toString(url, StandardCharsets.UTF_8);
      return Jsons.deserialize(jsonString, ConnectorRegistry.class);

    } catch (final Exception e) {
      throw new RuntimeException("Failed to fetch local connector registry", e);
    }
  }

  /**
   * Get map of source definition ids to the definition.
   *
   * @return map
   */
  public Map<UUID, ConnectorRegistrySourceDefinition> getSourceDefinitionsMap() {
    final ConnectorRegistry registry = getLocalConnectorRegistry();
    return registry.getSources().stream().collect(Collectors.toMap(
        ConnectorRegistrySourceDefinition::getSourceDefinitionId,
        source -> source.withProtocolVersion(
            AirbyteProtocolVersion.getWithDefault(source.getSpec() != null ? source.getSpec().getProtocolVersion() : null).serialize())));
  }

  /**
   * Get map of destination definition ids to the definition.
   *
   * @return map
   */
  public Map<UUID, ConnectorRegistryDestinationDefinition> getDestinationDefinitionsMap() {
    final ConnectorRegistry registry = getLocalConnectorRegistry();
    return registry.getDestinations().stream().collect(
        Collectors.toMap(
            ConnectorRegistryDestinationDefinition::getDestinationDefinitionId,
            destination -> destination.withProtocolVersion(
                AirbyteProtocolVersion.getWithDefault(
                    destination.getSpec() != null
                        ? destination.getSpec().getProtocolVersion()
                        : null)
                    .serialize())));
  }

  @Override
  public ConnectorRegistrySourceDefinition getSourceDefinition(final UUID definitionId) throws ConfigNotFoundException {
    final ConnectorRegistrySourceDefinition definition = getSourceDefinitionsMap().get(definitionId);
    if (definition == null) {
      throw new ConfigNotFoundException("local_registry:source_def", definitionId.toString());
    }
    return definition;
  }

  @Override
  public List<ConnectorRegistrySourceDefinition> getSourceDefinitions() {
    return new ArrayList<>(getSourceDefinitionsMap().values());
  }

  @Override
  public ConnectorRegistryDestinationDefinition getDestinationDefinition(final UUID definitionId) throws ConfigNotFoundException {
    final ConnectorRegistryDestinationDefinition definition = getDestinationDefinitionsMap().get(definitionId);
    if (definition == null) {
      throw new ConfigNotFoundException("local_registry:destination_def", definitionId.toString());
    }
    return definition;
  }

  @Override
  public List<ConnectorRegistryDestinationDefinition> getDestinationDefinitions() {
    return new ArrayList<>(getDestinationDefinitionsMap().values());
  }

}
