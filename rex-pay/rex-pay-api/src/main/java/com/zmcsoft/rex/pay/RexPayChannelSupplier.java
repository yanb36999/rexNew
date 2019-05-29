package com.zmcsoft.rex.pay;

import java.util.function.Supplier;

public interface RexPayChannelSupplier extends Supplier<RexPayChannel> {
    String getChannel();
}
