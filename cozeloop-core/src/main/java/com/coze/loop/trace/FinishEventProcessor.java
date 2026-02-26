package com.coze.loop.trace;

import org.slf4j.Logger;

import com.coze.loop.internal.CozeLoopLogger;

@FunctionalInterface
public interface FinishEventProcessor {
  Logger logger = CozeLoopLogger.getLogger(FinishEventProcessor.class);

  void process(FinishEventInfo info);

  FinishEventProcessor DEFAULT =
      info -> {
        if (info == null) {
          return;
        }
        if (info.isEventFail()) {
          logger.error(
              "finish_event[{}] fail, msg: {}",
              info.getEventType().getValue(),
              info.getDetailMsg());
        } else {
          logger.debug(
              "finish_event[{}] success, item_num: {}, msg: {}",
              info.getEventType().getValue(),
              info.getItemNum(),
              info.getDetailMsg());
        }
      };
}
