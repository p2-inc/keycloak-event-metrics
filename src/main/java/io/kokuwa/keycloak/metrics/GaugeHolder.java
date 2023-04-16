package io.kokuwa.keycloak.metrics;

import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import org.keycloak.models.KeycloakSessionFactory;

public interface GaugeHolder {

  default Iterable<Tag> tags(String... tags) {
    if (tags.length % 2 != 0) throw new IllegalStateException("Tag name value pairs must be even");
    ImmutableList.Builder<Tag> builder = new ImmutableList.Builder<Tag>();
    for (int i = 0; i < tags.length; i += 2) {
      builder.add(new ImmutableTag(tags[i], tags[i + 1]));
    }
    return builder.build();
  }

  void setup(KeycloakSessionFactory factory);

  void remove();
}
