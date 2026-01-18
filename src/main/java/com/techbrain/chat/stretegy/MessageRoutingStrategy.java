package com.techbrain.chat.stretegy;

import com.techbrain.chat.to.Message;

public interface MessageRoutingStrategy {
    void route(Message message);
}
