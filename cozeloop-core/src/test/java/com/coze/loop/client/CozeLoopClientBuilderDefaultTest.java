package com.coze.loop.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CozeLoopClientBuilderDefaultTest {

  @Test
  void testBuilderPicksUpSystemProperties() {
    System.setProperty("cozeloop.workspace-id", "test-workspace");
    System.setProperty("cozeloop.auth.token", "test-token");

    try {
      CozeLoopClientBuilder builder = new CozeLoopClientBuilder();
      CozeLoopClient client = builder.build();
      assertThat(client.getWorkspaceId()).isEqualTo("test-workspace");
    } finally {
      System.clearProperty("cozeloop.workspace-id");
      System.clearProperty("cozeloop.auth.token");
    }
  }
}
